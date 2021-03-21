package minedroid.network.masthead.server;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import lombok.SneakyThrows;
import minedroid.network.masthead.ThreadPool;
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

import java.util.*;
import java.util.stream.Collectors;

import static minedroid.network.masthead.Masthead.GSON;

public class MinecraftServerManager {

    private final ServerGroupManager serverGroupManager;
    private final PterodactylController pterodactylController;
    private final MongoDatabase mongoDatabase;
    private final ListenerManager listenerManager;

    public static final String MINECRAFT_SERVER_COLLECTION = "mh_minecraftserver";
    private static final String LONE_IGNORE_PREFIX = "MHIGNORE";

    private static final Object SERVER_CACHE_LOCK = new Object();
    private final Set<MinecraftServer> serverCache;

    private final Map<ServerGroup, ServerGroupMonitor> monitorMap;

    public MinecraftServerManager(ServerGroupManager serverGroupManager, PterodactylController pterodactylController, MongoDatabase mongoDatabase, ListenerManager listenerManager) {
        this.serverGroupManager = serverGroupManager;
        this.pterodactylController = pterodactylController;
        this.mongoDatabase = mongoDatabase;
        this.listenerManager = listenerManager;
        this.serverCache = new HashSet<>();
        this.monitorMap = new HashMap<>();

        new MinecraftServerListener(serverGroupManager, this, listenerManager).register();
    }

    public void load() {
        removeLoneServers();
        loadServers();
        cleanDisposableServers();
        beginMonitoring();
    }

    public void beginMonitoring() {
        for (ServerGroup serverGroup : serverGroupManager.getServerGroups()) {

            ServerGroupMonitor serverGroupMonitor = new ServerGroupMonitor(serverGroup, this);
            serverGroupMonitor.requestCreationUpdate(CreationUpdateReason.STARTUP);

            monitorMap.put(serverGroup, serverGroupMonitor);
        }
    }

    public void cleanDisposableServers() {
        synchronized (SERVER_CACHE_LOCK) {

            Set<MinecraftServer> clone = new HashSet<>(serverCache);
            Logger.info("Searching old non-disposable servers and replacing...");
            for (MinecraftServer minecraftServer : clone) {
                if (serverGroupManager.getGroupByName(minecraftServer.getServerGroupName()).isDisposable()) {
                    deleteServer(minecraftServer, false, false);
                }
            }
        }
    }

    public Set<MinecraftServer> getGroupServers(ServerGroup group) {
        return serverCache.stream().filter((sg) -> sg.getServerGroupName().equals(group.getName())).collect(Collectors.toSet());
    }

    public void updateServerStatus(MinecraftServer server, ServerStatus status, UtilizationState panelStatus) {
        server.setStatus(status);
        server.setPanelStatus(panelStatus);

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        collection.updateOne(new Document("name", server.getName()), new Document("$set", new Document()
                .append("status", status.name()).append("panelStatus", panelStatus.name())));


        if (status == ServerStatus.RUNNING)
            monitorMap.get(serverGroupManager.getGroupByName(server.getServerGroupName())).requestCreationUpdate(CreationUpdateReason.SERVER_STATUS_CHANGE);

    }

    public void removeLoneServers() {
        List<ApplicationServer> servers = pterodactylController.getServers();

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        FindIterable<Document> docs = collection.find();

        for (Document document : docs) {
            MinecraftServer s = GSON.fromJson(document.toJson(), MinecraftServer.class);

            if (servers.stream().noneMatch(ps -> ps.getName().equals(s.getName()))) {
                collection.deleteOne(document);
                Logger.info("Couldn't find a server in panel matching " + s.getName() + "; removed from database.");
            }
        }

        for (ApplicationServer server : servers) {
            if (server.getEgg().get().get().getIdLong() != 3) {
                // Egg
                continue;
            }

            if (collection.countDocuments(new Document("name", server.getName())) == 0 && !server.getName().startsWith(LONE_IGNORE_PREFIX)) {
                pterodactylController.deleteServer(server);
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

        deleteServer(server, true);
    }

    @SneakyThrows
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

    public void createServer(ServerGroup serverGroup, boolean async) {
        if (async) ThreadPool.ASYNC_POOL.submit(() -> createServerFlow(serverGroup));
        else createServerFlow(serverGroup);
    }

    public void createServer(ServerGroup serverGroup) {
        createServer(serverGroup, true);
    }

    public void createServerFlow(ServerGroup serverGroup) {
        String name = generateServerName(serverGroup);
        MinecraftServer server = pterodactylController.createServer(serverGroup, name);

        synchronized (SERVER_CACHE_LOCK) {
            serverCache.add(server);
        }

        addServerToDatabase(server);

        Logger.info("Successfully created " + name + ". Added to cache and database.");
    }

    public void deleteServer(MinecraftServer server, boolean monitoringUpdate) {
        deleteServer(server, monitoringUpdate, true);
    }

    public void deleteServer(MinecraftServer minecraftServer, boolean monitoringUpdate, boolean async) {
        if (async) {
            ThreadPool.ASYNC_POOL.submit(() -> deleteServerFlow(minecraftServer, monitoringUpdate));
        } else {
            deleteServerFlow(minecraftServer, monitoringUpdate);
        }
    }

    private void deleteServerFlow(MinecraftServer minecraftServer, boolean monitoringUpdate) {
        synchronized (SERVER_CACHE_LOCK) {
            serverCache.remove(minecraftServer);
        }

        pterodactylController.deleteServer(minecraftServer);

        deleteServerFromDatabase(minecraftServer);

        Logger.info("Successfully deleted " + minecraftServer.getName());

        ServerGroup group = serverGroupManager.getGroupByName(minecraftServer.getServerGroupName());

        if (monitoringUpdate) monitorMap.get(group).requestCreationUpdate(CreationUpdateReason.SERVER_DEAD);
    }

    public String generateServerName(ServerGroup group) {
        boolean nameFound = false;
        String endName = "";

        String nBase = group.getName();
        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);

        do {
            Random r = new Random();

            String s = "";
            for (int i = 0; i < 5; i++) {
                s += r.nextInt(10);
            }

            String name = nBase + s;

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
