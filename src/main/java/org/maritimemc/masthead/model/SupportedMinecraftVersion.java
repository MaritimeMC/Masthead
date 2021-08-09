package org.maritimemc.masthead.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SupportedMinecraftVersion {

    _188("1.8.8"),
    _117("1.17.1");

    private final String paperVersionId;

}
