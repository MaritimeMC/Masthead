package org.maritimemc.masthead.event;

import org.maritimemc.masthead.event.impl.ServerStatusChangeEvent;

public abstract class MastheadListener {

    public void onServerStatusChange(ServerStatusChangeEvent event) { }
}
