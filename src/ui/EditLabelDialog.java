package ui;

import java.util.concurrent.CompletableFuture;

import model.TurboLabel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EditLabelDialog implements Dialog<TurboLabel> {
	
	private final Stage parentStage;

	private CompletableFuture<TurboLabel> response;

	private TurboLabel originalLabel;

	public EditLabelDialog(Stage parentStage, TurboLabel originalLabel) {
		this.parentStage = parentStage;
		this.originalLabel = originalLabel;

		response = new CompletableFuture<>();
	}
		
	public CompletableFuture<TurboLabel> show() {
		showDialog();
		return response;
	}

	private static String toRGBCode(Color color) {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }
	
	private void showDialog() {

		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		Scene scene = new Scene(layout, 330, 150);

		Stage stage = new Stage();

		Platform.runLater(() -> stage.requestFocus());
				
		TextField labelNameField = new TextField();
		labelNameField.setText(originalLabel.getName());

		ColorPicker colourPicker =  new ColorPicker(Color.web("#" + originalLabel.getColour()));

		Button close = new Button("Close");
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER_RIGHT);
		buttonContainer.getChildren().add(close);
		HBox.setHgrow(buttonContainer, Priority.ALWAYS);

		layout.getChildren().addAll(labelNameField, colourPicker, buttonContainer);

		stage.setTitle("Edit Label");
		stage.setScene(scene);
//		stage.setOnCloseRequest(e -> {
//		});
		stage.initOwner(parentStage);
//		 secondStage.initModality(Modality.APPLICATION_MODAL);

		close.setOnAction(e -> {
			respond(labelNameField.getText(), toRGBCode(colourPicker.getValue()));
			stage.close();
		});

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}

	private void respond(String name, String rgbCode) {
		TurboLabel label = new TurboLabel("doesn't matter");
		label.copyValues(originalLabel);
		label.setName(name);
		label.setColour(rgbCode.substring(1));
		response.complete(label);
	}
}
