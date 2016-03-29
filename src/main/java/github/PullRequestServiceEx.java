package github;

import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.PullRequestService;
import util.HTLog;

import java.io.IOException;
import java.util.List;

import static org.eclipse.egit.github.core.client.IGitHubConstants.*;

public class PullRequestServiceEx extends PullRequestService {
    private static final Logger logger = HTLog.get(PullRequestServiceEx.class);

    public PullRequestServiceEx(GitHubClient client) {
        super(client);
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
        if (pullRequestNumber == null) {
            throw new IllegalArgumentException("Pull request number cannot be null");
        }
        if (pullRequestNumber.length() == 0) {
            throw new IllegalArgumentException("Pull request number cannot be empty");
        }

        logger.info("Getting review comments for PR" + pullRequestNumber + " " + repoId);

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repoId)
                .append(SEGMENT_PULLS)
                .append('/').append(pullRequestNumber)
                .append(SEGMENT_COMMENTS);

        PagedRequest<ReviewComment> request = createPagedRequest();
        request.setUri(uri);
        request.setType(new TypeToken<List<ReviewComment>>() {
        }.getType());

        return getAll(request);
    }
}
