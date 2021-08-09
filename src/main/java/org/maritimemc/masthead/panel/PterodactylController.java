package org.maritimemc.masthead.panel;

import com.mattmalec.pterodactyl4j.*;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.application.managers.ServerCreationAction;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import com.mattmalec.pterodactyl4j.exceptions.LoginException;
import lombok.RequiredArgsConstructor;
import org.maritimemc.masthead.event.ListenerManager;
import org.maritimemc.masthead.file.FileManager;
import org.maritimemc.masthead.log.Logger;
import org.maritimemc.masthead.model.MinecraftServer;
import org.maritimemc.masthead.model.ServerGroup;
import org.maritimemc.masthead.model.ServerStatus;
import org.maritimemc.masthead.model.SupportedMinecraftVersion;
import org.maritimemc.masthead.time.ProcessTimer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PterodactylController {

    public static final int EGG_ID = 15;

    private final FileManager fileManager;
    private final ListenerManager listenerManager;

    private PteroApplication pteroApplication;
    private PteroClient pteroClient;

    private PanelAuthDetails panelAuthDetails;

    public void load() {
        panelAuthDetails = new PanelAuthDetails(
                fileManager.getConfig().getData().get("pterodactyl_host").getAsString(),
                fileManager.getConfig().getData().get("pterodactyl_client_api_key").getAsString(),
                fileManager.getConfig().getData().get("pterodactyl_application_api_key").getAsString(),
                fileManager.getConfig().getData().get("pterodactyl_admin_user_id").getAsInt()
        );

        this.pteroApplication = PteroBuilder.createApplication(panelAuthDetails.getPanelUrl(), panelAuthDetails.getApplicationApiKey());
        this.pteroClient = PteroBuilder.createClient(panelAuthDetails.getPanelUrl(), panelAuthDetails.getClientApiKey());
    }

    public void ensureCredentials() {
        try {
            pteroClient.retrieveAccount().execute();
            Logger.info("Pterodactyl client key correct and working.");
        } catch (LoginException ex) {
            Logger.fatal("Pterodactyl client key incorrect. Please update. Exiting...");
            System.exit(1);
        }

        try {
            pteroApplication.retrieveServers().execute();
            Logger.info("Pterodactyl application key correct and working.");
        } catch (LoginException ex) {
            Logger.fatal("Pterodactyl application key incorrect. Please update. Exiting...");
            System.exit(1);
        }
    }

    public void deleteServer(MinecraftServer minecraftServer) {
        ApplicationServer server = pteroApplication.retrieveServerById(minecraftServer.getPanelId()).execute();
        deleteServer(server);
    }

    public void deleteServer(ApplicationServer server) {
        ProcessTimer pt = new ProcessTimer("Server deletion for " + server.getName());
        pt.startSilently();

        Logger.info("PterodactylController received deletion request for " + server.getName() + ".");

        ClientServer execute = pteroClient.retrieveServerByIdentifier(server.getIdentifier()).execute();

        if (!execute.isInstalling()) pteroClient.setPower(execute, PowerAction.STOP).execute();

        server.getController().delete(false).execute();

        pt.end();
    }

    public MinecraftServer createServer(ServerGroup serverGroup, String name) {
        ProcessTimer pt = new ProcessTimer("Server creation for " + serverGroup.getName());

        Logger.info("PterodactylController received creation request for " + serverGroup.getName() + ".");

        pt.startSilently();
        ServerCreationAction server = pteroApplication.createServer();

        ApplicationUser adminUser = pteroApplication.retrieveUserById(panelAuthDetails.getAdminUserId()).execute();
        server.setOwner(adminUser);

        Nest nest = pteroApplication.retrieveNestById(1).execute();
        ApplicationEgg egg = pteroApplication.retrieveEggById(nest, EGG_ID).execute();
        server.setEgg(egg);

        Location loc = pteroApplication.retrieveLocationById(1).execute();
        server.setLocation(loc);

        server.setName(name);

        server.setEnvironment(
                generateEnvironmentVariables(serverGroup.getMinecraftVersion(),
                        (serverGroup.getBasePlugin() != null) ? serverGroup.getBasePlugin().getRepositoryName() : null,
                        name,
                        serverGroup.isUseAntiCheat())
        );

        server.setMemory(serverGroup.getRam(), DataType.MB);
        server.setDisk(serverGroup.getDisk(), DataType.MB);
        server.setCPU(0);

        server.setBackups(0);
        server.setAllocations(0);
        server.setDatabases(0);

        server.setIO(500);
        server.setSwap(0, DataType.MB);

        server.startOnCompletion(true);

        ApplicationServer applicationServer = server.execute();

        Logger.info("Successfully created " + name + " in Panel.");

        MinecraftServer minecraftServer = buildMinecraftServer(applicationServer, serverGroup);

        ClientServer cs = pteroClient.retrieveServerByIdentifier(applicationServer.getIdentifier()).execute();
        cs.getWebSocketBuilder().addEventListeners(new WebSocketListener(minecraftServer, listenerManager)).build();
        pt.end();

        return minecraftServer;
    }

    private Map<String, EnvironmentValue<?>> generateEnvironmentVariables(SupportedMinecraftVersion smv, String repoName, String name, boolean useAntiCheat) {
        Map<String, EnvironmentValue<?>> env = new HashMap<>();
        env.put("MINECRAFT_VERSION", EnvironmentValue.ofString(smv.getPaperVersionId()));
        env.put("SERVER_NAME", EnvironmentValue.ofString(name));
        if (repoName != null) env.put("GROUP_REPO_NAME", EnvironmentValue.ofString(repoName));
        env.put("AC", EnvironmentValue.of(useAntiCheat));

        return env;
    }

    public ClientServer getClientServer(String identifier) {
        return pteroClient.retrieveServerByIdentifier(identifier).execute();
    }

    public List<ApplicationServer> getServers() {
        return pteroApplication.retrieveServers().execute();
    }

    private MinecraftServer buildMinecraftServer(ApplicationServer server, ServerGroup group) {
        assert server.getAllocations().isPresent();
        return new MinecraftServer(
                server.getName(),
                server.getIdLong(),
                server.getIdentifier(),
                server.getAllocations().get().get(0).getIP(),
                server.getAllocations().get().get(0).getPortInt(),
                ServerStatus.CREATING,
                UtilizationState.OFFLINE,
                0,
                group.getName()
        );
    }

}
