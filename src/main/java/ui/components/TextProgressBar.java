package ui.components;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class TextProgressBar extends StackPane {

	private final static int LABEL_WIDTH_PADDING = 10;
	private final static int LABEL_HEIGHT_PADDING = 4;

	private final ProgressBar bar = new ProgressBar();
	private final Text text = new Text();

	private float progress = 0;

	public TextProgressBar(String label) {

		bar.setMaxWidth(Double.MAX_VALUE);
		bar.getStyleClass().add("progress-bar");

		text.setText(label);

		// We defer this initial update because the text bounds won't be recomputed immediately
		Platform.runLater(this::syncProgress);

		getChildren().setAll(bar, text);
	}

	public void setProgress(float newProgress) {
		progress = Math.min(Math.max(0, newProgress), 1);
		syncProgress();
	}

	public void addProgress(int increment) {
		progress = Math.min(Math.max(0, progress + increment), 1);
		syncProgress();
	}

	private void syncProgress() {
		if (progress < 0.001) {
			bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		} else {
			bar.setProgress(progress);
		}
		bar.setMinHeight(text.getBoundsInLocal().getHeight() + LABEL_HEIGHT_PADDING * 2);
		bar.setMinWidth(text.getBoundsInLocal().getWidth() + LABEL_WIDTH_PADDING * 2);
	}
}

