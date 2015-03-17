package service.updateservice;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.PagedRequest;

import service.GitHubClientExtended;

import com.google.gson.reflect.TypeToken;

public class IssueUpdateService extends UpdateService<Issue>{	
	
	
	
	
	public IssueUpdateService(GitHubClientExtended client, String issuesETag, String lastIssueCheckTime){
		super(client, SEGMENT_ISSUES, issuesETag);
		lastCheckTime = new Date();
		super.setLastIssueCheckTime(lastIssueCheckTime);
	}

	protected String getFormattedDate(Date date){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(date);
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
