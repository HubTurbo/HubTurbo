package ui;

import java.lang.ref.WeakReference;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Model;
import model.TurboUser;

public class AssigneeManagementComponent {

	private final Model model;
	
	private ListView<TurboUser> listView;

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
		
		WeakReference<AssigneeManagementComponent> that = new WeakReference<AssigneeManagementComponent>(this);
		
		listView.setCellFactory(new Callback<ListView<TurboUser>, ListCell<TurboUser>>() {
			@Override
			public ListCell<TurboUser> call(ListView<TurboUser> list) {
				if(that.get() != null){
					return new AssigneeManagementCell();
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
