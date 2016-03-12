package ui.components.pickers;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

public class MilestoneProgressBar extends StackPane {
    private final ProgressBar bar = new ProgressBar();

    MilestoneProgressBar(double progress) {
        bar.setStyle("-fx-accent: #84BE54;");
        bar.setProgress(progress);
        bar.setMinHeight(8);
        bar.setMaxHeight(8);
        bar.setMinWidth(40);
        bar.setMaxWidth(40);
        getChildren().setAll(bar);
    }

}
