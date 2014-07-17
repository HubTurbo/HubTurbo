package ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class IssueDetailsDisplay extends VBox {
	private TabPane detailsTab;
	
	public IssueDetailsDisplay(){
		setupDisplay();
	}
	
	
	private void setupDetailsTab(){
		this.detailsTab = new TabPane();
		Tab commentsTab = createCommentsTab();
		Tab logTab = createChangeLogTab();
		detailsTab.getTabs().addAll(commentsTab, logTab);
//		detailsTab.setPrefWidth(DETAILS_WIDTH);
	}
	private void setupDisplay(){
		setupDetailsTab();
		this.getChildren().add(detailsTab);
	}
	
	private Tab createChangeLogTab(){
		Tab log = new Tab();
		log.setText("Log");
		log.setClosable(false);
		return log;
	}
	
	private Tab createCommentsTab(){
		Tab comments =  new Tab();
		comments.setText("C");
		comments.setClosable(false);
		return comments;
	}
	
}
