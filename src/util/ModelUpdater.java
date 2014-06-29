package util;

import model.Model;

public class ModelUpdater {
	private Model model;
	private IssueUpdateService issueUpdateService;
	
	public ModelUpdater(GitHubClientExtended client, Model model){
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client);
	}
	
	
}
