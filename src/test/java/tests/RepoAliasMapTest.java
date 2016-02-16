package tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import prefs.Preferences;
import ui.TestController;
import util.RepoAliasMap;

import static org.junit.Assert.*;

public class RepoAliasMapTest {

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

    private RepoAliasMap testMap;

    @Before
    public void init() {
        Preferences testPrefs = TestController.createTestPreferences();
        testMap = testPrefs.getRepoAliasMap();
        // The test map should be empty at this point i.e size == 0
        assertEquals(0, testMap.size());
        testMap.addMapping(REPO_ID_1, REPO_ALIAS_1);
        testMap.addMapping(REPO_ID_2, REPO_ALIAS_2);
        assertEquals(2, testMap.size());
    }

    @Test
    public void testHasAlias() {
        assertTrue(testMap.hasAlias(REPO_ID_1));
        assertTrue(testMap.hasAlias(REPO_ID_2));

        assertFalse(testMap.hasAlias(REPO_ID_DNE_1));
    }

    @Test
    public void testGetAlias() {
        assertEquals(REPO_ALIAS_1, testMap.getAlias(REPO_ID_1));
        assertEquals(REPO_ALIAS_2, testMap.getAlias(REPO_ID_2));

        assertEquals(null, testMap.getAlias(REPO_ID_DNE_1));
    }

    @Test
    public void testIsAlias() {
        assertTrue(testMap.isAlias(REPO_ALIAS_1));
        assertTrue(testMap.isAlias(REPO_ALIAS_2));

        assertFalse(testMap.isAlias(REPO_ALIAS_DNE_1));
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

        int sizeBefore = testMap.size();
        testMap.addMapping(REPO_ID_NEW, REPO_ALIAS_NEW);
        assertEquals(sizeBefore + 1, testMap.size());
        assertEquals(REPO_ALIAS_NEW, testMap.getAlias(REPO_ID_NEW));
        assertEquals(REPO_ID_NEW, testMap.getRepoId(REPO_ALIAS_NEW));

        testMap.removeMapping(REPO_ID_NEW, REPO_ALIAS_NEW);
        assertEquals(sizeBefore, testMap.size());
        assertEquals(null, testMap.getAlias(REPO_ID_NEW));
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
