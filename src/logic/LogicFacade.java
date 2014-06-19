package logic;

import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;

public class LogicFacade {
	
	private GitHubClient client = new GitHubClient();
	private IRepositoryIdProvider repoId = null;
	private AuthenticationManager authManager = new AuthenticationManager(client);
	private IssueManager issueManager = new IssueManager(client);
	private MilestoneManager milestoneManager = new MilestoneManager(client);
	private LabelManager labelManager = new LabelManager(client);
	
	public boolean login(String userId, String password) {
		return authManager.login(userId, password);
	}
	
	public void setRepository(String repository) {
		repoId = RepositoryId.create(client.getUser(), repository);
	}
	
	public List<TurboIssue> getIssues() {
		return issueManager.getAllIssues(repoId);
	}
	
	public List<TurboMilestone> getMilestones() {
		return milestoneManager.getAllMilestones(repoId);
	}
	
	public List<TurboLabel> getLabels() {
		return labelManager.getAllLabels(repoId);
	}
	
	public TurboIssue getIssue(int issueNumber) {
		return issueManager.getIssue(repoId, issueNumber);
	}
}
