package service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.Model;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;
import storage.DataCacheFileHandler;
import storage.TurboRepoData;

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
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.markdown4j.Markdown4jProcessor;

import service.updateservice.CommentUpdateService;
import service.updateservice.ModelUpdater;
import stubs.ServiceManagerStub;
import ui.StatusBar;

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
	private static final Logger logger = LogManager.getLogger(ServiceManager.class.getName());
	public static final boolean isTestMode = false;
	
	protected static final String METHOD_PUT = "PUT";
	protected static final String METHOD_POST = "POST";
	public static final String CHANGELOG_TAG = "[Change Log]";
	
	public static final String KEY_ISSUES = "issues";
	public static final String KEY_MILESTONES = "milestones";
	public static final String KEY_LABELS = "labels";
	public static final String KEY_COLLABORATORS = "collaborators";
	
	private static final ServiceManager serviceManagerInstance = new ServiceManager();
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
	private IRepositoryIdProvider repoId;
	private String issuesETag = null;
	private String collabsETag = null;
	private String labelsETag = null;
	private String milestonesETag = null;
	
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";

	// Login state
	private String password;
	private String repoOwner;
	private String repoName;

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
		if(modelUpdater != null){
			stopModelUpdate();
		}
		if(repoId != null){
			modelUpdater = new ModelUpdater(githubClient, model, issuesETag, collabsETag, labelsETag, milestonesETag);
			modelUpdater.startModelUpdate();
		}
	}
	
	public void restartModelUpdate(){
		if(modelUpdater != null){
			modelUpdater.stopModelUpdate();
			modelUpdater.startModelUpdate();
		}
	}
	
	public void stopModelUpdate(){
		if(modelUpdater !=  null){
			modelUpdater.stopModelUpdate();
		}
	}
	
	public static ServiceManager getInstance(){
		if(!isTestMode){
			return serviceManagerInstance;
		}else{
			return new ServiceManagerStub();
		}
	}
	
	public boolean login(String userId, String password){
		this.password = password;
		githubClient.setCredentials(userId, password);
		try {
			GitHubRequest request = new GitHubRequest();
			request.setUri("/");
			githubClient.get(request);
		} catch (IOException e) {
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
		repoOwner = owner;
		repoName = name;
		StatusBar.displayMessage("Authenticating...");
		repoId = RepositoryId.create(owner, name);
		if(checkRepository(repoId)){
			return model.loadComponents(repoId);
		}else{
			throw new IOException("Cannot access repository"); //TODO: create specific exception for this
		}
		
	}
	
	public String getRepoOwner() {
		return repoOwner;
	}
	
	public String getRepoName() {
		return repoName;
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
			if (e.getStatus() == HttpURLConnection.HTTP_NOT_FOUND)
				return false;
			throw e;
		}
	}
	
	public int getRemainingRequests(){
		return githubClient.getRemainingRequests();
	}
	
	public int getRequestLimit(){
		return githubClient.getRequestLimit();
	}
	
	@SuppressWarnings("rawtypes")
	public HashMap<String, List> getGitHubResources(IRepositoryIdProvider repoId) throws IOException {
		this.repoId = repoId;
		model.setRepoId(repoId);
		
		boolean needToGetResources = true;
		String repoIdString = repoId.toString();

		DataCacheFileHandler dcHandler = DataCacheFileHandler.getInstance();
		TurboRepoData repo = dcHandler.getRepoGivenId(repoIdString);
		if (repo != null) {
			needToGetResources = false;
		}

		if (!needToGetResources) {
			System.out.println("Loading from cache...");
			issuesETag = repo.getIssuesETag();
			collabsETag = repo.getCollaboratorsETag();
			labelsETag = repo.getLabelsETag();
			milestonesETag = repo.getMilestonesETag();
			List<TurboUser> collaborators = repo.getCollaborators();
			List<TurboLabel> labels = repo.getLabels();
			List<TurboMilestone> milestones = repo.getMilestones();
			// Delay getting of issues until labels and milestones are loaded in Model
			
			HashMap<String, List> map = new HashMap<String, List>();
			map.put(KEY_COLLABORATORS, collaborators);
			map.put(KEY_LABELS, labels);
			map.put(KEY_MILESTONES, milestones);
			return map;
		} else {
			// get resources
			List<User> ghCollaborators = null;
			List<Label> ghLabels = null;
			List<Milestone> ghMilestones = null;
			List<Issue> ghIssues = null;
			
			ghCollaborators = getCollaborators();
			ghLabels = getLabels();
			ghMilestones = getMilestones();
			ghIssues = getAllIssues();
			
			HashMap<String, List> map = new HashMap<String, List>();
			map.put(KEY_COLLABORATORS, ghCollaborators);
			map.put(KEY_LABELS, ghLabels);
			map.put(KEY_MILESTONES, ghMilestones);
			map.put(KEY_ISSUES, ghIssues);
			return map;
		}
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
	 * Methods to work with comments data from github
	 * */
	
	public void logIssueChanges(int issueId, String changes){
		String changeLog = CHANGELOG_TAG + "\n" + changes;
		try {
			createComment(issueId, changeLog);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public CommentUpdateService getCommentUpdateService(int id, List<Comment> list){
		return new CommentUpdateService(githubClient, id, list);
	}
	
	public Comment createComment(int issueId, String comment) throws IOException{
		if(repoId != null){
			return (Comment)issueService.createComment(repoId, Integer.toString(issueId), comment);
		}
		return null;
	}
	
	public String getMarkupForComment(Comment comment){
		try {
			return getContentMarkup(comment.getBody());
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			return comment.getBody();
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
	 * Gets comments for a issue from GitHub, or returns
	 * a cached version if already present.
	 * @param issueId
	 * @return
	 * @throws IOException
	 */
	public List<Comment> getComments(int issueId) throws IOException{
		if(repoId != null){
			List<Comment> cached = model.getCommentsListForIssue(issueId);
			if(cached == null){
				return getLatestComments(issueId);
			}else{
				return cached;
			}
		}
		return new ArrayList<Comment>();
	}
	
	/**
	 * Gets comments from an issue from GitHub and updates the cache.
	 * @param issueId
	 * @return
	 * @throws IOException
	 */
	private List<Comment> getLatestComments(int issueId) throws IOException{
		if(repoId != null){
			List<Comment> comments = issueService.getComments(repoId, issueId);
			List<Comment> list =  comments.stream()
						   				  .map(c -> {
						   					  			c.setBodyHtml(getMarkupForComment(c));
						   					  			return c;})
						   				  .collect(Collectors.toList());
			model.cacheCommentsListForIssue(list, issueId);
			return list;
		}
		return new ArrayList<Comment>();
	}
	
	public void deleteComment(long commentId) throws IOException{
		if(repoId != null){
			issueService.deleteComment(repoId, commentId);
		}
	}
	
	public Comment editComment(Comment comment) throws IOException{
		if(repoId != null){
			return issueService.editComment(repoId, comment);
		}
		return null;
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
