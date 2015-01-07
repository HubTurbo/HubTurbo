package ui.issuecolumn;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import storage.DataManager;
import ui.UI;
import ui.components.StatusBar;
import ui.issuepanel.HierarchicalIssuePanel;
import ui.issuepanel.IssuePanel;
import util.events.ColumnChangeEvent;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;
import util.events.RefreshDoneEvent;
import util.events.RefreshDoneEventHandler;
import command.TurboCommandExecutor;
import filter.expression.FilterExpression;


public class ColumnControl extends HBox {

	private final UI ui;
	private final Stage stage;
	private final Model model;
	
	@SuppressWarnings("unused")
	private final UIBrowserBridge uiBrowserBridge;

	private TurboCommandExecutor dragAndDropExecutor;
	private Optional<Integer> currentlySelectedColumn = Optional.empty();
	
	public ColumnControl(UI ui, Stage stage, Model model) {
		this.ui = ui;
		this.stage = stage;
		this.model = model;
		this.dragAndDropExecutor = new TurboCommandExecutor();
		this.uiBrowserBridge = new UIBrowserBridge(ui);
		setSpacing(10);
		setPadding(new Insets(0,10,0,10));
		setupModelChangeResponse();
		
		ui.registerEvent(new RefreshDoneEventHandler() {
			@Override
			public void handle(RefreshDoneEvent e) {
				// We need this because this is triggered from a Timer thread
				Platform.runLater(()-> {
					refresh();
				});	
			}
		});
		
		ui.registerEvent(new IssueSelectedEventHandler() {
			@Override
			public void handle(IssueSelectedEvent e) {
				currentlySelectedColumn = Optional.of(e.columnIndex);
			}
		});
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
				addColumn().filterByString(filter);
			}
		} else {
			addColumn();
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
			if (node instanceof IssueColumn) {
				IssueColumn panel = (IssueColumn) node;
				panel.setItems(model.getIssues());
			}
		}
	}
	
	private IssueColumn addColumn() {
		IssueColumn panel = new IssuePanel(ui, stage, model, this, getChildren().size(), dragAndDropExecutor);
		getChildren().add(panel);
		panel.setItems(model.getIssues());
		ui.triggerEvent(new ColumnChangeEvent());
		return panel;
	}

	public IssueColumn addColumnAt(int index) {
		IssueColumn panel = new IssuePanel(ui, stage, model, this, index, dragAndDropExecutor);
		getChildren().add(index, panel);
		panel.setItems(model.getIssues());
		updateColumnIndices();
		ui.triggerEvent(new ColumnChangeEvent());
		return panel;
	}

	public Column getColumn(int index) {
		return (Column) getChildren().get(index);
	}
	
	public void closeColumn(int index) {
		getChildren().remove(index);
		updateColumnIndices();
		ui.triggerEvent(new ColumnChangeEvent());
	}

	private void updateColumnIndices() {
		int i = 0;
		for (Node c : getChildren()) {
			((Column) c).updateIndex(i++);
		}
	}
	
	public void toggleColumn(int index) {
		IssueColumn column;
		IssueColumn current = (IssueColumn) getChildren().get(index);
		FilterExpression currentFilterExpr = current.getCurrentFilterExpression();
		if (current instanceof HierarchicalIssuePanel) {
			column = new IssuePanel(ui, stage, model, this, index, dragAndDropExecutor);
		} else {
			column = new HierarchicalIssuePanel(ui, stage, model, this, index, dragAndDropExecutor);
		}
		column.setItems(model.getIssues());
		column.filter(currentFilterExpr);
		getChildren().set(index, column);
	}
		
	public void createNewPanelAtStart() {
		addColumnAt(0);
	}

	public void createNewPanelAtEnd() {
		addColumn();
	}

	public void saveSession() {
		List<String> sessionFilters = new ArrayList<String>();
		getChildren().forEach(child -> {
			if (child instanceof IssueColumn) {
				String filter = ((IssueColumn) child).getCurrentFilterString();
				sessionFilters.add(filter);
			}
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
	
	public Optional<Integer> getCurrentlySelectedColumn() {
		return currentlySelectedColumn;
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
	
	public double getColumnWidth() {
		return (getChildren() == null || getChildren().size() == 0)
				? 0
				: 40 + Column.COLUMN_WIDTH;
		// COLUMN_WIDTH is used instead of
		// ((Column) getChildren().get(0)).getWidth();
		// because when this function is called, columns may not have been sized yet.
		// In any case column width is set to COLUMN_WIDTH at minimum, so we can assume
		// that they are that large.
	}
}
