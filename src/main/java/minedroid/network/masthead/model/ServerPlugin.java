package minedroid.network.masthead.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServerPlugin {

    HUB("Hub"),
    SKYWARS("SkyWars"),
    STAFF("StaffServer"),
    SURVIVAL("Survival"),
    BLOCKPARTY("BlockParty");

    private final String repositoryName;
}
