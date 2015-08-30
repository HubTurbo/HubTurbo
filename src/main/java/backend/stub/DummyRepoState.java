package backend.stub;

import backend.IssueMetadata;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.IssueEventType;
import github.TurboIssueEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DummyRepoState {

    private String dummyRepoId;

    private TreeMap<Integer, TurboIssue> issues = new TreeMap<>();
    private TreeMap<String, TurboLabel> labels = new TreeMap<>();
    private TreeMap<Integer, TurboMilestone> milestones = new TreeMap<>();
    private TreeMap<String, TurboUser> users = new TreeMap<>();

    private TreeMap<Integer, TurboIssue> updatedIssues = new TreeMap<>();
    private TreeMap<String, TurboLabel> updatedLabels = new TreeMap<>();
    private TreeMap<Integer, TurboMilestone> updatedMilestones = new TreeMap<>();
    private TreeMap<String, TurboUser> updatedUsers = new TreeMap<>();

    public DummyRepoState(String repoId) {
        this.dummyRepoId = repoId;

        initializeRepoEntities();
        connectRepoEntities();
        insertInitialMetadata();
    }

    private void initializeRepoEntities() {
        for (int i = 0; i < 10; i++) {
            // Issue #7 is a PR
            TurboIssue dummyIssue = (i != 6) ? makeDummyIssue() : makeDummyPR();
            // All default issues are treated as if created a long time ago
            dummyIssue.setUpdatedAt(LocalDateTime.of(2000 + i, 1, 1, 0, 0));
            TurboLabel dummyLabel = makeDummyLabel();
            TurboMilestone dummyMilestone = makeDummyMilestone();
            TurboUser dummyUser = makeDummyUser();

            // Populate state with defaults
            issues.put(dummyIssue.getId(), dummyIssue);
            labels.put(dummyLabel.getActualName(), dummyLabel);
            milestones.put(dummyMilestone.getId(), dummyMilestone);
            users.put(dummyUser.getLoginName(), dummyUser);
        }

        // Issue 6 is closed
        issues.get(6).setOpen(false);

        // Label groups for testing label picker exclusivity
        labels.put("p.low", new TurboLabel(dummyRepoId, "p.low"));
        labels.put("p.medium", new TurboLabel(dummyRepoId, "p.medium"));
        labels.put("p.high", new TurboLabel(dummyRepoId, "p.high"));
        labels.put("type.story", new TurboLabel(dummyRepoId, "type.story"));
        labels.put("type.research", new TurboLabel(dummyRepoId, "type.research"));
    }

    private void connectRepoEntities() {
        // Issues #1-5 are assigned milestones 1-5 respectively
        for (int i = 1; i <= 5; i++) {
            issues.get(i).setMilestone(milestones.get(i));
        }

        // Odd issues are assigned label 1, even issues are assigned label 2
        for (int i = 1; i <= 10; i++) {
            issues.get(i).addLabel((i % 2 == 0) ? "Label 1" : "Label 2");
        }

        // We assign a colorful label to issue 10
        labels.put("Label 11", new TurboLabel(dummyRepoId, "ffa500", "Label 11"));
        issues.get(10).addLabel("Label 11");

        // Each contributor is assigned to his corresponding issue
        for (int i = 1; i <= 10; i++) {
            issues.get(i).setAssignee("User " + i);
        }
    }

    private void insertInitialMetadata() {
        // Put down a comment by current HT user (empty string) for issue 9
        Comment ownComment = new Comment();
        ownComment.setCreatedAt(new Date());
        ownComment.setUser(new User().setLogin("test"));
        Comment[] ownComments = { ownComment };
        issues.get(9).setMetadata(new IssueMetadata(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(ownComments)),
                ""
        ));
        issues.get(9).setCommentCount(1);
        issues.get(9).setUpdatedAt(LocalDateTime.now());

        // Then put down three comments for issue 10
        Comment dummyComment1 = new Comment();
        Comment dummyComment2 = new Comment();
        Comment dummyComment3 = new Comment();
        dummyComment1.setCreatedAt(new Date()); // Recently posted
        dummyComment2.setCreatedAt(new Date());
        dummyComment3.setCreatedAt(new Date(0)); // Posted very long ago
        dummyComment1.setUser(new User().setLogin("User 1"));
        dummyComment2.setUser(new User().setLogin("User 2"));
        dummyComment3.setUser(new User().setLogin("User 3"));
        Comment[] dummyComments = { dummyComment1, dummyComment2, dummyComment3 };
        issues.get(10).setMetadata(new IssueMetadata(
                new ArrayList<>(),
                new ArrayList<Comment>(Arrays.asList(dummyComments)),
                ""
        ));
        issues.get(10).setCommentCount(3);
        issues.get(10).setUpdatedAt(LocalDateTime.now());
    }

    protected ImmutableTriple<List<TurboIssue>, String, Date>
        getUpdatedIssues(String eTag, Date lastCheckTime) {

        String currETag = eTag;
        if (!updatedIssues.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutableTriple<List<TurboIssue>, String, Date> toReturn = new ImmutableTriple<>(
            new ArrayList<>(updatedIssues.values()), currETag, lastCheckTime);

        updatedIssues = new TreeMap<>();
        return toReturn;
    }

    protected ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String eTag) {
        String currETag = eTag;
        if (!updatedLabels.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutablePair<List<TurboLabel>, String> toReturn
            = new ImmutablePair<>(new ArrayList<>(updatedLabels.values()), currETag);

        updatedLabels = new TreeMap<>();
        return toReturn;
    }

    protected ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String eTag) {
        String currETag = eTag;
        if (!updatedMilestones.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutablePair<List<TurboMilestone>, String> toReturn
            = new ImmutablePair<>(new ArrayList<>(updatedMilestones.values()), currETag);

        updatedMilestones = new TreeMap<>();
        return toReturn;
    }

    protected ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String eTag) {
        String currETag = eTag;
        if (!updatedUsers.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutablePair<List<TurboUser>, String> toReturn
            = new ImmutablePair<>(new ArrayList<>(updatedUsers.values()), currETag);

        updatedUsers = new TreeMap<>();
        return toReturn;
    }

    protected List<TurboIssue> getIssues() {
        return new ArrayList<>(issues.values());
    }

    protected List<TurboLabel> getLabels() {
        return new ArrayList<>(labels.values());
    }

    protected List<TurboMilestone> getMilestones() {
        return new ArrayList<>(milestones.values());
    }

    protected List<TurboUser> getCollaborators() {
        return new ArrayList<>(users.values());
    }

    private TurboIssue makeDummyIssue() {
        return new TurboIssue(dummyRepoId,
                issues.size() + 1,
                "Issue " + (issues.size() + 1),
                "User " + (issues.size() + 1),
                LocalDateTime.of(1999 + issues.size(), 1, 1, 0, 0),
                false);
    }

    private TurboIssue makeDummyPR() {
        return new TurboIssue(dummyRepoId,
                issues.size() + 1,
                "PR " + (issues.size() + 1),
                "User " + (issues.size() + 1),
                LocalDateTime.of(1999 + issues.size(), 1, 1, 0, 0),
                true);
    }

    private TurboLabel makeDummyLabel() {
        return new TurboLabel(dummyRepoId, "Label " + (labels.size() + 1));
    }

    private TurboMilestone makeDummyMilestone() {
        return new TurboMilestone(dummyRepoId, milestones.size() + 1, "Milestone " + (milestones.size() + 1));
    }

    private TurboUser makeDummyUser() {
        return new TurboUser(dummyRepoId, "User " + (users.size() + 1));
    }

    protected ImmutablePair<List<TurboIssueEvent>, String> getEvents(int issueId) {
        TurboIssue issueToGet = issues.get(issueId);
        if (issueToGet != null) {
            // Don't care about the ETag, just give a different ETag each time.
            return new ImmutablePair<>(issueToGet.getMetadata().getEvents(), UUID.randomUUID().toString());
        }
        // Fail silently
        return new ImmutablePair<>(new ArrayList<>(), "");
    }

    protected List<Comment> getComments(int issueId) {
        TurboIssue issueToGet = issues.get(issueId);
        if (issueToGet != null) {
            return issueToGet.getMetadata().getComments();
        }
        // Fail silently
        return new ArrayList<>();
    }

    // UpdateEvent methods to directly mutate the repo state
    protected void makeNewIssue() {
        TurboIssue toAdd = makeDummyIssue();
        issues.put(toAdd.getId(), toAdd);
        updatedIssues.put(toAdd.getId(), toAdd);
    }

    protected void makeNewLabel() {
        TurboLabel toAdd = makeDummyLabel();
        labels.put(toAdd.getActualName(), toAdd);
        updatedLabels.put(toAdd.getActualName(), toAdd);
    }

    protected void makeNewMilestone() {
        TurboMilestone toAdd = makeDummyMilestone();
        milestones.put(toAdd.getId(), toAdd);
        updatedMilestones.put(toAdd.getId(), toAdd);
    }

    protected void makeNewUser() {
        TurboUser toAdd = makeDummyUser();
        users.put(toAdd.getLoginName(), toAdd);
        updatedUsers.put(toAdd.getLoginName(), toAdd);
    }

    // Only updating of issues and milestones is possible. Labels and users are immutable.
    protected TurboIssue updateIssue(int itemId, String updateText) {
        TurboIssue issueToUpdate = issues.get(itemId);

        if (issueToUpdate != null) {
            return renameIssue(issueToUpdate, updateText);
        }
        return null;
    }

    private TurboIssue renameIssue(TurboIssue issueToUpdate, String updateText) {
        // Not allowed to mutate issueToUpdate itself as it introduces immediate changes in the GUI.
        TurboIssue updatedIssue = new TurboIssue(issueToUpdate);

        updatedIssue.setTitle(updateText);

        // Add renamed event to events list of issue
        List<TurboIssueEvent> eventsOfIssue = updatedIssue.getMetadata().getEvents();
        // Not deep copy as the same TurboIssueEvent objects of issueToUpdate are the TurboIssueEvents
        // of updatedIssue. Might create problems later if eventsOfIssue are to be mutable after downloading
        // from repo (which should not be the case).
        // (but this approach works if the metadata of the issue is not modified, which is the current case)
        // TODO make TurboIssueEvent immutable
        eventsOfIssue.add(new TurboIssueEvent(new User().setLogin("test-nonself"),
                IssueEventType.Renamed,
                new Date()));
        List<Comment> commentsOfIssue = updatedIssue.getMetadata().getComments();
        updatedIssue.setMetadata(new IssueMetadata(eventsOfIssue, commentsOfIssue, ""));
        updatedIssue.setUpdatedAt(LocalDateTime.now());

        // Add to list of updated issues, and replace issueToUpdate in main issues store.
        updatedIssues.put(updatedIssue.getId(), updatedIssue);
        issues.put(updatedIssue.getId(), updatedIssue);

        return issueToUpdate;
    }

    protected TurboMilestone updateMilestone(int itemId, String updateText) {
        TurboMilestone milestoneToUpdate = milestones.get(itemId);

        if (milestoneToUpdate != null) {
            return renameMilestone(milestoneToUpdate, updateText);
        }
        return null;
    }

    private TurboMilestone renameMilestone(TurboMilestone milestoneToUpdate, String updateText) {
        // Similarly to renameIssue, to avoid immediate update of the GUI when we update
        // the milestone, milestoneToUpdate is not to be mutated.
        TurboMilestone updatedMilestone = new TurboMilestone(milestoneToUpdate);
        updatedMilestone.setTitle(updateText);

        updatedMilestones.put(updatedMilestone.getId(), updatedMilestone);
        milestones.put(updatedMilestone.getId(), updatedMilestone);

        return milestoneToUpdate;
    }

    protected TurboIssue deleteIssue(int itemId) {
        updatedIssues.remove(itemId);
        return issues.remove(itemId);
    }

    protected TurboLabel deleteLabel(String idString) {
        updatedLabels.remove(idString);
        return labels.remove(idString);
    }

    protected TurboMilestone deleteMilestone(int itemId) {
        updatedMilestones.remove(itemId);
        return milestones.remove(itemId);
    }

    protected TurboUser deleteUser(String idString) {
        updatedUsers.remove(idString);
        return users.remove(idString);
    }

    protected List<Label> setLabels(int issueId, List<String> labels) {
        TurboIssue toSet = new TurboIssue(issues.get(issueId));

        // Update issue events
        List<TurboIssueEvent> eventsOfIssue = toSet.getMetadata().getEvents();
        // TODO change to expression lambdas
        List<String> labelsOfIssue = toSet.getLabels();
        labelsOfIssue.forEach(labelName ->
                eventsOfIssue.add(new TurboIssueEvent(new User().setLogin("test-nonself"),
                        IssueEventType.Unlabeled,
                        new Date()).setLabelName(labelName))
        );
        labels.forEach(labelName ->
                eventsOfIssue.add(new TurboIssueEvent(new User().setLogin("test-nonself"),
                        IssueEventType.Labeled,
                        new Date()).setLabelName(labelName))
        );
        List<Comment> commentsOfIssue = toSet.getMetadata().getComments();
        toSet.setMetadata(new IssueMetadata(eventsOfIssue, commentsOfIssue, ""));
        toSet.setUpdatedAt(LocalDateTime.now());

        // Actually setting label is done after updating issue events
        toSet.setLabels(labels);

        // Then update the relevant state arrays to reflect changes in UI
        issues.put(issueId, toSet);
        updatedIssues.put(issueId, toSet);

        return labels.stream().map(new Label()::setName).collect(Collectors.toList());
    }

    protected TurboIssue commentOnIssue(String author, String commentText, int issueId) {
        // Copy constructor used so that changes in the GUI are not introduced immediately
        TurboIssue toComment = new TurboIssue(issues.get(issueId));

        List<TurboIssueEvent> eventsOfIssue = toComment.getMetadata().getEvents();

        // Again, to prevent immediate changes, we create a new arraylist from comments
        List<Comment> commentsOfIssue = new ArrayList<>(toComment.getMetadata().getComments());
        Comment toAdd = new Comment();
        toAdd.setBody(commentText);
        toAdd.setCreatedAt(new Date());
        toAdd.setUser(new User().setLogin(author));
        commentsOfIssue.add(toAdd);

        toComment.setMetadata(new IssueMetadata(eventsOfIssue, commentsOfIssue, ""));
        toComment.setUpdatedAt(LocalDateTime.now());
        toComment.setCommentCount(toComment.getCommentCount() + 1);

        // Add to list of updated issues, and replace issueToUpdate in main issues store.
        updatedIssues.put(toComment.getId(), toComment);
        issues.put(toComment.getId(), toComment);

        return toComment;
    }

}
