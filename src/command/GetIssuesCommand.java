package command;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.TurboIssue;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

public class GetIssuesCommand implements Command {
	
	private IssueService issueService;
	private IRepositoryIdProvider repoId;
	private List<TurboIssue> issues;
	
	public GetIssuesCommand(GitHubClient ghClient, IRepositoryIdProvider repoId, List<TurboIssue> issues) {
		this.issueService = new IssueService(ghClient);
		this.repoId = repoId;
		this.issues = issues;
	}

	@Override
	public void execute() {
		Map<String, String> filters = new HashMap<String, String>();
		filters.put(IssueService.FIELD_FILTER, "all");
		filters.put(IssueService.FILTER_STATE, "all");
		try {		
			List<Issue> ghIssues = issueService.getIssues(repoId, filters);
			for (Issue ghIssue : ghIssues) {
				issues.add(new TurboIssue(ghIssue));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
