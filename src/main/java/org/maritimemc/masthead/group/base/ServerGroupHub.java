package org.maritimemc.masthead.group.base;

import org.maritimemc.masthead.model.*;

public class ServerGroupHub extends ServerGroup {

    public ServerGroupHub() {
        super(
                "hub",
                50,
                true,
                true,
                ServerPlugin.HUB,
                SupportedMinecraftVersion._188,
                false,
                LoadBalanceConfiguration.RANDOM,
                1024,
                2048,
                new CreationThresholdsContainer(1, 1, 1)
        );
    }
}
