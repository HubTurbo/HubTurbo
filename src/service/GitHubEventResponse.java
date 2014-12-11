package service;

import java.io.InputStream;

import org.eclipse.egit.github.core.client.GitHubResponse;

/**
 * A wrapper class for GitHubEvents that also contain event-specific
 * information.
 */
public class GitHubEventResponse {
	
	private GitHubResponse response;
	
	public GitHubEventResponse(GitHubResponse response, InputStream jsonBody) {
		this.response = response;
		
		// TODO process body
		
	}
	
	public GitHubResponse getResponse() {
		return response;
	}

}
