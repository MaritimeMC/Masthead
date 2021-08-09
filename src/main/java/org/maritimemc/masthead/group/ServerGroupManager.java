package org.maritimemc.masthead.group;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.maritimemc.masthead.Masthead;
import org.maritimemc.masthead.db.MongoDatabase;
import org.maritimemc.masthead.group.base.ServerGroupBlockParty;
import org.maritimemc.masthead.log.Logger;
import org.maritimemc.masthead.model.ServerGroup;

import java.util.HashSet;
import java.util.Set;

public class ServerGroupManager {

    private final MongoDatabase mongoDatabase;

    public static final String SERVER_GROUP_COLLECTION = "mh_servergroup";

    private static final Set<ServerGroup> DEFAULT_SERVER_GROUPS = Sets.newHashSet(
            new ServerGroupBlockParty()
    );

    private final Set<ServerGroup> serverGroups;
    private static final Object SERVER_GROUP_LOCK = new Object();

    public ServerGroupManager(MongoDatabase mongoDatabase) {
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
