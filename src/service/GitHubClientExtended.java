package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Type;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;

public class GitHubClientExtended extends GitHubClient{
	public static final int NO_UPDATE_RESPONSE_CODE = 304;
	protected static final int CONNECTION_TIMEOUT = 30000;
	
	public GitHubClientExtended(){
		
	}
	
	public HttpURLConnection createConnection(GitHubRequest request) throws IOException{
		HttpURLConnection connection = createGet(request.generateUri());
		return connection;
	}
	
	public HttpURLConnection createGitHubConnection(String path, String method)
			throws IOException {
		URL url = new URL("https://github.com/" + path);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		return configureRequest(connection);
	}
	
	public HttpURLConnection configureURLRequest(final HttpURLConnection request) {
		HttpURLConnection req = super.configureRequest(request);
		request.setRequestProperty(HEADER_ACCEPT,"text/html, application/xhtml+xml, application/xml");
		return req;
	}
	
	public HttpURLConnection createURLRequestConnection(String url) throws IOException{
		URL link = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) link.openConnection();
		connection.setRequestMethod(METHOD_GET);
		connection = configureURLRequest(connection);
		return connection;
	}
	
	public String getHTMLResponseFromURLRequest(String url) throws IOException{
		HttpURLConnection connection = createURLRequestConnection(url);
		InputStream responseStream = getResponseStream(connection);
		BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
		StringBuilder content = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null){
			content.append(line);
		}
		return content.toString();
	}
	
	public IOException createException(InputStream response, int code,
			String status){
		return super.createException(response, code, status);
	}
	public HttpURLConnection createPost(String uri) throws IOException {
		return super.createPost(uri);
	}

	
	public InputStream getStream(HttpURLConnection request)
			throws IOException {
		return super.getStream(request);
	}
	
	public Object getBody(GitHubRequest request, InputStream stream)
			throws IOException {
		return super.getBody(request, stream);
	}
	
	public boolean isError(final int code) {
		return super.isError(code);
	}
	
	public boolean isOk(final int code) {
		return super.isOk(code);
	}

	
	public void sendParams(HttpURLConnection request, Object params)
			throws IOException {
		super.sendParams(request, params);
	}
	
	public <V> V sendJson(final HttpURLConnection request,
			final Object params, final Type type) throws IOException {
		sendParams(request, params);
		final int code = request.getResponseCode();
		updateRateLimits(request);
		if (isOk(code))
			if (type != null)
				return parseJson(getStream(request), type);
			else
				return null;
		if (isEmpty(code))
			return null;
		throw createException(getStream(request), code,
				request.getResponseMessage());
	}
	
	@Override
	protected HttpURLConnection createConnection(String uri) throws IOException {
		HttpURLConnection connection = super.createConnection(uri);
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setReadTimeout(CONNECTION_TIMEOUT);
		return connection;
	}
}
