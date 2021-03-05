package minedroid.network.masthead.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MinecraftServer {

    private final String name;
    private final long panelId;
    private final String panelIdentifier;
    private final ServerStatus status;
    private final boolean online;
    private final int playerCount;
    private final String serverGroupName;

}
