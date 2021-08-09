package org.maritimemc.masthead;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.maritimemc.masthead.db.MongoDatabase;
import org.maritimemc.masthead.db.RedisDatabase;
import org.maritimemc.masthead.event.ListenerManager;
import org.maritimemc.masthead.file.FileManager;
import org.maritimemc.masthead.group.ServerGroupManager;
import org.maritimemc.masthead.log.Logger;
import org.maritimemc.masthead.panel.PterodactylController;
import org.maritimemc.masthead.server.MinecraftServerManager;
import org.maritimemc.masthead.time.ProcessTimer;

public class Masthead {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public Masthead() { }

    @SneakyThrows
    public void run() {
        Logger.info("============================== MASTHEAD ============================== ");

        ProcessTimer pt = new ProcessTimer("Startup Process");
        pt.start();

        Logger.empty();

        ListenerManager listenerManager = new ListenerManager();

        FileManager fileManager = new FileManager();
        fileManager.load();

        PterodactylController pterodactylController = new PterodactylController(fileManager, listenerManager);
        pterodactylController.load();
        pterodactylController.ensureCredentials();

        MongoDatabase mongoDatabase = new MongoDatabase(fileManager);
        mongoDatabase.connect();

        RedisDatabase redisDatabase = new RedisDatabase(fileManager);
        redisDatabase.connect();

        ServerGroupManager serverGroupManager = new ServerGroupManager(mongoDatabase);
        serverGroupManager.load();

        MinecraftServerManager minecraftServerManager = new MinecraftServerManager(serverGroupManager, pterodactylController, mongoDatabase, listenerManager, redisDatabase);
        minecraftServerManager.load();

        Logger.empty();
        pt.end();
        Logger.info("Masthead startup complete.");
        Logger.empty();
    }
}
