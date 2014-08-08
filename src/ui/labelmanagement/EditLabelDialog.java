package ui.labelmanagement;

import java.util.concurrent.CompletableFuture;

import ui.Dialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.TurboLabel;

public class EditLabelDialog extends Dialog<TurboLabel> {
	private TurboLabel originalLabel;
	TextField labelGrpField;
	TextField labelNameField;

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
	public CompletableFuture<TurboLabel> show(){
		CompletableFuture<TurboLabel> response = super.show();
		labelNameField.requestFocus();
		return response;
	}
	
	@Override
	protected Parent content() {
		
		setTitle("Edit Label");
		setSize(450, 45);
		
		labelGrpField = new TextField();
		if(originalLabel.getGroup() != null){
			labelGrpField.setText(originalLabel.getGroup());
		}
		labelGrpField.setPrefWidth(80);
		
		Text delimiter = new Text(originalLabel.getGroupDelimiter());
		
		labelNameField = new TextField();
		labelNameField.setText(originalLabel.getName());
		labelNameField.setPrefWidth(150);

		ColorPicker colourPicker =  new ColorPicker(Color.web("#" + originalLabel.getColour()));

		Button done = new Button("Done");
		done.setOnAction(e -> {
			if (!labelNameField.getText().isEmpty()) {
				respond(labelGrpField.getText(), labelNameField.getText(), toRGBCode(colourPicker.getValue()));
				close();
			}
		});

		labelNameField.setOnAction(e -> {
			if (!labelNameField.getText().isEmpty()) {
				respond(labelGrpField.getText(), labelNameField.getText(), toRGBCode(colourPicker.getValue()));
				close();
			}
		});

		HBox layout = new HBox();
		layout.setPadding(new Insets(5));
		layout.setSpacing(10);
		layout.setAlignment(Pos.BASELINE_CENTER);
		layout.getChildren().addAll(labelGrpField, delimiter, labelNameField, colourPicker, done);

		return layout;
	}
	
	private void respond(String grp, String name, String rgbCode) {		
		TurboLabel label = new TurboLabel();
		label.copyValues(originalLabel);
		label.setName(name);
		if(!grp.isEmpty()){
			String group = grp.replaceAll("\\.", "");
			group = group.replaceAll("\\-", "");
			label.setGroup(group);
		}
		label.setColour(rgbCode.substring(1));
		completeResponse(label);
	}
	
}
