package minedroid.network.masthead.group.monitor;

import minedroid.network.masthead.model.CreationThresholdsContainer;
import minedroid.network.masthead.model.MinecraftServer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.panel.PterodactylController;
import minedroid.network.masthead.server.MinecraftServerManager;

import java.util.Set;

public class ServerGroupMonitor {

    private final ServerGroup serverGroup;
    private final MinecraftServerManager minecraftServerManager;
    private final PterodactylController pterodactylController;

    public ServerGroupMonitor(ServerGroup serverGroup, MinecraftServerManager minecraftServerManager, PterodactylController pterodactylController) {
        this.serverGroup = serverGroup;
        this.minecraftServerManager = minecraftServerManager;
        this.pterodactylController = pterodactylController;
    }

    public void createServer() {
        minecraftServerManager.createServer(serverGroup);
    }

    public void init() {

        CreationThresholdsContainer creationThresholds = serverGroup.getCreationThresholdsContainer();

        Set<MinecraftServer> servers = minecraftServerManager.getGroupServers(serverGroup);

        int serverCount = servers.size();

        if (serverCount >= creationThresholds.getMaximumServers()) {
            // Cannot do anything; maximum server limit reached.
            return;
        }

    }


}
