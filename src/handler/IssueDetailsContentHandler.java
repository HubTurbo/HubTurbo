package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
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

import org.eclipse.egit.github.core.Comment;

import service.ServiceManager;
import service.updateservice.CommentUpdateService;

public class IssueDetailsContentHandler {
	private TurboIssue issue;
	
	private ObservableList<Comment> allGhContent = FXCollections.observableArrayList();
	private ObservableList<TurboComment> comments = FXCollections.observableArrayList();
	private ObservableList<TurboComment> log = FXCollections.observableArrayList();
	
	HashMap<Long, String> commentsMarkup = new HashMap<>();
	
	private CommentUpdateService commentsUpdater;
	private ListChangeListener<Comment> commentsChangeListener;
	
	private HashSet<TurboComment> commentsInEditMode = new HashSet<TurboComment>();
	
	public IssueDetailsContentHandler(TurboIssue issue){
		this.issue = issue;
	}
	
	private boolean isNotSetup(){
		return commentsUpdater == null || commentsChangeListener == null;
	}
	
	private void getDetailsContent(){
		try {
			List<Comment> allItems = ServiceManager.getInstance().getComments(issue.getId());
			setGithubCommentsList(allItems);
			updateData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setGithubCommentsList(List<Comment> allItems){
		//Reuse allGhContent instance to ensure that all observers get change signals
		allGhContent.clear();
		allGhContent.addAll(allItems);
	}
	
	private void setupContent(){
		//Computationally intensive as list of all comments and their html markup will be retrieved from github
		getDetailsContent();
		setupContentUpdater();
		setupCommentsChangeListener();
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
		allGhContent.addListener(listener);
	}

	private void setupContentUpdater(){
		commentsUpdater = ServiceManager.getInstance().getCommentUpdateService(issue.getId(), allGhContent);
	}
	
	public ObservableList<TurboComment> getComments(){
		return comments;
	}
	
	public ObservableList<TurboComment> getIssueHistory(){
		return log;
	}
	
	/**
	 * Content Update Methods
	 * */
	
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
		List<TurboComment> logItems = allGhContent.stream()
										   .map(item -> new TurboComment(item))
				   						   .filter(item -> item.isIssueLog())
				   						   .collect(Collectors.toList());
		
		setObservedLog(logItems);
	}
	
	private void updateCommentsList(){
		List<TurboComment> filteredComments = allGhContent.stream()
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
	 * */
	
	public boolean createComment(String text){
		try {
			ServiceManager.getInstance().createComment(issue.getId(), text);			
			restartContentUpdate();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
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
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void deleteComment(TurboComment comment){
		try {
			stopContentUpdate();
			ServiceManager.getInstance().deleteComment(comment.getId());
			removeCachedComment(comment.getId());
			startContentUpdate();
		} catch (IOException e) {
			e.printStackTrace();
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
