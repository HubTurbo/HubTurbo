package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.TurboIssue;

public class IssueIndicatorsDisplayBox extends HBox {
	
	private ObservableList<Integer> parentIssues = null;
	private StringProperty milestoneTitle = null;
	private ArrayList<Object> changeListeners = new ArrayList<Object>();
	private boolean displayWhenEmpty;
	
	public IssueIndicatorsDisplayBox(TurboIssue issue, boolean displayWhenEmpty) {
		this.parentIssues = issue.getParentsReference();
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
		
		parentIssues.addListener(new WeakListChangeListener<Integer>(createParentsChangeListener()));
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
	
	private ListChangeListener<Integer> createParentsChangeListener(){
		WeakReference<IssueIndicatorsDisplayBox> that = new WeakReference<IssueIndicatorsDisplayBox>(this);
		ListChangeListener<Integer> listChangeListener = new ListChangeListener<Integer>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Integer> arg0) {
				if(that.get() != null){
					that.get().updateIndicators(null);
				}
			}
		};
		changeListeners.add(listChangeListener);
		return listChangeListener;
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
		if (displayWhenEmpty && parentIssues.size() == 0) {
			label = new Label("Parents");
			label.getStyleClass().addAll("faded", "display-box-padding");
			getChildren().add(label);
		} else {
			StringBuilder parentSB = new StringBuilder();
			for (Integer p : parentIssues) {
				parentSB.append("#" + p);
				parentSB.append(", ");
			}
			if (parentSB.length() != 0) parentSB.delete(parentSB.length()-2, parentSB.length());

			if (displayWhenEmpty || (!displayWhenEmpty && !parentSB.toString().isEmpty())) {
				label = new Label(parentSB.toString());
				label.getStyleClass().addAll("display-box-padding");
				getChildren().add(label);
			}
		}
		
		
	}

}
