package ui.milestonemanagement;

import java.time.LocalDate;

import ui.components.Dialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.TurboMilestone;

public class EditMilestoneDialog extends Dialog<TurboMilestone>{
	
	private TurboMilestone originalMilestone;

	public EditMilestoneDialog(Stage parentStage, TurboMilestone originalMilestone) {
		super(parentStage);
		this.originalMilestone = originalMilestone != null ? originalMilestone : new TurboMilestone();
		
		setTitle("Edit Milestone");
		setSize(330, 45);
	}

	@Override
	protected Parent content() {
		TextField milestoneTitleField = new TextField();
		milestoneTitleField.setPrefWidth(120);
		milestoneTitleField.setText(originalMilestone.getTitle());
		
		DatePicker datePicker = (originalMilestone.getDueOn() != null) ?
			new DatePicker(originalMilestone.getDueOn()) : new DatePicker();
		datePicker.setPrefWidth(110);

		Button done = new Button("Done");
		done.setOnAction(e -> {
			if (!milestoneTitleField.getText().isEmpty()) {
				respond(milestoneTitleField.getText(), datePicker.getValue());
				close();
			}
		});
		
		HBox layout = new HBox();
		layout.setPadding(new Insets(5));
		layout.setSpacing(10);
		layout.setAlignment(Pos.BASELINE_CENTER);
		layout.getChildren().addAll(milestoneTitleField, datePicker, done);

		return layout;
	}

	private void respond(String title, LocalDate dueDate) {
		originalMilestone.setTitle(title);
		originalMilestone.setDueOn(dueDate);
		completeResponse(originalMilestone);
	}
	
}
