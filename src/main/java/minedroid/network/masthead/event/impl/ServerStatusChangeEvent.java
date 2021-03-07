package minedroid.network.masthead.event.impl;

import com.mattmalec.pterodactyl4j.UtilizationState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import minedroid.network.masthead.event.MastheadEvent;
import minedroid.network.masthead.model.MinecraftServer;

@RequiredArgsConstructor
@Getter
public class ServerStatusChangeEvent implements MastheadEvent {

    private final MinecraftServer server;
    private final UtilizationState status;

}
