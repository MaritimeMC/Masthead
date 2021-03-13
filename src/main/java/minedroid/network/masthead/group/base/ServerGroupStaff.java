package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.*;

public class ServerGroupStaff extends ServerGroup {

    public ServerGroupStaff() {
        super(
                "staff",
                50,
                true,
                false,
                ServerPlugin.STAFF,
                SupportedMinecraftVersion._188,
                LoadBalanceConfiguration.RANDOM,
                1024,
                512,
                new CreationThresholdsContainer(1, 1, 1)
        );
    }
}
