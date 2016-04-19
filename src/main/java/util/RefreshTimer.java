package util;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Facilitates the countdown for periodic refreshes.
 */
public class RefreshTimer extends TickingTimer{

    public RefreshTimer(String name, int period, Consumer<Integer> onTick, Runnable onTimeout,
                        TimeUnit timeUnit) {
        super(name, period, onTick, onTimeout, timeUnit);
    }

}
