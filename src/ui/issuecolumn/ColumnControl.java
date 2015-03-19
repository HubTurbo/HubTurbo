package ui.issuecolumn;

import command.TurboCommandExecutor;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import storage.DataManager;
import ui.UI;
import ui.components.HTStatusBar;
import ui.issuepanel.IssuePanel;
import util.events.ColumnClickedEvent;
import util.events.ColumnClickedEventHandler;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;
import util.events.ModelChangedEvent;
import util.events.ModelChangedEventHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class ColumnControl extends HBox {

	private final UI ui;
	private final Stage stage;
	private final Model model;
	
	@SuppressWarnings("unused")
	private final UIBrowserBridge uiBrowserBridge;

	private TurboCommandExecutor dragAndDropExecutor;
	private Optional<Integer> currentlySelectedColumn = Optional.empty();
	private final KeyCombination maximizeWindow = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
	private final KeyCombination minimizeWindow = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
	private final KeyCombination defaultSizeWindow = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
	
	public ColumnControl(UI ui, Stage stage, Model model) {
		this.ui = ui;
		this.stage = stage;
		this.model = model;
		this.dragAndDropExecutor = new TurboCommandExecutor();
		this.uiBrowserBridge = new UIBrowserBridge(ui);
		setSpacing(10);
		setPadding(new Insets(0,10,0,10));

		ui.registerEvent(new ModelChangedEventHandler() {
			@Override
			public void handle(ModelChangedEvent e) {
				Platform.runLater(() -> {
					forEach(child -> {
						if (child instanceof IssueColumn) {
							((IssueColumn) child).setItems(e.issues);
						}
					});
				});
			}
		});

		ui.registerEvent(new IssueSelectedEventHandler() {
			@Override
			public void handle(IssueSelectedEvent e) {
				currentlySelectedColumn = Optional.of(e.columnIndex);
			}
		});
		
		ui.registerEvent(new ColumnClickedEventHandler() {
			@Override
			public void handle(ColumnClickedEvent e) {
				currentlySelectedColumn = Optional.of(e.columnIndex);
			}
		});

		setupKeyEvents();
	}
	
	public void restoreColumns() {
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
		HTStatusBar.displayMessage(message);
	}
	
	public void recreateColumns() {
		saveSession();
		restoreColumns();
	}
	
	/**
	 * Returns a list of issues to download comments for
	 * @return
	 */
	public List<Integer> getUpdatedIssues() {
		HashSet<Integer> result = new HashSet<>();
		for (Node child : getChildren()) {
			IssueColumn panel = (IssueColumn) child;
			if (panel.getCurrentFilterExpression().getQualifierNames().contains("updated")) {
				panel.getIssueList().stream()
						.map(issue -> issue.getId()).
						forEach(issueId -> result.add(issueId));
			}
		}
		return new ArrayList<>(result);
	}
	
	public void forEach(Consumer<Column> callback) {
		getChildren().forEach(child -> callback.accept((Column) child));
	}
	
	public void refresh() {
		forEach(child -> child.refreshItems());
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
		return addColumnAt(getChildren().size());
	}

	public IssueColumn addColumnAt(int index) {
		IssueColumn panel = new IssuePanel(ui, stage, model, this, index, dragAndDropExecutor);
		getChildren().add(index, panel);
		panel.setItems(model.getIssues());
		updateColumnIndices();
		currentlySelectedColumn = Optional.of(index);
		return panel;
	}

	public Column getColumn(int index) {
		return (Column) getChildren().get(index);
	}
	
	public void closeAllColumns() {
		getChildren().clear();
		// There aren't any children left, so we don't need to update indices
	}
	
	public void openColumnsWithFilters(List<String> filters) {
		for (String filter : filters) {
			IssueColumn column = addColumn();
			column.filterByString(filter);
		}
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
	
	public void closeCurrentColumn() {
		if (currentlySelectedColumn.isPresent()) {
			closeColumn(currentlySelectedColumn.get());
			currentlySelectedColumn = Optional.empty();
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
	private void setupKeyEvents() {
		addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (maximizeWindow.match(event)) {
					ui.maximizeWindow();
				}
				if (minimizeWindow.match(event)) {
					stage.setIconified(true);
				}
				if (defaultSizeWindow.match(event)) {
					ui.setDefaultWidth();
					scrollandShowColumn(currentlySelectedColumn.get(), getChildren().size());
				}
				if (event.getCode() == KeyCode.F || event.getCode() == KeyCode.G) {
					handleKeys(event.getCode() == KeyCode.F);
					assert currentlySelectedColumn.isPresent() : "handleKeys doesn't set selectedIndex!";
				}
			}
			
		});
	}

	private void handleKeys(boolean isForwardKey) {
		if (!currentlySelectedColumn.isPresent()) return;
		if (getChildren().size() == 0) return;
		Column selectedColumn = getColumn(currentlySelectedColumn.get());
		if(selectedColumn instanceof IssueColumn){
			if(((IssueColumn) selectedColumn).filterTextField.isFocused()){
				return;
			} else {
				int newIndex = currentlySelectedColumn.get() + (isForwardKey ? 1 : -1);
				if (newIndex < 0)
					newIndex = getChildren().size() - 1;
				else if (newIndex > getChildren().size() - 1)
					newIndex = 0;
				currentlySelectedColumn = Optional.of(newIndex);
				selectedColumn = getColumn(currentlySelectedColumn.get());
				((IssueColumn) selectedColumn).requestFocus();
			}
		}
		scrollandShowColumn(currentlySelectedColumn.get(), getChildren().size());
	}

	private void scrollandShowColumn(int SelectedColumnIndex, int numOfColumns) {
		ui.getMenuControl().scrollTo(SelectedColumnIndex, numOfColumns);
	}

}
