package ui.issuecolumn;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import storage.DataManager;
import ui.StatusBar;
import ui.UI;
import ui.issuepanel.HierarchicalIssuePanel;
import ui.issuepanel.IssuePanel;
import ui.sidepanel.SidePanel;

import command.TurboCommandExecutor;

import filter.FilterExpression;


public class ColumnControl extends HBox {

	private final UI ui;
	private final Stage stage;
	private final Model model;
	private final SidePanel sidePanel;
	
	private TurboCommandExecutor dragAndDropExecutor;

	public ColumnControl(UI ui, Stage stage, Model model, SidePanel sidePanel) {
		this.ui = ui;
		this.stage = stage;
		this.model = model;
		this.sidePanel = sidePanel;
		this.dragAndDropExecutor = new TurboCommandExecutor();
		setSpacing(10);
		setPadding(new Insets(0,10,0,10));
		setupModelChangeResponse();
	}
	
	private void setupModelChangeResponse(){
		WeakReference<ColumnControl> selfRef = new WeakReference<>(this);
		//No need for weak listeners because ColumnControl is persistent for the lifetime of the app
		model.applyMethodOnModelChange(() -> selfRef.get().refresh());
	}
	
	public void resumeColumns() {
		getChildren().clear();
		
		List<String> filters = DataManager.getInstance().getFiltersFromPreviousSession(model.getRepoId());
		if (filters != null && !filters.isEmpty()) {
			for (String filter : filters) {
				addColumn(false).filterByString(filter);
			}
		} else {
			addColumn(false);
		}
	}

	public void displayMessage(String message) {
		StatusBar.displayMessage(message);
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
	
	private Column addColumn(boolean isSearchPanel) {
		Column panel = new IssuePanel(ui, stage, model, this, sidePanel, getChildren().size(), dragAndDropExecutor, isSearchPanel);
		getChildren().add(panel);
		panel.setItems(model.getIssues());
		return panel;
	}

	public Column addColumnAt(boolean isSearchPanel, int index) {
		Column panel = new IssuePanel(ui, stage, model, this, sidePanel, index, dragAndDropExecutor, isSearchPanel);
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
			column = new IssuePanel(ui, stage, model, this, sidePanel, index, dragAndDropExecutor, current.isSearchPanel());
		} else {
			column = new HierarchicalIssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor, current.isSearchPanel());
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
		DataManager.getInstance().setFiltersForNextSession(model.getRepoId(), sessionFilters);
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
	
	private int currentlyFocusedColumnIndex = -1;
	public int getCurrentlyFocusedColumnIndex() {
		return currentlyFocusedColumnIndex;
	}
	public void setCurrentlyFocusedColumnIndex(int i) {
		currentlyFocusedColumnIndex = i;
	}

	public void closeCurrentColumn() {
		if (currentlyFocusedColumnIndex != -1) {
			closeColumn(currentlyFocusedColumnIndex);
			currentlyFocusedColumnIndex = -1;
		}
	}
}
