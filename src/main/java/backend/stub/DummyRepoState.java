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
import org.eclipse.egit.github.core.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DummyRepoState {

    public static final int NO_OF_DUMMY_ISSUES = 12;
    private final String dummyRepoId;

    private final TreeMap<Integer, TurboIssue> issues = new TreeMap<>();
    private final TreeMap<String, TurboLabel> labels = new TreeMap<>();
    private final TreeMap<Integer, TurboMilestone> milestones = new TreeMap<>();
    private final TreeMap<String, TurboUser> users = new TreeMap<>();

    private TreeMap<Integer, TurboIssue> updatedIssues = new TreeMap<>(); // NOPMD
    private TreeMap<String, TurboLabel> updatedLabels = new TreeMap<>(); // NOPMD
    private TreeMap<Integer, TurboMilestone> updatedMilestones = new TreeMap<>(); // NOPMD
    private TreeMap<String, TurboUser> updatedUsers = new TreeMap<>(); // NOPMD

    // We store issueMetadata separately from issues so that metadata of issues returned by getUpdatedIssues/getIssues
    // is empty. This is the case when interfacing with GitHub. (and then metadata gets retrieved separately from
    // getEvents and getComments).
    private final HashMap<Integer, IssueMetadata> issueMetadata = new HashMap<>();
    // We keep track of issues that user has not gotten metadata from.
    private final HashSet<Integer> updatedEvents = new HashSet<>();
    private final HashSet<Integer> updatedComments = new HashSet<>();

    public DummyRepoState(String repoId) {
        this.dummyRepoId = repoId;

        initializeRepoEntities();
        connectRepoEntities();
        insertInitialMetadata();
    }

    private void initializeRepoEntities() {
        for (int i = 0; i < NO_OF_DUMMY_ISSUES; i++) {
            TurboIssue dummyIssue;
            // Issue #7 is a PR
            switch (i) {
            case 6:
            case 10:
                dummyIssue = makeDummyPR();
                break;
            default:
                dummyIssue = makeDummyIssue();
                break;
            }
            // All default issues are treated as if created a long time ago
            dummyIssue.setUpdatedAt(LocalDateTime.of(2000 + i, 1, 1, 0, 0));
            TurboLabel dummyLabel = makeDummyLabel();
            TurboMilestone dummyMilestone = makeDummyMilestone();
            TurboUser dummyUser = makeDummyUser();

            // Populate state with defaults
            issues.put(dummyIssue.getId(), dummyIssue);
            labels.put(dummyLabel.getFullName(), dummyLabel);
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

        // set milestones for testing milestone alias
        milestones.get(1).setDueDate(Optional.of(LocalDate.now().minusMonths(3)));
        milestones.get(1).setOpen(false);
        milestones.get(2).setDueDate(Optional.of(LocalDate.now().minusMonths(2))); // current
        milestones.get(3).setDueDate(Optional.of(LocalDate.now().minusMonths(1)));
        milestones.get(3).setOpen(false);
        milestones.get(4).setDueDate(Optional.of(LocalDate.now().plusMonths(1)));
    }

    private void connectRepoEntities() {
        // Issues #1-5 are assigned milestones 1-5 respectively
        for (int i = 1; i <= 5; i++) {
            issues.get(i).setMilestone(milestones.get(i));
            milestones.get(i).setOpenIssues(1);
        }

        // Odd issues are assigned label 1, even issues are assigned label 2
        for (int i = 1; i <= NO_OF_DUMMY_ISSUES; i++) {
            issues.get(i).addLabel((i % 2 == 0) ? "Label 2" : "Label 1");
        }

        // We assign a colorful label to issue 10
        labels.put("Label 11", new TurboLabel(dummyRepoId, "ffa500", "Label 11"));
        issues.get(10).addLabel("Label 11");

        // For issue 1 to 10, each contributor is assigned to his corresponding issue
        for (int i = 1; i <= 10; i++) {
            issues.get(i).setAssignee("User " + i);
        }
        // Issues following from here will have different assignments for testing
        issues.get(11).setAssignee("User 1");
        issues.get(12).setAssignee("User 2");
    }

    private void insertInitialMetadata() {
        // Put down a comment by current HT user (empty string) for issue 9
        Comment ownComment = new Comment();
        ownComment.setCreatedAt(new Date());
        ownComment.setUser(new User().setLogin("test"));
        Comment[] ownComments = { ownComment };
        issues.get(9).setCommentCount(1);
        issues.get(9).setUpdatedAt(LocalDateTime.now());
        issueMetadata.put(9, IssueMetadata.intermediate(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(ownComments)),
                "", ""
        ));
        updatedEvents.add(9);

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
        issues.get(10).setCommentCount(3);
        issues.get(10).setUpdatedAt(LocalDateTime.now());
        issueMetadata.put(10, IssueMetadata.intermediate(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(dummyComments)),
                "", ""
        ));
        updatedEvents.add(10);

        // Then set label 3 and 11 for issue 8, and immediately remove label 11
        String[] oldLabels = { "Label 3", "Label 11" };
        String[] newLabels = { "Label 3" };
        setLabels(8, Arrays.asList(oldLabels));
        setLabels(8, Arrays.asList(newLabels));
        // Then put a temporary (colourful) label into the repo
        labels.put("Deleted", new TurboLabel(dummyRepoId, "84b6eb", "Deleted"));
        String[] issue9Labels = { "Label 1" };
        String[] deletedLabels = { "Label 1", "Deleted" };
        setLabels(9, Arrays.asList(deletedLabels)); // add and unset it immediately on issue 9
        setLabels(9, Arrays.asList(issue9Labels));
        labels.remove("Deleted"); // Then remove this label. The labeling events should still display the color.
    }

    protected ImmutableTriple<List<TurboIssue>, String, Date> getUpdatedIssues(String eTag, Date lastCheckTime) {

        String currETag = eTag;
        if (!updatedIssues.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutableTriple<List<TurboIssue>, String, Date> toReturn = new ImmutableTriple<>(
                deepCopyIssues(updatedIssues), currETag, lastCheckTime);

        updatedIssues = new TreeMap<>();
        return toReturn;
    }

    protected ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String eTag) {
        String currETag = eTag;
        if (!updatedLabels.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutablePair<List<TurboLabel>, String> toReturn
                = new ImmutablePair<>(deepCopyLabels(updatedLabels), currETag);

        updatedLabels = new TreeMap<>();
        return toReturn;
    }

    protected ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String eTag) {
        String currETag = eTag;
        if (!updatedMilestones.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutablePair<List<TurboMilestone>, String> toReturn
                = new ImmutablePair<>(deepCopyMilestones(updatedMilestones), currETag);

        updatedMilestones = new TreeMap<>();
        return toReturn;
    }

    protected ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String eTag) {
        String currETag = eTag;
        if (!updatedUsers.isEmpty() || eTag == null) currETag = UUID.randomUUID().toString();

        ImmutablePair<List<TurboUser>, String> toReturn
                = new ImmutablePair<>(deepCopyUsers(updatedUsers), currETag);

        updatedUsers = new TreeMap<>();
        return toReturn;
    }

    protected List<TurboIssue> getIssues() {
        return deepCopyIssues(issues);
    }

    protected List<TurboLabel> getLabels() {
        return deepCopyLabels(labels);
    }

    protected List<TurboMilestone> getMilestones() {
        return deepCopyMilestones(milestones);
    }

    protected List<TurboUser> getCollaborators() {
        return deepCopyUsers(users);
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

    protected ImmutablePair<List<TurboIssueEvent>, String> getEvents(int issueId, String currentETag) {
        if (updatedEvents.contains(issueId)) {
            IssueMetadata metadataOfIssue = issueMetadata.get(issueId);

            // If updatedEvents contains issue, its metadata must also exist in the metadataOfIssue hashmap
            assert metadataOfIssue != null;

            // Remove issue from updatedEvents so that next time metadata is retrieved, the same ETag
            // will not be sent again unless more updates will have been introduced.
            updatedEvents.remove(issueId);

            // Finally, return events as a proper array.
            return new ImmutablePair<>(metadataOfIssue.getEvents(), UUID.randomUUID().toString());
        }
        return new ImmutablePair<>(new ArrayList<>(), currentETag);
    }

    protected List<Comment> getComments(int issueId) {
        // TODO after implementing support for comments ETags on GitHubClientExtended, change logic to getEvents'.
        IssueMetadata metadataOfIssue = issueMetadata.get(issueId);
        if (metadataOfIssue != null) {
            return new ArrayList<>(metadataOfIssue.getComments());
        }
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
        labels.put(toAdd.getFullName(), toAdd);
        updatedLabels.put(toAdd.getFullName(), toAdd);
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

    public TurboIssue updateIssue(int issueId, String updateText) {
        // Get copies of issue itself and its metadata
        ImmutablePair<TurboIssue, IssueMetadata> mutables = produceMutables(issueId);
        TurboIssue toRename = mutables.getLeft();
        IssueMetadata metadataOfIssue = mutables.getRight();
        List<TurboIssueEvent> eventsOfIssue = metadataOfIssue.getEvents();

        // Mutate the copies
        eventsOfIssue.add(new TurboIssueEvent(new User().setLogin("test-nonself"),
                                              IssueEventType.Renamed,
                                              new Date()));
        toRename.setTitle(updateText);
        toRename.setUpdatedAt(LocalDateTime.now());

        // Replace originals with copies, and queue them up to be retrieved
        markUpdatedEvents(toRename, IssueMetadata.intermediate(eventsOfIssue, metadataOfIssue.getComments(), "", ""));

        return toRename;
    }

    public boolean editIssueState(int issueId, boolean isOpen) {
        ImmutablePair<TurboIssue, IssueMetadata> mutables = produceMutables(issueId);
        TurboIssue toEdit = mutables.getLeft();
        IssueMetadata metadataOfIssue = mutables.getRight();
        List<TurboIssueEvent> eventsOfIssue = metadataOfIssue.getEvents();

        eventsOfIssue.add(new TurboIssueEvent(new User().setLogin("test-nonself"),
                                              isOpen ? IssueEventType.Reopened : IssueEventType.Closed,
                                              new Date()));
        toEdit.setOpen(isOpen);
        toEdit.setUpdatedAt(LocalDateTime.now());

        markUpdatedEvents(toEdit, IssueMetadata.intermediate(eventsOfIssue, metadataOfIssue.getComments(), "", ""));

        return true;
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

    protected final List<Label> setLabels(int issueId, List<String> newLabels) {
        // Get copies of issue itself and its metadata
        ImmutablePair<TurboIssue, IssueMetadata> mutables = produceMutables(issueId);
        TurboIssue toSet = mutables.getLeft();
        IssueMetadata metadataOfIssue = mutables.getRight();
        List<TurboIssueEvent> eventsOfIssue = metadataOfIssue.getEvents();

        // Mutate the copies
        List<String> labelsOfIssue = toSet.getLabels();
        labelsOfIssue.stream()
                .filter(existingLabel -> !newLabels.contains(existingLabel))
                .forEach(labelName -> eventsOfIssue.add(
                        new TurboIssueEvent(new User().setLogin("test-nonself"), IssueEventType.Unlabeled, new Date())
                                .setLabelName(labelName)
                                .setLabelColour(labels.get(labelName).getColour())));

        newLabels.stream()
                .filter(newLabel -> !labelsOfIssue.contains(newLabel))
                .forEach(newLabel -> eventsOfIssue.add(
                        new TurboIssueEvent(new User().setLogin("test-nonself"), IssueEventType.Labeled, new Date())
                                .setLabelName(newLabel)
                                .setLabelColour(labels.get(newLabel).getColour())));

        toSet.setLabels(newLabels);
        toSet.setUpdatedAt(LocalDateTime.now());

        // Replace originals with copies, and queue them up to be retrieved
        markUpdatedEvents(toSet, IssueMetadata.intermediate(eventsOfIssue, metadataOfIssue.getComments(), "", ""));

        return newLabels.stream().map(new Label()::setName).collect(Collectors.toList());
    }

    protected final Issue setMilestone(int issueId, Optional<Integer> milestone) {
        ImmutablePair<TurboIssue, IssueMetadata> mutables = produceMutables(issueId);
        TurboIssue issueToSet = mutables.getLeft();
        IssueMetadata metadataOfIssue = mutables.getRight();
        List<TurboIssueEvent> eventsOfIssue = metadataOfIssue.getEvents();

        // demilestone the issue, then set issue milestone using the new milestone
        issueToSet.getMilestone()
                .ifPresent(issueMilestone -> removeMilestoneFromIssue(issueMilestone, issueToSet, eventsOfIssue));
        milestone
                .ifPresent(newMilestone -> setMilestoneForIssue(newMilestone, issueToSet, eventsOfIssue));

        issueToSet.setUpdatedAt(LocalDateTime.now());
        // Replace originals with copies, and queue them up to be retrieved
        markUpdatedEvents(issueToSet, IssueMetadata.intermediate(eventsOfIssue, metadataOfIssue.getComments(), "", ""));

        Issue serverIssue = new Issue();
        milestone.ifPresent(newMilestone -> serverIssue.setMilestone(new Milestone().setNumber(newMilestone)));
        return serverIssue;
    }

    /**
     * Sets milestone for an issue, after triggering a 'Milestoned' TurboIssueEvent for it
     *
     * @param milestone
     * @param toSet
     * @param eventsOfIssue
     */
    private void setMilestoneForIssue(int milestone, TurboIssue toSet, List<TurboIssueEvent> eventsOfIssue) {
        eventsOfIssue.add(new TurboIssueEvent(new User().setLogin("test"),
                                              IssueEventType.Milestoned,
                                              new Date()).setMilestoneTitle(milestones.get(milestone).getTitle()));
        toSet.setMilestoneById(milestone);
    }

    /**
     * Removes the milestone of an issue, after triggerign a 'Demilestoned' TurboIssueEvent for it
     *
     * @param milestoneOfIssue
     * @param toSet
     * @param eventsOfIssue
     */
    private void removeMilestoneFromIssue(int milestoneOfIssue, TurboIssue toSet, List<TurboIssueEvent> eventsOfIssue) {
        eventsOfIssue.add(new TurboIssueEvent(new User().setLogin("test"),
                                              IssueEventType.Demilestoned,
                                              new Date())
                .setMilestoneTitle(milestones.get(milestoneOfIssue).getTitle()));

        toSet.removeMilestone();
    }

    protected TurboIssue commentOnIssue(String author, String commentText, int issueId) {
        // Get copies of issue itself and its metadata
        ImmutablePair<TurboIssue, IssueMetadata> mutables = produceMutables(issueId);
        TurboIssue toComment = mutables.getLeft();
        IssueMetadata metadataOfIssue = mutables.getRight();
        List<Comment> commentsOfIssue = metadataOfIssue.getComments();

        // Mutate the copies
        Comment toAdd = new Comment();
        toAdd.setBody(commentText);
        toAdd.setCreatedAt(new Date());
        toAdd.setUser(new User().setLogin(author));
        commentsOfIssue.add(toAdd);
        toComment.setUpdatedAt(LocalDateTime.now());
        toComment.setCommentCount(toComment.getCommentCount() + 1);

        // Replace originals with copies, and queue them up to be retrieved
        markUpdatedComments(toComment,
                            IssueMetadata.intermediate(metadataOfIssue.getEvents(), commentsOfIssue, "", ""));

        return toComment;
    }

    /**
     * Auxiliary method to retrieve a copy of the issue with the given ID, as well as a copy of its metadata.
     * The copying process ensures that changes to the issue database does not instantly propagate to the UI.
     *
     * @param issueId The ID of the issue to mutate
     * @return Copy of the given issue and its metadata
     */
    private ImmutablePair<TurboIssue, IssueMetadata> produceMutables(int issueId) {
        TurboIssue toMutate = new TurboIssue(issues.get(issueId));

        IssueMetadata metadataOfIssue = issueMetadata.get(toMutate.getId());
        List<TurboIssueEvent> eventsOfIssue = metadataOfIssue != null ?
                metadataOfIssue.getEvents() :
                new ArrayList<>();
        List<Comment> commentsOfIssue = metadataOfIssue != null ?
                metadataOfIssue.getComments() :
                new ArrayList<>();

        return new ImmutablePair<>(toMutate, IssueMetadata.intermediate(eventsOfIssue, commentsOfIssue, "", ""));
    }

    /**
     * Auxiliary method that replaces original issue & metadata with the mutated copies after updating the issue.
     * Simulates the event of an user action causing the ETag of the issue events to change.
     *
     * @param toMark   The mutated copy of the issue, to replace the original issue
     * @param toInsert The mutated metadata of the issue, to replace the original issue metadata
     */
    private void markUpdatedEvents(TurboIssue toMark, IssueMetadata toInsert) {
        int issueId = toMark.getId();

        issues.put(issueId, toMark);
        updatedIssues.put(issueId, toMark);
        issueMetadata.put(issueId, toInsert);
        updatedEvents.add(issueId);
    }

    /**
     * Auxiliary method that replaces original issue & metadata with the mutated copies after updating the issue.
     * Simulates the event of an user action causing the ETag of the issue comments to change.
     *
     * @param toMark   The mutated copy of the issue, to replace the original issue
     * @param toInsert The mutated metadata of the issue, to replace the original issue metadata
     */
    private void markUpdatedComments(TurboIssue toMark, IssueMetadata toInsert) {
        int issueId = toMark.getId();

        issues.put(issueId, toMark);
        updatedIssues.put(issueId, toMark);
        issueMetadata.put(issueId, toInsert);
        updatedComments.add(issueId);
    }

    /**
     * Copies the TreeMap of issues by creating a List containing copies of state-stored issues. Prevents
     * external mutation of the issue objects from propagating to the repo state.
     *
     * @param issuesToCopy A TreeMap containing state-stored issues to copy from.
     * @return A list containing copies of given issues.
     */
    private List<TurboIssue> deepCopyIssues(TreeMap<Integer, TurboIssue> issuesToCopy) {
        ArrayList<TurboIssue> copiedIssues = new ArrayList<>();
        issuesToCopy.values().forEach(issue -> copiedIssues.add(new TurboIssue(issue)));

        return copiedIssues;
    }

    /**
     * Copies the TreeMap of labels by creating a List containing copies of state-stored labels. Prevents
     * external mutation of the label objects from propagating to the repo state.
     *
     * @param labelsToCopy A TreeMap containing state-stored labels to copy from.
     * @return A list containing copies of given labels.
     */
    private List<TurboLabel> deepCopyLabels(TreeMap<String, TurboLabel> labelsToCopy) {
        ArrayList<TurboLabel> copiedLabels = new ArrayList<>();
        labelsToCopy.values().forEach(label -> copiedLabels.add(new TurboLabel(label)));

        return copiedLabels;
    }

    /**
     * Copies the TreeMap of milestones by creating a List containing copies of state-stored milestones. Prevents
     * external mutation of the milestone objects from propagating to the repo state.
     *
     * @param milestonesToCopy A TreeMap containing state-stored milestones to copy from.
     * @return A list containing copies of given milestones.
     */
    private List<TurboMilestone> deepCopyMilestones(TreeMap<Integer, TurboMilestone> milestonesToCopy) {
        ArrayList<TurboMilestone> copiedMilestones = new ArrayList<>();
        milestonesToCopy.values().forEach(milestone -> copiedMilestones.add(new TurboMilestone(milestone)));

        return copiedMilestones;
    }

    /**
     * Copies the TreeMap of users by creating a List containing copies of state-stored users. Prevents
     * external mutation of the user objects from propagating to the repo state.
     *
     * @param usersToCopy A TreeMap containing state-stored users to copy from.
     * @return A list containing copies of given users.
     */
    private List<TurboUser> deepCopyUsers(TreeMap<String, TurboUser> usersToCopy) {
        ArrayList<TurboUser> copiedUsers = new ArrayList<>();
        usersToCopy.values().forEach(user -> copiedUsers.add(new TurboUser(user)));

        return copiedUsers;
    }
}
