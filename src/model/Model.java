package model;

public class Model {
	
	private IssueManager issueManager = new IssueManager();
	private MilestoneManager milestoneManager = new MilestoneManager();
	private LabelManager labelManager = new LabelManager();
	private CollaboratorManager collaboratorManager = new CollaboratorManager();
		
	public IssueManager getIssueManager() {
		return this.issueManager;
	}
	
	public LabelManager getLabelManager() {
		return this.labelManager;
	}
	
	public MilestoneManager getMilestoneManager() {
		return this.milestoneManager;
	}
	
	public CollaboratorManager getCollaboratorManager() {
		return this.collaboratorManager;
	}
	
}
