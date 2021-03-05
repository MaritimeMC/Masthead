package minedroid.network.masthead.file;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.SneakyThrows;
import minedroid.network.masthead.log.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;

import static minedroid.network.masthead.Masthead.GSON;

public class JsonConfigurationFile {

    private static final String FILE_DIRECTORY = "data";

    private final String name;

    @Getter
    private JsonObject data;

    public JsonConfigurationFile(String name) {
        this.name = name;
    }

    @SneakyThrows
    public void load() {
        File dir = new File(FILE_DIRECTORY);
        if (!dir.exists()) dir.mkdirs();

        File f = new File(FILE_DIRECTORY, name);
        if (!f.exists()) {
            InputStream stream = getClass().getResourceAsStream("/" + name);
            FileUtils.copyInputStreamToFile(stream, f);
            Logger.info("File " + name + " not found in " + FILE_DIRECTORY + " directory. We saved it.");
        }

        this.data = JsonParser.parseReader(new FileReader(f)).getAsJsonObject();
        Logger.info("Successfully loaded: " + name);
    }

    @SneakyThrows
    public void save() {
        if (data == null) {
            Logger.warning("Could not save " + name + " as file was not loaded.");
            return;
        }

        File f = new File(FILE_DIRECTORY, name);

        try (FileWriter fw = new FileWriter(f)) {
            fw.write(GSON.toJson(data));
            Logger.info("Successfully saved: " + name);
        }
    }
}
