package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    
    private ChangeListener<String> createMilestoneTitleListener(TurboMilestone milestone, Text milestoneTitle){
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
    
	@Override
	public void updateItem(TurboMilestone milestone, boolean empty) {
		super.updateItem(milestone, empty);
		if (milestone == null)
			return;

		Text milestoneTitle = new Text(milestone.getTitle());
		milestone.titleProperty().addListener(new WeakChangeListener<String>(createMilestoneTitleListener(milestone, milestoneTitle)));

		VBox everything = new VBox();
		everything.setSpacing(2);
		everything.getChildren()
				.addAll(milestoneTitle);

		setGraphic(everything);
		
		setContextMenu(createContextMenu(milestone));
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
