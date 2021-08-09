package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.*;

public class ServerGroupSurvival extends ServerGroup {

    public ServerGroupSurvival() {
        super("survival",
                100,
                false,
                true,
                ServerPlugin.SURVIVAL,
                SupportedMinecraftVersion._188,
                false,
                LoadBalanceConfiguration.RANDOM,
                4096,
                51200,
                new CreationThresholdsContainer(1, 0, 1));
    }
}
