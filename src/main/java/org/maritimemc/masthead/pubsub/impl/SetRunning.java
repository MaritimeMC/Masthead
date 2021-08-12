package org.maritimemc.masthead.pubsub.impl;

import lombok.RequiredArgsConstructor;
import org.maritimemc.masthead.model.ServerStatus;
import org.maritimemc.masthead.pubsub.IncomingHandler;
import org.maritimemc.masthead.server.MinecraftServerManager;

@RequiredArgsConstructor
public class SetRunning implements IncomingHandler {

    private final MinecraftServerManager minecraftServerManager;

    @Override
    public String getChannel() {
        return "masthead:set_running";
    }

    @Override
    public void handle(String data) {
        minecraftServerManager.updateServerStatus(data, ServerStatus.RUNNING);
    }
}
