package ui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import util.ConfigFileHandler;
import util.SessionConfigurations;

import command.TurboCommandExecutor;

import filter.FilterExpression;


public class ColumnControl extends HBox {

	private final Stage stage;
	private final Model model;
	private final SidePanel sidePanel;
	
	private TurboCommandExecutor dragAndDropExecutor;
	
	private SessionConfigurations sessionConfig;
	private StatusBar statusBar;

	public ColumnControl(Stage stage, Model model, SidePanel sidePanel, StatusBar statusBar) {
		this.stage = stage;
		this.model = model;
		this.sidePanel = sidePanel;
		this.dragAndDropExecutor = new TurboCommandExecutor();
		this.sessionConfig = ConfigFileHandler.loadSessionConfig();
		this.statusBar = statusBar;
	}
	
	public void resumeColumns() {
		List<String> filters = sessionConfig.getFiltersFromPreviousSession(model.getRepoId());
		if (filters != null && !filters.isEmpty()) {
			for (String filter : filters) {
				addColumn(false).filterByString(filter);
			}
		} else {
			addColumn(false);
		}
	}

	public void displayMessage(String message) {
		statusBar.setText(message);
	}
	
	public void refresh() {
		getChildren().forEach(child -> ((Column) child).refreshItems());
	}
	
	public void deselect() {
		getChildren().forEach(child -> ((Column) child).deselect());
	}

	public void loadIssues() {
		for (Node node : getChildren()) {
			Column panel = (Column) node;
			panel.setItems(model.getIssues());
		}
	}

	public void addNormalColumn() {
		addColumn(false);
	}
	
	private Column addColumn(boolean isSearchPanel) {
		Column panel = new IssuePanel(stage, model, this, sidePanel, getChildren().size(), dragAndDropExecutor, isSearchPanel, statusBar);
		getChildren().add(panel);
		panel.setItems(model.getIssues());
		return panel;
	}

	public Column addColumnAt(boolean isSearchPanel, int index) {
		Column panel = new IssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor, isSearchPanel, statusBar);
		getChildren().add(index, panel);
		panel.setItems(model.getIssues());
		updateColumnIndices();
		return panel;
	}

	public Column getColumn(int index) {
		return (Column) getChildren().get(index);
	}
	
	public void closeColumn(int index) {
		getChildren().remove(index);
		updateColumnIndices();
	}

	private void updateColumnIndices() {
		int i = 0;
		for (Node c : getChildren()) {
			((Column) c).updateIndex(i++);
		}
	}
	
	public void toggleColumn(int index) {
		Column column;
		Column current = (Column) getChildren().get(index);
		FilterExpression currentFilterExpr = current.getCurrentFilterExpression();
		if (current instanceof HierarchicalIssuePanel) {
			column = new IssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor, current.isSearchPanel(), statusBar);
		} else {
			column = new HierarchicalIssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor, current.isSearchPanel(), statusBar);
		}
		column.setItems(model.getIssues());
		column.filter(currentFilterExpr);
		getChildren().set(index, column);
	}
		
	public void createNewSearchPanelAtStart() {
		addColumnAt(true, 0);
	}

	public void createNewSearchPanelAtEnd() {
		addColumn(true);
	}

	public void saveSession() {
		List<String> sessionFilters = new ArrayList<String>();
		getChildren().forEach(child -> {
			String filter = ((Column) child).getCurrentFilterExpression().toString();
			sessionFilters.add(filter);
		});
		sessionConfig.setFiltersForNextSession(model.getRepoId(), sessionFilters);
		ConfigFileHandler.saveSessionConfig(sessionConfig);
	}

	public void swapColumns(int columnIndex, int columnIndex2) {
		Column one = getColumn(columnIndex);
		Column two = getColumn(columnIndex2);
		one.updateIndex(columnIndex2);
		two.updateIndex(columnIndex);
		// This method of swapping is used because Collections.swap
		// will assign one child without removing the other, causing
		// a duplicate child exception. HBoxes are constructed because
		// null also causes an exception.
		getChildren().set(columnIndex, new HBox());
		getChildren().set(columnIndex2, new HBox());
		getChildren().set(columnIndex, two);
		getChildren().set(columnIndex2, one);
	}
	
	private int currentlyDraggedColumnIndex = -1;
	public int getCurrentlyDraggedColumnIndex() {
		return currentlyDraggedColumnIndex;
	}
	public void setCurrentlyDraggedColumnIndex(int i) {
		currentlyDraggedColumnIndex = i;
	}
}
