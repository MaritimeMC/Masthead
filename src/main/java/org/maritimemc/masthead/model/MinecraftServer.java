package org.maritimemc.masthead.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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
    private ServerPanelState panelStatus;

    @Setter
    private int playerCount;

    private final String serverGroupName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftServer that = (MinecraftServer) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
