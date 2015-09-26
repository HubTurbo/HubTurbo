package github.update;

import com.google.gson.reflect.TypeToken;
import github.GitHubClientEx;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.PagedRequest;
import util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_PULLS;

public class PullRequestUpdateService extends UpdateService<PullRequest> {
    private final Date lastIssueCheckTime;

    public PullRequestUpdateService(GitHubClientEx client, String pullRequestsETags, Date lastIssueCheckTime) {
        super(client, SEGMENT_PULLS, pullRequestsETags);
        this.lastIssueCheckTime = new Date(lastIssueCheckTime.getTime());
    }

    private Map<String, String> createUpdatedPullRequestsParams(){
        Map<String, String> params = new HashMap<>();
        params.put("since", Utility.formatDateISO8601(lastIssueCheckTime));
        params.put("state", "all");
        return params;
    }

    @Override
    protected PagedRequest<PullRequest> createUpdatedRequest(IRepositoryIdProvider repoId){
        PagedRequest<PullRequest> request = super.createUpdatedRequest(repoId);
        request.setParams(createUpdatedPullRequestsParams());
        request.setType(new TypeToken<PullRequest>(){}.getType());
        request.setArrayType(new TypeToken<ArrayList<PullRequest>>(){}.getType());
        return request;
    }
}
