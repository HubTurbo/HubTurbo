package tests;

import backend.RepoId;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class RepoIdTest {

    private static final String REPO1 = "Owner1/Repo1";
    private static final String REPO2 = "Owner2/Repo2";

    private static RepoId repoId, repoIdAllCaps, repoIdMixed, repoIdDiff;

    @BeforeClass
    public static void initialize() {
        repoId = new RepoId(REPO1.toLowerCase());
        repoIdAllCaps = new RepoId(REPO1.toUpperCase());
        repoIdMixed = new RepoId(REPO1);
        repoIdDiff = new RepoId(REPO2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void repoId_invalidRepoIdString_throwIllegalArgumentException() {
        // Test null and empty repoIdString
        new RepoId(null);
        new RepoId("");

        // Test repoIdString of the wrong format
        new RepoId("invalidFormat");
        new RepoId("two//slashes");
    }

    @Test
    public void isValidRepoId_invalidRepoIdString_returnsFalse() {
        // Test null and empty repoIdString
        assertFalse(RepoId.isValidRepoId(""));
        assertFalse(RepoId.isValidRepoId(null));

        // Test repoIdString of the wrong format
        assertFalse(RepoId.isValidRepoId("invalidFormat"));
        assertFalse(RepoId.isValidRepoId("two/slashes/"));
    }

    @Test
    public void isValidRepoId_validRepoIdString_returnsTrue() {
        assertTrue(RepoId.isValidRepoId("hubturbo/hubturbo"));
    }

    @Test
    public void equals_differentRepoId_notEqual() {
        assertNotEquals(repoId, repoIdDiff);
        assertNotEquals(repoId, null);
        assertNotEquals(repoId, 1);
    }

    @Test
    public void equals_sameRepoIdButDifferentCapitalization_areEqual() {
        // Test equality for repo names that are of different cases
        // e.g. OWNER/REPO and owner/repo are the same RepoId
        assertEquals(repoId, repoId);
        assertEquals(repoId, repoIdAllCaps);
        assertEquals(repoId, repoIdMixed);
        assertEquals(repoIdAllCaps, repoIdMixed);
    }

    @Test
    public void hashCode_differentRepoId_haveDifferentHashCode() {
        assertNotEquals(repoId.hashCode(), repoIdDiff.hashCode());
    }

    @Test
    public void hashCode_sameRepoIdButDifferentCapitalization_haveSameHashCode() {
        // Test hashcode of objects initialised with different cases
        // e.g. OWNER/REPO and owner/repo have the same hash code
        assertEquals(repoId.hashCode(), repoId.hashCode());
        assertEquals(repoId.hashCode(), repoIdAllCaps.hashCode());
        assertEquals(repoId.hashCode(), repoIdMixed.hashCode());
        assertEquals(repoIdAllCaps.hashCode(), repoIdMixed.hashCode());
    }

    @Test
    public void getters_getAttributes_getAttributesCorrectly(){
        //repoOwner
        assertEquals("owner1", repoIdMixed.getRepoOwner());
        assertEquals(repoIdMixed.getRepoOwner(), repoIdMixed.getRepoOwner());

        //repoName
        assertEquals("repo1", repoIdMixed.getRepoName());
        assertEquals(repoIdMixed.getRepoName(), repoIdMixed.getRepoName());
    }

    @Test
    public void toString_getString_returnsFullRepoId() {
        assertEquals(REPO1.toLowerCase(), repoIdMixed.toString());
        assertEquals(repoIdMixed.toString(), repoIdMixed.toString());
    }
}
