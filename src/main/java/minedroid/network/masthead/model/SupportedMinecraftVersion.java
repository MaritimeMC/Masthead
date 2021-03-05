package minedroid.network.masthead.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SupportedMinecraftVersion {

    _188("1.8.8"),
    _1165("1.16.5");

    private final String paperVersionId;

}
