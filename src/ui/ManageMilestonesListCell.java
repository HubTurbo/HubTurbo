package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Model;
import model.TurboMilestone;

public class ManageMilestonesListCell extends ListCell<TurboMilestone> {
	private final Model model;
	
	public ManageMilestonesListCell(Model model) {
		super();
		this.model = model;
	}

	@Override
	public void updateItem(TurboMilestone milestone, boolean empty) {
		super.updateItem(milestone, empty);
		if (milestone == null)
			return;

		Text milestoneTitle = new Text(milestone.getTitle());
		milestone.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				milestoneTitle.setText(milestone.getTitle());
			}
		});

		VBox everything = new VBox();
		everything.setSpacing(2);
		everything.getChildren()
				.addAll(milestoneTitle);

		setGraphic(everything);
		
		setContextMenu(createContextMenu());
	}

	private ContextMenu createContextMenu() {
		MenuItem edit = new MenuItem("Edit Milestone");
		edit.setOnAction((event) -> {
			System.out.println("edit milestone");
		});
		MenuItem delete = new MenuItem("Delete Milestone");
		delete.setOnAction((event) -> {
			System.out.println("delete milestone");
		});
		
		return new ContextMenu(new MenuItem[] {edit, delete});
	}
}
