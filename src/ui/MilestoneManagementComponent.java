package ui;

import handler.MilestoneHandler;

import java.lang.ref.WeakReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import model.Model;
import model.TurboMilestone;

public class MilestoneManagementComponent {
	private static final Logger logger = LogManager.getLogger(MilestoneManagementComponent.class.getName());
	private final Stage parentStage;
	private final MilestoneHandler msHandler;
	
	private ListView<TurboMilestone> listView;

	public MilestoneManagementComponent(Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.msHandler = new MilestoneHandler(model);
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
					return new ManageMilestonesListCell(parentStage, msHandler, that.get());
				}else{
					return null;
				}
			}
		});
	}

	private Node createListView() {
		listView = new ListView<>();
		listView.setItems(msHandler.getMilestones());
		VBox.setVgrow(listView, Priority.ALWAYS);
		
		refresh();

		return listView;
	}

	private Node createButtons() {
		Button create = new Button("New Milestone");
		create.setOnAction(event -> {
			(new EditMilestoneDialog(parentStage, null)).show().thenApply(response -> {
				msHandler.createMilestone(response);
				return true;
			}).exceptionally(exception -> {
				logger.error(exception.getLocalizedMessage(), exception);
				return false;
			});
		});
		HBox.setHgrow(create, Priority.ALWAYS);
		create.setMaxWidth(Double.MAX_VALUE);

		VBox container = new VBox();
		container.setSpacing(5);
		container.getChildren().addAll(create);
		
		return container;
	}
}
