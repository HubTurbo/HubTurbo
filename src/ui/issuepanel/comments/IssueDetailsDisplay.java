package ui.issuepanel.comments;

import java.lang.ref.WeakReference;

import ui.StatusBar;
import handler.IssueDetailsContentHandler;
import model.TurboIssue;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class IssueDetailsDisplay extends VBox {
//	public enum DisplayType{
//		COMMENTS,
//		LOG
//	}
	
	private TabPane detailsTab;
	
	private IssueDetailsContentHandler contentHandler;
	
	private TurboIssue issue;
	
	private int loadFailCount = 0;
	
	DetailsPanel commentsDisplay;
	
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
		detailsTab.getTabs().add(commentsTab);
	}
	
	private void setupDisplay(){
		setupDetailsTab();
		this.getChildren().add(detailsTab);
	}
	
	public void show(){
		if(issue == null || issue.getId() <= 0){
			return;
		}
		loadIssueDetailsInBackground();
	}
	
	private ProgressIndicator createProgressIndicator(){
		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setPrefSize(50, 50);
		indicator.setMaxSize(50, 50);
		return indicator;
	}
	
	private void loadIssueDetailsInBackground(){
		Task<Boolean> bgTask = new Task<Boolean>(){

			@Override
			protected Boolean call() throws Exception {
				contentHandler.startContentUpdate();
				return true;
			}
			
		};
		
		ProgressIndicator indicator = createProgressIndicator();
		indicator.progressProperty().bind(bgTask.progressProperty());
		
		WeakReference<IssueDetailsDisplay> selfRef = new WeakReference<>(this);
		bgTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
            	IssueDetailsDisplay self = selfRef.get();
            	if(self != null){
            		self.hideProgressIndicator(indicator);
            		self.scrollDisplayToBottom();
            		self.loadFailCount = 0;
            	}
            }
        });

		bgTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
            	IssueDetailsDisplay self = selfRef.get();
            	if(self != null){
            		self.loadFailCount += 1;
            		if(loadFailCount <= 3){
            			contentHandler.stopContentUpdate();
            			self.show();
            		}else{
            			//Notify user of load failure and reset count
            			loadFailCount = 0;
            			StatusBar.displayMessage("An error occured while loading the issue's comments. Comments partially loaded");
            			self.hideProgressIndicator(indicator);
            		}
            	}
            }
        });
		
		displayProgressIndicator(indicator);
		backgroundThread = new Thread(bgTask);
		backgroundThread.start();
	}
	
	private void displayProgressIndicator(ProgressIndicator indicator){
		Platform.runLater(() -> {
			commentsDisplay.addItemToDisplay(indicator);
		});
	}
	
	private void hideProgressIndicator(ProgressIndicator indicator){
		Platform.runLater(() -> {
			commentsDisplay.removeItemFromDisplay(indicator);
		});
	}
	
	private void scrollDisplayToBottom(){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				commentsDisplay.scrollToBottom();
			}
		});	
	}
	
	public void hide(){
		contentHandler.stopContentUpdate();
	}
	
	public void cleanup(){
		contentHandler.stopContentUpdate();
	}
	
	public void refresh(){
		contentHandler.restartContentUpdate();
	}
	
	private DetailsPanel createTabContentsDisplay(){
		return new DetailsPanel(issue, contentHandler);
	}
	
	private Tab createCommentsTab(){
		Tab comments =  new Tab();
		comments.setText("Comments");
		comments.setClosable(false);
		commentsDisplay = createTabContentsDisplay();
		VBox.setVgrow(commentsDisplay, Priority.ALWAYS);
		comments.setContent(commentsDisplay);
		return comments;
	}
	
}
