package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;

import java.io.IOException;
import java.net.HttpURLConnection;
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

public class IssueUpdateService {
	public static final int NO_UPDATE_RESPONSE_CODE = 304;
	
	private String lastETag;
	
	private GitHubClientExtended client;
	
	Date lastCheckTime;
	
	
	public IssueUpdateService(GitHubClientExtended client){
		updateLastCheckTime();
		this.client = client;
	}
	
	public ArrayList<Issue> getUpdatedIssues(IRepositoryIdProvider repoId){
		try {
			GitHubRequest request = createUpdatedIssuesRequest(repoId);
			HttpURLConnection connection = createUpdatedIssuesConnection(request);
			updateLastCheckTime();
			updateLastETag(connection);
			int responseCode = connection.getResponseCode();
			System.out.println(responseCode);
			if(!client.isError(responseCode) && responseCode != NO_UPDATE_RESPONSE_CODE){
				return (ArrayList<Issue>)client.getBody(request, client.getStream(connection));
			}else{
				return new ArrayList<Issue>();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Issue>();
	}
	
	
	private void updateLastETag(HttpURLConnection connection){
		lastETag = connection.getHeaderField("ETag");
	}
	
	private HttpURLConnection createUpdatedIssuesConnection(GitHubRequest request) throws IOException{
		HttpURLConnection connection = client.createConnection(request);
		if(lastETag != null){
			connection.setRequestProperty("If-None-Match", lastETag);
		}
		return connection;
	}
	
	
	private GitHubRequest createUpdatedIssuesRequest(IRepositoryIdProvider repoId){
		GitHubRequest request = new GitHubRequest();
		String path = SEGMENT_REPOS + "/" + repoId.generateId() + SEGMENT_ISSUES;
		request.setUri(path);
		request.setParams(createUpdatedIssuesParams());
		request.setResponseContentType(CONTENT_TYPE_JSON);
		request.setType(Issue.class);
		request.setArrayType(new TypeToken<ArrayList<Issue>>(){}.getType());
		return request;
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

}
