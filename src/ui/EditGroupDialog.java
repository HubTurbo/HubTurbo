package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditGroupDialog extends Dialog2<TurboLabelGroup> {
	
	private String generatedName;
	private boolean exclusive;
	private boolean showExclusiveCheckbox = true;

	public EditGroupDialog(Stage parentStage, TurboLabelGroup group) {
		super(parentStage);
		this.generatedName = group.getValue();
		this.exclusive = group.isExclusive();
		
		setTitle("Edit Group");
		setSize(330, 90);
	}
	
	public EditGroupDialog setExclusiveCheckboxEnabled(boolean visible) {
		showExclusiveCheckbox = visible;
		return this;
	}
	
	@Override
	protected Parent content() {

		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
				
		Button close = new Button("Close");
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER_RIGHT);
		buttonContainer.getChildren().add(close);
		HBox.setHgrow(buttonContainer, Priority.ALWAYS);
		
		CheckBox checkbox = new CheckBox("Exclusive");
		checkbox.setSelected(exclusive);
		checkbox.setDisable(!showExclusiveCheckbox);
				
		HBox checkBoxContainer = new HBox();
		checkBoxContainer.setAlignment(Pos.CENTER_LEFT);
		checkBoxContainer.getChildren().add(checkbox);
		HBox.setHgrow(checkBoxContainer, Priority.ALWAYS);
		
		HBox bottomContainer = new HBox();
		bottomContainer.getChildren().addAll(checkBoxContainer, buttonContainer);
		
		TextField groupNameField = new TextField();
		groupNameField.setText(generatedName);
		
		close.setOnAction(e -> {
			respond(groupNameField.getText(), showExclusiveCheckbox ? checkbox.isSelected() : exclusive);
			close();
		});

		layout.getChildren().addAll(groupNameField, bottomContainer);
		
		return layout;
	}

	private void respond(String name, boolean exclusive) {
		TurboLabelGroup res = new TurboLabelGroup(name);
		res.setExclusive(exclusive);
		completeResponse(res);
	}
}
