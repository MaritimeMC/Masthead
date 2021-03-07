package minedroid.network.masthead.group.base;

import minedroid.network.masthead.model.CreationThresholdsContainer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.model.ServerPlugin;
import minedroid.network.masthead.model.SupportedMinecraftVersion;

public class ServerGroupHub extends ServerGroup {

    public ServerGroupHub() {
        super(
                "hub",
                30,
                true,
                true,
                ServerPlugin.HUB,
                SupportedMinecraftVersion._188,
                1024,
                512,
                new CreationThresholdsContainer(1, 1, 4)
        );
    }
}
