package minedroid.network.masthead;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    public static ExecutorService ASYNC_POOL = Executors.newCachedThreadPool();
}
