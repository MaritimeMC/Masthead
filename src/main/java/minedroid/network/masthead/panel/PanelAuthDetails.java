package minedroid.network.masthead.panel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PanelAuthDetails {

    private final String panelUrl;
    private final String clientApiKey;
    private final String applicationApiKey;
    private final int adminUserId;
}
