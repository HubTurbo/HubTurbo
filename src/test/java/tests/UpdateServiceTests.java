package tests;

import github.GitHubClientEx;
import github.update.MilestoneUpdateService;
import github.update.PullRequestUpdateService;
import github.update.UpdateService;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateServiceTests {
    @Test
    public void testCombineEtags()
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Method layoutMethod = UpdateService.class.getDeclaredMethod("combineETags", List.class);
        layoutMethod.setAccessible(true);

        List<String> etags = new ArrayList<>();

        etags.add("123");
        assertEquals(Optional.of("123"), layoutMethod.invoke(null, etags));

        etags.add("abcd");
        etags.add("e12fasd5");
        assertEquals(Optional.of("123#abcd#e12fasd5"), layoutMethod.invoke(null, etags));

        assertEquals(Optional.of(""), layoutMethod.invoke(null, new ArrayList<>()));
    }

    @Test
    public void testGetUpdatedItems() {
        GitHubClientEx client = new GitHubClientEx();
        MilestoneUpdateService service = new MilestoneUpdateService(client, "abcd");

        assertTrue(service.getUpdatedItems(RepositoryId.create("name", "nonexistentrepo")).isEmpty());
    }

    /**
     * Tests if PullRequestUpdateService returns an empty list if the repo is invalid
     */
    @Test
    public void testGetUpdatedPullRequests() {
        GitHubClientEx client = new GitHubClientEx();
        PullRequestUpdateService service = new PullRequestUpdateService(client, new Date());
        assertTrue(service.getUpdatedItems(RepositoryId.create("name", "nonexistentrepo")).isEmpty());
    }
}
