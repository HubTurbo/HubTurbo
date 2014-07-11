package ui;

import java.util.stream.Collectors;

import model.Model;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

public class AssigneeManagementComponent {

	private final Model model;
	
	private ListView<String> listView;

	public AssigneeManagementComponent(Model model) {
		this.model = model;
	}

	public HBox initialise() {
		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		layout.getChildren().addAll(createListView());
		return layout;
	}

	private Node createListView() {
		listView = new ListView<>();
		listView.setItems(FXCollections.observableArrayList(model.getCollaborators().stream().map(c -> c.getGithubName()).collect(Collectors.toList())));
		return listView;
	}
}
