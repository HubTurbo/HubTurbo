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
        // Test invalid repoID
        new RepoId("invalidTest");
    }

    @Test
    public void isWellFormed() {
        // Test not well formed
        assertFalse(RepoId.isWellFormedRepoIdString(""));

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
        // Test different objects
        assertNotEquals(repoId.hashCode(), repoIdDiff.hashCode());

        // Test hashcode of objects initialised with different cases
        assertEquals(repoId.hashCode(), repoId.hashCode());
        assertEquals(repoId.hashCode(), repoIdAllCaps.hashCode());
        assertEquals(repoId.hashCode(), repoIdMixed.hashCode());
        assertEquals(repoIdAllCaps.hashCode(), repoIdMixed.hashCode());
    }

    @Test
    public void getters(){
        //repoIDString
        assertEquals(REPO1.toLowerCase(), repoIdMixed.getRepoIDString());
        assertEquals(repoIdMixed.getRepoIDString(), repoIdMixed.getRepoIDString());

        //repoOwner
        assertEquals("owner1", repoIdMixed.getRepoOwner());
        assertEquals(repoIdMixed.getRepoOwner(), repoIdMixed.getRepoOwner());

        //repoName
        assertEquals("repo1", repoIdMixed.getRepoName());
        assertEquals(repoIdMixed.getRepoName(), repoIdMixed.getRepoName());
    }
}
