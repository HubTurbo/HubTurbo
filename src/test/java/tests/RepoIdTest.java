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
    public void createInstance() {
        // Test null and empty repoIdString
        new RepoId(null);
        new RepoId("");

        // Test repoIdString of the wrong format
        new RepoId("invalidFormat");
        new RepoId("two//slashes");
    }

    @Test
    public void isWellFormed() {
        // Test null and empty repoIdString
        assertFalse(RepoId.isWellFormedRepoIdString(""));
        assertFalse(RepoId.isWellFormedRepoIdString(null));

        // Test repoIdString of the wrong format
        assertFalse(RepoId.isWellFormedRepoIdString("invalidFormat"));
        assertFalse(RepoId.isWellFormedRepoIdString("two/slashes/"));

        // Test well formed
        assertTrue(RepoId.isWellFormedRepoIdString("hubturbo/hubturbo"));
    }

    @Test
    public void equality() {
        // Test different objects
        assertNotEquals(repoId, repoIdDiff);
        assertNotEquals(repoId, null);
        assertNotEquals(repoId, 1);

        // Test equality for repo names that are of different cases
        assertEquals(repoId, repoId);
        assertEquals(repoId, repoIdAllCaps);
        assertEquals(repoId, repoIdMixed);
        assertEquals(repoIdAllCaps, repoIdMixed);
    }

    @Test
    public void hashCodeTest() {
        // Test different objects have different hash code
        assertNotEquals(repoId.hashCode(), repoIdDiff.hashCode());

        // Test hashcode of objects initialised with different cases
        // e.g. OWNER/REPO and owner/repo have the same hash code
        assertEquals(repoId.hashCode(), repoId.hashCode());
        assertEquals(repoId.hashCode(), repoIdAllCaps.hashCode());
        assertEquals(repoId.hashCode(), repoIdMixed.hashCode());
        assertEquals(repoIdAllCaps.hashCode(), repoIdMixed.hashCode());
    }

    @Test
    public void getters(){
        //repoIDString
        assertEquals(REPO1.toLowerCase(), repoIdMixed.getRepoIdString());
        assertEquals(repoIdMixed.getRepoIdString(), repoIdMixed.getRepoIdString());

        //repoOwner
        assertEquals("owner1", repoIdMixed.getRepoOwner());
        assertEquals(repoIdMixed.getRepoOwner(), repoIdMixed.getRepoOwner());

        //repoName
        assertEquals("repo1", repoIdMixed.getRepoName());
        assertEquals(repoIdMixed.getRepoName(), repoIdMixed.getRepoName());
    }
}
