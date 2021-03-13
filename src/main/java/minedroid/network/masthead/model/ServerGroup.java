package minedroid.network.masthead.model;

import lombok.*;

/**
 * TODO JavaDoc
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class ServerGroup {

    private final String name;

    @Setter
    private int serverCount = 0;

    private final int maximumPlayers;
    private final boolean disposable;
    private final boolean useAntiCheat;
    private final ServerPlugin basePlugin;
    private final SupportedMinecraftVersion minecraftVersion;
    private final LoadBalanceConfiguration loadBalanceConfiguration;

    private final int ram;
    private final int disk;

    private final CreationThresholdsContainer creationThresholdsContainer;

}
