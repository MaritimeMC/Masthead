package minedroid.network.masthead.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.sun.deploy.security.MozillaJSSDSASignature;
import lombok.Getter;
import minedroid.network.masthead.file.FileManager;
import minedroid.network.masthead.log.Logger;

import java.util.logging.Level;

public class MongoDatabase {

    private final FileManager fileManager;

    @Getter
    private com.mongodb.client.MongoDatabase database;

    static {
        // Disable Mongo log spam.
        java.util.logging.Logger.getLogger("org.mongodb").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.mongodb.driver.connection").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.mongodb.driver.cluster").setLevel(Level.OFF);
    }

    public MongoDatabase(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void connect() {
        String host = fileManager.getConfig().getData().get("mongo_host").getAsString();
        String username = fileManager.getConfig().getData().get("mongo_username").getAsString();
        String password = fileManager.getConfig().getData().get("mongo_password").getAsString();
        String database = fileManager.getConfig().getData().get("mongo_database").getAsString();

        ServerAddress address = new ServerAddress(host, 27017);
        MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
        MongoClientOptions options = MongoClientOptions.builder().build();

        MongoClient client = new MongoClient(address, credential, options);

        this.database = client.getDatabase(database);

        Logger.info("Successfully connected to MongoDB at " + host + "/" + database + ".");
    }

}
