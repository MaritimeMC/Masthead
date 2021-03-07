package minedroid.network.masthead.panel;

import com.mattmalec.pterodactyl4j.client.ws.events.StatusUpdateEvent;
import com.mattmalec.pterodactyl4j.client.ws.hooks.ClientSocketListenerAdapter;
import minedroid.network.masthead.event.ListenerManager;
import minedroid.network.masthead.event.impl.ServerStatusChangeEvent;
import minedroid.network.masthead.model.MinecraftServer;

public class WebSocketListener extends ClientSocketListenerAdapter {

    private final MinecraftServer minecraftServer;
    private final ListenerManager listenerManager;

    public WebSocketListener(MinecraftServer minecraftServer, ListenerManager listenerManager) {
        this.minecraftServer = minecraftServer;
        this.listenerManager = listenerManager;
    }

    @Override
    public void onStatusUpdate(StatusUpdateEvent event) {
        listenerManager.callEvent(new ServerStatusChangeEvent(minecraftServer, event.getState()));
    }
}
