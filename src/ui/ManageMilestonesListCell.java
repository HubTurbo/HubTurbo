package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboMilestone;


public class ManageMilestonesListCell extends ListCell<TurboMilestone> {
	private final Stage stage;
	private final Model model;
	private final MilestoneManagementComponent parentDialog;
	
	private ArrayList<ChangeListener<?>> changeListeners = new ArrayList<ChangeListener<?>>();

    public ManageMilestonesListCell(Stage stage, Model model, MilestoneManagementComponent parentDialog) {
		super();
		this.stage = stage;
		this.model = model;
		this.parentDialog = parentDialog;
	}

	@Override
	public void updateItem(TurboMilestone milestone, boolean empty) {
		super.updateItem(milestone, empty);
		if (milestone == null)
			return;

		setGraphic(createMilestoneItem(milestone));
		setContextMenu(createContextMenu(milestone));
	}

	private Node createMilestoneItem(TurboMilestone milestone) {
		Label title = new Label(milestone.getTitle());
		milestone.titleProperty().addListener(new WeakChangeListener<String>(createMilestoneTitleListener(milestone, title)));
		HBox titleContainer = new HBox();
		HBox.setHgrow(titleContainer, Priority.ALWAYS);
		titleContainer.setAlignment(Pos.BASELINE_LEFT);
		titleContainer.getChildren().add(title);
		
		HBox top = new HBox();
		HBox.setHgrow(top, Priority.ALWAYS);
		top.getChildren().add(titleContainer);
		
		if (milestone.getDueOn() != null) {
			// TODO ADD CHANGE LISTENER
			Label dueDate = new Label(milestone.getDueOn().toString());
			HBox dueDateContainer = new HBox();
			HBox.setHgrow(dueDateContainer, Priority.ALWAYS);
			dueDateContainer.setAlignment(Pos.BASELINE_RIGHT);
			dueDateContainer.getChildren().add(dueDate);
			top.getChildren().add(dueDateContainer);
		}
		
		ProgressBar progressBar = new ProgressBar(milestone.getProgress());
		
		VBox milestoneItem = new VBox();
		milestoneItem.setStyle("-fx-border-color: yellow; -fx-border-width: 3px"); 
		milestoneItem.setSpacing(3);
		milestoneItem.getChildren().addAll(top, progressBar);

		return milestoneItem;
	}
	
    private ChangeListener<String> createMilestoneTitleListener(TurboMilestone milestone, Label milestoneTitle){
    	WeakReference<TurboMilestone> milestoneRef = new WeakReference<TurboMilestone>(milestone);
    	ChangeListener<String> listener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				TurboMilestone milestone = milestoneRef.get();
				if(milestone != null){
					milestoneTitle.setText(milestone.getTitle());
				}
			}
		};
		changeListeners.add(listener);
		return listener;
    }

	private ContextMenu createContextMenu(TurboMilestone milestone) {
		MenuItem edit = new MenuItem("Edit Milestone");
		edit.setOnAction((event) -> {
			(new EditMilestoneDialog(stage, milestone)).show().thenApply(response -> {
				model.updateMilestone(response);
				return true;
			}).exceptionally(e -> {
				e.printStackTrace();
				return false;
			});
		});
		MenuItem delete = new MenuItem("Delete Milestone");
		delete.setOnAction((event) -> {
			model.deleteMilestone(milestone);
			parentDialog.refresh();
		});
		
		return new ContextMenu(new MenuItem[] {edit, delete});
	}
}
