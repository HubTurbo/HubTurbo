package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RepositorySelector extends HBox {

	private final ComboBox<String> comboBox = new ComboBox<>();
	private Consumer<String> onValueChangeCallback = e -> {};

	public RepositorySelector() {
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
			onValueChangeCallback.accept(newVal);
		});
	}
	
	public void setOnValueChange(Consumer<String> callback) {
		onValueChangeCallback = callback;
	}

	private void loadContents() {
//		List<String> items = DataManager.getInstance().getLastViewedRepositories();
		List<String> items = new ArrayList<>();
		comboBox.getItems().addAll(items);
	}

	public void refreshContents(String repoId) {
		comboBox.getItems().clear();
		loadContents();
		comboBox.setValue(repoId);
	}
}
