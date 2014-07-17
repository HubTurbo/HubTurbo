package ui;

import java.lang.ref.WeakReference;

import command.TurboCommandExecutor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.TurboIssue;

public class IssuePanel extends Column {

	private final Stage mainStage;
	private final Model model;
	private final ColumnControl parentColumnControl;
	private final int columnIndex;
	private final SidePanel sidePanel;

	private ListView<TurboIssue> listView;
	
	public IssuePanel(Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex, TurboCommandExecutor dragAndDropExecutor, boolean isSearchPanel) {
		super(mainStage, model, parentColumnControl, sidePanel, columnIndex, dragAndDropExecutor, isSearchPanel);
		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;
		this.sidePanel = sidePanel;
		
		listView = new ListView<>();
		setupListView();
		getChildren().add(listView);
		
		refreshItems();
	}
	
	@Override
	public void deselect() {
		listView.getSelectionModel().clearSelection();
	}
	
	@Override
	public void refreshItems() {
		super.refreshItems();
		WeakReference<IssuePanel> that = new WeakReference<IssuePanel>(this);
		
		// Set the cell factory every time - this forces the list view to update
		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				if(that.get() != null){
					return new IssuePanelCell(mainStage, model, that.get(), columnIndex, sidePanel, parentColumnControl);
				} else{
					return null;
				}
			}
		});
		
		// Supposedly this also causes the list view to update - not sure
		// if it actually does on platforms other than Linux...
		listView.setItems(null);
		listView.setItems(getFilteredList());
	}

	@SuppressWarnings("unused")
	private ChangeListener<TurboIssue> listener;
	private void setupListView() {
		setVgrow(listView, Priority.ALWAYS);
		listView.getSelectionModel().selectedItemProperty().addListener(new WeakChangeListener<TurboIssue>(
			listener = (observable, previousIssue, currentIssue) -> {
				if (currentIssue == null) return;
				
				// TODO save the previous issue?
				
				sidePanel.triggerIssueEdit(currentIssue);
			}));
	}
}
