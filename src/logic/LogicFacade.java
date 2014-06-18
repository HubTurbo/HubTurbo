package logic;

import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;

public class LogicFacade {
	
	private GitHubClient client = new GitHubClient();
	private IRepositoryIdProvider repoId = null;
	private IssueManager issueManager = new IssueManager(client);
	private MilestoneManager milestoneManager = new MilestoneManager(client);
	
	public boolean login(String userId, String password) {
		client.setCredentials(userId, password);
		//TODO check login success
		return true; // stub
	}
	
	public void setRepository(String repository) {
		repoId = RepositoryId.create(client.getUser(), repository);
	}
	
	public List<TurboIssue> getIssues() {
		return issueManager.getAllIssues();
	}
	
	public List<TurboMilestone> getMilestones() {
		return milestoneManager.getAllMilestones(repoId);
	}
	
	public TurboIssue getIssue(int issueNumber) {
		return issueManager.getIssue(repoId, issueNumber);
	}
}
