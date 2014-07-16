package ui;

import model.TurboLabel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EditLabelDialog extends Dialog<TurboLabel> {

	private TurboLabel originalLabel;

	public EditLabelDialog(Stage parentStage, TurboLabel originalLabel) {
		super(parentStage);
		this.originalLabel = originalLabel;
		
		setTitle("Edit Label");
		setSize(330, 125);
	}
		
	private static String toRGBCode(Color color) {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }
	
	@Override
	protected Parent content() {

		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		TextField labelNameField = new TextField();
		labelNameField.setText(originalLabel.getName());

		ColorPicker colourPicker =  new ColorPicker(Color.web("#" + originalLabel.getColour()));

		Button done = new Button("Done");
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER_RIGHT);
		buttonContainer.getChildren().add(done);
		HBox.setHgrow(buttonContainer, Priority.ALWAYS);

		layout.getChildren().addAll(labelNameField, colourPicker, buttonContainer);

		done.setOnAction(e -> {
			respond(labelNameField.getText(), toRGBCode(colourPicker.getValue()));
			close();
		});

		return layout;
	}
	
	private void respond(String name, String rgbCode) {
		TurboLabel label = new TurboLabel("doesn't matter");
		label.copyValues(originalLabel);
		label.setName(name);
		label.setColour(rgbCode.substring(1));
		completeResponse(label);
	}
}
