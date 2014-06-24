package command;

import java.io.IOException;

import model.TurboIssue;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

public class CreateIssueCommand implements UndoableCommand {
	
	private IssueService issueService;
	private IRepositoryIdProvider repoId;
	private TurboIssue newIssue;
	private Issue createdIssue;
	
	public CreateIssueCommand(GitHubClient ghClient, IRepositoryIdProvider repoId ,TurboIssue issue){
		this.issueService = new IssueService(ghClient);
		this.repoId = repoId;
		this.newIssue = issue;
	}

	@Override
	public void execute() {
		Issue ghIssue = newIssue.toGhIssue();
		try {
			createdIssue = issueService.createIssue(repoId, ghIssue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub
		
	}


}
