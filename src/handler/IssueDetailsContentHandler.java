package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.stream.Collectors;

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
	
	private CommentUpdateService commentsUpdater;
	private ListChangeListener<Comment> commentsChangeListener;
	
	public IssueDetailsContentHandler(TurboIssue issue){
		this.issue = issue;
	}
	
	private boolean isNotSetup(){
		return commentsUpdater == null || commentsChangeListener == null;
	}
	
	private void setupContent(){
		getDetailsContent();
		commentsUpdater = ServiceManager.getInstance().getCommentUpdateService(issue.getId(), allGhContent);
		setupCommentsChangeListener();
	}
	
	public ObservableList<TurboComment> getComments(){
		return comments;
	}
	
	public ObservableList<TurboComment> getIssueHistory(){
		return log;
	}
	
	public void startContentUpdate(){
		if(isNotSetup()){
			setupContent();
		}
		commentsUpdater.startCommentsListUpdate();
	}
	
	public void stopContentUpdate(){
		commentsUpdater.stopCommentsListUpdate();
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
	
	private void updateData(){
		updateCommentsList();
		updateLogContents();
	}
	
	private void getDetailsContent(){
		try {
			List<Comment> allItems = ServiceManager.getInstance().getComments(issue.getId());
			allGhContent.addAll(allItems);
			updateData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateCommentsList(){
		List<TurboComment> filteredComments = allGhContent.stream()
												   .map(item -> new TurboComment(item))
												   .filter(item -> !item.isIssueLog())
												   .collect(Collectors.toList());
		for(int i = filteredComments.size() - 1; i >= 0; i--){
			updateItemInCommentsList(filteredComments.get(i));
		}
	}
	
	private void updateItemInCommentsList(TurboComment comment){
		for(TurboComment item : comments){
			if(item.getId() == comment.getId()){
				item.copyValues(comment);
				return;
			}
		}
		comments.add(0, comment);
	}
	
	private void updateLogContents(){
		List<TurboComment> logItems = allGhContent.stream()
										   .map(item -> new TurboComment(item))
				   						   .filter(item -> item.isIssueLog())
				   						   .collect(Collectors.toList());
		log.clear();
		log.addAll(logItems);
	}
}
