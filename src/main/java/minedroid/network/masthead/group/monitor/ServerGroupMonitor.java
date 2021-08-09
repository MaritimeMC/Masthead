package minedroid.network.masthead.group.monitor;

import minedroid.network.masthead.log.Logger;
import minedroid.network.masthead.model.CreationThresholdsContainer;
import minedroid.network.masthead.model.MinecraftServer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.model.ServerStatus;
import minedroid.network.masthead.server.MinecraftServerManager;

import java.util.Set;

public class ServerGroupMonitor {

    private final ServerGroup serverGroup;
    private final MinecraftServerManager minecraftServerManager;

    public ServerGroupMonitor(ServerGroup serverGroup, MinecraftServerManager minecraftServerManager) {
        this.serverGroup = serverGroup;
        this.minecraftServerManager = minecraftServerManager;
    }

    public void createServer(boolean async) {
        minecraftServerManager.createServer(serverGroup, async);
    }

    public void requestCreationUpdate(CreationUpdateReason cur) {
        if (cur != CreationUpdateReason.AUTOMATIC) Logger.info("Requesting creation update for group " + serverGroup.getName() + ": CreationUpdateReason#" + cur.toString());

        doCreations(cur);
    }

    private void doCreations(CreationUpdateReason cur) {
        boolean async = cur != CreationUpdateReason.STARTUP;

        CreationThresholdsContainer creationThresholds = serverGroup.getCreationThresholdsContainer();

        int idleServers = 0;
        int totalServers = 0;

        if (cur != CreationUpdateReason.AUTOMATIC) {

            Set<MinecraftServer> servers = minecraftServerManager.getGroupServers(serverGroup);

            int serverCount = servers.size();

            if (serverCount >= creationThresholds.getMaximumServers()) {
                // Cannot do anything; maximum server limit reached.
                Logger.info("Cannot perform any creations as maximum server limit for group is reached.");
                return;
            }

            for (MinecraftServer server : servers) {
                if (server.getStatus() != ServerStatus.DEAD) totalServers++;
                if (server.getStatus() != ServerStatus.RUNNING && server.getStatus() != ServerStatus.DEAD) idleServers++;
            }

            if (idleServers < creationThresholds.getMinimumIdleServers()) {
                int amountNeeded = creationThresholds.getMinimumIdleServers() - idleServers;

                int serversThatWouldExist = amountNeeded + totalServers;
                if (serversThatWouldExist > creationThresholds.getMaximumServers()) {
                    int dif = serversThatWouldExist - creationThresholds.getMaximumServers();
                    amountNeeded -= dif;
                }

                if (amountNeeded >= 1) {
                    Logger.info("Not enough idle servers for " + serverGroup.getName() + "; creating " + amountNeeded);
                    for (int i = 0; i < amountNeeded; i++) {
                        createServer(async);
                    }

                    totalServers += amountNeeded;
                }
            }

            if (totalServers < creationThresholds.getMinimumServers()) {
                int amountNeeded = creationThresholds.getMinimumServers() - totalServers;

                Logger.info("Not enough total servers for " + serverGroup.getName() + "; creating " + amountNeeded);
                for (int i = 0; i < amountNeeded; i++) {
                    createServer(async);
                }

                totalServers += amountNeeded;
            }
        }

        if (serverGroup.isDisposable()) {
            int totalPlayers = 0,
                    ceilingPlayers = minecraftServerManager.getGroupServers(serverGroup).size() * serverGroup.getMaximumPlayers();

            for (MinecraftServer groupServer : minecraftServerManager.getGroupServers(serverGroup)) {
                totalPlayers += groupServer.getPlayerCount();
            }

            if (totalPlayers / (double) ceilingPlayers > 0.75) {
                // Too many players, make another server.

                int posTotal = totalServers + 1;

                if (posTotal <= creationThresholds.getMaximumServers()) {
                    Logger.info("Group " + serverGroup.getName() + ": " + totalPlayers + " with ceiling of " + ceilingPlayers + ". Creating another server.");
                    createServer(async);
                }
            }
        }
    }


}
