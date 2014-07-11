package ui;

import java.lang.ref.WeakReference;
import model.Model;
import model.TurboMilestone;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class MilestoneManagementComponent {

	private static final String NEW_MILESTONE_NAME = "newmilestone";
	
	private final Model model;
	
	private ListView<TurboMilestone> listView;

	public MilestoneManagementComponent(Model model) {
		this.model = model;
	}

	public HBox initialise() {
		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		layout.getChildren().addAll(createListView(), createButtons());
		return layout;
	}
	
	public void refresh() {
		
		WeakReference<MilestoneManagementComponent> that = new WeakReference<MilestoneManagementComponent>(this);
		
		listView.setCellFactory(new Callback<ListView<TurboMilestone>, ListCell<TurboMilestone>>() {
			@Override
			public ListCell<TurboMilestone> call(ListView<TurboMilestone> list) {
				if(that.get() != null){
					return new ManageMilestonesListCell(model, that.get());
				}else{
					return null;
				}
			}
		});
	}

	private Node createListView() {
		listView = new ListView<>();
		listView.setItems(model.getMilestones());
		listView.setEditable(true);
		
		refresh();
				
		return listView;
	}

	private Node createButtons() {
		Button create = new Button("Create Milestone");
		create.setOnAction(e -> {
			model.createMilestone(new TurboMilestone(NEW_MILESTONE_NAME));
		});

		VBox container = new VBox();
		container.setSpacing(5);
		container.getChildren().addAll(create);
		
		return container;
	}
}
