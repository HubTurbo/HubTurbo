package tests;

import org.junit.Test;
import util.TickingTimer;
import util.TickingTimerManager;
import util.Utility;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * This class tests the correct behaviour of the updateTickerTimer method of the TickingTimerManager class.
 */
public class TickingTimerManagerTest {

    TickingTimerManager timerMgr;

    public TickingTimerManagerTest() {
        TickingTimer refreshTimer = new TickingTimer("Refresh Timer",
                TickingTimerManager.DEFAULT_REFRESH_PERIOD_IN_SEC, (Integer stub) -> {
        }, () -> {
        }, TimeUnit.SECONDS);
        timerMgr = new TickingTimerManager(refreshTimer);
    }

    @Test
    public void computeTickerTimerWhenInitAppLaunch() {
        long result = timerMgr.computeTickerTimerPeriod(100, 10, 0);
        assertEquals(result, Utility.secsToMins(TickingTimerManager.DEFAULT_REFRESH_PERIOD_IN_SEC));
    }

    @Test
    public void computeTickerTimerWhenOutOfQuota() {
        long result = timerMgr.computeTickerTimerPeriod(0, 35, 0);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void computeTickerTimerWhenMinimalQuota() {
        long result = timerMgr.computeTickerTimerPeriod(1, 35, 15);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void computeTickerTimerWhenQuotaBelowAPIQuotaBuffer() {
        long result = timerMgr.computeTickerTimerPeriod(199,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);

        result = timerMgr.computeTickerTimerPeriod(1,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void computeTickerTimerWhenQuotaAtAPIQuotaBuffer() {
        long result = timerMgr.computeTickerTimerPeriod(200,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void computeTickerTimerWhenQuotaAboveAPIQuotaBuffer() {
        long result;
        /*
        Case #1: when remainingQuota - buffer is less than lastApiCallsUsed,
        Don't auto refresh until next apiQuota renewal
        */
        result = timerMgr.computeTickerTimerPeriod(201, 35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);

        result = timerMgr.computeTickerTimerPeriod(224, 35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);

        /*
        Case #2: when (remainingQuota - buffer) is more or equal than lastApiCallsUsed,
        Calculate the refreshTime as normal
        */
        result = timerMgr.computeTickerTimerPeriod(225, 35, 25);
        assertEquals(result, 36);

        result = timerMgr.computeTickerTimerPeriod(226, 35, 25);
        assertEquals(result, 36);

        result = timerMgr.computeTickerTimerPeriod(3000, 35, 25);
        assertEquals(result, 1);

        result = timerMgr.computeTickerTimerPeriod(3000, 35, 223);
        assertEquals(result, 3);
    }

    @Test
    public void computeTickerTimerWhenNoOfRefreshEqualOne() {
        long result = timerMgr.computeTickerTimerPeriod(886, 9, 502);
        assertEquals(result, 10);
    }

    @Test
    public void computeTickerTimerWhenRemainingTimeEqualZero() {
        long result = timerMgr.computeTickerTimerPeriod(0, 0, 502);
        assertEquals(result, 1);
    }
}
