package org.maritimemc.masthead.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServerPlugin {

    BLOCKPARTY("BlockParty");

    private final String repositoryName;
}
