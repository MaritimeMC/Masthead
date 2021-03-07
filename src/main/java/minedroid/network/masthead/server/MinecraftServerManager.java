package minedroid.network.masthead.server;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import minedroid.network.masthead.ThreadPool;
import minedroid.network.masthead.bungee.BungeeCordManager;
import minedroid.network.masthead.db.MongoDatabase;
import minedroid.network.masthead.event.ListenerManager;
import minedroid.network.masthead.group.ServerGroupManager;
import minedroid.network.masthead.group.monitor.CreationUpdateReason;
import minedroid.network.masthead.group.monitor.ServerGroupMonitor;
import minedroid.network.masthead.log.Logger;
import minedroid.network.masthead.model.MinecraftServer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.model.ServerStatus;
import minedroid.network.masthead.panel.PterodactylController;
import minedroid.network.masthead.panel.WebSocketListener;
import org.bson.Document;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

import static minedroid.network.masthead.Masthead.GSON;

public class MinecraftServerManager {

    private final ServerGroupManager serverGroupManager;
    private final PterodactylController pterodactylController;
    private final MongoDatabase mongoDatabase;
    private final BungeeCordManager bungeeCordManager;
    private final ListenerManager listenerManager;

    private static final String MINECRAFT_SERVER_COLLECTION = "mh_minecraftserver";

    private static final Object SERVER_CACHE_LOCK = new Object();
    private final Set<MinecraftServer> serverCache;

    private final Map<ServerGroup, ServerGroupMonitor> monitorMap;

    public MinecraftServerManager(ServerGroupManager serverGroupManager, PterodactylController pterodactylController, MongoDatabase mongoDatabase, BungeeCordManager bungeeCordManager, ListenerManager listenerManager) {
        this.serverGroupManager = serverGroupManager;
        this.pterodactylController = pterodactylController;
        this.mongoDatabase = mongoDatabase;
        this.bungeeCordManager = bungeeCordManager;
        this.listenerManager = listenerManager;
        this.serverCache = new HashSet<>();
        this.monitorMap = new HashMap<>();

        new MinecraftServerListener(serverGroupManager, this, listenerManager).register();
    }

    public void load() {
        removeLoneServers();
        loadServers();
        beginMonitoring();
    }

    public void beginMonitoring() {
        for (ServerGroup serverGroup : serverGroupManager.getServerGroups()) {

            ServerGroupMonitor serverGroupMonitor = new ServerGroupMonitor(serverGroup, this);
            serverGroupMonitor.requestCreationUpdate(CreationUpdateReason.STARTUP);

            monitorMap.put(serverGroup, serverGroupMonitor);
        }
    }

    public Set<MinecraftServer> getGroupServers(ServerGroup group) {
        return serverCache.stream().filter((sg) -> sg.getServerGroupName().equals(group.getName())).collect(Collectors.toSet());
    }

    public void updateServerStatus(MinecraftServer server, ServerStatus status) {
        server.setStatus(status);

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        collection.updateOne(new Document("name", server.getName()), new Document("$set", new Document("status", status.name())));
    }

    public void removeLoneServers() {
        List<ApplicationServer> servers = pterodactylController.getServers();

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        FindIterable<Document> docs = collection.find();

        for (Document document : docs) {
            MinecraftServer s = GSON.fromJson(document.toJson(), MinecraftServer.class);

            if (servers.stream().noneMatch(ps -> ps.getName().equals(s.getName()))) {
                collection.deleteOne(document);
                bungeeCordManager.serverDown(s);
                Logger.info("Couldn't find a server in panel matching " + s.getName() + "; removed from database.");
            }
        }

        for (ApplicationServer server : servers) {
            // TODO URGENT get client server and check if it has a value for SERVER_NAME. if it doesn't, do nothing to avoid deleting non-masthead servers.
            if (collection.countDocuments(new Document("name", server.getName())) == 0) {
                pterodactylController.deleteServer(server);
                bungeeCordManager.serverDown(server.getName());
                Logger.info("Couldn't find a server in database matching " + server.getName() + "; deleted from panel.");
            }
        }
    }

    public void disposeOfDeadServer(MinecraftServer server) {
        ServerGroup group = serverGroupManager.getGroupByName(server.getServerGroupName());

        if (!group.isDisposable()) {
            Logger.warning("Why are we trying to dispose of a non-disposable server? :(");
            return;
        }

        deleteServer(server);
    }

    public void loadServers() {

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        for (Document document : collection.find()) {
            MinecraftServer s = GSON.fromJson(document.toJson(), MinecraftServer.class);

            synchronized (SERVER_CACHE_LOCK) {
                serverCache.add(s);
            }

            Logger.info("Found server " + s.getName() + "; adding to cache.");

            ClientServer clientServer = pterodactylController.getClientServer(s.getPanelIdentifier());
            clientServer.getWebSocketBuilder().addEventListeners(new WebSocketListener(s, listenerManager)).build();
        }

    }

    public void createServer(ServerGroup serverGroup) {
        ThreadPool.ASYNC_POOL.submit(() -> {
            String name = generateServerName(serverGroup);
            MinecraftServer server = pterodactylController.createServer(serverGroup, name);

            synchronized (SERVER_CACHE_LOCK) {
                serverCache.add(server);
            }

            addServerToDatabase(server);

            bungeeCordManager.serverUp(server);
            Logger.info("Successfully created " + name + ". Added to cache, database and informed Bungee.");
        });

    }

    public void deleteServer(MinecraftServer minecraftServer) {
        ThreadPool.ASYNC_POOL.submit(() -> {
            pterodactylController.deleteServer(minecraftServer);

            synchronized (SERVER_CACHE_LOCK) {
                serverCache.remove(minecraftServer);
            }

            deleteServerFromDatabase(minecraftServer);

            bungeeCordManager.serverDown(minecraftServer);

            ServerGroup group = serverGroupManager.getGroupByName(minecraftServer.getServerGroupName());
            monitorMap.get(group).requestCreationUpdate(CreationUpdateReason.SERVER_DEAD);
        });
    }

    public String generateServerName(ServerGroup group) {
        boolean nameFound = false;
        String endName = "";

        StringBuilder nBase = new StringBuilder(group.getName() + "_");
        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);

        do {

            UUID uuid = UUID.randomUUID();
            String name = nBase.append(uuid.toString(), 0, 8).toString();

            if (collection.countDocuments(new Document("name", name)) == 0) {
                nameFound = true;
                endName = name;
            }

        } while (!nameFound);

        return endName;
    }

    private void addServerToDatabase(MinecraftServer minecraftServer) {
        Document document = Document.parse(GSON.toJson(minecraftServer));

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        collection.insertOne(document);
    }

    private void deleteServerFromDatabase(MinecraftServer minecraftServer) {
        // Use name instead of full document as values may differ if cached improperly.
        Document document = new Document("name", minecraftServer.getName());

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        collection.deleteOne(document);
    }
}
