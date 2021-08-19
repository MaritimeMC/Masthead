package org.maritimemc.masthead.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServerPlugin {

    BLOCKPARTY("block-party"),
    HUB("hub"),
    SURVIVAL("survival");

    private final String repositoryName;
}
