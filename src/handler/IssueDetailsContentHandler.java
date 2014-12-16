package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import model.TurboComment;
import model.TurboIssue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;

import service.ServiceManager;
import service.TurboIssueEvent;
import service.updateservice.CommentUpdateService;
import util.DialogMessage;

public class IssueDetailsContentHandler {
	private static final Logger logger = LogManager.getLogger(IssueDetailsContentHandler.class.getName());
	private TurboIssue issue;
	
	private ObservableList<Comment> ghCommentsAndLogs = FXCollections.observableArrayList();
	private ObservableList<TurboIssueEvent> ghEvents = FXCollections.observableArrayList();
	private ObservableList<TurboComment> comments = FXCollections.observableArrayList();
	private ObservableList<TurboComment> log = FXCollections.observableArrayList();
	
	HashMap<Long, String> commentsMarkup = new HashMap<>();
	
	private CommentUpdateService commentsUpdater;
	private ListChangeListener<Comment> commentsChangeListener;
	
	private HashSet<TurboComment> commentsInEditMode = new HashSet<TurboComment>();
	
	public IssueDetailsContentHandler(TurboIssue issue){
		this.issue = issue;
	}
	
	/**
	 * Synchronously gets HTML markup for all comments associated with an issue.
	 * Caches in memory when possible.
	 * 
	 * Also sets up a polling service to update that issue's comments, and the UI
	 * listeners necessary to trigger changes upon the comments being updated.
	 */
	private void setupContent() {
		getDetailsContent();
		setupContentUpdater();
		setupCommentsChangeListener();
	}
	
	private boolean isNotSetup(){
		return commentsUpdater == null || commentsChangeListener == null;
	}

	private void getDetailsContent(){
		try {
			// Get comments from GitHub
			List<Comment> comments = ServiceManager.getInstance().getComments(issue.getId());
			// Get events
			List<TurboIssueEvent> events = ServiceManager.getInstance().getEvents(issue.getId());
			
			// Update UI
			setGithubCommentsList(comments);
			setGithubEventsList(events);
			updateData();
		} catch (SocketTimeoutException | UnknownHostException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Internet Connection Timeout", 
						"Timeout while loading comments from GitHub, please check your internet connection");
			});
		}catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	private void setGithubCommentsList(List<Comment> comments){
		//Reuse allGhContent instance to ensure that all observers get change signals
		ghCommentsAndLogs.clear();
		ghCommentsAndLogs.addAll(comments);
	}

	// There is no change listener set up for events at the moment
	private void setGithubEventsList(List<TurboIssueEvent> events){
		ghEvents.clear();
		ghEvents.addAll(events);
	}

	private void setupCommentsChangeListener(){
		WeakReference<IssueDetailsContentHandler> selfRef = new WeakReference<>(this);
		commentsChangeListener = new ListChangeListener<Comment>(){

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Comment> arg0) {
				IssueDetailsContentHandler self = selfRef.get();
				if(self != null){
					self.updateData();
				}
			}
		};
		WeakListChangeListener<Comment> listener = new WeakListChangeListener<>(commentsChangeListener);
		ghCommentsAndLogs.addListener(listener);
	}

	private void setupContentUpdater(){
		commentsUpdater = ServiceManager.getInstance().getCommentUpdateService(issue.getId(), ghCommentsAndLogs);
	}
	
	public ObservableList<TurboComment> getComments(){
		return comments;
	}
	
	public ObservableList<TurboComment> getLogComments(){
		return log;
	}
	
	public ObservableList<TurboIssueEvent> getEvents(){
		return FXCollections.observableArrayList(ghEvents);
	}

	/**
	 * Content Update Methods
	 **/
	
	public void startContentUpdate(){
		if(isNotSetup()){
			setupContent();
		}
		if(commentsUpdater != null){
			commentsUpdater.startCommentsListUpdate();			
		}
	}
	
	public void stopContentUpdate(){
		if(commentsUpdater != null){
			commentsUpdater.stopCommentsListUpdate();			
		}
	}
	
	public void restartContentUpdate(){
		stopContentUpdate();
		startContentUpdate();
	}
	
	private void updateData(){
		updateCommentsList();
		updateLogContents();
	}
	
	private void updateLogContents(){
		List<TurboComment> logItems = ghCommentsAndLogs.stream()
										   .map(item -> new TurboComment(item))
				   						   .filter(item -> item.isIssueLog())
				   						   .collect(Collectors.toList());
		
		setObservedLog(logItems);
	}
	
	private void updateCommentsList(){
		List<TurboComment> filteredComments = ghCommentsAndLogs.stream()
												   .map(item -> new TurboComment(item))
												   .collect(Collectors.toList());
		for(TurboComment item : filteredComments){
			updateItemInCommentsList(item);
		}
		Platform.runLater(() -> {removeDifference(comments, filteredComments);});
	}
	
	/**
	 * Methods to track whether comment is in edit mode
	 * */
	
	public void toggleCommentEditState(TurboComment comment){
		if(commentIsInEditState(comment)){
			commentsInEditMode.remove(comment);
		}else{
			commentsInEditMode.add(comment);
		}
	}
	
	public void setCommentEditStateFalse(TurboComment comment){
		commentsInEditMode.remove(comment);
	}
	
	public boolean commentIsInEditState(TurboComment comment){
		return commentsInEditMode.contains(comment);
	}
	
	/**
	 * Methods to create/edit/delete comment
	 **/
	
	public boolean createComment(String text){
		try {
			ServiceManager.getInstance().createComment(issue.getId(), text);			
			restartContentUpdate();
			return true;
		} catch (SocketTimeoutException | UnknownHostException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Internet Connection Timeout", 
						"Timeout while adding comment to issue in GitHub, please check your internet connection.");
			});
			return false;
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}
	
	public boolean editComment(TurboComment comment){
		try {
			stopContentUpdate();
			updateItemInCommentsList(comment);
			Comment ghComment = comment.toGhComment();
			ServiceManager.getInstance().editComment(ghComment);
			startContentUpdate();
			return true;
		} catch (SocketTimeoutException | UnknownHostException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Internet Connection Timeout", 
						"Timeout while editing comment in GitHub, please check your internet connection.");
			});
			return false;
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}
	
	public void deleteComment(TurboComment comment){
		try {
			stopContentUpdate();
			ServiceManager.getInstance().deleteComment(comment.getId());
			removeCachedComment(comment.getId());
			startContentUpdate();
		} catch (SocketTimeoutException | UnknownHostException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Internet Connection Timeout", 
						"Timeout while deleting comment to issue in GitHub, please check your internet connection.");
			});
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	

	
	private void removeDifference(List<TurboComment> storedList, List<TurboComment> fetchedList){
		List<TurboComment> removed = storedList.stream()
											   .filter(item -> !fetchedList.contains(item))
											   .collect(Collectors.toList());
		storedList.removeAll(removed);
	}
	
	private void setObservedLog(List<TurboComment> logItems){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				log.clear();
				log.addAll(logItems);
			}
		});
	}
	
	private void removeCachedComment(long commentId){
		Platform.runLater(() -> {
			for(TurboComment item: comments){
				if(item.getId() == commentId){
					comments.remove(item);
					return;
				}
			}
		});
	}
	
	private void updateItemInCommentsList(TurboComment comment){
		Platform.runLater(()->{
			for(TurboComment item : comments){
				if(item.getId() == comment.getId()){
					item.copyValues(comment);
					return;
				}
			}
			comments.add(comment);
		});
	}
	
	@Override
	public void finalize(){
		commentsUpdater.stopCommentsListUpdate();
	}
}
