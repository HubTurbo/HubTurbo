package ui.issuecolumn;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import prefs.Preferences;
import ui.UI;
import ui.components.HTStatusBar;
import ui.issuepanel.IssuePanel;
import util.events.ColumnClickedEventHandler;
import util.events.IssueSelectedEventHandler;
import util.events.ModelUpdatedEventHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class ColumnControl extends HBox {

	private final UI ui;
	private final Preferences prefs;
	private IModel model;
	private Optional<Integer> currentlySelectedColumn = Optional.empty();

	public ColumnControl(UI ui, Preferences prefs) {
		this.ui = ui;
		this.prefs = prefs;

		// Set up the connection to the browser
		new UIBrowserBridge(ui);

		setSpacing(10);
		setPadding(new Insets(0,10,0,10));

		ui.registerEvent((ModelUpdatedEventHandler) e -> {
			updateModel(e.model);
			forEach(child -> {
				if (child instanceof IssueColumn) {
					((IssueColumn) child).setItems(e.model.getIssues());
				}
			});
		});

		ui.registerEvent((IssueSelectedEventHandler) e ->
			setCurrentlySelectedColumn(Optional.of(e.columnIndex)));
		ui.registerEvent((ColumnClickedEventHandler) e ->
			setCurrentlySelectedColumn(Optional.of(e.columnIndex)));

		setupKeyEvents();
	}

	/**
	 * Called on login
	 */
	public void init() {
		restoreColumns();
	}

	private void updateModel(IModel newModel) {
		model = newModel;
	}
	
	public void recreateColumns() {
		saveSession();
		restoreColumns();
	}

	public void saveSession() {
		List<String> sessionFilters = new ArrayList<>();
		getChildren().forEach(child -> {
			if (child instanceof IssueColumn) {
				String filter = ((IssueColumn) child).getCurrentFilterString();
				sessionFilters.add(filter);
			}
		});
		prefs.setLastOpenFilters(sessionFilters);
	}

	public void restoreColumns() {
		getChildren().clear();

		List<String> filters = prefs.getLastOpenFilters();

		if (filters.isEmpty()) {
			addColumn();
			return;
		}

		for (String filter : filters) {
			addColumn().filterByString(filter);
		}
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
					.map(TurboIssue::getId)
					.forEach(result::add);
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
	
	private IssueColumn addColumn() {
		return addColumnAt(getChildren().size());
	}

	public IssueColumn addColumnAt(int index) {
		IssueColumn panel = new IssuePanel(ui, model, this, index);
		getChildren().add(index, panel);
		panel.setItems(model.getIssues());
		updateColumnIndices();
		setCurrentlySelectedColumn(Optional.of(index));
		return panel;
	}

	private void setCurrentlySelectedColumn(Optional<Integer> selectedColumn) {
		currentlySelectedColumn = selectedColumn;
		updateCSSforColumns();
	}

	private void updateCSSforColumns() {
		if(currentlySelectedColumn.isPresent()) {
			for(int index = 0; index < getChildren().size();index++) {
				getColumn(index).getStyleClass().remove("panel-focused");
			}
			getColumn(currentlySelectedColumn.get()).getStyleClass().add("panel-focused");
		}	
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
		Node child = getChildren().remove(index);
		updateColumnIndices();
		((Column) child).close();
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
			int columnIndex = currentlySelectedColumn.get();
			closeColumn(columnIndex);
			if (getChildren().size() == 0) {
				setCurrentlySelectedColumn(Optional.empty());
			} else {
				int newColumnIndex = (columnIndex > getChildren().size() - 1)
									 ? columnIndex - 1
									 : columnIndex;
				setCurrentlySelectedColumn(Optional.of(newColumnIndex));
				getColumn(currentlySelectedColumn.get()).requestFocus();
			}
		}
	}
	
	public double getPanelWidth() {
		// COLUMN_WIDTH is used instead of
		// ((Column) getChildren().get(0)).getWidth();
		// because when this function is called, columns may not have been sized yet.
		// In any case actual column width is COLUMN_WIDTH at minimum, so we can assume
		// that they are that large.
		return 40 + Column.COLUMN_WIDTH;
	}
	private void setupKeyEvents() {
		addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.F || event.getCode() == KeyCode.D) {
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
				setCurrentlySelectedColumn(Optional.of(newIndex));
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
