package minedroid.network.masthead.panel;

import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.PowerAction;
import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.application.managers.ServerAction;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import com.mattmalec.pterodactyl4j.client.ws.hooks.ClientSocketListenerAdapter;
import com.mattmalec.pterodactyl4j.exceptions.LoginException;
import lombok.RequiredArgsConstructor;
import minedroid.network.masthead.ThreadPool;
import minedroid.network.masthead.event.ListenerManager;
import minedroid.network.masthead.file.FileManager;
import minedroid.network.masthead.log.Logger;
import minedroid.network.masthead.model.MinecraftServer;
import minedroid.network.masthead.model.ServerGroup;
import minedroid.network.masthead.model.ServerStatus;
import minedroid.network.masthead.model.SupportedMinecraftVersion;
import minedroid.network.masthead.time.ProcessTimer;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PterodactylController {

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
        ServerAction server = pteroApplication.createServer();

        ApplicationUser adminUser = pteroApplication.retrieveUserById(panelAuthDetails.getAdminUserId()).execute();
        server.setOwner(adminUser);

        Nest nest = pteroApplication.retrieveNestById(1).execute();
        ApplicationEgg egg = pteroApplication.retrieveEggById(nest, 3).execute();
        server.setEgg(egg);

        Location loc = pteroApplication.retrieveLocationById(1).execute();
        server.setLocation(loc);

        server.setName(name);

        server.setEnvironment(
                generateEnvironmentVariables(serverGroup.getMinecraftVersion(), name)
        );

        server.setMemory(serverGroup.getRam(), DataType.MB);
        server.setDisk(serverGroup.getDisk(), DataType.MB);
        server.setCPU(0);

        server.setBackups(0);
        server.setAllocations(0);
        server.setDatabases(0);

        server.setIO(500);
        server.setSwap(0, DataType.MB);

        ApplicationServer applicationServer = server.build().execute();

        Logger.info("Successfully created " + name + " in Panel.");

        MinecraftServer minecraftServer = buildMinecraftServer(applicationServer, serverGroup);

        ClientServer cs = pteroClient.retrieveServerByIdentifier(applicationServer.getIdentifier()).execute();
        cs.getWebSocketBuilder().addEventListeners(new WebSocketListener(minecraftServer, listenerManager)).build();
        pt.end();

        return minecraftServer;
    }

    private Map<String, String> generateEnvironmentVariables(SupportedMinecraftVersion smv, String name) {
        Map<String, String> env = new HashMap<>();
        env.put("MINECRAFT_VERSION", smv.getPaperVersionId());
        env.put("SERVER_JARFILE", "server.jar");
        env.put("BUILD_NUMBER", "latest");
        env.put("SERVER_NAME", name);

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
                true,
                0,
                group.getName()
        );
    }

}
