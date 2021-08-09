package minedroid.network.masthead.pubsub;

import minedroid.network.masthead.db.RedisDatabase;
import minedroid.network.masthead.log.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscribeUtil {

    /**
     * Subscribe to a redis channel based upon the pattern.
     *
     * @param pattern       A pattern, e.g. 'minedroid:chat:*'
     * @param handlers      A Set of handlers to use for incoming messages based upon channel.
     * @param redisDatabase A RedisDatabase instance.
     */
    public static void subscribe(String pattern, Set<IncomingHandler> handlers, RedisDatabase redisDatabase) {

        JedisPubSub ps = new JedisPubSub() {
            @Override
            public void onPMessage(String pattern, String channel, String message) {

                for (IncomingHandler handler : handlers) {
                    if (handler.getChannel().equals(channel)) {
                        handler.handle(message);
                    }
                }

            }
        };

        ExecutorService thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            try (Jedis j = redisDatabase.getResource()) {

                try {
                    j.psubscribe(ps, pattern);
                } catch (Exception exception) {
                    Logger.severe("Error occured during subscription: " + exception);
                    exception.printStackTrace();
                    thread.shutdown();
                }
            }
        });
    }
}
