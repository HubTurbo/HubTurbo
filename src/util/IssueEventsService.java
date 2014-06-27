package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.HOST_API;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_EVENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class IssueEventsService {
	public static final int NO_UPDATE_RESPONSE_CODE = 304;
	
	private int pollRate = 60000; //poll rate in ms
	
	Date lastEventCheckTime;
	
	public IssueEventsService(){
		lastEventCheckTime = new Date();
	}
	
	public int getPollRate(){
		return pollRate;
	}
	
	public ArrayList<Integer> getUpdatedIssues(){
		ArrayList<Integer> updatedIssues = new ArrayList<Integer>();
		//TODO:
		return updatedIssues;
	}
	
	public boolean checkExistingIssuesUpdate(String user, String repository) {
		StringBuilder uri = new StringBuilder(HOST_API + SEGMENT_REPOS);
		uri.append('/').append(user).append('/').append(repository);
		uri.append(SEGMENT_ISSUES);
		uri.append(SEGMENT_EVENTS);
		
		HttpURLConnection connection;
		try {
			connection = setupGETRequest(new URI(uri.toString()));
			
			Map<String, List<String>> header = connection.getHeaderFields();
			
			pollRate = getPollRateLimit(header);
			return connection.getResponseCode() != NO_UPDATE_RESPONSE_CODE;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private int getPollRateLimit(Map<String, List<String>> header){
		List<String> limit = header.get("X-RateLimit-Limit");
		if(limit != null && limit.size()>0){
			return Integer.parseInt(limit.get(0));
		}
		return pollRate;
	}
	
	private HttpURLConnection setupGETRequest(URI uri) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
		connection.setRequestMethod("GET");
		return connection;
	}
}
