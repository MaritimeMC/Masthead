package minedroid.network.masthead.model;

import com.mattmalec.pterodactyl4j.UtilizationState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class MinecraftServer {

    private final String name;
    private final long panelId;
    private final String panelIdentifier;
    private final String ip;
    private final int port;

    @Setter
    private ServerStatus status;

    @Setter
    private UtilizationState panelStatus;

    @Setter
    private boolean online;

    @Setter
    private int playerCount;

    private final String serverGroupName;

}
