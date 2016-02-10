package ui.components.issue_creators;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import util.HTLog;
import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;

/**
 * Presenter class that synchronizes TurboIssue with dialog view
 */
public class IssueCreatorPresenter {

    public static final int NEWISSUE_ID = -1;

    private static final Logger logger = HTLog.get(IssueCreator.class);
    private final Model repo;
    private final TurboIssue issue;
    
    private List<String> currentLabels;

    public IssueCreatorPresenter(Model repo, Optional<TurboIssue> issue) {
        this.repo = repo;
        this.issue = issue.isPresent() 
            ? new TurboIssue(issue.get()) : new TurboIssue(repo.getRepoId(), NEWISSUE_ID, "");
        currentLabels = this.issue.getLabels();
    }
    
    
    List<TurboUser> getUsers() {
        return repo.getUsers();
    }

    List<TurboIssue> getIssues() {
        return repo.getIssues();
    }

    List<TurboMilestone> getMilestones() {
        return repo.getMilestones();
    }

    List<TurboLabel> getAllLabels() {
        return repo.getLabels();
    }

    // Issue specific data

    List<TurboLabel> getCurrentLabels() {
        return getAllLabels().stream()
            .filter(label -> currentLabels.contains(label.getActualName()))
            .collect(Collectors.toList());
    }

    TurboIssue getResult() {
        return issue;
    }

    Optional<String> getAssignee() {
        return issue.getAssignee();
    }

    void setAssignee(String assignee) {
        issue.setAssignee(assignee);
    }

    Optional<Integer> getMilestone() {
        return issue.getMilestone();
    }

    void setMilestone(String milestone) {
        try {
            issue.setMilestone(Integer.parseInt(milestone));
        } catch (NumberFormatException e) {
            logger.info("Please enter a valid milestone");
        }
    }

    String getIssueTitle() {
        return issue.getTitle();
    }

    void setIssueTitle(String title) {
        issue.setTitle(title);
    }

    String getIssueBody() {
        return issue.getDescription();
    }

    void setIssueBody(String body) {
        issue.setDescription(body);
    }

    // ===============
    // Utility methods
    // ===============

    /**
     * Checks if this is a new issue
     */
    public boolean isNewIssue() {
        return issue.getId() == NEWISSUE_ID;
    }

    /**
     * Determines dialog title for issue or pull request
     * @return issue title to be shown
     */
    public String resolveIssueTitle() {
        return "Editing " + (issue.isPullRequest() ? "PR #" : "Issue #") + issue.getId();
    }
    
}
