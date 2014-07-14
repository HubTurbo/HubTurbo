package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
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
	private ArrayList<Object> changeListeners = new ArrayList<Object>();
	
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
		setMaxWidth(330);
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
		changeListeners.add(listener);
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
			Label labelText = new Label(label.getName());
			labelText.getStyleClass().add("labels");
			labelText.setStyle(getBackgroundColourStyle(label));
			label.nameProperty().addListener(new WeakChangeListener<String>(createLabelNameListener(labelText)));
			if (label.getGroup() != null) {
				Tooltip groupTooltip = new Tooltip(label.getGroup());
				label.groupProperty().addListener(new WeakChangeListener<String>(createLabelGroupListener(groupTooltip)));
				labelText.setTooltip(groupTooltip);
			}
			getChildren().add(labelText);
		}
	}
	
	private ChangeListener<String> createLabelNameListener(Label labelText){
		WeakReference<Label> labelTextRef = new WeakReference<Label>(labelText);
		ChangeListener<String> listener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				Label labelText = labelTextRef.get();
				if(labelText != null){
					labelText.setText(newValue);
				}
			}
		};
		changeListeners.add(listener);
		return listener;
	}

	private ChangeListener<String> createLabelGroupListener(Tooltip groupTooltip) {
		WeakReference<Tooltip> groupTooltipRef = new WeakReference<Tooltip>(groupTooltip);
		ChangeListener<String> listener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				Tooltip groupToolTip = groupTooltipRef.get();
				if(groupToolTip != null){
					groupToolTip.setText(newValue);
				}
			}
		};
		changeListeners.add(listener);
		return listener;
	}
	
	public LabelDisplayBox setLabels(ObservableList<TurboLabel> labels) {
		clearChangeListeners();
		this.labels = labels;
		populateWithLabels();
		return this;
	}
	
	private void clearChangeListeners(){
		changeListeners.clear();
	}

	private String getBackgroundColourStyle(TurboLabel label) {
		return "-fx-background-color: #" + label.getColour() + ";";
	}
}
