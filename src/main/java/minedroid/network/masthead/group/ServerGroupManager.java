package minedroid.network.masthead.group;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import minedroid.network.masthead.Masthead;
import minedroid.network.masthead.db.MongoDatabase;
import minedroid.network.masthead.file.FileManager;
import minedroid.network.masthead.group.base.ServerGroupHub;
import minedroid.network.masthead.group.base.ServerGroupSkyWars;
import minedroid.network.masthead.group.base.ServerGroupStaff;
import minedroid.network.masthead.group.monitor.ServerGroupMonitor;
import minedroid.network.masthead.log.Logger;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.panel.PterodactylController;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

public class ServerGroupManager {

    private final PterodactylController pterodactylController;
    private final FileManager fileManager;
    private final MongoDatabase mongoDatabase;

    private static final String SERVER_GROUP_COLLECTION = "mh_servergroup";

    private static final Set<ServerGroup> DEFAULT_SERVER_GROUPS = Sets.newHashSet(
            new ServerGroupHub(),
            new ServerGroupSkyWars(),
            new ServerGroupStaff()
    );

    private final Set<ServerGroup> serverGroups;
    private static final Object SERVER_GROUP_LOCK = new Object();

    public ServerGroupManager(PterodactylController pterodactylController, FileManager fileManager, MongoDatabase mongoDatabase) {
        this.pterodactylController = pterodactylController;
        this.fileManager = fileManager;
        this.mongoDatabase = mongoDatabase;
        this.serverGroups = new HashSet<>();
    }

    public Set<ServerGroup> getServerGroups() {
        synchronized (SERVER_GROUP_LOCK) {
            return serverGroups;
        }
    }

    public void load() {

        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(SERVER_GROUP_COLLECTION);
        for (Document document : collection.find()) {
            ServerGroup group = Masthead.GSON.fromJson(document.toJson(), ServerGroup.class);
            Logger.info("Found ServerGroup " + group.getName() + " in database; caching now.");

            synchronized (SERVER_GROUP_LOCK) {
                serverGroups.add(group);
            }
        }

        for (ServerGroup defaultServerGroup : DEFAULT_SERVER_GROUPS) {

            /*
                Use names for matching instead of equals() in case that values have been changed in DB and not in code.
                DB takes precedence.
            */
            if (serverGroups.stream().noneMatch((sg) -> sg.getName().equals(defaultServerGroup.getName()))) {
                Logger.warning("Default group " + defaultServerGroup.getName() + " not found. Adding...");
                addGroup(defaultServerGroup);
            }
        }

    }

    public ServerGroup getGroupByName(String name) {
        return serverGroups.stream().filter(sg -> sg.getName().equals(name)).findAny().orElse(null);
    }

    public void addGroup(ServerGroup group) {
        MongoCollection<Document> collection = mongoDatabase.getDatabase().getCollection(SERVER_GROUP_COLLECTION);
        collection.insertOne(Document.parse(Masthead.GSON.toJson(group)));

        synchronized (SERVER_GROUP_LOCK) {
            serverGroups.add(group);
        }

        Logger.info("Added ServerGroup " + group.getName() + ".");
    }
}
