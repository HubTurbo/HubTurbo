package ui.labelmanagement;

import ui.TurboLabelGroup;
import ui.components.Dialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class EditGroupDialog extends Dialog<TurboLabelGroup> {
	
	private String generatedName;
	private boolean exclusive;
	private boolean showExclusiveCheckbox = true;

	public EditGroupDialog(Stage parentStage, TurboLabelGroup group) {
		super(parentStage);
		this.generatedName = group.getValue();
		this.exclusive = group.isExclusive();
		
		setTitle("Edit Group");
		setSize(330, 45);
	}
	
	public EditGroupDialog setExclusiveCheckboxEnabled(boolean visible) {
		showExclusiveCheckbox = visible;
		return this;
	}
	
	@Override
	protected Parent content() {
		TextField groupNameField = new TextField();
		groupNameField.setText(generatedName);
		
		CheckBox checkbox = new CheckBox("Exclusive");
		checkbox.setSelected(exclusive);
		checkbox.setDisable(!showExclusiveCheckbox);

		Button done = new Button("Done");
		done.setOnAction(e -> {
			if (!groupNameField.getText().isEmpty()) {
				respond(groupNameField.getText(), showExclusiveCheckbox ? checkbox.isSelected() : exclusive);
				close();
			}
		});
		
		HBox layout = new HBox();
		layout.setPadding(new Insets(5));
		layout.setSpacing(10);
		layout.setAlignment(Pos.BASELINE_CENTER);
		layout.getChildren().addAll(groupNameField, checkbox, done);
		
		return layout;
	}

	private void respond(String name, boolean exclusive) {
		TurboLabelGroup res = new TurboLabelGroup(name);
		res.setExclusive(exclusive);
		completeResponse(res);
	}
}
