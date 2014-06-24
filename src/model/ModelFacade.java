package model;

import org.eclipse.egit.github.core.client.GitHubClient;

public class ModelFacade {
	
	private GitHubClient client = new GitHubClient();
	private IssueManager issueManager = new IssueManager();
	private AuthenticationManager authManager = new AuthenticationManager(client);
	private MilestoneManager milestoneManager = new MilestoneManager();
	private LabelManager labelManager = new LabelManager();
	private CollaboratorManager collaboratorManager = new CollaboratorManager();
	
	public boolean login(String userId, String password) {
		return authManager.login(userId, password);
	}
	
	public IssueManager getIssueManager() {
		return this.issueManager;
	}
	
	public LabelManager getLabelManager() {
		return this.labelManager;
	}
	
	public MilestoneManager getMilestoneManager() {
		return this.milestoneManager;
	}
	
	public CollaboratorManager getCollaborators() {
		return this.collaboratorManager;
	}
	
}
