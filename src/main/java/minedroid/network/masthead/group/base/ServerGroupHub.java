package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.*;

public class ServerGroupHub extends ServerGroup {

    public ServerGroupHub() {
        super(
                "hub",
                30,
                true,
                true,
                ServerPlugin.HUB,
                SupportedMinecraftVersion._188,
                LoadBalanceConfiguration.LEAST_PLAYERS,
                1024,
                512,
                new CreationThresholdsContainer(1, 1, 4)
        );
    }
}
