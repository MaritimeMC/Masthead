package org.maritimemc.masthead.event.impl;

import com.mattmalec.pterodactyl4j.UtilizationState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.maritimemc.masthead.event.MastheadEvent;
import org.maritimemc.masthead.model.MinecraftServer;

@RequiredArgsConstructor
@Getter
public class ServerStatusChangeEvent implements MastheadEvent {

    private final MinecraftServer server;
    private final UtilizationState status;

}
