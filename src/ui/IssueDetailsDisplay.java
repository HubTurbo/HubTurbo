package ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.stream.Collectors;

import model.TurboComment;
import model.TurboIssue;

import org.eclipse.egit.github.core.Comment;

import service.ServiceManager;
import service.updateservice.CommentUpdateService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class IssueDetailsDisplay extends VBox {
	public enum DisplayType{
		COMMENTS,
		LOG
	}
	
	private TabPane detailsTab;
	
	private TurboIssue issue;
	
	private ObservableList<Comment> allGhContent = FXCollections.observableArrayList();
	private ObservableList<TurboComment> comments = FXCollections.observableArrayList();
	private ObservableList<TurboComment> log = FXCollections.observableArrayList();
	
	private ListChangeListener<Comment> commentsChangeListener;
	private CommentUpdateService commentsUpdater;
	
	public IssueDetailsDisplay(TurboIssue issue){
		this.issue = issue;
		setupContent();
		setupDisplay();
	}
	
	
	private void setupDetailsTab(){
		this.detailsTab = new TabPane();
		Tab commentsTab = createCommentsTab();
		Tab logTab = createChangeLogTab();
		detailsTab.getTabs().addAll(commentsTab, logTab);
	}
	
	private void setupDisplay(){
		setupDetailsTab();
		this.getChildren().add(detailsTab);
	}
	
	private void setupContent(){
		getDetailsContent();
		commentsUpdater = ServiceManager.getInstance().getCommentUpdateService(issue.getId(), allGhContent);
		//TODO:
		setupCommentsChangeListener();
	}
	
	private void setupCommentsChangeListener(){
		WeakReference<IssueDetailsDisplay> selfRef = new WeakReference<>(this);
		commentsChangeListener = new ListChangeListener<Comment>(){

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Comment> arg0) {
				IssueDetailsDisplay self = selfRef.get();
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
	
	private DetailsPanel createTabContentsDisplay(DisplayType type){
		if(type == DisplayType.COMMENTS){
			return new DetailsPanel(issue, comments, type);
		}else{
			return new DetailsPanel(issue, log, type);
		}
	}
	
	private Tab createCommentsTab(){
		Tab comments =  new Tab();
		comments.setText("C");
		comments.setClosable(false);
		comments.setContent(createTabContentsDisplay(DisplayType.COMMENTS));
		return comments;
	}
	
	private Tab createChangeLogTab(){
		Tab log = new Tab();
		log.setText("Log");
		log.setClosable(false);
		log.setContent(createTabContentsDisplay(DisplayType.LOG));
		return log;
	}
	
}
