package org.maritimemc.masthead.pubsub.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.maritimemc.masthead.pubsub.IncomingHandler;
import org.maritimemc.masthead.server.MinecraftServerManager;

@RequiredArgsConstructor
public class PlayerCountUpdate implements IncomingHandler {

    private final MinecraftServerManager minecraftServerManager;

    @Override
    public String getChannel() {
        return "masthead:player_count_update";
    }

    @Override
    public void handle(String data) {
        JsonObject o = JsonParser.parseString(data).getAsJsonObject();

        minecraftServerManager.updatePlayerCount(o.get("server").getAsString(), o.get("count").getAsInt());
    }
}
