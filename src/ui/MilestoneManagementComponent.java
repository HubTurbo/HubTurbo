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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class MilestoneManagementComponent {

	private static final String NEW_MILESTONE_NAME = "newmilestone";
	
	private final Stage parentStage;
	private final Model model;
	
	private ListView<TurboMilestone> listView;

	public MilestoneManagementComponent(Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.model = model;
	}

	public VBox initialise() {
		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		layout.getChildren().addAll(createButtons(), createListView());
		return layout;
	}
	
	public void refresh() {
		
		WeakReference<MilestoneManagementComponent> that = new WeakReference<MilestoneManagementComponent>(this);
		
		listView.setCellFactory(new Callback<ListView<TurboMilestone>, ListCell<TurboMilestone>>() {
			@Override
			public ListCell<TurboMilestone> call(ListView<TurboMilestone> list) {
				if(that.get() != null){
					return new ManageMilestonesListCell(parentStage, model, that.get());
				}else{
					return null;
				}
			}
		});
	}

	private Node createListView() {
		listView = new ListView<>();
		listView.setItems(model.getMilestones());
		VBox.setVgrow(listView, Priority.ALWAYS);
		
		refresh();

		return listView;
	}

	private Node createButtons() {
		Button create = new Button("Create Milestone");
		create.setOnAction(e -> {
			model.createMilestone(new TurboMilestone(NEW_MILESTONE_NAME));
		});
		HBox.setHgrow(create, Priority.ALWAYS);
		create.setMaxWidth(Double.MAX_VALUE);

		VBox container = new VBox();
		container.setSpacing(5);
		container.getChildren().addAll(create);
		
		return container;
	}
}
