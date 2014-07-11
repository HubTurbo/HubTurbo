package service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Model;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.IGitHubConstants;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;

import service.updateservice.ModelUpdater;

public class ServiceManager {
	protected static final String METHOD_PUT = "PUT";
	protected static final String METHOD_POST = "POST";
	public static final String CHANGELOG_TAG = "[Change Log]\n";
	
	private static final ServiceManager serviceManagerInstance = new ServiceManager();
	private GitHubClientExtended githubClient;
	
	private CollaboratorService collabService;
	private IssueServiceExtended issueService;
	private LabelServiceFixed labelService;
	private MilestoneService milestoneService;
	
	private ModelUpdater modelUpdater;
	private Model model;
	private IRepositoryIdProvider repoId;
	
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";
	private int bufferSize = 8192;
	
	private ServiceManager(){
		githubClient = new GitHubClientExtended();
		collabService = new CollaboratorService(githubClient);
		issueService = new IssueServiceExtended(githubClient);
		labelService = new LabelServiceFixed(githubClient);
		milestoneService = new MilestoneService(githubClient);
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
			modelUpdater = new ModelUpdater(githubClient, model);
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
		return serviceManagerInstance;
	}
	
	public boolean login(String userId, String password){
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
	
	public void setupRepository(String owner, String name){
		repoId = RepositoryId.create(owner, name);
		//TODO:
		model.loadComponents(repoId);
		setupAndStartModelUpdate();
	}
	
	public int getRemainingRequests(){
		return githubClient.getRemainingRequests();
	}
	
	public int getRequestLimit(){
		return githubClient.getRequestLimit();
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
	
	public void closeIssue(int issueId) throws IOException{
		String statusChangePath = repoId.generateId() + "issue_comments";
		HttpURLConnection request = githubClient.createGitHubConnection(statusChangePath, METHOD_POST);
		byte[] data = createIssueStatusOpenJsonString(issueId).getBytes(IGitHubConstants.CHARSET_UTF8);
		sendData(request, data);
	}
	
	public void openIssue(int issueId) throws IOException{
		String statusChangePath = repoId.generateId() + "issue_comments";
		HttpURLConnection request = githubClient.createGitHubConnection(statusChangePath, METHOD_POST);
		byte[] data = createIssueStatusCloseJsonString(issueId).getBytes(IGitHubConstants.CHARSET_UTF8);
		sendData(request, data);
	}
	
	/**
	 * Methods to work with comments data from github
	 * */
	
	public void logIssueChanges(int issueId, String changes){
		String changeLog = CHANGELOG_TAG + changes;
		try {
			createComment(issueId, changeLog);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Comment createComment(int issueId, String comment) throws IOException{
		if(repoId != null){
			return (Comment)issueService.createComment(repoId, Integer.toString(issueId), comment);
		}
		return null;
	}
	
	public List<Comment> getComments(int issueId) throws IOException{
		if(repoId != null){
			return issueService.getComments(repoId, issueId);
		}
		return new ArrayList<Comment>();
	}
	
	public void deleteComment(int commentId) throws IOException{
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
	
	public List<Label> setLabelsForIssue(int issueId, List<Label> labels) throws IOException{
		return labelService.setLabels(repoId, Integer.toString(issueId), labels);
	}
	
	/**
	 * Adds list of labels to a github issue. Returns all the labels for the issue.
	 * */
	public List<Label> addLabelsToIssue(int issueId, List<Label> labels) throws IOException{
		return labelService.addLabelsToIssue(repoId, Integer.toString(issueId), labels);
	}
	
	public void deleteLabelFromIssue(int issueId, Label label) throws IOException{
		labelService.deleteLabelFromIssue(repoId, Integer.toString(issueId), label);
	}
	
	/**
	 * Methods to work with issue milestones
	 **/
	
	public void addMilestoneToIssue(int issueId, Milestone milestone) throws IOException{
		String addMilestonePath = repoId.generateId() + "issues/milestones";
		HttpURLConnection request = githubClient.createGitHubConnection(addMilestonePath, METHOD_PUT);
		byte[] data = createMilestoneAddJsonString(issueId, milestone).getBytes(IGitHubConstants.CHARSET_UTF8);
		sendData(request, data);
	}
	
	public void clearMilestoneFromIssue(int issueId) throws IOException{
		String milestonePath = repoId.generateId() + "issues/milestones";
		HttpURLConnection request = githubClient.createGitHubConnection(milestonePath, METHOD_PUT);
		byte[] data = createMilestoneClearJsonString(issueId).getBytes(IGitHubConstants.CHARSET_UTF8);
		sendData(request, data);
	}
	
	public void addAssigneeToIssue(int issueId, User user) throws IOException{
		String assigneePath = repoId.generateId() + "/issues/" + issueId;
		HttpURLConnection request = githubClient.createGitHubConnection(assigneePath, METHOD_PUT);
		byte[] data = createAssigneeAddJsonStrong(issueId, user).getBytes(IGitHubConstants.CHARSET_UTF8);
		sendData(request, data);
	}
	
	public void clearAssigneeFromIssue(int issueId) throws IOException{
		addAssigneeToIssue(issueId, null);
	}
	
	/**
	 * Private service methods
	 * */
	private void sendData(HttpURLConnection request, byte[] data) throws IOException{
		writeDataToGithubServer(request, data);
		final int code = request.getResponseCode();
		if(githubClient.isError(code)){
			throw githubClient.createException(githubClient.getStream(request), code,
					request.getResponseMessage());
		}
	}
	
	private void writeDataToGithubServer(HttpURLConnection request, byte[] data) throws IOException{
		request.setDoInput(true);
		request.setFixedLengthStreamingMode(data.length);
		BufferedOutputStream output = new BufferedOutputStream(
				request.getOutputStream(), bufferSize);
		try {
			output.write(data);
			output.flush();
		} finally {
			try {
				output.close();
			} catch (IOException e) {
			}
		}
	}
	
	private String createAssigneeAddJsonStrong(int issueId, User user){
		String jsonString = "{\"issue[assignee]\":\"%1d\"}";
		if(user == null){
			String.format(jsonString, "");
		}
		return String.format(jsonString, user.getLogin());
	}
	
	private String createMilestoneAddJsonString(int issueId, Milestone milestone){
		String jsonString = "{"
				+ "\"issues[]\":\"%1d\","
				+ "\"milestone\":\"%2d\""
				+ "}";
		return String.format(jsonString, issueId, milestone.getNumber());
	}
	private String createMilestoneClearJsonString(int issueId){
		String jsonString = "{"
				+ "\"issues[]\":\"%1d\","
				+ "\"milestone\":\"clear\""
				+ "}";
		return String.format(jsonString, issueId);
	}
	
	private String createIssueStatusOpenJsonString(int issueId){
		String jsonString = "{"
				+ "\"comment_and_open\":\"1\","
				+ "\"issue\":\"%1d\","
				+"\"comment[body]\":\"\"}";
		return String.format(jsonString, issueId);
	}
	private String createIssueStatusCloseJsonString(int issueId){
		String jsonString = "{"
				+ "\"comment_and_close\":\"1\","
				+ "\"issue\":\"%1d\","
				+"\"comment[body]\":\"\"}";
		return String.format(jsonString, issueId);
	}
}
