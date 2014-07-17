package ui;

import model.TurboIssue;

import org.eclipse.egit.github.core.Comment;

import ui.IssueDetailsDisplay.DisplayType;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class DetailsPanel extends VBox {
	
	private ListView<Comment> listView;
	private ObservableList<Comment> commentsList;
	private TurboIssue issue;
	private DisplayType displayType;
	
	public DetailsPanel(TurboIssue issue, ObservableList<Comment> comments, DisplayType displayType){
		this.issue = issue;
		this.listView = new ListView<Comment>();
		this.commentsList = comments;
		this.displayType = displayType;
		loadItems();
	}
	
	private Callback<ListView<Comment>, ListCell<Comment>> commentCellFactory(){
		Callback<ListView<Comment>, ListCell<Comment>> factory = new Callback<ListView<Comment>, ListCell<Comment>>() {
			@Override
			public ListCell<Comment> call(ListView<Comment> list) {
				return new DetailsCell(issue, displayType);
			}
		};
		return factory;
	}
	
	public void loadItems() {
		listView.setCellFactory(commentCellFactory());
		listView.setItems(commentsList);
	}
}
