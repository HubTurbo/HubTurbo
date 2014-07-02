package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_MILESTONES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.MilestoneService;

import com.google.gson.reflect.TypeToken;

public class MilestoneServiceExtended extends MilestoneService{
	public static final String MILESTONE_ETAG = "etag";
	public static final String MILESTONE_CONTENTS = "milestone";
	public static final String MILESTONE_RESPONSE = "response";
	
	private GitHubClientExtended ghClient;
	
	public MilestoneServiceExtended(GitHubClientExtended client){
		super(client);
		this.ghClient = client;
	}
	
	public HashMap<String, Object> getLabelsData(IRepositoryIdProvider repository, String ETag) throws IOException{
		HashMap<String, Object> result = new HashMap<String, Object>();
		GitHubRequest request = createGetMilestonesRequest(repository);
		HttpURLConnection connection = createGetMilestonesConnection(request, ETag);
		
		String newETag = connection.getHeaderField("ETag");
		result.put(MILESTONE_ETAG, newETag);
		
		int responseCode = connection.getResponseCode();
		result.put(MILESTONE_RESPONSE, responseCode);
		
		if(responseCode != GitHubClientExtended.NO_UPDATE_RESPONSE_CODE && !ghClient.isError(responseCode)){
			List<Milestone> labels = (List<Milestone>)ghClient.getBody(request, ghClient.getStream(connection));
			result.put(MILESTONE_CONTENTS, labels);
		}
		
		return result;
	}
	
	private GitHubRequest createGetMilestonesRequest(IRepositoryIdProvider repository){
		String id = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_MILESTONES);
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(new TypeToken<List<Milestone>>() {
		}.getType());
		return request;
	}
	
	private HttpURLConnection createGetMilestonesConnection(GitHubRequest request, String ETag) throws IOException{
		HttpURLConnection connection = ghClient.createConnection(request);
		connection.setRequestProperty("If-None-Match", ETag);
		return connection;
	}
}
