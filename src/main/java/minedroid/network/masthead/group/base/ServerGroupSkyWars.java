package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.*;

public class ServerGroupSkyWars extends ServerGroup {

    public ServerGroupSkyWars() {
        super(
                "skywars",
                20,
                true,
                true,
                ServerPlugin.SKYWARS,
                SupportedMinecraftVersion._188,
                LoadBalanceConfiguration.MOST_PLAYERS,
                1024,
                1024,
                new CreationThresholdsContainer(1, 1, 8)
        );
    }
}
