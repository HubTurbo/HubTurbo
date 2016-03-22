package github.update;

import com.google.gson.reflect.TypeToken;
import github.GitHubClientEx;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.PagedRequest;
import util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;

public class IssueUpdateService extends UpdateService<Issue> {

    private final Date lastIssueCheckTime;

    public IssueUpdateService(GitHubClientEx client, String issuesETag, Date lastIssueCheckTime) {
        super(client, SEGMENT_ISSUES, issuesETag);
        this.lastIssueCheckTime = new Date(lastIssueCheckTime.getTime());
    }

    private Map<String, String> createUpdatedIssuesParams() {
        Map<String, String> params = new HashMap<>();
        params.put("since", Utility.formatDateISO8601(lastIssueCheckTime));
        params.put("state", "all");
        return params;
    }

    @Override
    protected PagedRequest<Issue> createUpdatedRequest(IRepositoryIdProvider repoId) {
        PagedRequest<Issue> request = super.createUpdatedRequest(repoId);
        request.setParams(createUpdatedIssuesParams());
        request.setType(new TypeToken<Issue>() {
        }.getType());
        request.setArrayType(new TypeToken<ArrayList<Issue>>() {
        }.getType());
        return request;
    }
}
