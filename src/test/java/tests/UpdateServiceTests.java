package tests;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import github.GitHubClientExtended;
import github.update.MilestoneUpdateService;
import github.update.PageHeaderIterator;
import github.update.UpdateService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.junit.Test;

public class UpdateServiceTests {
    /**
     * Tests that head request to nonexistent repo throws an exception
     * @throws IOException
     */
    @Test(expected = FileNotFoundException.class)
    public void testHeadRequest() throws IOException {
        GitHubClientExtended client = new GitHubClientExtended();

        PagedRequest<Milestone> request = new PagedRequest<>();
        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        String path = SEGMENT_REPOS + "/nonexistentrepo";
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);

        client.head(request);
    }

    @Test(expected = NoSuchPageException.class)
    public void testHeaderIterator() {
        GitHubClientExtended client = new GitHubClientExtended();

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
        GitHubClientExtended client = new GitHubClientExtended();
        MilestoneUpdateService service = new MilestoneUpdateService(client, "abcd");

        assertTrue(service.getUpdatedItems(RepositoryId.create("name", "nonexistentrepo")).isEmpty());
    }
}
