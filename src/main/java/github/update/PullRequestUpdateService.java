package github.update;

import com.google.gson.reflect.TypeToken;
import github.GitHubClientEx;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.PagedRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_PULLS;

public class PullRequestUpdateService extends UpdateService<PullRequest> {
    public PullRequestUpdateService(GitHubClientEx client, String pullRequestsETags) {
        super(client, SEGMENT_PULLS, pullRequestsETags);
    }

    private Map<String, String> createUpdatedPullRequestsParams(){
        Map<String, String> params = new HashMap<>();
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
