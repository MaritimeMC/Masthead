package minedroid.network.masthead.server;

import com.mattmalec.pterodactyl4j.UtilizationState;
import minedroid.network.masthead.event.ListenerManager;
import minedroid.network.masthead.event.MastheadListener;
import minedroid.network.masthead.event.ServerStatusChangeEvent;
import minedroid.network.masthead.group.ServerGroupManager;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.model.ServerStatus;

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
                    minecraftServerManager.updateServerStatus(event.getServer(), ServerStatus.DEAD);
                    return;
                case RUNNING:
                    minecraftServerManager.updateServerStatus(event.getServer(), ServerStatus.IDLE);
                    return;
                case STARTING:
                    minecraftServerManager.updateServerStatus(event.getServer(), ServerStatus.STARTING);

            }
        }
    }
}
