package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.TurboIssue;

public class IssueIndicatorsDisplayBox extends HBox {
	
	private IntegerProperty parentIssue = null;
	private StringProperty milestoneTitle = null;
	private ArrayList<Object> changeListeners = new ArrayList<Object>();
	private boolean displayWhenEmpty;
	
	public IssueIndicatorsDisplayBox(TurboIssue issue, boolean displayWhenEmpty) {
		this.parentIssue = issue.parentIssueProperty();
		if (issue.getMilestone() != null) {
			this.milestoneTitle = issue.getMilestone().titleProperty();
		}
		this.displayWhenEmpty = displayWhenEmpty;
		setup();
	}
	
	private void setup() {
		if (displayWhenEmpty) {
			getStyleClass().add("faded-borders");
		}
		
		setSpacing(3);
		
		parentIssue.addListener(new WeakChangeListener<Number>(createParentsChangeListener()));
		if (this.milestoneTitle != null) {
			milestoneTitle.addListener(new WeakChangeListener<String>(createMilestoneChangeListener()));
		}
		updateIndicators(null);
	}

	private ChangeListener<String> createMilestoneChangeListener() {
		WeakReference<IssueIndicatorsDisplayBox> that = new WeakReference<IssueIndicatorsDisplayBox>(this);
		ChangeListener<String> changeListener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				if(that.get() != null){
					that.get().updateIndicators(newValue);
				}
			}
		};
		changeListeners.add(changeListener);
		return changeListener;
	}
	
	private ChangeListener<Number> createParentsChangeListener(){
		WeakReference<IssueIndicatorsDisplayBox> that = new WeakReference<IssueIndicatorsDisplayBox>(this);
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				if(that.get() != null){
					that.get().updateIndicators(null);
				}
			}
		};
		changeListeners.add(changeListener);
		return changeListener;
	}

	private void updateIndicators(String newMilestoneTitle) {
		getChildren().clear();
		
		if (this.milestoneTitle != null) {
			Label milestoneLabel = new Label();
			milestoneLabel.getStyleClass().add("display-box-padding");
			if (newMilestoneTitle != null) {
				milestoneLabel.setText(newMilestoneTitle);
			} else {
				milestoneLabel.setText(milestoneTitle.get());
			}
			getChildren().add(milestoneLabel);
		}

		Label label;
		if (displayWhenEmpty && parentIssue.get() <= 0) {
			label = new Label("Parent");
			label.getStyleClass().addAll("faded", "display-box-padding");
			getChildren().add(label);
		} else if(parentIssue.get() >= 0){
			String parentString = "#" + parentIssue.get();
			label = new Label(parentString);
			label.getStyleClass().addAll("display-box-padding");
			getChildren().add(label);
		}
		
		
	}

}
