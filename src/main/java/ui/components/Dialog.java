package ui.components;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.concurrent.CompletableFuture;

public abstract class Dialog<T> {

	private final Stage parentStage;
	private Stage stage = null;
	private CompletableFuture<T> response;

	private double width = 300, height = 400;
	private String title = "";
	private StageStyle stageStyle = StageStyle.UTILITY;
	private Modality modality = Modality.APPLICATION_MODAL;

	public Dialog(Stage parentStage) {
		this.parentStage = parentStage;
		this.response = new CompletableFuture<>();
	}

	public CompletableFuture<T> show() {
		Scene scene = new Scene(content(), width, height);
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(title);
		stage.setOnCloseRequest(this::onClose);
		stage.initOwner(parentStage);
		stage.initModality(modality);
		stage.initStyle(stageStyle);
//		stage.setX(parentStage.getX() + x);
//		stage.setY(parentStage.getY() + y);
		stage.show();
		Platform.runLater(stage::requestFocus);
		return response;
	}

	// Getters and setters for stage properties
	// (Some only work before show() is called)
	
	public Dialog<T> setTitle(String title) {
		this.title = title;
		if (stage != null) stage.setTitle(title);
		return this;
	}
	
	public Dialog<T> setSize(double width, double height) {
		this.width = width;
		this.height = height;
		return this;
	}
	
	public Dialog<T> setStageStyle(StageStyle stageStyle) {
		this.stageStyle = stageStyle;
		return this;
	}
	
	public void setModality(Modality modality){
		this.modality = modality;
	}

	// Dialog actions

	public void close() {
		stage.close();
	}
	
	protected void completeResponse(T value) {
		response.complete(value);
	}
	
	// To be overridden by subclasses

	protected void onClose(WindowEvent e) {
		// To be implemented by extending classes
	}

	protected Parent content() {
		// To be implemented by extending classes
		return null;
	}
}
