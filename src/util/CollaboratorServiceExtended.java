package util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.CollaboratorService;

import com.google.gson.reflect.TypeToken;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COLLABORATORS;

public class CollaboratorServiceExtended extends CollaboratorService{	
	public static final String COLLABORATOR_ETAG = "etag";
	public static final String COLLABORATOR_CONTENTS = "collaborator";
	public static final String COLLABORATOR_RESPONSE = "response";
	
	private GitHubClientExtended ghClient;
	
	public CollaboratorServiceExtended(GitHubClientExtended client){
		super(client);
		this.ghClient = client;
	}
	
	public HashMap<String, Object> getCollaboratorsData(IRepositoryIdProvider repository, String ETag) throws IOException{
		HashMap<String, Object> result = new HashMap<String, Object>();
		GitHubRequest request = createGetCollaboratorsRequest(repository);
		HttpURLConnection connection = createGetCollaboratorsConnection(request, ETag);
		
		String newETag = connection.getHeaderField("ETag");
		result.put(COLLABORATOR_ETAG, newETag);
		
		int responseCode = connection.getResponseCode();
		result.put(COLLABORATOR_RESPONSE, responseCode);
		
		if(responseCode != GitHubClientExtended.NO_UPDATE_RESPONSE_CODE && !ghClient.isError(responseCode)){
			List<User> collaborators = (List<User>)ghClient.getBody(request, ghClient.getStream(connection));
			result.put(COLLABORATOR_CONTENTS, collaborators);
		}
		
		return result;
	}
	
	private GitHubRequest createGetCollaboratorsRequest(IRepositoryIdProvider repository){
		String id = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COLLABORATORS);
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(new TypeToken<List<User>>() {
		}.getType());
		return request;
	}
	
	private HttpURLConnection createGetCollaboratorsConnection(GitHubRequest request, String ETag) throws IOException{
		HttpURLConnection connection = ghClient.createConnection(request);
		connection.setRequestProperty("If-None-Match", ETag);
		return connection;
	}
	
}
