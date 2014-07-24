package ui;

import java.util.concurrent.CompletableFuture;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

import org.eclipse.egit.github.core.RepositoryId;

import service.ServiceManager;
import ui.issuepanel.IssueDisplayPane;
import util.SessionConfigurations;

public class SidePanel extends VBox {
	protected static final int PANEL_PREF_WIDTH = 300;
	public boolean expandedIssueView = false;

	public enum Layout {
		TABS, ISSUE, HISTORY
	}

	private Layout layout;
	private Stage parentStage;
	private Model model;
	private ColumnControl columns = null;
	IssueDisplayPane currentIssueDisplay = null;
	
	public SidePanel(Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.model = model;
		getStyleClass().add("sidepanel");
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
	
	// For passing information to and from the issue panel display
	
	private TurboIssue displayedIssue;
	private boolean focusRequested;
	private CompletableFuture<String> response = null;
	
	private CompletableFuture<String> displayIssue(TurboIssue issue, boolean requestFocus) {
		response = null; // Make sure previous response doesn't remain
		
		// Pass required information to the issue display
		displayedIssue = issue;
		focusRequested = requestFocus;
		setLayout(Layout.ISSUE); // This method sets this.response
		assert response != null;
		return response;
	}
	
	public void onCreateIssueHotkey() {
		triggerIssueCreate(new TurboIssue("", "", model));
	}
	
	public void triggerIssueCreate(TurboIssue issue) {
		displayIssue(issue, true).thenApply(r -> {
			if (r.equals("done")) {
				model.createIssue(issue);
			}
			columns.refresh();
			displayTabs();
			return true;
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return false;
		});
	}
	
	public void triggerIssueEdit(TurboIssue issue, boolean requestFocus) {
		TurboIssue oldIssue = new TurboIssue(issue);
		TurboIssue modifiedIssue = new TurboIssue(issue);
		displayIssue(modifiedIssue, requestFocus).thenApply(r -> {
			if (r.equals("done")) {
				model.updateIssue(oldIssue, modifiedIssue);
			}
			columns.refresh();
			displayTabs();
			return true;
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return false;
		});
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
		
		tabs.getTabs().addAll(labelsTab, milestonesTab, assigneesTab);
		
		HBox repoFields = createRepoFields();

		everything.getChildren().addAll(repoFields, tabs);
		
		everything.setPrefWidth(PANEL_PREF_WIDTH);
		
		return everything;
	}

	private HBox createRepoFields() {
		final ComboBox<String> comboBox = new ComboBox<String>();
		comboBox.setFocusTraversable(false);
		comboBox.setEditable(true);
		
		if (ServiceManager.getInstance().getRepoId() != null) {
			String repoId = ServiceManager.getInstance().getRepoId().generateId();
			comboBox.setValue(repoId);
			SessionConfigurations.addToLastViewedRepositories(repoId);
			comboBox.getItems().addAll(SessionConfigurations.getLastViewedRepositories());
		}
		
		comboBox.valueProperty().addListener((a, b, c) -> loadRepo(comboBox));

		HBox repoIdBox = new HBox();
		repoIdBox.setSpacing(5);
		repoIdBox.setPadding(new Insets(5));
		repoIdBox.setAlignment(Pos.CENTER);
		repoIdBox.getChildren().addAll(comboBox);
		comboBox.prefWidthProperty().bind(repoIdBox.widthProperty());
		return repoIdBox;
	}

	private void loadRepo(final ComboBox<String> comboBox) {
		RepositoryId repoId = RepositoryId.createFromId(comboBox.getValue());
		if (repoId != null) {
			columns.saveSession();
			ServiceManager.getInstance().setupRepository(repoId.getOwner(), repoId.getName());
			columns.resumeColumns();
			this.refresh();
			comboBox.setItems(FXCollections.observableArrayList(
					SessionConfigurations.addToLastViewedRepositories(repoId.generateId())));
		}
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
		currentIssueDisplay = new IssueDisplayPane(displayedIssue, parentStage, model, columns, this, focusRequested);
		response = currentIssueDisplay.getResponse();
		return currentIssueDisplay;
	}
}
