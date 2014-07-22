package ui.issuepanel.comments;

import util.DialogMessage;
import handler.IssueDetailsContentHandler;
import model.TurboIssue;
import javafx.concurrent.Task;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class IssueDetailsDisplay extends VBox {
	public enum DisplayType{
		COMMENTS,
		LOG
	}
	
	private TabPane detailsTab;
	
	private IssueDetailsContentHandler contentHandler;
	
	private TurboIssue issue;
	
	DetailsPanel commentsDisplay;
	DetailsPanel issueLogDisplay;
	
	public IssueDetailsDisplay(TurboIssue issue){
		this.issue = issue;
		setupDetailsContents();
		setupDisplay();
	}
	
	private void setupDetailsContents(){
		contentHandler = new IssueDetailsContentHandler(issue);
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
	
	public void show(){
		Task<Boolean> bgTask = new Task<Boolean>(){

			@Override
			protected Boolean call() throws Exception {
				contentHandler.startContentUpdate();
//				commentsDisplay.scrollToBottom();
//				issueLogDisplay.scrollToBottom();
				return true;
			}
			
		};
		DialogMessage.showProgressDialog(bgTask, "Loading Issue Comments...");
		new Thread(bgTask).start();
	}
	
	public void hide(){
		contentHandler.stopContentUpdate();
	}
	
	public void cleanup(){
		contentHandler.stopContentUpdate();
	}
	
	private DetailsPanel createTabContentsDisplay(DisplayType type){
		return new DetailsPanel(issue, contentHandler, type);
	}
	
	private Tab createCommentsTab(){
		Tab comments =  new Tab();
		comments.setText("C");
		comments.setClosable(false);
		commentsDisplay = createTabContentsDisplay(DisplayType.COMMENTS);
		comments.setContent(commentsDisplay);
		return comments;
	}
	
	private Tab createChangeLogTab(){
		Tab log = new Tab();
		log.setText("Log");
		log.setClosable(false);
		issueLogDisplay = createTabContentsDisplay(DisplayType.LOG);
		log.setContent(issueLogDisplay);
		return log;
	}
	
}
