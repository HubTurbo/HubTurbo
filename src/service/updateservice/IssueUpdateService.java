package service.updateservice;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.PagedRequest;

import service.GitHubClientExtended;

import com.google.gson.reflect.TypeToken;

public class IssueUpdateService extends UpdateService<Issue>{	
	
	
	
	
	public IssueUpdateService(GitHubClientExtended client, String issuesETag){
		super(client);
		apiSuffix = SEGMENT_ISSUES;
		super.setLastETag(issuesETag);
		lastCheckTime = new Date();
	}
	
	private Map<String, String> createUpdatedIssuesParams(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("since", getFormattedDate(lastCheckTime));
		params.put("state", "all");
		return params;
	}
	
	@Override
	public ArrayList<Issue> getUpdatedItems(IRepositoryIdProvider repoId){
		ArrayList<Issue> updatedItems = super.getUpdatedItems(repoId);
		return updatedItems;
	}
	
	@Override
	protected PagedRequest<Issue> createUpdatedRequest(IRepositoryIdProvider repoId){
		PagedRequest<Issue> request = super.createUpdatedRequest(repoId);
		request.setParams(createUpdatedIssuesParams());
		request.setType(new TypeToken<Issue>(){}.getType());
		request.setArrayType(new TypeToken<ArrayList<Issue>>(){}.getType());
		return request;
	}

}
