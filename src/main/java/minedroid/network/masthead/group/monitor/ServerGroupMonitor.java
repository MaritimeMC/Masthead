package minedroid.network.masthead.group.monitor;

import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.panel.PterodactylController;

public class ServerGroupMonitor {

    private ServerGroup serverGroup;
    private PterodactylController pterodactylController;

    public ServerGroupMonitor(ServerGroup serverGroup, PterodactylController pterodactylController) {
        this.serverGroup = serverGroup;
        this.pterodactylController = pterodactylController;
    }

    public void createServer() {

    }
}
