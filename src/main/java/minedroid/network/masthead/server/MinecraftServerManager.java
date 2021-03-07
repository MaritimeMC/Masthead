package minedroid.network.masthead.server;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import minedroid.network.masthead.ThreadPool;
import minedroid.network.masthead.bungee.BungeeCordManager;
import minedroid.network.masthead.db.MongoDatabase;
import minedroid.network.masthead.log.Logger;
import minedroid.network.masthead.model.MinecraftServer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.panel.PterodactylController;
import org.bson.Document;

import java.util.*;

import static minedroid.network.masthead.Masthead.GSON;

public class MinecraftServerManager {

    private final PterodactylController pterodactylController;
    private final MongoDatabase mongoDatabase;
    private BungeeCordManager bungeeCordManager;

    private static final String MINECRAFT_SERVER_COLLECTION = "mh_minecraftserver";

    private static final Object SERVER_CACHE_LOCK = new Object();
    private final Set<MinecraftServer> serverCache;

    public MinecraftServerManager(PterodactylController pterodactylController, MongoDatabase mongoDatabase, BungeeCordManager bungeeCordManager) {
        this.pterodactylController = pterodactylController;
        this.mongoDatabase = mongoDatabase;
        this.bungeeCordManager = bungeeCordManager;
        this.serverCache = new HashSet<>();
    }

    public void load() {
        removeLoneServers();
        loadServers();
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
            if (collection.countDocuments(new Document("name", server.getName())) == 0) {
                pterodactylController.deleteServer(server);
                bungeeCordManager.serverDown(server.getName());
                Logger.info("Couldn't find a server in database matching " + server.getName() + "; deleted from panel.");
            }
        }
    }

    public void loadServers() {

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(MINECRAFT_SERVER_COLLECTION);
        for (Document document : collection.find()) {
            MinecraftServer s = GSON.fromJson(document.toJson(), MinecraftServer.class);

            synchronized (SERVER_CACHE_LOCK) {
                serverCache.add(s);
            }

            Logger.info("Found server " + s.getName() + "; adding to cache.");
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
            bungeeCordManager.serverDown(minecraftServer);

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
}
