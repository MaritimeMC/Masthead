package org.maritimemc.masthead.group.monitor;

import org.maritimemc.masthead.model.ServerGroup;

/**
 * Represents a reason why {@link ServerGroupMonitor} is requested to run a
 * monitor check on its {@link ServerGroup}.
 */
public enum CreationUpdateReason {

    /**
     * The server amounts should be checked as a server has been deleted.
     */
    SERVER_DEAD,

    /**
     * The server amounts should be checked as Masthead is starting up.
     */
    STARTUP,

    /**
     * The server amounts should be checked as a server has changed its status from IDLE to RUNNING.
     */
    SERVER_STATUS_CHANGE,

    /**
     * The player amounts are checked at 10 second intervals to determine if another should be
     * created based on group-wide player counts.
     */
    AUTOMATIC;

}
