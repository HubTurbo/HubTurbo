package ui;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

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
		this.originalMilestone = originalMilestone;
		
		setTitle("Edit Milestone");
		setSize(330, 50);
	}

	@Override
	protected Parent content() {
		TextField milestoneTitleField = new TextField();
		milestoneTitleField.setPrefWidth(120);
		milestoneTitleField.setText(originalMilestone.getTitle());
		
		DatePicker datePicker = (originalMilestone.getDueOn() != null) ?
			new DatePicker(toTimeDate(originalMilestone.getDueOn())) : new DatePicker();
		datePicker.setPrefWidth(110);

		Button done = new Button("Done");
		done.setOnAction(e -> {
			respond(milestoneTitleField.getText(), toUtilDate(datePicker.getValue()));
			close();
		});
		
		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		layout.setAlignment(Pos.BASELINE_CENTER);
		layout.getChildren().addAll(milestoneTitleField, datePicker, done);

		return layout;
	}
	
	private LocalDate toTimeDate(Date date) {
		Instant instant = date.toInstant();
		ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
		LocalDate localDate = zdt.toLocalDate();
		return localDate;
	}

	private Date toUtilDate(LocalDate localDate) {
		long epochInMilliseconds = localDate.toEpochDay() * 24 * 60 * 60 * 1000;
		Date date = new Date(epochInMilliseconds);
		return date;
	}

	private void respond(String title, Date dueDate) {
		originalMilestone.setTitle(title);
		originalMilestone.setDueOn(dueDate);
		completeResponse(originalMilestone);
	}
	
	
	
	
	
}
