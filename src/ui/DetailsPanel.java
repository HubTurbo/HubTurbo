package ui;

import handler.IssueDetailsContentHandler;
import model.TurboComment;
import model.TurboIssue;
import ui.IssueDetailsDisplay.DisplayType;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class DetailsPanel extends VBox {
	
	private ListView<TurboComment> listView;
	private IssueDetailsContentHandler handler;
	private TurboIssue issue;
	private DisplayType displayType;
	
	public DetailsPanel(TurboIssue issue, IssueDetailsContentHandler handler, DisplayType displayType){
		this.issue = issue;
		this.listView = new ListView<TurboComment>();
		this.handler = handler;
		this.displayType = displayType;
		loadItems();
	}
	
	private Callback<ListView<TurboComment>, ListCell<TurboComment>> commentCellFactory(){
		Callback<ListView<TurboComment>, ListCell<TurboComment>> factory = new Callback<ListView<TurboComment>, ListCell<TurboComment>>() {
			@Override
			public ListCell<TurboComment> call(ListView<TurboComment> list) {
				return new DetailsCell(issue, displayType);
			}
		};
		return factory;
	}
	
	public void loadItems() {
		listView.setCellFactory(commentCellFactory());
		if(displayType == DisplayType.COMMENTS){
			listView.setItems(handler.getComments());
		}else{
			listView.setItems(handler.getIssueHistory());
		}
	}
}
