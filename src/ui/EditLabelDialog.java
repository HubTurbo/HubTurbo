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
		
		setTitle("Edit Label");
		setSize(330, 50);
	}
		
	private static String toRGBCode(Color color) {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }
	
	@Override
	protected Parent content() {
		TextField labelNameField = new TextField();
		labelNameField.setText(originalLabel.getName());

		ColorPicker colourPicker =  new ColorPicker(Color.web("#" + originalLabel.getColour()));

		Button done = new Button("Done");
		done.setOnAction(e -> {
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
		TurboLabel label = new TurboLabel("doesn't matter");
		label.copyValues(originalLabel);
		label.setName(name);
		label.setColour(rgbCode.substring(1));
		completeResponse(label);
	}
}
