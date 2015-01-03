package ui.issuepanel;

import java.lang.ref.WeakReference;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.TurboIssue;
import ui.ScrollingListView;
import ui.UI;
import ui.issuecolumn.ColumnControl;
import ui.issuecolumn.IssueColumn;
import ui.sidepanel.SidePanel;
import util.events.IssueSelectedEvent;

import command.TurboCommandExecutor;

public class IssuePanel extends IssueColumn {

	private final Stage mainStage;
	private final Model model;
	private final ColumnControl parentColumnControl;
	private final int columnIndex;
	private final SidePanel sidePanel;
	private final UI ui;

	private ScrollingListView<TurboIssue> listView;

	public IssuePanel(UI ui, Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex, TurboCommandExecutor dragAndDropExecutor) {
		super(ui, mainStage, model, parentColumnControl, sidePanel, columnIndex, dragAndDropExecutor);
		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		// TODO move this to super
		this.columnIndex = columnIndex;
		this.sidePanel = sidePanel;
		this.ui = ui;
		
		listView = new ScrollingListView<>();
		setupListView();
		getChildren().add(listView);
		
		refreshItems();
	}
	
	// TODO move this to super
	private boolean isThisColumnSelected() {
		return parentColumnControl.getCurrentlySelectedColumn().isPresent()
			&& parentColumnControl.getCurrentlySelectedColumn().get() == columnIndex;
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
					return new IssuePanelCell(ui, mainStage, model, that.get(), columnIndex, sidePanel, parentColumnControl);
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
