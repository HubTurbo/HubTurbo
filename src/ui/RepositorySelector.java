package ui;

import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import storage.DataManager;

public class RepositorySelector extends HBox {
	private final ComboBox<String> comboBox = new ComboBox<>();
	private Consumer<String> methodOnValueChange;

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
		loadComboBoxContents();
		comboBox.valueProperty().addListener((observable, old, newVal) -> {
			if (methodOnValueChange != null) {
				methodOnValueChange.accept(newVal);
			}
		});
	}

	public void setValue(String val) {
		comboBox.setValue(val);
	}

	public void setComboValueChangeMethod(Consumer<String> method) {
		methodOnValueChange = method;
	}

	private void loadComboBoxContents() {
		List<String> items = DataManager.getInstance().getLastViewedRepositories();
		comboBox.getItems().addAll(items);
	}

	public void refreshComboBoxContents() {
		comboBox.getItems().clear();
		loadComboBoxContents();
	}
}
