package tests;

import github.GitHubClientEx;
import github.update.MilestoneUpdateService;
import github.update.PageHeaderIterator;
import github.update.UpdateService;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateServiceTests {
    /**
     * Tests that head request to nonexistent repo throws an exception
     * @throws IOException
     */
    @Test(expected = IOException.class)
    public void testHeadRequest() throws IOException {
        GitHubClientEx client = new GitHubClientEx();

        PagedRequest<Milestone> request = new PagedRequest<>();
        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        String path = SEGMENT_REPOS + "/nonexistentrepo";
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);

        client.head(request);
    }

    @Test(expected = Exception.class)
    public void testHeaderIterator() {
        GitHubClientEx client = new GitHubClientEx();

        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        PagedRequest<Milestone> request = new PagedRequest<>();
        String path = SEGMENT_REPOS + "/nonexistentrepo";
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);

        PageHeaderIterator iter = new PageHeaderIterator(request, client, "ETag");
        if (iter.hasNext()) {
            iter.next();
        }
    }

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
}
