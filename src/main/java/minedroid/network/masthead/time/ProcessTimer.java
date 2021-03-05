package minedroid.network.masthead.time;

import lombok.RequiredArgsConstructor;
import minedroid.network.masthead.log.Logger;

@RequiredArgsConstructor
public class ProcessTimer {

    private final String name;
    private long start;

    public void start() {
        start(false);
    }

    public void startSilently() {
        start(true);
    }

    private void start(boolean silentStart) {
        start = System.currentTimeMillis();
        if (!silentStart) Logger.info(name + " begins at " + start + ".");
    }

    public void end() {
        long end = System.currentTimeMillis();
        Logger.info(name + " ended. Took " + (end-start) + "ms.");
    }

}
