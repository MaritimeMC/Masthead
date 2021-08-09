package org.maritimemc.masthead.file;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.SneakyThrows;
import org.maritimemc.masthead.log.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

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

}
