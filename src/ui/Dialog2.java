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

	private double x = 0, y = 0, width = 300, height = 400;
	private String title = "";
	
	public Dialog2(Stage parentStage) {
		this.parentStage = parentStage;
		this.response = new CompletableFuture<T>();
	}

	public CompletableFuture<T> show() {
		Scene scene = new Scene(content(), width, height);
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(title);
		stage.setOnCloseRequest(e -> onClose());
		stage.initOwner(parentStage);
//		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(parentStage.getX() + x);
		stage.setY(parentStage.getY() + y);
		stage.show();
		Platform.runLater(() -> stage.requestFocus());
		return response;
	}

	// Getters and setters for stage properties
	// (Some only work before show() is called)
	
	public Dialog2<T> setTitle(String title) {
		this.title = title;
		if (stage != null) stage.setTitle(title);
		return this;
	}
	
	public Dialog2<T> setSize(double width, double height) {
		this.width = width;
		this.height = height;
		return this;
	}
	
	public Dialog2<T> setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Dialog2<T> center() {
		double x = (parentStage.getWidth() - width) / 2;
		double y = (parentStage.getHeight() - height) / 2;
		setPosition(x, -y);
		return this;
	}

	// Dialog actions

	public void close() {
		stage.close();
	}
	
	protected void completeResponse(T value) {
		response.complete(value);
	}
	
	// To be overridden by subclasses

	protected void onClose() {
		// To be implemented by extending classes
	}

	protected Parent content() {
		// To be implemented by extending classes
		return null;
	}
}
