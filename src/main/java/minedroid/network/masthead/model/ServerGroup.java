package minedroid.network.masthead.model;

import lombok.*;

/**
 * TODO JavaDoc
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class ServerGroup {

    private final String name;

    private final int maximumPlayers;
    private final boolean disposable;
    private final boolean useAntiCheat;
    private final ServerPlugin basePlugin;
    private final SupportedMinecraftVersion minecraftVersion;
    private final boolean gameServer;
    private final LoadBalanceConfiguration loadBalanceConfiguration;

    private final int ram;
    private final int disk;

    private final CreationThresholdsContainer creationThresholdsContainer;

}
