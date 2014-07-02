package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubRequest;

import com.google.gson.reflect.TypeToken;

public class UpdateService<T> {
	protected String apiSuffix;
	protected GitHubClientExtended client;
	protected String lastETag;
	
	public UpdateService(GitHubClientExtended client){
		this.client = client;
	}
	
	protected void updateLastETag(HttpURLConnection connection){
		lastETag = connection.getHeaderField("ETag");
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
	
	public ArrayList<T> getUpdatedItems(IRepositoryIdProvider repoId){
		try {
			GitHubRequest request = createUpdatedRequest(repoId);
			HttpURLConnection connection = createUpdatedConnection(request);
			updateLastETag(connection);
			int responseCode = connection.getResponseCode();
			System.out.println(responseCode);
			if(!client.isError(responseCode) && responseCode != GitHubClientExtended.NO_UPDATE_RESPONSE_CODE){
				return (ArrayList<T>)client.getBody(request, client.getStream(connection));
			}else{
				return new ArrayList<T>();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<T>();
	}
	
}
