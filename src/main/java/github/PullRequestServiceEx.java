package github;

import com.google.gson.reflect.TypeToken;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.io.IOException;
import java.util.List;

import static org.eclipse.egit.github.core.client.IGitHubConstants.*;

public class PullRequestServiceEx extends PullRequestService {
    /**
     * Gets a pull request's review comments
     *
     * @param repository
     * @param pullRequestNumber
     * @return list of review comments
     * @throws IOException
     */
    public List<ReviewComment> getReviewComments(IRepositoryIdProvider repository,
                                                 int pullRequestNumber) throws IOException {
        return getReviewComments(repository, Integer.toString(pullRequestNumber));
    }

    /**
     * Gets a pull request's review comments
     *
     * @param repository
     * @param pullRequestNumber
     * @return list of review comments
     * @throws IOException
     */
    public List<ReviewComment> getReviewComments(IRepositoryIdProvider repository,
                                                 String pullRequestNumber) throws IOException {
        String repoId = getId(repository);
        return getReviewComments(repoId, pullRequestNumber);
    }

    /**
     * Gets a pull request's review comments
     *
     * @param repoId
     * @param pullRequestNumber
     * @return list of review comments
     * @throws IOException
     */
    private List<ReviewComment> getReviewComments(String repoId, String pullRequestNumber)
            throws IOException {
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repoId);
        uri.append(SEGMENT_PULLS);
        uri.append('/').append(pullRequestNumber);
        uri.append(SEGMENT_COMMENTS);

        PagedRequest<ReviewComment> request = createPagedRequest();
        request.setUri(uri);
        request.setType(new TypeToken<List<ReviewComment>>() {
        }.getType());

        System.out.println(request.getUri());

        return getAll(request);
    }
}
