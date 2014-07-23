package ui.issuepanel.comments;

import java.lang.ref.WeakReference;

import util.DialogMessage;
import handler.IssueDetailsContentHandler;
import model.TurboIssue;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
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
	
	Thread backgroundThread;
	
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
		if(issue == null || issue.getId() <= 0){
			return;
		}
		
		Task<Boolean> bgTask = new Task<Boolean>(){

			@Override
			protected Boolean call() throws Exception {
				contentHandler.startContentUpdate();
				return true;
			}
			
		};	
		WeakReference<IssueDetailsDisplay> selfRef = new WeakReference<>(this);
		bgTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
            	IssueDetailsDisplay self = selfRef.get();
            	if(self != null){
            		self.scrollDisplayToBottom();
            	}
            }
        });
		DialogMessage.showProgressDialog(bgTask, "Loading Issue Comments...");
		backgroundThread = new Thread(bgTask);
		backgroundThread.start();
	}
	
	private void scrollDisplayToBottom(){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				commentsDisplay.scrollToBottom();
        		issueLogDisplay.scrollToBottom();
			}
		});	
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
		VBox.setVgrow(commentsDisplay, Priority.ALWAYS);
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
