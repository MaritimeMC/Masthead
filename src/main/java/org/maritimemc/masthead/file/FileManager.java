package org.maritimemc.masthead.file;

import lombok.Getter;

public class FileManager {

    @Getter
    private JsonConfigurationFile config;

    public void load() {
        this.config = new JsonConfigurationFile("config.json");
        config.load();
    }
}
