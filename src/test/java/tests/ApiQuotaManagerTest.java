package tests;

import org.junit.Test;
import util.ApiQuotaManager;

import static org.junit.Assert.*;

/**
 * Tests the correct behaviour of the methods in the ApiQuotaManager class.
 */
public class ApiQuotaManagerTest {

    @Test
    public void computeRefreshTimerPeriodTest(){
        verify(100, //remainingApiQuota
                10, //minutesToNextQuotaTopup
                0, //apiCallsUsedInRecentRefresh
                50, //apiQuotaBuffer
                1, //minRefreshPeriod
                1); //Since no API call used in recent refresh, refresh in nearest possible time.

        verify(0, 35, 0, 0, 1, 1); //Already out of quota. Hence, refresh in nearest possible time to keep trying.
        verify(0, 35, 15, 100, 1, 35); //Out of quota. Hence refresh when API is top up.
        verify(1, 35, 15, 1, 1, 35); //RemainingApiQuota < apiCallsUsedInRecentRefresh. Hence refresh when API
                                     //quota is top up.
        verify(1, 35, 15, 0, 1, 35); //RemainingApiQuota < apiCallsUsedInRecentRefresh. Hence, refresh when API
                                     //quota is top up. To test special case when apiQuotaBuffer is zero
        verify(199, 35, 25, 200, 1, 35); //RemainingApiQuota is less than apiQuotaBuffer, refresh when API quota is
                                         //top up.
        verify(1, 35, 25, 2, 37, 37); //minRefreshPeriod > minutesToNextQuotaTopup. Hence minRefreshPeriod is used
        verify(1, 35, 25, 2, 2, 35); //RemainingApiQuota < apiQuotaBuffer. Hence refresh when API quota is top up.
        verify(200, 35, 25, 200, 1, 35); //remainingApiQuota == apiQuotaBuffer, need to reserve for manual operations.
                                         //Hence, refresh when API quota is top up.
        verify(201, 35, 25, 200, 1, 35); //RemainingApiQuota > apiQuotaBuffer
                                         //but RemainingApiQuota < apiCallsUsedInRecentRefresh.
                                         //Hence refresh when API quota is top up.
        verify(224, 35, 25, 200, 1, 35); //RemainingApiQuota > apiQuotaBuffer
                                         //but RemainingApiQuota < apiCallsUsedInRecentRefresh.
                                         //Hence refresh when API quota is top up.
        verify(225, 35, 25, 200, 1, 35); //RemainingApiQuota > apiQuotaBuffer
                                         //but RemainingApiQuota == apiCallsUsedInRecentRefresh.
                                         //Hence refresh when API quota is top up.
        verify(226, 35, 25, 200, 1, 35); //usableApiQuota is only sufficient for one refresh.
                                         //Hence refresh when API quota is top up.
        verify(3000, 35, 25, 200, 5, 5); //usableApiQuota is sufficient for multiple refresh before the next API top up.
                                         //Hence the expected result is 5.
        verify(3000, 35, 223, 0, 1, 3); //usableApiQuota is sufficient for multiple refresh before the next API top up.
                                        //apiCallsUsedInRecentRefresh is larger. Hence expected result is 3.
        verify(3000, 1, 223, 1, 1, 1); //minutesToNextQuotaTopup == 1. Hence the division with 1 will result in
                                       //expected result of 1.
        verify(886, 9, 502, 200, 1, 9); // 686(usableApiQuota)/502(apiCallsUsedInRecentRefresh) results in 1.
                                        // Hence 9/1 is 9.
        verify(0, 0, 502, 1, 2, 2); //minutesToNextQuotaTopup == 0. Hence refresh in the nearest possible time.
    }

    private void verify(int remainingApiQuota, int minutesToNextQuotaTopup, int apiCallsUsedInRecentRefresh,
                        int apiQuotaBuffer, int minRefreshPeriod, int expectedResult) {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(remainingApiQuota, minutesToNextQuotaTopup,
                                                                apiCallsUsedInRecentRefresh, apiQuotaBuffer,
                                                                minRefreshPeriod);
        assertEquals(result, expectedResult);
    }

}
