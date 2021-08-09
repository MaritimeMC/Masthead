package org.maritimemc.masthead.group.base;

import org.maritimemc.masthead.model.*;

public class ServerGroupBlockParty extends ServerGroup {

    public ServerGroupBlockParty() {
        super(
                "bp",
                20,
                true,
                true,
                ServerPlugin.BLOCKPARTY,
                SupportedMinecraftVersion._188,
                true,
                LoadBalanceConfiguration.MOST_PLAYERS,
                1024,
                2048,
                new CreationThresholdsContainer(2, 2, 8)
        );
    }
}
