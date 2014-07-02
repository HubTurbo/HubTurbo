package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubRequest;

import com.google.gson.reflect.TypeToken;

public class IssueUpdateService extends UpdateService<Issue>{	
	
	protected Date lastCheckTime;
	
	
	public IssueUpdateService(GitHubClientExtended client){
		super(client);
		apiSuffix = SEGMENT_ISSUES;
		updateLastCheckTime();
	}
	
	private Map<String, String> createUpdatedIssuesParams(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("since", getFormattedDate(lastCheckTime));
		params.put("state", "all");
		return params;
	}
	
	private void updateLastCheckTime(){
		lastCheckTime = new Date();
	}
	
	private String getFormattedDate(Date date){
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	    df.setTimeZone(tz);
	    String formatted = df.format(date);
	    return formatted;
	}
	
	@Override
	public ArrayList<Issue> getUpdatedItems(IRepositoryIdProvider repoId){
		updateLastCheckTime();
		return super.getUpdatedItems(repoId);
	}
	
	@Override
	protected GitHubRequest createUpdatedRequest(IRepositoryIdProvider repoId){
		GitHubRequest request = super.createUpdatedRequest(repoId);
		request.setParams(createUpdatedIssuesParams());
		request.setType(new TypeToken<Issue>(){}.getType());
		request.setArrayType(new TypeToken<ArrayList<Issue>>(){}.getType());
		return request;
	}

}
