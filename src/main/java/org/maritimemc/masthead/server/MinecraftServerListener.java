package org.maritimemc.masthead.server;

import com.mattmalec.pterodactyl4j.UtilizationState;
import org.maritimemc.masthead.event.ListenerManager;
import org.maritimemc.masthead.event.MastheadListener;
import org.maritimemc.masthead.event.impl.ServerStatusChangeEvent;
import org.maritimemc.masthead.group.ServerGroupManager;
import org.maritimemc.masthead.model.ServerGroup;
import org.maritimemc.masthead.model.ServerStatus;

public class MinecraftServerListener extends MastheadListener {

    private final ServerGroupManager serverGroupManager;
    private final MinecraftServerManager minecraftServerManager;
    private final ListenerManager listenerManager;

    public MinecraftServerListener(ServerGroupManager serverGroupManager, MinecraftServerManager minecraftServerManager, ListenerManager listenerManager) {
        this.serverGroupManager = serverGroupManager;
        this.minecraftServerManager = minecraftServerManager;
        this.listenerManager = listenerManager;
    }

    public void register() {
        listenerManager.register(this);
    }

    @Override
    public void onServerStatusChange(ServerStatusChangeEvent event) {

        ServerGroup group = serverGroupManager.getGroupByName(event.getServer().getServerGroupName());

        if (group.isDisposable() && event.getStatus() == UtilizationState.OFFLINE) {

            minecraftServerManager.disposeOfDeadServer(event.getServer());

        } else {
            switch (event.getStatus()) {
                case OFFLINE:
                case STOPPING:
                    minecraftServerManager.updateServerStatus(event.getServer(), ServerStatus.DEAD, UtilizationState.STOPPING);
                    return;
                case RUNNING:

                    if (group.isGameServer())
                        minecraftServerManager.updateServerStatus(event.getServer(), ServerStatus.IDLE, UtilizationState.RUNNING);
                    else
                        minecraftServerManager.updateServerStatus(event.getServer(), ServerStatus.RUNNING, UtilizationState.RUNNING);



                    return;
                case STARTING:
                    minecraftServerManager.updateServerStatus(event.getServer(), ServerStatus.STARTING, UtilizationState.STARTING);
            }
        }
    }
}
