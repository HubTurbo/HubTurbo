package tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.RepoAliasMap;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class RepoAliasMapTest {

    /**
     * These mappings must be in the test file:
     * 1. "HubTurbo/hubturbo" -> "ht"
     * 2. "dummy2/dummy2" -> "d2"
     */
    private static final String REPO_ID_1 = "HubTurbo/hubturbo";
    private static final String REPO_ALIAS_1 = "ht";
    private static final String REPO_ID_2 = "dummy2/dummy2";
    private static final String REPO_ALIAS_2 = "d2";

    /**
     * These mappings must NOT be in the test file:
     * 1. "DoesNotExist" -> "DNE"
     */
    private static final String REPO_ID_DNE_1 = "DoesNotExist/DNE";
    private static final String REPO_ALIAS_DNE_1 = "dne";

    /**
     * The test file to create for the test
     */
    public static final String TEST_FILE_DIRECTORY = "settings";
    public static final String TEST_FILE_NAME = "test_repo_alias_mapping.json";

    @Before
    public void init() {
        try {
            File testFile = new File(TEST_FILE_DIRECTORY, TEST_FILE_NAME);
            if (testFile.exists()) {
                assertTrue(testFile.delete());
            }

            assertTrue(testFile.createNewFile());
            try (FileOutputStream os = new FileOutputStream(testFile)) {
                os.write(String.format("[[\"%s\", \"%s\"], [\"%s\", \"%s\"]]",
                        REPO_ID_1,
                        REPO_ALIAS_1,
                        REPO_ID_2,
                        REPO_ALIAS_2).getBytes("UTF-8"));
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @After
    public void tearDown() {
        File testFile = new File(TEST_FILE_DIRECTORY, TEST_FILE_NAME);
        if (testFile.exists()) {
            assertTrue(testFile.delete());
        }
    }

    @Test
    public void testHasAlias() {
        RepoAliasMap testMap = RepoAliasMap.getTestInstance();

        assertTrue(testMap.hasAlias(REPO_ID_1));
        assertTrue(testMap.hasAlias(REPO_ID_2));

        assertFalse(testMap.hasAlias(REPO_ID_DNE_1));
    }

    @Test
    public void testGetAlias() {
        RepoAliasMap testMap = RepoAliasMap.getTestInstance();

        assertEquals(REPO_ALIAS_1, testMap.getAlias(REPO_ID_1));
        assertEquals(REPO_ALIAS_2, testMap.getAlias(REPO_ID_2));

        assertEquals(null, testMap.getAlias(REPO_ID_DNE_1));
    }

    @Test
    public void testIsAlias() {
        RepoAliasMap testMap = RepoAliasMap.getTestInstance();

        assertTrue(testMap.isAlias(REPO_ALIAS_1));
        assertTrue(testMap.isAlias(REPO_ALIAS_2));

        assertFalse(testMap.isAlias(REPO_ALIAS_DNE_1));
    }

    @Test
    public void testGetRepoId() {
        RepoAliasMap testMap = RepoAliasMap.getTestInstance();

        assertEquals(REPO_ID_1, testMap.getRepoId(REPO_ALIAS_1));
        assertEquals(REPO_ID_2, testMap.getRepoId(REPO_ALIAS_2));

        assertEquals(null, testMap.getRepoId(REPO_ALIAS_DNE_1));
    }

    @Test
    public void testToMappingArray() {
        RepoAliasMap testMap = RepoAliasMap.getTestInstance();

        String[][] expectedMappingArray = new String[2][2];
        expectedMappingArray[0] = new String[] {REPO_ID_1, REPO_ALIAS_1};
        expectedMappingArray[1] = new String[] {REPO_ID_2, REPO_ALIAS_2};

        String[][] producedMappingArray = testMap.toMappingsArray();

        assertEquals(expectedMappingArray.length, producedMappingArray.length);

        int count = 0;
        int expectedCount = 2;
        for (String[] producedMapping : producedMappingArray) {
            String producedMapString = Arrays.toString(producedMapping);
            for (String[] expectedMapping : expectedMappingArray) {
                String expectedMapString = Arrays.toString(expectedMapping);
                if (producedMapString.equals(expectedMapString)) {
                    count++;
                    //break;
                }
            }
        }
        assertEquals(expectedCount, count);
    }

    @Test
    public void testAddRemoveMappings() {
        final String REPO_ID_NEW = "hello/world";
        final String REPO_ALIAS_NEW = "hw";

        RepoAliasMap testMap = RepoAliasMap.getTestInstance();
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
        RepoAliasMap testMap = RepoAliasMap.getTestInstance();

        assertEquals(REPO_ID_1, testMap.resolveRepoId(REPO_ALIAS_1));
        assertEquals(REPO_ID_2, testMap.resolveRepoId(REPO_ALIAS_2));

        assertEquals(REPO_ID_DNE_1, testMap.resolveRepoId(REPO_ID_DNE_1));
        assertEquals(REPO_ALIAS_DNE_1, testMap.resolveRepoId(REPO_ALIAS_DNE_1));
    }
}
