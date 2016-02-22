package tests;

import org.junit.Before;
import org.junit.Test;
import prefs.Preferences;
import ui.TestController;
import util.RepoConfig;

import static org.junit.Assert.*;

public class RepoConfigTest {

    /**
     * These mappings must be in the test map:
     * 1. "HubTurbo/hubturbo" -> "ht"
     * 2. "dummy2/dummy2" -> "d2"
     */
    private static final String REPO_ID_1 = "HubTurbo/hubturbo";
    private static final String REPO_ALIAS_1 = "ht"; 
    private static final String REPO_ID_2 = "dummy2/dummy2";
    private static final String REPO_ALIAS_2 = "d2";

    /**
     * These mappings must NOT be in the test map:
     * 1. "DoesNotExist" -> "DNE"
     */
    private static final String REPO_ID_DNE_1 = "DoesNotExist/DNE";
    private static final String REPO_ALIAS_DNE_1 = "dne";

    private RepoConfig testMap;

    @Before
    public void init() {
        Preferences testPrefs = TestController.createTestPreferences();
        testMap = testPrefs.getRepoConfig();
        // The test map should be empty at this point i.e size == 0
        assertEquals(0, testMap.getAliasCount());
        testMap.addAliasMapping(REPO_ID_1, REPO_ALIAS_1);
        testMap.addAliasMapping(REPO_ID_2, REPO_ALIAS_2);
        assertEquals(2, testMap.getAliasCount());
    }

    @Test
    public void testHasAlias() {
        assertTrue(testMap.hasRepoAlias(REPO_ID_1));
        assertTrue(testMap.hasRepoAlias(REPO_ID_2));

        assertFalse(testMap.hasRepoAlias(REPO_ID_DNE_1));
    }

    @Test
    public void testGetAlias() {
        assertEquals(REPO_ALIAS_1, testMap.getRepoAlias(REPO_ID_1));
        assertEquals(REPO_ALIAS_2, testMap.getRepoAlias(REPO_ID_2));

        assertEquals(null, testMap.getRepoAlias(REPO_ID_DNE_1));
    }

    @Test
    public void testIsAlias() {
        assertTrue(testMap.isRepoAlias(REPO_ALIAS_1));
        assertTrue(testMap.isRepoAlias(REPO_ALIAS_2));

        assertFalse(testMap.isRepoAlias(REPO_ALIAS_DNE_1));
    }

    @Test
    public void testGetRepoId() {
        assertEquals(REPO_ID_1, testMap.getRepoId(REPO_ALIAS_1));
        assertEquals(REPO_ID_2, testMap.getRepoId(REPO_ALIAS_2));

        assertEquals(null, testMap.getRepoId(REPO_ALIAS_DNE_1));
    }

    @Test
    public void testAddRemoveMappings() {
        final String REPO_ID_NEW = "hello/world";
        final String REPO_ALIAS_NEW = "hw";

        int sizeBefore = testMap.getAliasCount();
        testMap.addAliasMapping(REPO_ID_NEW, REPO_ALIAS_NEW);
        assertEquals(sizeBefore + 1, testMap.getAliasCount());
        assertEquals(REPO_ALIAS_NEW, testMap.getRepoAlias(REPO_ID_NEW));
        assertEquals(REPO_ID_NEW, testMap.getRepoId(REPO_ALIAS_NEW));

        testMap.removeAliasMapping(REPO_ID_NEW, REPO_ALIAS_NEW);
        assertEquals(sizeBefore, testMap.getAliasCount());
        assertEquals(null, testMap.getRepoAlias(REPO_ID_NEW));
        assertEquals(null, testMap.getRepoId(REPO_ALIAS_NEW));
    }

    @Test
    public void testResolveRepoId() {
        assertEquals(REPO_ID_1, testMap.resolveRepoId(REPO_ALIAS_1));
        assertEquals(REPO_ID_2, testMap.resolveRepoId(REPO_ALIAS_2));

        assertEquals(REPO_ID_DNE_1, testMap.resolveRepoId(REPO_ID_DNE_1));
        assertEquals(REPO_ALIAS_DNE_1, testMap.resolveRepoId(REPO_ALIAS_DNE_1));
    }
}
