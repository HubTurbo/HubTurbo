package tests;

import org.junit.Test;
import util.TickingTimer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TickingTimerTests {

    private static void delay(double seconds) {
        int time = (int) (seconds * 1000);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static TickingTimer createTickingTimer() {
        return new TickingTimer("test", 10, (i) -> {}, () -> {}, TimeUnit.SECONDS);
    }

    @Test(expected = AssertionError.class)
    public void doublePauseTest() {
        final TickingTimer tickingTimer = createTickingTimer();
        tickingTimer.start();
        tickingTimer.pause();
        tickingTimer.pause();
    }

    @Test
    public void threadingTest() {
        final int attempts = 10;

        final TickingTimer tickingTimer = createTickingTimer();
        tickingTimer.start();
        ExecutorService es = Executors.newFixedThreadPool(2);
        Runnable positive = () -> {
            for (int i=0; i<attempts; i++) {
                tickingTimer.pause();
                delay(2);
            }
        };
        Runnable negative = () -> {
            for (int i=0; i<attempts; i++) {
                tickingTimer.resume();
                delay(2);
            }
        };
        es.execute(positive);
        delay(1);
        es.execute(negative);
    }
}
