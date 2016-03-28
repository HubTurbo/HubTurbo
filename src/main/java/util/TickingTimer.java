package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A abstract timer that counts down ('ticks') on a specified period. Repeats
 * once the countdown is completed ('times out'). Supports hooks for ticks and
 * for timing out. Also supports various operations on the state of the timer.
 * <p>
 * This class is thread-safe, however compound operations require explicit
 * synchronization:
 * <p>
 * TickingTimer t = new TickingTimer(...);
 * synchronized (t) {
 * if (t.isPaused()) {
 * t.resume();
 * }
 * }
 */
public class TickingTimer {

    private static final Logger logger = LogManager.getLogger(TickingTimer.class.getName());

    // TICK_PERIOD must divide period, so a small value is best
    private static final int TICK_PERIOD = 1;
    private final TimeUnit timeUnit;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    // The name of the timer. Used to identify it in logging messages and such.
    private final String name;

    // The period after which the timer times out.
    private final int period;

    // onTick will not pause the timer when run, so it should not be a long-running task.
    // Will run before onTimeout.
    private final Consumer<Integer> onTick;

    // onTimeout will pause the timer when run.
    private final Runnable onTimeout;

    // Mutable state -- all internal access to these fields must be synchronized!
    private int time;
    private boolean paused = false;
    private boolean started = false;
    private final List<CountDownLatch> latches = Collections.synchronizedList(new ArrayList<>());

    public TickingTimer(String name, int period, Consumer<Integer> onTick, Runnable onTimeout, TimeUnit timeUnit) {
        this.name = name;
        this.period = period;
        this.onTick = onTick;
        this.onTimeout = onTimeout;
        this.timeUnit = timeUnit;

        this.time = period;
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    /**
     * Pauses the timer. Must be called in pairs with {@link #resume() resume}.
     */
    public synchronized void pause() {
        assert !paused : "Attempt to pause already-paused TickingTimer";
        paused = true;
    }

    /**
     * Resumes the timer. Must be called in pairs with {@link #pause() pause}.
     */
    public synchronized void resume() {
        assert paused : "Attempt to unpause TickingTimer that isn't paused";
        paused = false;
    }

    public synchronized void restart() {
        time = period;
    }

    /**
     * Causes the timer to time out on the next tick.
     * Returns a latch that will block until after onTimeout is called.
     */
    public synchronized CountDownLatch trigger() {
        CountDownLatch latch = new CountDownLatch(1);
        latches.add(latch);
        time = TICK_PERIOD;
        return latch;
    }

    /**
     * Starts the timer. Must be called in pairs with {@link #stop() stop}.
     */
    public void start() {
        synchronized (this) {
            assert !started : "Attempt to start TickingTimer that has already been started";
            started = true;
        }
        executor.scheduleWithFixedDelay(() -> {
            boolean restarted = false;
            int currentTime;
            synchronized (this) {
                if (paused) {
                    return;
                }
                time -= TICK_PERIOD;
                if (time == 0) {
                    restart();
                    restarted = true;
                }
                currentTime = time;
            }
            onTick.accept(currentTime);
            if (restarted) {
                onTimeout.run();
            }
            synchronized (this) {
                latches.forEach(CountDownLatch::countDown);
                latches.clear();
            }
        }, 0, TICK_PERIOD, timeUnit);
        logger.info("Started TickingTimer " + name);
    }

    /**
     * Stops the timer and cleans up the threading machinery.
     * Must be called in pairs with {@link #start() start}.
     */
    public void stop() {
        synchronized (this) {
            assert started : "Attempt to stop TickingTimer that is not running";
            started = false;
        }
        logger.info("Stopping TickingTimer " + name);
        executor.shutdown();
        try {
            executor.awaitTermination(TICK_PERIOD, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Successfully stopped TickingTimer " + name);
    }

    public boolean isStarted() {
        return started;
    }

}
