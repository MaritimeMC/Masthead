package minedroid.network.masthead.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Represents a {@link ServerGroup}'s server creation thresholds.
 *
 * @author Embrasure, special thanks to elkell.
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public class CreationThresholdsContainer {

    private final int minimumServers;
    private final int minimumIdleServers;
    private final int maximumServers;

    /**
     * The minimum amount of servers which should always be available and online.
     *
     * @return The minimum servers value.
     */
    public int getMinimumServers() {
        return minimumServers;
    }

    /**
     * The minimum amount of servers which should always be idle.
     * If a server changes from idle to running, then another should be created to replace it.
     *
     * @return The minimum idle servers value.
     */
    public int getMinimumIdleServers() {
        return minimumIdleServers;
    }

    /**
     * The maximum number of servers which should ever be present.
     * <p>
     * No more servers should be created, even if the amount of idle servers is less
     * than the minimum idle servers value.
     *
     * @return The maximum servers value.
     */
    public int getMaximumServers() {
        return maximumServers;
    }
}
