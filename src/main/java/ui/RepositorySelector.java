package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import util.Utility;

import java.util.function.Consumer;

public class RepositorySelector extends HBox {

	private final ComboBox<String> comboBox = new ComboBox<>();
	private final UI ui;
	private Consumer<String> onValueChangeCallback = e -> {};
	private boolean changesDisabled = false;

	public RepositorySelector(UI ui) {
		this.ui = ui;
		setupLayout();
		setupComboBox();
		getChildren().addAll(comboBox);
		this.setId("repositorySelector");
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
		comboBox.valueProperty().addListener((observable, old, newVal) -> {
			if (Utility.isWellFormedRepoId(newVal) && !changesDisabled) {
				onValueChangeCallback.accept(newVal);
			}
		});
	}
	
	public void setOnValueChange(Consumer<String> callback) {
		assert callback != null;
		onValueChangeCallback = callback;
	}

	private void loadContents() {
		comboBox.getItems().addAll(ui.logic.getOpenRepositories());
	}

	public String getText() {
		return comboBox.getValue();
	}

	public void setText(String repoId) {
		changesDisabled = true;
		comboBox.setValue(repoId);
		changesDisabled = false;
	}

	public void refreshContents() {
		String text = getText();

		comboBox.getItems().clear();
		loadContents();

		setText(text);
	}
}
