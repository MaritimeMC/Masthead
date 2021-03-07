package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.CreationThresholdsContainer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.model.ServerPlugin;
import minedroid.network.masthead.model.SupportedMinecraftVersion;

public class ServerGroupSkyWars extends ServerGroup {

    public ServerGroupSkyWars() {
        super(
                "skywars",
                20,
                true,
                true,
                ServerPlugin.SKYWARS,
                SupportedMinecraftVersion._188,
                1024,
                1024,
                new CreationThresholdsContainer(1, 1, 8)
        );
    }
}
