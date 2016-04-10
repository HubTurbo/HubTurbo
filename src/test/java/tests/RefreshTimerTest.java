package tests;

import org.junit.Test;
import util.RefreshTimer;

import static org.junit.Assert.*;

/**
 * This class tests the correct behaviour of the methods in the RefreshTimer class.
 */
public class RefreshTimerTest {

    @Test
    public void computeTickerTimerWhenInitAppLaunch() {
        long result = RefreshTimer.computeRefreshTimerPeriod(100, 10, 0, 1, 1);
        assertEquals(result, 1);
    }

    @Test
    public void computeTickerTimerWhenOutOfQuota() {
        long result = RefreshTimer.computeRefreshTimerPeriod(0, 35, 0, 0, 1);
        assertEquals(result, 35);
    }

    @Test
    public void computeTickerTimerWhenMinimalQuota() {
        long result = RefreshTimer.computeRefreshTimerPeriod(1, 35, 15, 1, 1);
        assertEquals(result, 36);
    }

    @Test
    public void computeTickerTimerWhenQuotaBelowAPIQuotaBuffer() {
        long result = RefreshTimer.computeRefreshTimerPeriod(199, 35, 25, 2, 1);
        assertEquals(result, 37);

        result = RefreshTimer.computeRefreshTimerPeriod(1,
                35, 25, 2, 60);
        assertEquals(result, 37);
    }

    @Test
    public void computeTickerTimerWhenQuotaAtAPIQuotaBuffer() {
        long result = RefreshTimer.computeRefreshTimerPeriod(200, 35, 25, 1, 1);
        assertEquals(result, 36);
    }

    @Test
    public void computeTickerTimerWhenQuotaAboveAPIQuotaBuffer() {
        long result;
        /*
        Case #1: when remainingQuota - buffer is less than lastApiCallsUsed,
        Don't auto refresh until next apiQuota renewal
        */
        result = RefreshTimer.computeRefreshTimerPeriod(201, 35, 25, 1, 1);
        assertEquals(result, 36);

        result = RefreshTimer.computeRefreshTimerPeriod(224, 35, 25, 2, 1);
        assertEquals(result, 37);

        /*
        Case #2: when (remainingQuota - buffer) is more or equal than lastApiCallsUsed,
        Calculate the refreshTime as normal
        */
        result = RefreshTimer.computeRefreshTimerPeriod(225, 35, 25, 0, 1);
        assertEquals(result, 35);

        result = RefreshTimer.computeRefreshTimerPeriod(226, 35, 25, 1, 1);
        assertEquals(result, 36);

        result = RefreshTimer.computeRefreshTimerPeriod(3000, 35, 25, 1, 3);
        assertEquals(result, 1);

        result = RefreshTimer.computeRefreshTimerPeriod(3000, 35, 223, 1, 1);
        assertEquals(result, 3);
    }

    @Test
    public void computeTickerTimerWhenNoOfRefreshEqualOne() {
        long result = RefreshTimer.computeRefreshTimerPeriod(886, 9, 502, 1, 1);
        assertEquals(result, 10);
    }

    @Test
    public void computeTickerTimerWhenRemainingTimeEqualZero() {
        long result = RefreshTimer.computeRefreshTimerPeriod(0, 0, 502, 1, 2);
        assertEquals(result, 2);
    }
}
