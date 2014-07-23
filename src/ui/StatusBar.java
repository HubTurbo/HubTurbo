package ui;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class StatusBar extends HBox {

	private static StatusBar instance = null;
	public static StatusBar getInstance() {
		if (instance == null) {
			instance = new StatusBar();
		}
		return instance;
	}
	
	private final Label text;
	
	public StatusBar() {
		text = new Label();
		HBox.setMargin(text, new Insets(3));
		HBox.setHgrow(this, Priority.ALWAYS);
		getStyleClass().add("top-borders");
		getChildren().add(text);
	}
	
	public static void displayMessage(String text) {
		if (!getInstance().signingIn) {
			getInstance().text.setText(text);
		}
	}
	
	private Timer signIn = new Timer();
	private boolean signingIn = false;
	private TimerTask animateLabel = new TimerTask() {
	    public void run() {
	         Platform.runLater(() -> {
	        	 if (text.getText().endsWith("...")) {
		        	 text.setText("Signing in");
	        	 }
	        	 else if (text.getText().endsWith("..")) {
		        	 text.setText("Signing in...");
	        	 }
	        	 else if (text.getText().endsWith(".")) {
		        	 text.setText("Signing in..");
	        	 }
	        	 else {
	        		 text.setText("Signing in.");
	        	 }
	         });
	    }
	};
	
	public static void setSigningIn(boolean yes) {
		getInstance().signingIn = yes;
		if (yes) {
			getInstance().signIn.scheduleAtFixedRate(getInstance().animateLabel, 200, 200);
		} else {
			getInstance().signIn.cancel();
			getInstance().signIn.purge();
		}
	}
	
}
