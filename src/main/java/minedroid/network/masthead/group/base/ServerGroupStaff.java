package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.CreationThresholdsContainer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.model.ServerPlugin;
import minedroid.network.masthead.model.SupportedMinecraftVersion;

public class ServerGroupStaff extends ServerGroup {

    public ServerGroupStaff() {
        super(
                "staff",
                50,
                true,
                false,
                ServerPlugin.STAFF,
                SupportedMinecraftVersion._188,
                1024,
                512,
                new CreationThresholdsContainer(1, 1, 1)
        );
    }
}
