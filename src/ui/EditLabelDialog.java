package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.TurboLabel;

public class EditLabelDialog extends Dialog<TurboLabel> {

	private TurboLabel originalLabel;

	public EditLabelDialog(Stage parentStage, TurboLabel originalLabel) {
		super(parentStage);
		this.originalLabel = originalLabel;
	}
		
	private static String toRGBCode(Color color) {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }
	
	@Override
	protected Parent content() {
		
		setTitle("Edit Label");
		setSize(400, 50);

		TextField labelNameField = new TextField();
		if (originalLabel.getGroup() == null) {
			labelNameField.setText(originalLabel.getName());
		} else {
			labelNameField.setText(originalLabel.getGroup() + "." + originalLabel.getName());
		}

		ColorPicker colourPicker =  new ColorPicker(Color.web("#" + originalLabel.getColour()));

		Button done = new Button("Done");
		done.setOnAction(e -> {
			respond(labelNameField.getText(), toRGBCode(colourPicker.getValue()));
			close();
		});

		labelNameField.setOnAction(e -> {
			respond(labelNameField.getText(), toRGBCode(colourPicker.getValue()));
			close();
		});

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		layout.setAlignment(Pos.BASELINE_CENTER);
		layout.getChildren().addAll(labelNameField, colourPicker, done);

		return layout;
	}
	
	private void respond(String name, String rgbCode) {
		
		String group = null;
		String[] nameParts = TurboLabel.parseName(name);
		if (nameParts != null) {
			group = nameParts[0];
			name = nameParts[1];
		}
		
		TurboLabel label = new TurboLabel("doesn't matter");
		label.copyValues(originalLabel);
		label.setName(name);
		label.setGroup(group);
		label.setColour(rgbCode.substring(1));
		completeResponse(label);
	}
}
