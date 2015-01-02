package ui.feedmanagement;

import java.lang.ref.WeakReference;

import ui.UI;
import util.events.IssueSelectedEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import model.Model;
import model.TurboFeed;

public class FeedManagementComponent {
	private final UI ui;
	private final Model model;
	private ListView<TurboFeed> listView;
	
	public FeedManagementComponent(UI ui, Model model) {
		this.model = model;
		this.ui = ui;
	}

	public VBox initialise() {
		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		Node node = createListView();
		node.setOnKeyReleased(((e) -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				TurboFeed selectedIssue = listView.getSelectionModel().selectedItemProperty().get();
				if (selectedIssue != null) {
					ui.triggerEvent(new IssueSelectedEvent(selectedIssue.getIssueNum(), -1));
				}
			}
		}));
		node.setOnMouseClicked(((e) -> {
				TurboFeed selectedIssue = listView.getSelectionModel().selectedItemProperty().get();
				if (selectedIssue != null) {
					ui.triggerEvent(new IssueSelectedEvent(selectedIssue.getIssueNum(), -1));
				}
		}));
		layout.getChildren().addAll(node);
		VBox.setVgrow(listView, Priority.ALWAYS);
		return layout;
	}
	
	private Node createListView() {
		listView = new ListView<>();
		
		WeakReference<FeedManagementComponent> that = new WeakReference<FeedManagementComponent>(this);
		
		listView.setCellFactory(new Callback<ListView<TurboFeed>, ListCell<TurboFeed>>() {
			@Override
			public ListCell<TurboFeed> call(ListView<TurboFeed> list) {
				if(that.get() != null){
					return new ManageFeedListCell();
				} else{
					return null;
				}
			}
		});
		
		listView.setItems(null);
		listView.setItems(model.getFeeds());

		return listView;
	}
}