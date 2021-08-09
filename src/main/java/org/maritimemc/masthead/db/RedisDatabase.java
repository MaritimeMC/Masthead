package org.maritimemc.masthead.db;

import org.maritimemc.masthead.file.FileManager;
import org.maritimemc.masthead.file.JsonConfigurationFile;
import org.maritimemc.masthead.log.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Timer;
import java.util.TimerTask;

public class RedisDatabase {

    private final RedisCredentials redisCredentials;

    private JedisPool jedisPool;

    public RedisDatabase(FileManager fileManager) {
        this.redisCredentials = new RedisCredentials(fileManager.getConfig());
    }

    public void connect() {
        this.jedisPool = new JedisPool(redisCredentials.getHost(), redisCredentials.getPort());

        ping();
        Logger.info("Successfully connected to Redis at " + redisCredentials.getHost() + ".");
    }

    public Jedis getResource() {
        Jedis j = jedisPool.getResource();

        String p = redisCredentials.getPassword();
        if (!p.equals("")) j.auth(p);

        return j;
    }

    public void ping() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (jedisPool.isClosed()) return;

                try (Jedis j = getResource()) {
                    j.ping();
                } catch (JedisConnectionException e) {
                    Logger.severe("Could not connect to Redis; closing pool.");
                    jedisPool.close();
                    cancel();
                }
            }
        }, 0, 6000);
    }

    public static class RedisCredentials {

        private final JsonConfigurationFile config;

        public RedisCredentials(JsonConfigurationFile config) {
            this.config = config;
        }

        public String getHost() {
            return config.getData().get("redis_host").getAsString();
        }

        public String getPassword() {
            return config.getData().get("redis_password").getAsString();
        }

        public int getPort() {
            return 6379;
        }
    }
}
