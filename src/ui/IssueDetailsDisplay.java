package ui;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import model.TurboIssue;

import org.eclipse.egit.github.core.Comment;

import service.ServiceManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class IssueDetailsDisplay extends VBox {
	public enum DisplayType{
		COMMENTS,
		LOG
	}
	
	private TabPane detailsTab;
	
	TurboIssue issue;
	private ObservableList<Comment> allContent = FXCollections.observableArrayList();
	private ObservableList<Comment> comments = FXCollections.observableArrayList();
	private ObservableList<Comment> log = FXCollections.observableArrayList();
	
	public IssueDetailsDisplay(TurboIssue issue){
		this.issue = issue;

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
	
	private void setup(){
		
	}
	
	private void getDetailsContent(){
		try {
			List<Comment> allItems = ServiceManager.getInstance().getComments(issue.getId());
			allContent.addAll(allItems);
			updateCommentsList();
			updateLogContents();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isIssueLog(Comment comment){
		String text = comment.getBody();
		return text.startsWith(ServiceManager.CHANGELOG_TAG);
	}
	
	private void updateCommentsList(){
		List<Comment> filteredComments = allContent.stream()
												   .filter(item -> !isIssueLog(item))
												   .collect(Collectors.toList());
		for(int i = filteredComments.size() - 1; i >= 0; i--){
			updateItemInCommentsList(filteredComments.get(i));
		}
	}
	
	private void updateItemInCommentsList(Comment comment){
		for(Comment item : comments){
			if(item.getId() == comment.getId()){
				//TODO:
				return;
			}
		}
		comments.add(0, comment);
	}
	
	private void updateLogContents(){
		List<Comment> logItems = allContent.stream()
				   						   .filter(item -> isIssueLog(item))
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
