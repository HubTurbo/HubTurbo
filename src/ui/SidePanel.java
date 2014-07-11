package ui;

import model.Model;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class SidePanel extends VBox {

	public enum Layout {
		TABS, ISSUE, HISTORY
	}

	private Layout layout;
	private Stage parentStage;
	private Model model;
	
	public SidePanel(Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.model = model;
		setLayout(Layout.TABS);
	}

	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
		changeLayout();
	}

	public void refresh() {
		changeLayout();
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
		
		return everything;
	}

	private Tab createFeedTab() {
		return new Tab();
	}

	private Tab createAssgineesTab() {
		return new Tab();
	}

	private Tab createMilestonesTab() {
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("M");
		tab.setContent(new MilestoneManagementComponent(model).initialise());
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
		// TODO Auto-generated method stub
		return null;
	}

	private Node issueLayout() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
