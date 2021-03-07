package minedroid.network.masthead.bungee;

import minedroid.network.masthead.bungee.format.ServerCreationFormat;
import minedroid.network.masthead.bungee.format.ServerDeletionFormat;
import minedroid.network.masthead.db.RedisDatabase;
import minedroid.network.masthead.model.MinecraftServer;
import redis.clients.jedis.Jedis;

import static minedroid.network.masthead.Masthead.GSON;

public class BungeeCordManager {

    private final RedisDatabase redisDatabase;

    private static final String SERVER_CREATION_CHANNEL = "masthead:server_created";
    private static final String SERVER_DELETION_CHANNEL = "masthead:server_deleted";

    public BungeeCordManager(RedisDatabase redisDatabase) {
        this.redisDatabase = redisDatabase;
    }

    public void serverUp(MinecraftServer server) {
        try (Jedis j = redisDatabase.getResource()) {
            j.publish(SERVER_CREATION_CHANNEL, GSON.toJson(new ServerCreationFormat(server.getName(), server.getIp(), server.getPort())));
        }
    }

    public void serverDown(String name) {
        try (Jedis j = redisDatabase.getResource()) {
            j.publish(SERVER_DELETION_CHANNEL, GSON.toJson(new ServerDeletionFormat(name)));
        }
    }

    public void serverDown(MinecraftServer minecraftServer) {
        serverDown(minecraftServer.getName());
    }
}
