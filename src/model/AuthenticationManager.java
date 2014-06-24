package model;

import java.io.IOException;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.OAuthService;

public class AuthenticationManager {
	private OAuthService service;
	private GitHubClient client;
	
	AuthenticationManager(GitHubClient client) {
		this.client = client;
		service = new OAuthService(client);
	}
	
	boolean login(String userId, String password) {
		client.setCredentials(userId, password);
		try {
			GitHubRequest request = new GitHubRequest();
			request.setUri("/");
			client.get(request);
		} catch (IOException e) {
			// Login failed
			return false;
		}
		return true;
	}
}
