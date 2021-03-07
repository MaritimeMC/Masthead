package minedroid.network.masthead.event;

import minedroid.network.masthead.event.impl.ServerStatusChangeEvent;

public abstract class MastheadListener {

    public void onServerStatusChange(ServerStatusChangeEvent event) { }
}
