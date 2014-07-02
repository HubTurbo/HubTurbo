package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_LABELS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.LabelService;

import com.google.gson.reflect.TypeToken;

public class LabelServiceFixed extends LabelService {
	public static final String LABEL_ETAG = "etag";
	public static final String LABEL_CONTENTS = "label";
	public static final String LABEL_RESPONSE = "response";
	
	private GitHubClientExtended ghClient;
	
	public LabelServiceFixed() {
		super();
	}

	public LabelServiceFixed(GitHubClientExtended client) {
		super(client);
		this.ghClient = client;
	}

	/**
	 * 
	 * @param repository
	 * @param label: label with edited fields
	 * @param name: name of label to edit
	 * @return
	 * @throws IOException
	 */
	public Label editLabel(IRepositoryIdProvider repository, Label label , String name)
			throws IOException {
		String repoId = getId(repository);
		if (label == null)
			throw new IllegalArgumentException("Label cannot be null"); //$NON-NLS-1$
		if (name == null)
			throw new IllegalArgumentException("Label name cannot be null"); //$NON-NLS-1$
		if (name.length() == 0)
			throw new IllegalArgumentException("Label name cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_LABELS);
		uri.append('/').append(name);

		return client.post(uri.toString(), label, Label.class);
	}
	
	public HashMap<String, Object> getLabelsData(IRepositoryIdProvider repository, String ETag) throws IOException{
		HashMap<String, Object> result = new HashMap<String, Object>();
		GitHubRequest request = createGetLabelsRequest(repository);
		HttpURLConnection connection = createGetLabelsConnection(request, ETag);
		
		String newETag = connection.getHeaderField("ETag");
		result.put(LABEL_ETAG, newETag);
		
		int responseCode = connection.getResponseCode();
		result.put(LABEL_RESPONSE, responseCode);
		
		if(responseCode != GitHubClientExtended.NO_UPDATE_RESPONSE_CODE && !ghClient.isError(responseCode)){
			List<Label> labels = (List<Label>)ghClient.getBody(request, ghClient.getStream(connection));
			result.put(LABEL_CONTENTS, labels);
		}
		
		return result;
	}
	
	private GitHubRequest createGetLabelsRequest(IRepositoryIdProvider repository){
		String id = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_LABELS);
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(new TypeToken<List<Label>>() {
		}.getType());
		return request;
	}
	
	private HttpURLConnection createGetLabelsConnection(GitHubRequest request, String ETag) throws IOException{
		HttpURLConnection connection = ghClient.createConnection(request);
		connection.setRequestProperty("If-None-Match", ETag);
		return connection;
	}
	
}
