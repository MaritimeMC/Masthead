package org.maritimemc.masthead.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SupportedMinecraftVersion {

    _188("1.8.8", "quay.io/pterodactyl/core:java"),
    _117("1.17.1", "quay.io/parkervcp/pterodactyl-images:debian_openjdk-16");

    private final String paperVersionId;
    private final String image;

}
