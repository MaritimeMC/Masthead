package org.maritimemc.masthead.group.base;

import org.maritimemc.masthead.model.*;

public class ServerGroupSurvival extends ServerGroup {

    public ServerGroupSurvival() {
        super("survival",
                100,
                false,
                true,
                ServerPlugin.SURVIVAL,
                SupportedMinecraftVersion._118,
                false,
                LoadBalanceConfiguration.RANDOM,
                5120,
                0,
                new CreationThresholdsContainer(1, 0, 1));
    }
}
