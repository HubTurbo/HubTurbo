package service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import model.Model;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.markdown4j.Markdown4jProcessor;

import service.updateservice.ModelUpdater;
import storage.DataCacheFileHandler;
import storage.TurboRepoData;
import stubs.ServiceManagerStub;
import ui.UI;
import ui.components.StatusBar;
import util.events.RefreshDoneEvent;

/**
 * Singleton class that provides access to the GitHub API services required by HubTurbo
 * 
 * Only data from a single repository can be loaded at any point of time. 
 * The currently loaded repository is stored in the application's ServiceManager instance
 * 
 * Also holds a reference to the application's current Model instance, which stores the repository's 
 * labels, milestones, assignees and issues.
 * */
public class ServiceManager {
	
	private static final ServiceManager serviceManagerInstance = new ServiceManager();
	public static final boolean isTestMode = false;
	
	public static ServiceManager getInstance(){
		if(!isTestMode){
			return serviceManagerInstance;
		}else{
			return new ServiceManagerStub();
		}
	}

	private static final Logger logger = LogManager.getLogger(ServiceManager.class.getName());
	
	protected static final String METHOD_PUT = "PUT";
	protected static final String METHOD_POST = "POST";
	
	public static final String KEY_ISSUES = "issues";
	public static final String KEY_MILESTONES = "milestones";
	public static final String KEY_LABELS = "labels";
	public static final String KEY_COLLABORATORS = "collaborators";
	public static final int MAX_FEED = 10;
	private GitHubClientExtended githubClient;
	
	private CollaboratorService collabService;
	private IssueServiceExtended issueService;
	private LabelServiceFixed labelService;
	private MilestoneService milestoneService;
	private RepositoryServiceExtended repositoryService;
	private MarkdownService markdownService;
	private ContentsService contentService;
	
	private ModelUpdater modelUpdater;
	private Model model;
	private RepositoryId repoId;
	private String issuesETag = null;
	private String collabsETag = null;
	private String labelsETag = null;
	private String milestonesETag = null;
	private String issueCheckTime = null;

	private static final int REFRESH_INTERVAL = 60;
	private final ScheduledExecutorService refreshExecutor = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> refreshResult;
	
	private static final int TICK_INTERVAL = 1;
	private final ScheduledExecutorService timeUntilRefreshExecutor = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> timeUntilRefreshResult;
	
	private int timeRemainingUntilRefresh = REFRESH_INTERVAL;
	
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";

	// Login state
	private String password;

	protected ServiceManager(){
		githubClient = new GitHubClientExtended();
		collabService = new CollaboratorService(githubClient);
		issueService = new IssueServiceExtended(githubClient);
		labelService = new LabelServiceFixed(githubClient);
		milestoneService = new MilestoneService(githubClient);
		repositoryService = new RepositoryServiceExtended(githubClient);
		markdownService = new MarkdownService(githubClient);
		contentService = new ContentsService(githubClient);
		model = new Model();
	}

	public IRepositoryIdProvider getRepoId(){
		return repoId;
	}
	
	public Model getModel(){
		return model;
	}
	
	public Date getLastModelUpdateTime(){
		if(modelUpdater != null){
			return modelUpdater.getLastUpdateTime();
		}
		return null;
	}
	
	public void setupAndStartModelUpdate() {
		if(repoId != null){
			modelUpdater = new ModelUpdater(githubClient, model, issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime);
			startModelUpdate();
		}
	}
	
	/**
	 * Starts the concurrent tasks which update the model.
	 */
	public void startModelUpdate(){
		
		// Ensure that model update isn't ongoing
		stopModelUpdate();

		// We get the repo id from the model now. On task completion, the
		// repo id may be different if the project was switched, so we
		// validate with this repo id at that point.
		
		final IRepositoryIdProvider repoId = RepositoryId.createFromId(model.getRepoId().generateId());
		
		Runnable pollTask = new Runnable() {
			@Override
			public void run() {
				modelUpdater.updateModel(repoId);
				UI.getInstance().triggerEvent(new RefreshDoneEvent());
			}
		};
		refreshResult = refreshExecutor.scheduleWithFixedDelay(pollTask, 0, REFRESH_INTERVAL, TimeUnit.SECONDS);
		
		Runnable countdown = new Runnable() {
			@Override
			public void run() {
				StatusBar.displayMessage("Next refresh in " + getTime());
			}
		};
		timeUntilRefreshResult = timeUntilRefreshExecutor.scheduleWithFixedDelay(countdown, 0, TICK_INTERVAL, TimeUnit.SECONDS);
	}
	
	/**
	 * Stops the concurrent tasks which update the model.
	 */
	public void stopModelUpdate() {
		
		// If the model update was never started, don't do anything
		if (refreshResult == null || refreshResult.isCancelled()) return;
		if (timeUntilRefreshResult == null || timeUntilRefreshResult.isCancelled()) return;
		
		refreshResult.cancel(true);
		timeUntilRefreshResult.cancel(true);
		timeRemainingUntilRefresh = REFRESH_INTERVAL;
		
		// Indicate that model update has been stopped
		refreshResult = null;
		timeUntilRefreshResult = null;
	}
	
	private int getTime() {
	    if (timeRemainingUntilRefresh == 1) {
	        timeRemainingUntilRefresh = REFRESH_INTERVAL;
	    } else {
	    	--timeRemainingUntilRefresh;
	    }
	    return timeRemainingUntilRefresh;
	}
	
	/**
	 * Called on application exit. Will be called only once.
	 */
	public void shutdownModelUpdate() {
		stopModelUpdate();
		refreshExecutor.shutdown();
		timeUntilRefreshExecutor.shutdown();
	}
	
	/**
	 * Helper function for restarting model update.
	 */
	public void restartModelUpdate(){
		stopModelUpdate();
		startModelUpdate();
	}
		
	public boolean login(String userId, String password){
		this.password = password;
		githubClient.setCredentials(userId, password);
		try {
			GitHubRequest request = new GitHubRequest();
			request.setUri("/");
			githubClient.get(request);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			// Login failed
			return false;
		}
		return true;
	}
	
	public String getUserId(){
		return githubClient.getUser();
	}
	
	public String getPassword(){
		assert password != null;
		return password;
	}

	public boolean setupRepository(String owner, String name) throws IOException{
		logger.info("Authenticating...");
		repoId = RepositoryId.create(owner, name);
		if(checkRepository(repoId)){
			return model.loadComponents(repoId);
		}else{
			throw new IOException("Cannot access repository"); //TODO: create specific exception for this
		}
		
	}
	
	public String getRepoOwner() {
		return repoId.getOwner();
	}
	
	public String getRepoName() {
		return repoId.getName();
	}
	
	public boolean checkRepository(String repo) throws IOException{
		String repoURL = SEGMENT_REPOS + "/" + repo;
		return check(repoURL);
	}
	
	public boolean checkRepository(IRepositoryIdProvider repo) throws IOException{
		return checkRepository(repo.generateId());
	}
	
	protected boolean check(String uri) throws IOException {
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
	
	public int getRemainingRequests(){
		return githubClient.getRemainingRequests();
	}
	
	public int getRequestLimit(){
		return githubClient.getRequestLimit();
	}
	
	/**
	 * Retrieves a data structure containing all resources of the given repository.
	 * May load from cache if available or download from GitHub.
	 * Should be run as a task.
	 * @param repoId the repository to get resources for
	 * @return a data structure containing all resoureces
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public HashMap<String, List> getResources(RepositoryId repoId) throws IOException {
		this.repoId = repoId;

		String repoIdString = repoId.toString();

		DataCacheFileHandler dcHandler = new DataCacheFileHandler(repoIdString);
		model.setDataCacheFileHandler(dcHandler);
		model.setRepoId(repoId);

		TurboRepoData cachedRepoData = dcHandler.getRepo();
		boolean needToGetResources = cachedRepoData == null;
		
		if (needToGetResources) {
			logger.info("Cache not found, loading data from GitHub...");
			return getGitHubResources();
		} else {
			logger.info("Loading from cache...");
			issuesETag = cachedRepoData.getIssuesETag();
			collabsETag = cachedRepoData.getCollaboratorsETag();
			labelsETag = cachedRepoData.getLabelsETag();
			milestonesETag = cachedRepoData.getMilestonesETag();
			issueCheckTime = cachedRepoData.getIssueCheckTime();
			List<TurboUser> collaborators = cachedRepoData.getCollaborators();
			List<TurboLabel> labels = cachedRepoData.getLabels();
			List<TurboMilestone> milestones = cachedRepoData.getMilestones();
			// Delay getting of issues until labels and milestones are loaded in Model
			
			HashMap<String, List> map = new HashMap<String, List>();
			map.put(KEY_COLLABORATORS, collaborators);
			map.put(KEY_LABELS, labels);
			map.put(KEY_MILESTONES, milestones);
			return map;
		}
	}

	@SuppressWarnings("rawtypes")
	public HashMap<String, List> getGitHubResources() throws IOException {
		issuesETag = null;
		collabsETag = null;
		labelsETag = null;
		milestonesETag = null;
		issueCheckTime = null;
		
		List<User> ghCollaborators = new ArrayList<User>();
		
		List<Label> ghLabels = getLabels();
		List<Milestone> ghMilestones = getMilestones();
		List<Issue> ghIssues = getAllIssues();
		
		HashMap<String, List> map = new HashMap<String, List>();
		map.put(KEY_COLLABORATORS, ghCollaborators);
		map.put(KEY_LABELS, ghLabels);
		map.put(KEY_MILESTONES, ghMilestones);
		map.put(KEY_ISSUES, ghIssues);
		return map;
	}
	
	/**
	 * Label Services
	 * */
	
	public List<Label> getLabels() throws IOException{
		if(repoId != null){
			return labelService.getLabels(repoId);
		}
		return new ArrayList<Label>();
	}
	
	public Label createLabel(Label ghLabel) throws IOException{
		if(repoId != null){
			return labelService.createLabel(repoId, ghLabel);
		}
		return null; //TODO:
	}
	
	public void deleteLabel(String label) throws IOException{
		if(repoId != null){
			labelService.deleteLabel(repoId, label);
		}
	}
	
	public Label editLabel(Label label , String name) throws IOException{
		if(repoId != null){
			return (Label)labelService.editLabel(repoId, label, name);
		}
		return null;
	}
	
	/**
	 * Milestone Services
	 * */
	public List<Milestone> getMilestones() throws IOException{
		if(repoId != null){
			return milestoneService.getMilestones(repoId, STATE_ALL);
		}
		return new ArrayList<Milestone>();
	}
	
	public Milestone createMilestone(Milestone milestone) throws IOException{
		if(repoId != null){
			return milestoneService.createMilestone(repoId, milestone);
		}
		return null;
	}
	
	public void deleteMilestone(int milestoneNum) throws IOException{
		if(repoId != null){
			milestoneService.deleteMilestone(repoId, milestoneNum);
		}
	}
	
	public Milestone editMilestone(Milestone milestone) throws IOException{
		if(repoId != null){
			return (Milestone)milestoneService.editMilestone(repoId, milestone);
		}
		return null;
	}
	
	
	/**
	 * Services for IssueEvent
	 * */
	public List<IssueEvent> getFeeds(int issueNum) throws IOException{
		ArrayList<IssueEvent> eventList = new ArrayList<IssueEvent>();
		String user = getRepoOwner();
		String repo = getRepoName();
		boolean toContinue = true;
		PageIterator<IssueEvent> iter = issueService.pageIssueEvents(user, repo, issueNum);
		if (iter != null && iter.hasNext()) {
			for (Collection<IssueEvent> currentPage : iter) {
				if (!currentPage.isEmpty()) {
					for (IssueEvent event : currentPage) {
						if (event != null) {
							IssueEvent fetched = issueService.getIssueEvent(user, repo, event.getId());
							if (fetched != null) {
								if (eventList.size() < MAX_FEED) { 
									eventList.add(fetched);
								} else {
									toContinue = false;
									break;
								}
							}
						}
					}
				}
				if (!toContinue) {
					break;
				}
			}
		}
		return eventList;
	}

	/**
	 * Collaborator Services 
	 * */
	
	public List<User> getCollaborators() throws IOException{
		if(repoId != null){
			return collabService.getCollaborators(repoId);
		}
		return new ArrayList<User>();
	}
	
	/**
	 * Issue Services
	 * */
	
	public List<Issue> getAllIssues() throws IOException{
		if(repoId != null){
			Map<String, String> filters = new HashMap<String, String>();
			filters.put(IssueService.FIELD_FILTER, STATE_ALL);
			filters.put(IssueService.FILTER_STATE, STATE_ALL);
			return issueService.getIssues(repoId, filters);
		}
		return new ArrayList<Issue>();
	}
	
	public Issue createIssue(Issue issue) throws IOException{
		if(repoId != null){
			return issueService.createIssue(repoId, issue);
		}
		return null;
	}
	
	public Issue getIssue(int issueId) throws IOException{
		if(repoId !=  null){
			return issueService.getIssue(repoId, issueId);
		}
		return null;
	}
	
	public HashMap<String, Object> getIssueData(int issueId) throws IOException{
		if(repoId != null){
			return issueService.getIssueData(repoId, issueId);
		}
		return new HashMap<String, Object>();
	}
	
	public String getDateFromIssueData(HashMap<String, Object> issueData){
		return (String)issueData.get(IssueServiceExtended.ISSUE_DATE);
	}
	
	public Issue getIssueFromIssueData(HashMap<String, Object> issueData){
		return (Issue)issueData.get(IssueServiceExtended.ISSUE_CONTENTS);
	}
	
	public Issue editIssue(Issue latest, String dateModified) throws IOException{
		if(repoId != null){
			return (Issue)issueService.editIssue(repoId, latest, dateModified);
		}
		return null;
	}
	
	public Issue editIssueTitle(int issueId, String title) throws IOException{
		if(repoId != null){
			return issueService.editIssueTitle(repoId, issueId, title);
		}
		return null;
	}
	
	public Issue editIssueBody(int issueId, String body) throws IOException{
		if(repoId != null){
			return issueService.editIssueBody(repoId, issueId, body);
		}
		return null;
	}
	
	public void closeIssue(int issueId) throws IOException{
		if(repoId != null){
			issueService.editIssueState(repoId, issueId, false);
		}
	}
	
	public void openIssue(int issueId) throws IOException{
		if(repoId != null){
			issueService.editIssueState(repoId, issueId, true);
		}
	}
	
	/**
	 * Gets events for a issue from GitHub, or returns
	 * a cached version if already present.
	 * @param issueId
	 * @return
	 * @throws IOException
	 */
	public ArrayList<TurboIssueEvent> getEvents(int issueId) throws IOException{
		if(repoId != null){
			return issueService.getIssueEvents(repoId, issueId).getTurboIssueEvents();
		}
		return new ArrayList<>();
	}
	
	/**
	 * Methods to work with issue labels
	 * */
	
	public List<Label> setLabelsForIssue(long issueId, List<Label> labels) throws IOException{
		if(repoId != null){
			return labelService.setLabels(repoId, Long.toString(issueId), labels);
		}
		return new ArrayList<Label>();
	}
	
	/**
	 * Adds list of labels to a github issue. Returns all the labels for the issue.
	 * */
	public List<Label> addLabelsToIssue(int issueId, List<Label> labels) throws IOException{
		if(repoId != null){
			return labelService.addLabelsToIssue(repoId, Integer.toString(issueId), labels);
		}
		return new ArrayList<Label>();
	}
	
	public void deleteLabelsFromIssue(int issueId, List<Label> labels) throws IOException{
		for(Label label : labels){
			deleteLabelFromIssue(issueId, label);
		}
	}
	
	public void deleteLabelFromIssue(int issueId, Label label) throws IOException{
		if(repoId != null){
			labelService.deleteLabelFromIssue(repoId, Integer.toString(issueId), label);
		}		
	}
	
	public boolean setIssueMilestone(int issueId, Milestone milestone) throws IOException{
		if(repoId != null){
			Issue result = issueService.setIssueMilestone(repoId, issueId, milestone);
			Milestone resMilestone = result.getMilestone();
			if(resMilestone == null){
				return milestone == null;
			}else{
				return milestone.getNumber() == resMilestone.getNumber();
			}
		}
		return false;
	}
	
	public boolean setIssueAssignee(int issueId, User user) throws IOException{
		if(repoId != null){
			Issue result = issueService.setIssueAssignee(repoId, issueId, user);
			User assignee = result.getAssignee();
			if(assignee == null){
				return user == null;
			}else{
				return assignee.getLogin().equals(user.getLogin());
			}
		}
		return false;
	}
	
	/**
	 * Get user repositories
	 * */
	
	/**
	 * Returns a list of the user's public repositories
	 * */
	public List<Repository> getRepositories() throws IOException{
		return repositoryService.getRepositories();
	}
	
	/**
	 * Returns a list of the names of the user's public repositories
	 * */
	public List<String> getRepositoriesNames() throws IOException{
		return repositoryService.getRepositoriesNames(getUserId());
	}
	
	/**
	 * Returns a list of the public repositories belonging to the user and the user's organisations
	 * */
	public List<Repository> getAllRepositories() throws IOException{
		return repositoryService.getAllRepositories(getUserId());
	}
	
	/**
	 * Returns a list of the names of the public repositories belonging to the user and the user's organisations
	 * */
	public List<String> getAllRepositoryNames() throws IOException{
		return repositoryService.getAllRepositoriesNames(getUserId());
	}
	
	/**
	 * Markdown service methods
	 * */
	public String getContentMarkup(final String text) throws IOException{
		if(text.contains("#")){
			return getRepositoryHtml(text);
		}
		return new Markdown4jProcessor().process(text);
	}
	
	public String getRepositoryHtml(final String text) throws IOException {
		if(repoId != null){
			return markdownService.getRepositoryHtml(repoId, text);
		}else{
			return "";
		}
	}
	
	public String getHtml(final String text, final String mode)
			throws IOException {
		return markdownService.getHtml(text, mode);
	}
	
	/**
	 * Contents service methods
	 * */
	public List<RepositoryContents> getContents(IRepositoryIdProvider repository)
			throws IOException {
		return contentService.getContents(repository);
	}
	public List<RepositoryContents> getContents(
			IRepositoryIdProvider repository, String path) throws IOException {
		return contentService.getContents(repository, path);
	}


}
