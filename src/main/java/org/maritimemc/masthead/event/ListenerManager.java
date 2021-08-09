package org.maritimemc.masthead.event;

import org.maritimemc.masthead.event.impl.ServerStatusChangeEvent;

import java.util.HashSet;
import java.util.Set;

public class ListenerManager {

    private final Set<MastheadListener> listenerSet;

    public ListenerManager() {
        this.listenerSet = new HashSet<>();
    }

    public void register(MastheadListener listener) {
        listenerSet.add(listener);
    }

    public void callEvent(MastheadEvent event) {
        for (MastheadListener mastheadListener : listenerSet) {

            if (event instanceof ServerStatusChangeEvent) mastheadListener.onServerStatusChange((ServerStatusChangeEvent) event);

        }
    }
}
