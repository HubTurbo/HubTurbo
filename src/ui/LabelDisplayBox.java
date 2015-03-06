package ui;

import java.lang.ref.WeakReference;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import model.TurboLabel;

public class LabelDisplayBox extends FlowPane {

	private ObservableList<TurboLabel> labels;
	private boolean displayWhenEmpty;
	private String labelWhenEmpty;
	
	public LabelDisplayBox() {
		this(FXCollections.observableArrayList(), false, "");
	}

	public LabelDisplayBox(ObservableList<TurboLabel> labels, boolean displayWhenEmpty, String labelWhenEmpty) {
		this.labels = labels;
		this.displayWhenEmpty = displayWhenEmpty;
		this.labelWhenEmpty = labelWhenEmpty;
		setup();
	}

	private void setup() {
		if (displayWhenEmpty) {
			getStyleClass().add("faded-borders");
		}
		setHgap(3);
		setVgap(3);

		labels.addListener(new WeakListChangeListener<TurboLabel>(createLabelsChangeListener()));
		populateWithLabels();
	}
	
	private ListChangeListener<TurboLabel> createLabelsChangeListener(){
		WeakReference<LabelDisplayBox> that = new WeakReference<LabelDisplayBox>(this);
		ListChangeListener<TurboLabel> listener = new ListChangeListener<TurboLabel>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TurboLabel> arg0) {
				if(that.get() != null){
					that.get().populateWithLabels();
				}
			}
		};
		return listener;
	}

	private void populateWithLabels() {
		getChildren().clear();
		
		if (displayWhenEmpty && labels.size() == 0) {
			
			Label noLabels = new Label(labelWhenEmpty);
			noLabels.getStyleClass().addAll("faded", "display-box-padding");
			getChildren().add(noLabels);

			return;
		}
		
		for (TurboLabel label : labels) {
			getChildren().add(label.getNode());
		}
	}
	
	public LabelDisplayBox setLabels(ObservableList<TurboLabel> labels) {
		this.labels = labels;
		populateWithLabels();
		return this;
	}
}
