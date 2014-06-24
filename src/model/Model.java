package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

public class Model {
	
	public static final String MILESTONES_ALL = "all";
	public static final String MILESTONES_OPEN = "open";
	public static final String MILESTONES_CLOSED = "closed";
	
	private ObservableList<TurboCollaborator> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();

	private CollaboratorService collabService;
	private IssueService issueService;
	private LabelService labelService;
	private MilestoneService milestoneService;

	public Model(GitHubClient ghClient) {
		this.collabService = new CollaboratorService(ghClient);
		this.issueService = new IssueService(ghClient);
		this.labelService = new LabelService(ghClient);
		this.milestoneService = new MilestoneService(ghClient);
	}
	
	public ObservableList<TurboIssue> getIssues() {
		return issues;
	}

	public ObservableList<TurboCollaborator> getCollaborators() {
		return collaborators;
	}

	public ObservableList<TurboLabel> getLabels() {
		return labels;
	}

	public ObservableList<TurboMilestone> getMilestones() {
		return milestones;
	}
	
	public boolean loadCollaborators(IRepositoryIdProvider repoId) {
		try {
			List<User> ghCollaborators = collabService.getCollaborators(repoId);
			for(User ghCollaborator : ghCollaborators) {
				collaborators.add(new TurboCollaborator(ghCollaborator));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean loadIssues(IRepositoryIdProvider repoId) {
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
			return false;
		}
		return true;
	}
	
	public boolean loadLabels(IRepositoryIdProvider repoId){
		try {
			List<Label> ghLabels = labelService.getLabels(repoId);
			for (Label ghLabel : ghLabels) {
				labels.add(new TurboLabel(ghLabel));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean loadMilestones(IRepositoryIdProvider repoId){
		try {		
			List<Milestone> ghMilestones = milestoneService.getMilestones(repoId, Model.MILESTONES_ALL);
			for (Milestone ghMilestone : ghMilestones) {
				milestones.add(new TurboMilestone(ghMilestone));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
