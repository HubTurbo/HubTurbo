package ui.issuepanel;

import java.lang.ref.WeakReference;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.TurboIssue;
import ui.UI;
import ui.components.NavigableListView;
import ui.issuecolumn.ColumnControl;
import ui.issuecolumn.IssueColumn;
import ui.sidepanel.SidePanel;
import util.events.IssueSelectedEvent;
import command.TurboCommandExecutor;

public class IssuePanel extends IssueColumn {

	private final Model model;
	private final UI ui;

	private NavigableListView<TurboIssue> listView;

	public IssuePanel(UI ui, Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex, TurboCommandExecutor dragAndDropExecutor) {
		super(ui, mainStage, model, parentColumnControl, sidePanel, columnIndex, dragAndDropExecutor);
		this.model = model;
		this.ui = ui;
		
		listView = new NavigableListView<>();
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
					return new IssuePanelCell(ui, model, that.get(), columnIndex);
				} else{
					return null;
				}
			}
		});
		
		// Supposedly this also causes the list view to update - not sure
		// if it actually does on platforms other than Linux...
		listView.setItems(null);
		listView.setItems(getIssueList());
	}
	
	private void setupListView() {
		setVgrow(listView, Priority.ALWAYS);
		listView.setOnItemSelected(i -> {
			ui.triggerEvent(new IssueSelectedEvent(listView.getItems().get(i).getId(), columnIndex));
		});
	}
}
