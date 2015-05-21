package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import storage.Preferences;

import java.util.List;
import java.util.function.Consumer;

public class RepositorySelector extends HBox {

	private final ComboBox<String> comboBox = new ComboBox<>();
	private final Preferences prefs;
	private Consumer<String> onValueChangeCallback = e -> {};
	private boolean changesDisabled = false;

	public RepositorySelector(Preferences prefs) {
		this.prefs = prefs;
		setupLayout();
		setupComboBox();
		getChildren().addAll(comboBox);
	}

	private void setupLayout() {
		setSpacing(5);
		setPadding(new Insets(5));
		setAlignment(Pos.BASELINE_LEFT);
		comboBox.setPrefWidth(250);
	}

	private void setupComboBox() {
		comboBox.setFocusTraversable(false);
		comboBox.setEditable(true);
		loadContents();
		comboBox.valueProperty().addListener((observable, old, newVal) -> {
			System.out.println(old + " " + newVal);
			if (!changesDisabled) {
				onValueChangeCallback.accept(newVal);
			}
		});
	}
	
	public void setOnValueChange(Consumer<String> callback) {
		onValueChangeCallback = callback;
	}

	private void loadContents() {
		List<String> items = prefs.getLastViewedRepositories();
		comboBox.getItems().addAll(items);
	}

	public void refreshContents(String repoId) {
		comboBox.getItems().clear();
		loadContents();
		changesDisabled = true;
		comboBox.setValue(repoId);
		changesDisabled = false;
	}
}
