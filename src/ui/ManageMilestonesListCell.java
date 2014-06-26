package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Model;
import model.TurboMilestone;

public class ManageMilestonesListCell extends ListCell<TurboMilestone> {
	private final Stage mainStage;
	private final Model model;
	
	public ManageMilestonesListCell(Stage mainStage, Model model) {
		super();
		this.mainStage = mainStage;
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
	}
}
