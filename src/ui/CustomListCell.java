package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import logic.TurboIssue;
import logic.TurboLabel;

public class CustomListCell extends ListCell<TurboIssue> {

	private static final String STYLE_PARENT_NAME = "-fx-font-size: 11px;";
	private static final String STYLE_ISSUE_NAME = "-fx-font-size: 24px;";

	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;

		VBox everything = new VBox();

		Text issueName = new Text(issue.getTitle());
		issueName.setStyle(STYLE_ISSUE_NAME);
		issue.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				issueName.setText(newValue);
			}
		});

		Text parentName = new Text("parent");
		parentName.setStyle(STYLE_PARENT_NAME);

		HBox labels = new HBox();
		labels.setStyle(Demo.STYLE_BORDERS);
		issue.getLabels().addListener(new ListChangeListener<TurboLabel>() {

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends TurboLabel> arg0) {
				populateLabels(labels, issue.getLabels());
			}

		});
		populateLabels(labels, issue.getLabels());

		Text assignees = new Text("assignees");

		everything.getChildren().addAll(issueName, parentName, labels,
				assignees);
		// everything.getChildren().stream().forEach((node) ->
		// node.setStyle(Demo.STYLE_BORDERS));

		if (issue != null) {
			setGraphic(everything);
		}
	}

	private void populateLabels(HBox parent, ObservableList<TurboLabel> labels) {
		for (TurboLabel label : labels) {
			Text labelText = new Text(label.getName());
			label.nameProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(
						ObservableValue<? extends String> stringProperty,
						String oldValue, String newValue) {
					labelText.setText(newValue);
				}
			});
			parent.getChildren().add(labelText);
		}
	}
}
