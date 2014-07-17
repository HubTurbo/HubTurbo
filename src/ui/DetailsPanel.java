package ui;

import java.util.List;

import model.TurboIssue;

import org.eclipse.egit.github.core.Comment;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class CommentsPanel extends VBox {
	
	private ListView<Comment> listView;
	private TurboIssue issue;
	
	public CommentsPanel(TurboIssue issue, List<Comment> comments){
		this.issue = issue;
		this.listView = new ListView<Comment>();
	}
	
	private Callback<ListView<Comment>, ListCell<Comment>> commentCellFactory(){
		Callback<ListView<Comment>, ListCell<Comment>> factory = new Callback<ListView<Comment>, ListCell<Comment>>() {
			@Override
			public ListCell<Comment> call(ListView<Comment> list) {
				//TODO:
				return new CommentsCell();
			}
		};
		return factory;
	}
	
}
