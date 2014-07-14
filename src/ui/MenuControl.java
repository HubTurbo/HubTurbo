package ui;

import service.ServiceManager;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class MenuControl extends MenuBar {

//	private UI ui;

	private Stage mainStage;
	private ColumnControl columns;
//	private Model model;

	public MenuControl(Stage mainStage, Model model, ColumnControl columns, UI ui) {
		this.mainStage = mainStage;
//		this.ui = ui;
//		this.model = model;
		this.columns = columns;
		
		createMenuItems(mainStage, model, columns);
//		disableMenuItemsRequiringLogin();
	}
	
	MenuItem manageMilestonesMenuItem;
	MenuItem newIssueMenuItem;
	MenuItem manageLabelsMenuItem;
	MenuItem refreshMenuItem;

//	private void disableMenuItemsRequiringLogin() {
//		manageMilestonesMenuItem.setDisable(true);
//		newIssueMenuItem.setDisable(true);
//		manageLabelsMenuItem.setDisable(true);
//		refreshMenuItem.setDisable(true);
//	}
	
//	private void enableMenuItemsRequiringLogin() {
//		manageMilestonesMenuItem.setDisable(false);
//		newIssueMenuItem.setDisable(false);
//		manageLabelsMenuItem.setDisable(false);
//		refreshMenuItem.setDisable(false);
//	}

	private void createMenuItems(Stage mainStage, Model model, ColumnControl columns) {
//		Menu projects = new Menu("Projects");
//		projects.getItems().addAll(createLoginMenuItem());

//		Menu milestones = new Menu("Milestones");
//		milestones.getItems().addAll(createManageMilestonesMenuItem(mainStage, model));

		Menu issues = new Menu("Issues");
		issues.getItems().addAll(createNewIssueMenuItem(mainStage, model, columns));

//		Menu labels = new Menu("Labels");
//		labels.getItems().addAll(createManageLabelsMenuItem(mainStage, model));

		Menu view = new Menu("View");
		view.getItems().addAll(createRefreshMenuItem(), createColumnsMenuItem(columns));

		getMenus().addAll(issues, view);
	}

	private Menu createColumnsMenuItem(ColumnControl columns) {
		Menu columnsMenu = new Menu("Change number of columns....");
		final ToggleGroup numberOfCols = new ToggleGroup();
		for (int i = 1; i <= 9; i++) {
			RadioMenuItem item = new RadioMenuItem(Integer.toString(i));
			item.setToggleGroup(numberOfCols);
			columnsMenu.getItems().add(item);

			final int j = i;
			item.setOnAction((e) -> columns.setColumnCount(j));
			item.setAccelerator(new KeyCodeCombination(KeyCode.valueOf("DIGIT"
					+ Integer.toString(j)), KeyCombination.SHIFT_DOWN,
					KeyCombination.ALT_DOWN));

			if (i == 1)
				item.setSelected(true);
		}
		return columnsMenu;
	}

	private MenuItem createRefreshMenuItem() {
		refreshMenuItem = new MenuItem("Refresh");
		refreshMenuItem.setOnAction((e) -> {
			handleRefresh();
		});
		refreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
		return refreshMenuItem;
	}

	private MenuItem createManageLabelsMenuItem(Stage mainStage, Model model) {
		manageLabelsMenuItem = new MenuItem("<Removed>");
//		manageLabelsMenuItem.setOnAction(e -> {
//			(new LabelManagementComponent(mainStage, model)).show().thenApply(
//					response -> {
//						return true;
//					})
//				.exceptionally(ex -> {
//					ex.printStackTrace();
//					return false;
//				});
//		});
		return manageLabelsMenuItem;
	}

	private MenuItem createNewIssueMenuItem(Stage mainStage, Model model, ColumnControl columns) {
		newIssueMenuItem = new MenuItem("New Issue");
		newIssueMenuItem.setOnAction(e -> {
			
		});
		return newIssueMenuItem;
	}

//	private MenuItem createManageMilestonesMenuItem(Stage mainStage, Model model) {
//		manageMilestonesMenuItem = new MenuItem("Manage milestones...");
//		manageMilestonesMenuItem.setOnAction(e -> {
//			(new MilestoneManagementComponent(mainStage, model)).show().thenApply(
//					response -> {
//						return true;
//					})
//					.exceptionally(ex -> {
//						ex.printStackTrace();
//						return false;
//					});
//		});
//		return manageMilestonesMenuItem;
//	}

//	private MenuItem createLoginMenuItem() {
//		MenuItem login = new MenuItem("Login");
//		login.setOnAction((e) -> {
//
//		});
//		
//		return login;
//	}
	
		

	private void handleRefresh(){
	}

	// private void setUpHotkeys(Scene scene) {
	// scene.getAccelerators().put(
	// new KeyCodeCombination(KeyCode.DIGIT1,
	// KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
	// (Runnable) () -> changePanelCount(1));
	// }


}
