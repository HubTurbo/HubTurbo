package ui;

import java.lang.ref.WeakReference;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import model.Model;
import model.TurboUser;

public class CollaboratorManagementComponent {

	private final Model model;
	
	private ListView<TurboUser> listView;

	public CollaboratorManagementComponent(Model model) {
		this.model = model;
	}

	public VBox initialise() {
		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		layout.getChildren().addAll(createListView());
		VBox.setVgrow(listView, Priority.ALWAYS);
		return layout;
	}

	private Node createListView() {
		listView = new ListView<>();
		
		WeakReference<CollaboratorManagementComponent> that = new WeakReference<CollaboratorManagementComponent>(this);
		
		listView.setCellFactory(new Callback<ListView<TurboUser>, ListCell<TurboUser>>() {
			@Override
			public ListCell<TurboUser> call(ListView<TurboUser> list) {
				if(that.get() != null){
					return new ManageAssigneeListCell();
				} else{
					return null;
				}
			}
		});
		
		listView.setItems(null);
		listView.setItems(model.getCollaborators());
		
		return listView;
	}
}
