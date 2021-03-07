package minedroid.network.masthead;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import minedroid.network.masthead.bungee.BungeeCordManager;
import minedroid.network.masthead.db.MongoDatabase;
import minedroid.network.masthead.db.RedisDatabase;
import minedroid.network.masthead.file.FileManager;
import minedroid.network.masthead.group.ServerGroupManager;
import minedroid.network.masthead.log.Logger;
import minedroid.network.masthead.model.MinecraftServer;
import minedroid.network.masthead.panel.PterodactylController;
import minedroid.network.masthead.server.MinecraftServerManager;
import minedroid.network.masthead.time.ProcessTimer;

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

        FileManager fileManager = new FileManager();
        fileManager.load();

        PterodactylController pterodactylController = new PterodactylController(fileManager);
        pterodactylController.load();
        pterodactylController.ensureCredentials();

        MongoDatabase mongoDatabase = new MongoDatabase(fileManager);
        mongoDatabase.connect();

        RedisDatabase redisDatabase = new RedisDatabase(fileManager);
        redisDatabase.connect();

        ServerGroupManager serverGroupManager = new ServerGroupManager(pterodactylController, fileManager, mongoDatabase);
        serverGroupManager.load();

        BungeeCordManager bungeeCordManager = new BungeeCordManager(redisDatabase);

        MinecraftServerManager minecraftServerManager = new MinecraftServerManager(pterodactylController, mongoDatabase, bungeeCordManager);
        minecraftServerManager.load();

        Logger.empty();
        pt.end();
        Logger.info("Masthead startup complete.");
        Logger.empty();
    }
}
