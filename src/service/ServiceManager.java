package service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import javafx.application.Platform;
import model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.markdown4j.Markdown4jProcessor;

import storage.CacheFileHandler;
import storage.CachedRepoData;
import tests.stubs.ServiceManagerStub;
import ui.UI;
import ui.components.HTStatusBar;
import util.PlatformEx;
import util.Utility;

/**
 * Singleton class that provides access to the GitHub API services required by
 * HubTurbo
 * <p>
 * Only data from a single repository can be loaded at any point of time. The
 * currently loaded repository is stored in the application's ServiceManager
 * instance
 * <p>
 * Also holds a reference to the application's current Model instance, which
 * stores the repository's labels, milestones, assignees and issues.
 */
@SuppressWarnings("unused")
public class ServiceManager {

	private static final ServiceManager instance = new ServiceManager();

	// Set externally by test runners
	public static boolean isInTestMode = false;

	public static ServiceManager getInstance() {
		if (!isInTestMode) {
			return instance;
		} else {
			return new ServiceManagerStub();
		}
	}

	private static final Logger logger = LogManager.getLogger(ServiceManager.class.getName());

	public static final int SYNC_PERIOD = 60;

	// Login state

	protected String lastUsedPassword;

	// Services

	private GitHubClientExtended githubClient;

	private CollaboratorService collabService;
	private IssueServiceExtended issueService;
	private LabelServiceFixed labelService;
	private MilestoneService milestoneService;
	private RepositoryServiceExtended repositoryService;
	private MarkdownService markdownService;
	private ContentsService contentService;

	// Model updates

	private ModelUpdater modelUpdater;
	private UpdatedIssueMetadata updatedIssueMetadata = new UpdatedIssueMetadata(this);
	protected Model model;
	protected RepositoryId repoId;

	private UpdateSignature updateSignature = new UpdateSignature();

	private final TickingTimer timer;

	private static final String ISSUE_STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";

	protected ServiceManager() {
		githubClient = new GitHubClientExtended();
		collabService = new CollaboratorService(githubClient);
		issueService = new IssueServiceExtended(githubClient);
		labelService = new LabelServiceFixed(githubClient);
		milestoneService = new MilestoneService(githubClient);
		repositoryService = new RepositoryServiceExtended(githubClient);
		markdownService = new MarkdownService(githubClient);
		contentService = new ContentsService(githubClient);
		timer = createTickingTimer();
		// TODO construct model later
		model = new Model();
	}

	/**
	 * Methods concerned with login logic
	 */
	private void ______LOGIN______() {
	}

	/**
	 * Given a username and password, attempts to log into GitHub.
	 * Returns true on success and false otherwise.
	 *
	 * @param userId
	 * @param password
	 * @return
	 */
	public boolean login(String userId, String password) {

		this.lastUsedPassword = password;
		githubClient.setCredentials(userId, password);

		// Attempt login
		try {
			GitHubRequest request = new GitHubRequest();
			request.setUri("/");
			githubClient.get(request);
		} catch (IOException e) {
			// Login failed
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Returns the username last used to log in.
	 *
	 * @return
	 */
	public String getUserId() {
		return githubClient.getUser();
	}

	/**
	 * Returns the password last used to log in.
	 *
	 * @return
	 */
	public String getLastUsedPassword() {
		assert lastUsedPassword != null;
		return lastUsedPassword;
	}

	/**
	 * Methods concerned with dealing with online GitHub repositories
	 */
	private void ______REPOSITORY______() {
	}

	// TODO should return a copy
	public IRepositoryIdProvider getRepoId() {
		return repoId;
	}

	/**
	 * Given a repository owner and name, loads its contents into the model.
	 * Assumes that authentication has already been done, so should be called
	 * after {@link #login(String, String) login}.
	 *
	 * @param owner the owner of the repository
	 * @param name the repository name
	 * @param taskUpdate a callback to handle progress updates
	 * @return
	 * @throws IOException
	 */
	public boolean setupRepository(String owner, String name, BiConsumer<String, Float> taskUpdate) throws IOException {

		assert lastUsedPassword != null : "setupRepository should be called only after login";
		logger.info("Authenticating...");

		this.repoId = RepositoryId.create(owner, name);

		if (isRepositoryValid(repoId)) {
			return model.loadComponents(repoId, taskUpdate);
		} else {
			// TODO: create specific exception for this
			throw new IOException("Cannot access repository");
		}
	}

	public String getRepoOwner() {
		return repoId.getOwner();
	}

	public String getRepoName() {
		return repoId.getName();
	}

	/**
	 * Determines if a repository is a valid one. Returns false if not, otherwise
	 * returns true. Throws an IOException if the check fails in any other way.
	 *
	 * @param repo
	 * @return
	 * @throws IOException
	 */
	public boolean isRepositoryValid(IRepositoryIdProvider repo) throws IOException {
		return isRepositoryValid(repo.generateId());
	}

	/**
	 * Determines if a repository is a valid one. Returns false if not, otherwise
	 * returns true. Throws an IOException if the check fails in any other way.
	 *
	 * @param repo
	 * @return
	 * @throws IOException
	 */
	private boolean isRepositoryValid(String repo) throws IOException {
		String repoURL = SEGMENT_REPOS + "/" + repo;
		return checkRepositoryValidity(repoURL);
	}

	/**
	 * Determines if a repository is a valid one. Returns false if not, otherwise
	 * returns true. Throws an IOException if the check fails in any other way.
	 * @param uri the URL of the repository
	 * @return true if the repository is valid
	 * @throws IOException
	 */
	protected boolean checkRepositoryValidity(String uri) throws IOException {
		try {
			GitHubRequest req = new GitHubRequest();
			githubClient.get(req.setUri(uri));
			return true;
		} catch (RequestException e) {
			logger.error(e.getLocalizedMessage(), e);
			if (e.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
				return false;
			}
			throw e;
		}
	}

	public int getRemainingRequests() {
		return githubClient.getRemainingRequests();
	}

	public int getRequestLimit() {
		return githubClient.getRequestLimit();
	}

	/**
	 * Returns a list of the user's public repositories.
	 *
	 * @return
	 * @throws IOException
	 */
	public List<Repository> getRepositories() throws IOException {
		return repositoryService.getRepositories();
	}

	/**
	 * Returns a list of the names of the user's public repositories
	 *
	 * @return
	 * @throws IOException
	 */
	public List<String> getRepositoriesNames() throws IOException {
		return repositoryService.getRepositoriesNames(getUserId());
	}

	/**
	 * Returns a list of the public repositories belonging to the user and the
	 * user's organisations
	 *
	 * @return
	 * @throws IOException
	 */
	public List<Repository> getAllRepositories() throws IOException {
		return repositoryService.getAllRepositories(getUserId());
	}

	/**
	 * Returns a list of the names of the public repositories belonging to the
	 * user and the user's organisations
	 */
	public List<String> getAllRepositoryNames() throws IOException {
		return repositoryService.getAllRepositoriesNames(getUserId());
	}

	/**
	 * Methods concerned with updating or interfacing with the model
	 */
	private void ______MODEL______() {
	}

	/**
	 * Retrieves resources for the given repository. Abstracts away differences
	 * between a cache and online source
	 * @param repoId the repository to load
	 * @param taskUpdate a callback to handle progress updates
	 * @return all requested resources for the given repository
	 * @throws IOException
	 */
	public RepositoryResources getResources(RepositoryId repoId, BiConsumer<String, Float> taskUpdate) throws IOException {
		this.repoId = repoId;

		CacheFileHandler dcHandler = new CacheFileHandler(repoId.toString());
		// TODO set these paramters in constructor instead
		model.setDataCacheFileHandler(dcHandler);
		model.setRepoId(repoId);

		boolean needToGetResources = true;

		CachedRepoData repo = dcHandler.getRepo();
		if (repo != null) {
			needToGetResources = false;
		}

		if (!needToGetResources) {
			return getCacheResources(repo, taskUpdate);
		} else {
			logger.info("Cache not found, loading data from GitHub...");
			return getGitHubResources(taskUpdate);
		}
	}

	/**
	 * Loads resources from the cache.
	 * @param repo the repository to load
	 * @param taskUpdate a callback to handle progress updates
	 * @return the requested resources
	 */
	private RepositoryResources getCacheResources(CachedRepoData repo, BiConsumer<String, Float> taskUpdate) {
		logger.info("Loading from cache...");

		Date issueCheckTime = repo.getIssueCheckTime() == null
			? new Date()
			: Utility.localDateTimeToDate(repo.getIssueCheckTime());

		updateSignature = new UpdateSignature(repo.getIssuesETag(), repo.getLabelsETag(),
			repo.getMilestonesETag(), repo.getCollaboratorsETag(), issueCheckTime);

		taskUpdate.accept("Loading collaborators...", 0f);
		List<TurboUser> collaborators = repo.getCollaborators();
		taskUpdate.accept("Loading labels...", 0.25f);
		List<TurboLabel> labels = repo.getLabels();
		taskUpdate.accept("Loading milestones...", 0.5f);
		List<TurboMilestone> milestones = repo.getMilestones();
		taskUpdate.accept("Loading issues...", 0.75f);
		List<TurboIssue> issues = repo.getIssues(model);

		return RepositoryResources.fromCache(issues, labels, milestones, collaborators);
	}

	/**
	 * Loads resources from github
	 * @param taskUpdate a callback to handle progress updates
	 * @return the requested resources
	 * @throws IOException
	 */
	public RepositoryResources getGitHubResources(BiConsumer<String, Float> taskUpdate) throws IOException {

		updateSignature = new UpdateSignature();

		taskUpdate.accept("Loading collaborators...", 0f);
		List<User> users = new ArrayList<>();
		taskUpdate.accept("Loading labels...", 0.25f);
		List<Label> labels = getLabels();
		taskUpdate.accept("Loading milestones...", 0.5f);
		List<Milestone> milestones = getMilestones();
		taskUpdate.accept("Loading issues...", 0.75f);
		List<Issue> issues = getAllIssues(repoId, taskUpdate);

		return RepositoryResources.fromGitHub(issues, labels, milestones, users);
	}

	public Model getModel() {
		return model;
	}

	private TickingTimer createTickingTimer() {
		return new TickingTimer("Sync Timer", SYNC_PERIOD, HTStatusBar::updateRefreshTimer, () -> {
			preventRepoSwitchingAndUpdateModel(model.getRepoId().generateId());
		});
	}

	/**
	 * Updates the contents of the model with data from the given repository.
	 *
	 * @param repoId the repository get updates for/from
	 */
	private void preventRepoSwitchingAndUpdateModel(String repoId) {

		modelUpdater = new ModelUpdater(githubClient, model, updateSignature);

		// Disable repository selection
		PlatformEx.runAndWait(() -> {
			UI.getInstance().disableRepositorySwitching();
		});

		// Wait for the update to complete

		modelUpdater.updateModel(repoId);

		updateSignature = modelUpdater.getNewUpdateSignature();
		model.updateCache(updateSignature);

		updatedIssueMetadata.download();
		model.triggerModelChangeEvent();

		// Reset progress UI
		HTStatusBar.updateProgress(0);

		// Enable repository switching
		Platform.runLater(() -> {
			UI.getInstance().enableRepositorySwitching();
		});
	}

	/**
	 * To be called when the app starts.
	 */
	public void startModelUpdate() {
		timer.start();
	}

	/**
	 * To be called when the app closes.
	 */
	public void stopModelUpdate() {
		timer.stop();
	}

	/**
	 * Triggers an update of the model.
	 *
	 * @return a latch which blocks until the model is finished updating
	 */
	public CountDownLatch updateModelNow() {
		return timer.trigger();
	}

	/**
	 * Compound, synchronous action. After being called the contents of the
	 * model will be of the given repoId.
	 *
	 * @param repoId the repository to switch to
	 * @param taskUpdate a callback to handle progress updates
	 * @throws IOException
	 */
	public void switchRepository(RepositoryId repoId, BiConsumer<String, Float> taskUpdate) throws IOException {
		timer.pause();
		model.populateComponents(repoId, getResources(repoId, taskUpdate));
		timer.resume();

		taskUpdate.accept("Making sure everything is updated...", 1f);
		try {
			updateModelNow().await();
		} catch (InterruptedException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		taskUpdate.accept("Done!", 1f);
	}

	/**
	 * Compound, synchronous action. After being called the contents of the
	 * model reflect the latest version of the currently-loaded repository.
	 *
	 * @param taskUpdate a callback to handle progress updates
	 */
	public void forceRefresh(BiConsumer<String, Float> taskUpdate) {
		timer.pause();
		try {
			model.forceReloadComponents(taskUpdate);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		timer.resume();

		taskUpdate.accept("Making sure everything is updated...", 1f);
		try {
			updateModelNow().await();
		} catch (InterruptedException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		taskUpdate.accept("Done!", 1f);
	}

	private void ______LABELS______() {
	}

	public List<Label> getLabels() throws IOException {
		if (repoId != null) {
			return labelService.getLabels(repoId);
		}
		return new ArrayList<>();
	}

	public Label createLabel(Label ghLabel) throws IOException {
		if (repoId != null) {
			return labelService.createLabel(repoId, ghLabel);
		}
		return null;
	}

	public void deleteLabel(String label) throws IOException {
		if (repoId != null) {
			labelService.deleteLabel(repoId, label);
		}
	}

	public Label editLabel(Label label, String name) throws IOException {
		if (repoId != null) {
			return labelService.editLabel(repoId, label, name);
		}
		return null;
	}

	private void ______MILESTONES______() {
	}

	public List<Milestone> getMilestones() throws IOException {
		if (repoId != null) {
			return milestoneService.getMilestones(repoId, ISSUE_STATE_ALL);
		}
		return new ArrayList<>();
	}

	public Milestone createMilestone(Milestone milestone) throws IOException {
		if (repoId != null) {
			return milestoneService.createMilestone(repoId, milestone);
		}
		return null;
	}

	public void deleteMilestone(int milestoneNum) throws IOException {
		if (repoId != null) {
			milestoneService.deleteMilestone(repoId, milestoneNum);
		}
	}

	public Milestone editMilestone(Milestone milestone) throws IOException {
		if (repoId != null) {
			return milestoneService.editMilestone(repoId, milestone);
		}
		return null;
	}

	private void ______ISSUES______() {
	}

	/**
	 * Retrieves issues from the given repository.
	 * @param repoId the repository to get issues from
	 * @param taskUpdate a callback to handle progress updates
	 * @return the requested issues
	 * @throws IOException
	 */
	public List<Issue> getAllIssues(RepositoryId repoId, BiConsumer<String, Float> taskUpdate) throws IOException {
		// TODO make this an assertion
		if (repoId != null) {
			Map<String, String> filters = new HashMap<>();
			filters.put(IssueService.FIELD_FILTER, ISSUE_STATE_ALL);
			filters.put(IssueService.FILTER_STATE, ISSUE_STATE_ALL);
			return getAllIssuesPaged(repoId, filters, taskUpdate);
		}
		return new ArrayList<>();
	}

	private List<Issue> getAllIssuesPaged(IRepositoryIdProvider repoId, Map<String, String> filters,
	                                      BiConsumer<String, Float> taskUpdate) throws IOException {
		return getAll(issueService.pageIssues(repoId, filters), taskUpdate);
	}

	private List<Issue> getAll(PageIterator<Issue> iterator, BiConsumer<String, Float> taskUpdate) throws IOException {
		List<Issue> elements = new ArrayList<>();
		int totalIssueCount;

		try {
			while (iterator.hasNext()) {
				Collection<Issue> additions = iterator.next();
				elements.addAll(additions);

				// Compute progress

				// Total is only available after iterator.next() is called at least once.
				// Even then it's approximate: always >= the actual amount.
				totalIssueCount = iterator.getLastPage() * PagedRequest.PAGE_SIZE;
				assert totalIssueCount >= elements.size();

				float progress = 0.75f + 0.25f * ((float) elements.size() / (float) totalIssueCount);

				taskUpdate.accept("Loaded " + elements.size() + " issues...", progress);
			}
		} catch (NoSuchPageException pageException) {
			throw pageException.getCause();
		}
		return elements;
	}

	public Issue createIssue(Issue issue) throws IOException {
		if (repoId != null) {
			return issueService.createIssue(repoId, issue);
		}
		return null;
	}

	public Issue getIssue(int issueId) throws IOException {
		if (repoId != null) {
			return issueService.getIssue(repoId, issueId);
		}
		return null;
	}

	public HashMap<String, Object> getIssueData(int issueId) throws IOException {
		if (repoId != null) {
			return issueService.getIssueData(repoId, issueId);
		}
		return new HashMap<>();
	}

	public String getDateFromIssueData(HashMap<String, Object> issueData) {
		return (String) issueData.get(IssueServiceExtended.ISSUE_DATE);
	}

	/**
	 * Get user repositories
	 */

	public Issue getIssueFromIssueData(HashMap<String, Object> issueData) {
		return (Issue) issueData.get(IssueServiceExtended.ISSUE_CONTENTS);
	}

	public Issue editIssue(Issue latest, String dateModified) throws IOException {
		if (repoId != null) {
			return issueService.editIssue(repoId, latest, dateModified);
		}
		return null;
	}

	public Issue editIssueTitle(int issueId, String title) throws IOException {
		if (repoId != null) {
			return issueService.editIssueTitle(repoId, issueId, title);
		}
		return null;
	}

	public Issue editIssueBody(int issueId, String body) throws IOException {
		if (repoId != null) {
			return issueService.editIssueBody(repoId, issueId, body);
		}
		return null;
	}

	public void closeIssue(int issueId) throws IOException {
		if (repoId != null) {
			issueService.editIssueState(repoId, issueId, false);
		}
	}

	public void openIssue(int issueId) throws IOException {
		if (repoId != null) {
			issueService.editIssueState(repoId, issueId, true);
		}
	}

	/**
	 * Methods to work with issue labels
	 */

	public List<Label> setLabelsForIssue(long issueId, List<Label> labels) throws IOException {
		if (repoId != null) {
			return labelService.setLabels(repoId, Long.toString(issueId), labels);
		}
		return new ArrayList<>();
	}

	/**
	 * Adds list of labels to a github issue. Returns all the labels for the
	 * issue.
	 */
	public List<Label> addLabelsToIssue(int issueId, List<Label> labels) throws IOException {
		if (repoId != null) {
			return labelService.addLabelsToIssue(repoId, Integer.toString(issueId), labels);
		}
		return new ArrayList<>();
	}

	public void deleteLabelsFromIssue(int issueId, List<Label> labels) throws IOException {
		for (Label label : labels) {
			deleteLabelFromIssue(issueId, label);
		}
	}

	public void deleteLabelFromIssue(int issueId, Label label) throws IOException {
		if (repoId != null) {
			labelService.deleteLabelFromIssue(repoId, Integer.toString(issueId), label);
		}
	}

	public boolean setIssueMilestone(int issueId, Milestone milestone) throws IOException {
		if (repoId != null) {
			Issue result = issueService.setIssueMilestone(repoId, issueId, milestone);
			Milestone resMilestone = result.getMilestone();
			if (resMilestone == null) {
				return milestone == null;
			} else {
				return milestone.getNumber() == resMilestone.getNumber();
			}
		}
		return false;
	}

	public boolean setIssueAssignee(int issueId, User user) throws IOException {
		if (repoId != null) {
			Issue result = issueService.setIssueAssignee(repoId, issueId, user);
			User assignee = result.getAssignee();
			if (assignee == null) {
				return user == null;
			} else {
				return assignee.getLogin().equals(user.getLogin());
			}
		}
		return false;
	}

	private void ______COLLABORATORS______() {
	}

	public List<User> getCollaborators() throws IOException {
		if (repoId != null) {
			return collabService.getCollaborators(repoId);
		}
		return new ArrayList<>();
	}

	private void ______EVENTS______() {
	}

	/**
	 * Gets events for a issue from GitHub. This only includes information
	 * that GitHub exposes, such as milestones being added, labels being
	 * removed, etc. Events like comments being added must be gotten separately.
	 *
	 * @param issueId
	 * @return
	 * @throws IOException
	 */
	public List<TurboIssueEvent> getEvents(int issueId) throws IOException {
		assert repoId != null;
		return issueService.getIssueEvents(repoId, issueId).getTurboIssueEvents();
	}

	private void ______MARKDOWN______() {
	}

	public String getContentMarkup(final String text) throws IOException {
		if (text.contains("#")) {
			return getRepositoryHtml(text);
		}
		return new Markdown4jProcessor().process(text);
	}

	public String getRepositoryHtml(final String text) throws IOException {
		if (repoId != null) {
			return markdownService.getRepositoryHtml(repoId, text);
		} else {
			return "";
		}
	}

	public String getHtml(final String text, final String mode) throws IOException {
		return markdownService.getHtml(text, mode);
	}

	private void ______REPOSITORY_CONTENTS______() {
	}

	public List<RepositoryContents> getContents(IRepositoryIdProvider repository) throws IOException {
		return contentService.getContents(repository);
	}

	public List<RepositoryContents> getContents(IRepositoryIdProvider repository, String path) throws IOException {
		return contentService.getContents(repository, path);
	}

	private void ______COMMENTS______() {
	}

	public List<Comment> getLatestComments(int issueId) throws IOException {
		if (repoId != null) {
			return issueService.getComments(repoId, issueId);
		}
		return new ArrayList<>();
	}
}
