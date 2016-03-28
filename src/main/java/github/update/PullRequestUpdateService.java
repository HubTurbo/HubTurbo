package github.update;

import com.google.gson.reflect.TypeToken;
import github.GitHubClientEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;

import java.io.IOException;
import java.util.*;

import static org.eclipse.egit.github.core.client.IGitHubConstants.*;

public class PullRequestUpdateService extends UpdateService<PullRequest> {
    private static final Logger logger = LogManager.getLogger(PullRequestUpdateService.class.getName());

    private final Date lastIssueCheckTime;

    public PullRequestUpdateService(GitHubClientEx client, Date lastIssueCheckTime) {
        super(client, SEGMENT_PULLS, "");
        this.lastIssueCheckTime = new Date(lastIssueCheckTime.getTime());
    }

    private Map<String, String> createUpdatedPullRequestsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("state", "all");
        params.put("sort", "updated");
        params.put("direction", "desc");
        return params;
    }

    /**
     * Overrides parent's method to change the number of items per page. This allow the update process
     * to stop earlier once it encounters the first item whose updatedAt time is before lastIssueCheckTime
     *
     * @param repoId the repository to make the request for
     * @return a list of pull requests
     */
    @Override
    protected PagedRequest<PullRequest> createUpdatedRequest(IRepositoryIdProvider repoId) {
        PagedRequest<PullRequest> request = new PagedRequest<>(1, 30);

        String path = SEGMENT_REPOS + "/" + repoId.generateId() + apiSuffix;
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);

        request.setParams(createUpdatedPullRequestsParams());
        request.setType(new TypeToken<PullRequest>() {
        }.getType());
        request.setArrayType(new TypeToken<ArrayList<PullRequest>>() {
        }.getType());
        return request;
    }

    /**
     * Overrides the parent's method to remove ETags checking step and use the specialized
     * method get
     *
     * @param repoId the repository to get the items from
     * @return
     */
    @Override
    public ArrayList<PullRequest> getUpdatedItems(IRepositoryIdProvider repoId) {
        if (updatedItems != null) {
            return updatedItems;
        }

        ArrayList<PullRequest> result = new ArrayList<>();
        String resourceDesc = repoId.generateId() + apiSuffix;
        logger.info(String.format("Updating %s", resourceDesc));

        try {
            PagedRequest<PullRequest> request = createUpdatedRequest(repoId);
            result = new ArrayList<>(getPagedItems(resourceDesc, new PageIterator<>(request, client)));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return result;
        }

        updatedItems = result;
        return result;
    }

    /**
     * Overrides parent's method to stop getting items if some items in a page has
     * updatedAt time before the lastIssueCheckTime
     *
     * @param resourceDesc
     * @param iterator     the paged request to iterate through
     * @return
     * @throws IOException
     */
    @Override
    protected List<PullRequest> getPagedItems(String resourceDesc, PageIterator<PullRequest> iterator)
            throws IOException {
        List<PullRequest> elements = new ArrayList<>();
        int page = 0;

        try {
            while (iterator.hasNext()) {
                Collection<PullRequest> newPullRequests = iterator.next();
                int numAddedItems = addItemsUpdatedSince(elements, newPullRequests, lastIssueCheckTime);

                logger.info(resourceDesc + " | page " + (page++) + ": " + numAddedItems + " items");

                if (numAddedItems < newPullRequests.size()) {
                    break;
                }
            }
        } catch (NoSuchPageException pageException) {
            throw pageException.getCause();
        }

        return elements;
    }

    /**
     * Add all pull requests in {@code src} to {@code dest} of which updated time is after {@code since}
     *
     * @param dest  current list of pull requests
     * @param src   new pull quests to be added
     * @param since
     * @return number of pull requests added to {@code dest}
     */
    private int addItemsUpdatedSince(List<PullRequest> dest, Collection<PullRequest> src, Date since) {
        int numPullRequestsAdded = 0;

        for (PullRequest pr : src) {
            if (pr.getUpdatedAt().after(since)) {
                dest.add(pr);
                numPullRequestsAdded++;
            }
        }

        return numPullRequestsAdded;
    }
}
