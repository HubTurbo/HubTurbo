package ui;

import handler.IssueDetailsContentHandler;
import model.TurboComment;
import model.TurboIssue;
import ui.IssueDetailsDisplay.DisplayType;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class DetailsPanel extends VBox {
	public static int COMMENTS_CELL_HEIGHT = 200;
	public static int COMMENTS_CELL_WIDTH = 330;
	public static int COMMENTS_PADDING = 5;
	
	private ListView<TurboComment> listView;
	private IssueDetailsContentHandler handler;
	private TurboIssue issue;
	private DisplayType displayType;
	
	public DetailsPanel(TurboIssue issue, IssueDetailsContentHandler handler, DisplayType displayType){
		this.issue = issue;
		this.listView = new ListView<TurboComment>();
		this.handler = handler;
		this.displayType = displayType;
		setupLayout();
		loadItems();
	}
	
	private void setupLayout(){
		this.setPadding(new Insets(COMMENTS_PADDING));
		this.setSpacing(COMMENTS_PADDING);
	}
	
	private Callback<ListView<TurboComment>, ListCell<TurboComment>> commentCellFactory(){
		Callback<ListView<TurboComment>, ListCell<TurboComment>> factory = new Callback<ListView<TurboComment>, ListCell<TurboComment>>() {
			@Override
			public ListCell<TurboComment> call(ListView<TurboComment> list) {
				return new DetailsCell(issue, displayType, handler);
			}
		};
		return factory;
	}
	
	private void loadItems() {
		if(displayType == DisplayType.COMMENTS){
			loadNewCommentsBox();
		}
		setListItems();
		getChildren().add(0, listView);
	}

	
	private void loadNewCommentsBox(){
		CommentsEditBox box = new CommentsEditBox(handler);
		box.setPrefHeight(COMMENTS_CELL_HEIGHT);
		box.setPrefWidth(COMMENTS_CELL_WIDTH);
		getChildren().add(box);
	}
	
	private void setListItems(){
		listView.setPrefWidth(COMMENTS_CELL_WIDTH);
		listView.setCellFactory(commentCellFactory());
		if(displayType == DisplayType.COMMENTS){
			listView.setItems(handler.getComments());
		}else{
			listView.setItems(handler.getIssueHistory());
		}
	}
}
