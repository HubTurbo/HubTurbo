package service.updateservice;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubRequest;

import service.GitHubClientExtended;

public class UpdateService<T> {
	protected String apiSuffix;
	protected GitHubClientExtended client;
	protected String lastETag;
	protected Date lastCheckTime;
	
	public UpdateService(GitHubClientExtended client){
		this.client = client;
	}
	
	protected void updateLastETag(HttpURLConnection connection){
		lastETag = connection.getHeaderField("ETag");
	}
	
	protected String getFormattedDate(Date date){
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	    df.setTimeZone(tz);
	    String formatted = df.format(date);
	    return formatted;
	}
	
	private void updateLastCheckTime(HttpURLConnection connection) throws ParseException{
		String date = connection.getHeaderField("Date");
		lastCheckTime = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse(date);
	}
	
	protected HttpURLConnection createUpdatedConnection(GitHubRequest request) throws IOException{
		HttpURLConnection connection = client.createConnection(request);
		if(lastETag != null){
			connection.setRequestProperty("If-None-Match", lastETag);
		}
		return connection;
	}
	
	protected GitHubRequest createUpdatedRequest(IRepositoryIdProvider repoId){
		GitHubRequest request = new GitHubRequest();
		String path = SEGMENT_REPOS + "/" + repoId.generateId() + apiSuffix;
		request.setUri(path);
		request.setResponseContentType(CONTENT_TYPE_JSON);
		return request;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> getUpdatedItems(IRepositoryIdProvider repoId){
		try {
			
			GitHubRequest request = createUpdatedRequest(repoId);
			HttpURLConnection connection = createUpdatedConnection(request);
			int responseCode = connection.getResponseCode();
			System.out.println(responseCode);
			if(client.isError(responseCode)){
				return new ArrayList<T>();
			}
			updateLastETag(connection);
			updateLastCheckTime(connection);
			if(responseCode != GitHubClientExtended.NO_UPDATE_RESPONSE_CODE){
				return (ArrayList<T>)client.getBody(request, client.getStream(connection));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<T>();
	}
	
}
