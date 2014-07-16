package ui;

import java.util.concurrent.CompletableFuture;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class SidePanel extends VBox {
	protected static final int PANEL_PREF_WIDTH = 300;

	public enum Layout {
		TABS, ISSUE, HISTORY
	}

	private Layout layout;
	private Stage parentStage;
	private Model model;
	private ColumnControl columns = null;
	
	public SidePanel(Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.model = model;
		setLayout(Layout.TABS);
	}

	// Needed due to a circular dependency with ColumnControl
	public void setColumns(ColumnControl columns) {
		this.columns = columns;
	}
	
	public Layout getLayout() {
		return layout;
	}

	private void setLayout(Layout layout) {
		this.layout = layout;
		changeLayout();
	}

	public void refresh() {
		changeLayout();
	}
	
	private TurboIssue displayedIssue;
	private CompletableFuture<String> response = null;
	
	public CompletableFuture<String> displayIssue(TurboIssue issue) {
		response = null; // Make sure previous response doesn't remain
		displayedIssue = issue;
		setLayout(Layout.ISSUE); // This method sets this.response
		assert response != null;
		return response;
	}
	
	public void displayTabs() {
		setLayout(Layout.TABS);
	}
	
	private void changeLayout() {
		getChildren().clear();
		switch (layout) {
		case TABS:
			getChildren().add(tabLayout());
			break;
		case HISTORY:
			getChildren().add(historyLayout());
			break;
		case ISSUE:
			getChildren().add(issueLayout());
			break;
		default:
			assert false;
			break;
		}
	}

	private Node tabLayout() {
		
		VBox everything = new VBox();
		
		TabPane tabs = new TabPane();
		Tab labelsTab = createLabelsTab();
		Tab milestonesTab = createMilestonesTab();
		Tab assigneesTab = createAssgineesTab();
		Tab feedTab = createFeedTab();
		
		tabs.getTabs().addAll(labelsTab, milestonesTab, assigneesTab, feedTab);
		
		HBox repoFields = new HBox();
		
		everything.getChildren().addAll(repoFields, tabs);
		
		everything.setPrefWidth(PANEL_PREF_WIDTH);
		
		return everything;
	}

	private Tab createFeedTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("Feed");
		return tab;
	}

	private Tab createAssgineesTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("A");
		tab.setContent(new AssigneeManagementComponent(model).initialise());
		return tab;
	}

	private Tab createMilestonesTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("M");
		tab.setContent(new MilestoneManagementComponent(parentStage, model).initialise());
		return tab;
	}

	private Tab createLabelsTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("L");
		tab.setContent(new LabelManagementComponent(parentStage, model).initialise());
		return tab;
	}

	private Node historyLayout() {
		return new VBox();
	}

	private Node issueLayout() {
		IssueDisplayPane result = new IssueDisplayPane(displayedIssue, parentStage, model, columns);
		response = result.getResponse();
		return result;
	}
}
