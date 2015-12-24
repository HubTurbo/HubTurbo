package tests;

import github.GitHubClientEx;
import github.update.PageHeaderIterator;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;

public class PageHeaderIteratorTests {
    @Test(expected = NoSuchElementException.class)
    public void testHeaderIterator() throws NoSuchElementException {
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
}
