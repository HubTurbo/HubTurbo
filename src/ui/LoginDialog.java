package ui;

import java.io.BufferedReader;
import java.io.FileReader;

import org.controlsfx.control.NotificationPane;

import service.ServiceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginDialog extends Dialog2<Boolean> {

	public LoginDialog(Stage parentStage) {
		super(parentStage);
	}
	
	@Override
	protected void onClose() {
		completeResponse(false);
	}
	
	@Override
	protected Parent content() {

		setTitle("GitHub Login");
		setSize(320, 200);
		setModality(Modality.APPLICATION_MODAL);

		NotificationPane notificationPane = new NotificationPane();
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Label repoNameLabel = new Label("Repository:");
		grid.add(repoNameLabel, 0, 0);

		TextField repoOwnerField = new TextField("HubTurbo");
		grid.add(repoOwnerField, 1, 0);

		Label slash = new Label("/");
		grid.add(slash, 2, 0);

		TextField repoNameField = new TextField("HubTurbo");
		grid.add(repoNameField, 3, 0);

		Label usernameLabel = new Label("Username:");
		grid.add(usernameLabel, 0, 1);

		TextField usernameField = new TextField();
		grid.add(usernameField, 1, 1, 3, 1);

		Label passwordLabel = new Label("Password:");
		grid.add(passwordLabel, 0, 2);

		PasswordField passwordField = new PasswordField();
		grid.add(passwordField, 1, 2, 3, 1);

		repoOwnerField.setMaxWidth(80);
		repoNameField.setMaxWidth(80);

		Button loginButton = new Button("Sign in");
		loginButton.setOnAction(ev -> onLoginClick(repoOwnerField.getText(), repoNameField.getText(), usernameField.getText(), passwordField.getText(), notificationPane));

		HBox buttons = new HBox(10);
		buttons.setAlignment(Pos.BOTTOM_RIGHT);
		buttons.getChildren().add(loginButton);
		grid.add(buttons, 3, 3);
		
		notificationPane.setContent(grid);
		
		return notificationPane;
	}
	

	private void onLoginClick(String owner, String repo, String username, String password, NotificationPane notificationPane) {
		
		if (username.isEmpty() && password.isEmpty()) {
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader("credentials.txt"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (username.isEmpty())
						username = line;
					else
						password = line;
				}
				System.out.println("Logged in using credentials.txt");
			} catch (Exception e) {
				System.out.println("Failed to find or open credentials.txt");
			}
		}

		boolean success = ServiceManager.getInstance().login(username, password);
		
		if (!success) {
//		        notificationPane.getActions().addAll(new AbstractAction("Retry") {
//		            @Override public void handle(ActionEvent ae) {
//		            	System.out.println("clicked button");
//		            	notificationPane.hide();
//		            }
//		        });
			notificationPane.setText("Failed to log in. Please try again.");
			notificationPane.show();
		}
		else {
			loadRepository(owner, repo);
			completeResponse(true);
			close();
		}
	}
	
	private void loadRepository(String owner, String repoName) {
		ServiceManager.getInstance().setupRepository(owner, repoName);

		setTitle("HubTurbo (" + ServiceManager.getInstance().getRemainingRequests() + " requests remaining out of " + ServiceManager.getInstance().getRequestLimit() + ")");
//		enableMenuItemsRequiringLogin();
	}
}
