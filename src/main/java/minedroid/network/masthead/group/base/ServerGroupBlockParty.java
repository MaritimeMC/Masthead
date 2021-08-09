package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.*;

public class ServerGroupBlockParty extends ServerGroup {

    public ServerGroupBlockParty() {
        super(
                "party",
                16,
                true,
                true,
                ServerPlugin.BLOCKPARTY,
                SupportedMinecraftVersion._188,
                true,
                LoadBalanceConfiguration.MOST_PLAYERS,
                1024,
                1024,
                new CreationThresholdsContainer(3, 2, 8)
        );
    }
}
