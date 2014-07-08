package ui;

import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class Dialog2<T> {
	
	private final Stage parentStage;
	private Stage stage;
	private CompletableFuture<T> response;

	public Dialog2(Stage parentStage) {
		this.parentStage = parentStage;
		this.stage = new Stage();
		this.response = new CompletableFuture<T>();
	}

	public CompletableFuture<T> show() {
		Scene scene = new Scene(content(), stage.getWidth(), stage.getHeight());
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> onClose());
		stage.initOwner(parentStage);
//		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());
		stage.show();
		Platform.runLater(() -> stage.requestFocus());
		return response;
	}
		
	public Dialog2<T> setTitle(String title) {
		stage.setTitle(title);
		return this;
	}
	
	public Dialog2<T> setSize(int width, int height) {
		stage.setWidth(width);
		stage.setHeight(height);
		return this;
	}
	
	public void close() {
		stage.close();
	}
	
	protected void completeResponse(T value) {
		response.complete(value);
	}

	protected void onClose() {
		// To be implemented by extending classes
	}

	protected Parent content() {
		// To be implemented by extending classes
		return null;
	}
}
