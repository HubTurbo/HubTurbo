package ui.sidepanel;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

import service.ServiceManager;
import ui.RepositorySelector;
import ui.UI;
import ui.collaboratormanagement.CollaboratorManagementComponent;
import ui.issuecolumn.ColumnControl;
import ui.issuepanel.expanded.IssueDisplayPane;
import ui.labelmanagement.LabelManagementComponent;
import ui.milestonemanagement.MilestoneManagementComponent;

/**
 * Represents the panel on the left side.
 * Encapsulates operations involving collapsing it and changing its layout.
 */
public class SidePanel extends VBox {
	public enum IssueEditMode{
		NIL, CREATE, EDIT
	};
	public enum Layout {
		TABS, ISSUE, HISTORY
	};
	
	protected static final int PANEL_PREF_WIDTH = 300;
	public boolean expandedIssueView = false;
	
	private Tab feedTab;
	private Tab labelsTab;
	private Tab milestonesTab;
	private Tab assigneesTab;
	private TabPane tabs;
	private SingleSelectionModel<Tab> selectionModel;
	private RepositorySelector repoFields;
	
	private UI ui;
	private Layout layout;
	private Stage parentStage;
	private Model model;
	private ColumnControl columns = null;
	private IssueDisplayPane currentIssueDisplay = null;
	
    // To cater for the SidePanel to collapse or expand
    private static final String EXPAND_RIGHT_POINTING_TRIANGLE = "\u25C0";
    private static final String COLLAPSE_LEFT_POINTING_TRIANGLE = "\u25B6";
	private Label controlLabel;

	public SidePanel(UI ui, Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.model = model;
		this.ui = ui;
		getStyleClass().add("sidepanel");
		setLayout(Layout.TABS);

	    // Set up controls & animation to allow the SidePanel to collapse or expand
		controlLabel = new Label(EXPAND_RIGHT_POINTING_TRIANGLE);
		controlLabel.getStyleClass().add("label-button");
		controlLabel.setOnMouseClicked((e) -> {
			if (isVisible()) {
				getChildren().clear();
				collapse();
				controlLabel.setText(COLLAPSE_LEFT_POINTING_TRIANGLE);
			} else {
				changeLayout();
				expand();
				controlLabel.setText(EXPAND_RIGHT_POINTING_TRIANGLE);
			}
		});
	}

    // Get control Label to enable the SidePanel to collapse or expand
    public Label getControlLabel() { 
    	return controlLabel; 
    }

	// Needed due to a circular dependency with ColumnControl
	public void setColumns(ColumnControl columns) {
		this.columns = columns;
	}
	
    private void collapse() {
		setVisible(false);
		controlLabel.setText(COLLAPSE_LEFT_POINTING_TRIANGLE);
    }

    private void expand() {
		setVisible(true);
		controlLabel.setText(EXPAND_RIGHT_POINTING_TRIANGLE);
    }
    
	public Layout getLayout() {
		return layout;
	}

	private void setLayout(Layout layout) {
		this.layout = layout;
		changeLayout();
	}

	public void refresh() {
		refreshSidebar();
		String repoStr = ServiceManager.getInstance().getRepoId().generateId();
		repoFields.setValue(repoStr);
	}
	
	public void refreshSidebar(){
		resetTabs();
		changeLayout();
	}
	
	public void refreshSidebarLabels(){
		labelsTab = null;
		changeLayout();
	}
	
	// For passing information to and from the issue panel display
	
	private TurboIssue displayedIssue;
	private boolean focusRequested;
	private IssueEditMode mode = IssueEditMode.NIL;
	
	public void onCreateIssueHotkey() {
		triggerIssueCreate(new TurboIssue("", "", model));
	}
	
	public void triggerIssueCreate(TurboIssue issue) {
		mode = IssueEditMode.CREATE;
		displayedIssue = issue;
		focusRequested = true;
		setLayout(Layout.ISSUE);
		expand();
	}
	
	public void triggerIssueEdit(TurboIssue issue, boolean requestFocus) {
		mode = IssueEditMode.EDIT;
		displayedIssue = new TurboIssue(issue);
		focusRequested = requestFocus;
		setLayout(Layout.ISSUE);
		expand();
	}
	
	public void displayTabs() {
		mode = IssueEditMode.NIL; //TODO:
		setLayout(Layout.TABS);
	}
	
	private void changeLayout() {
		// on repo switching, this sets the selected tab back to the first tab
		if (tabs != null) {
			Tab selectedTab = selectionModel.getSelectedItem();
			if (selectedTab.equals(milestonesTab)) {
				selectionModel.selectFirst();
			} else if (selectedTab.equals(assigneesTab)) {
				selectionModel.selectFirst();
			} 
		}
		
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

	private void resetTabs(){
		feedTab = null;
		labelsTab = null;
		milestonesTab = null;
		assigneesTab = null;
	}
	
	private Node tabLayout() {
		
		VBox everything = new VBox();
		
		tabs = new TabPane();

		if (labelsTab ==  null) {
			labelsTab = createLabelsTab();
		}
		if (milestonesTab == null) {
			milestonesTab = createMilestonesTab();
		}
		if (assigneesTab ==  null) {
			assigneesTab = createCollaboratorsTab();
		}
		
		tabs.getTabs().addAll(feedTab, labelsTab, milestonesTab, assigneesTab);
		//tabs.getTabs().addAll(assigneesTab);
		
		selectionModel = tabs.getSelectionModel();
		
		everything.getChildren().addAll(repoFields, tabs);
		everything.setPrefWidth(PANEL_PREF_WIDTH);
		return everything;
	}

	private Tab createCollaboratorsTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("Collaborators");
		tab.setContent(new CollaboratorManagementComponent(model).initialise());
		return tab;
	}

	private Tab createMilestonesTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("Milestones");
		tab.setContent(new MilestoneManagementComponent(parentStage, model).initialise());
		return tab;
	}

	private Tab createLabelsTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("Labels");
		tab.setContent(new LabelManagementComponent(parentStage, model, this).initialise());
		return tab;
	}

	private Node historyLayout() {
		return new VBox();
	}

	private Node issueLayout() {
		if(currentIssueDisplay != null){
			currentIssueDisplay.cleanup();
		}
		currentIssueDisplay = new IssueDisplayPane(ui, displayedIssue, parentStage, model, columns, this, focusRequested, mode);
		return currentIssueDisplay;
	}
}