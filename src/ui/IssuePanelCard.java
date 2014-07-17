package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.TurboIssue;
import model.TurboLabel;

public class IssuePanelCard extends VBox {

	/**
	 * A card that is constructed with an issue as argument. Its components
	 * are bound to the issue's fields and will update automatically.
	 */
	
	private final TurboIssue issue;
	private FlowPane issueDetails = new FlowPane();
	private ArrayList<Object> changeListeners = new ArrayList<Object>();
	
	public IssuePanelCard(TurboIssue issue) {
		this.issue = issue;
		setup();
	}
	
	private void setup() {
		Text issueTitle = new Text("#" + issue.getId() + " " + issue.getTitle());
		issueTitle.setWrappingWidth(340);
		issueTitle.getStyleClass().add("issue-panel-name");
		if (!issue.getOpen()) issueTitle.getStyleClass().add("issue-panel-closed");
		issue.titleProperty().addListener(new WeakChangeListener<String>(createIssueTitleListener(issue, issueTitle)));
	
		setupIssueDetailsBox();
		
		setPadding(new Insets(0,0,3,0));
		setSpacing(1);
		getChildren().addAll(issueTitle, issueDetails);
	}
	
	private void setupIssueDetailsBox() {
		issueDetails.setMaxWidth(340);
		issueDetails.setPrefWrapLength(340);
		issueDetails.setHgap(3);
		
		issue.getLabelsReference().addListener(new WeakListChangeListener<TurboLabel>(createLabelsChangeListener()));
		issue.parentIssueProperty().addListener(new WeakChangeListener<Number>(createParentsChangeListener()));
		if (issue.getMilestone() != null) {
			issue.getMilestone().titleProperty().addListener(new WeakChangeListener<String>(createMilestoneChangeListener()));
		}
		updateDetails();
	}
	
	private void updateDetails() {
		issueDetails.getChildren().clear();
		
		for (TurboLabel label : issue.getLabels()) {
			Label labelText = new Label(label.getName());
			labelText.getStyleClass().add("labels");
			labelText.setStyle(getBackgroundColourStyle(label));
			label.nameProperty().addListener(new WeakChangeListener<String>(createLabelNameListener(labelText)));
			if (label.getGroup() != null) {
				Tooltip groupTooltip = new Tooltip(label.getGroup());
				label.groupProperty().addListener(new WeakChangeListener<String>(createLabelGroupListener(groupTooltip)));
				labelText.setTooltip(groupTooltip);
			}
			issueDetails.getChildren().add(labelText);
		}
		
		if(issue.getParentIssue() >= 0){
			String parentString = "#" + issue.getParentIssue();
			Label parent = new Label(parentString);
			parent.getStyleClass().addAll("display-box-padding");
			issueDetails.getChildren().add(parent);
		}
		
		if (issue.getMilestone() != null) {
			Label milestone = new Label(issue.getMilestone().getTitle());
			issueDetails.getChildren().add(milestone);
		}

		if (issue.getAssignee() != null) {
			Label assignee = new Label((issue.getAssignee().getGithubName()));
			assignee.getStyleClass().add("display-box-padding");
			issueDetails.getChildren().add(assignee);
		}
		
	}
	
	private String getBackgroundColourStyle(TurboLabel label) {
		return "-fx-background-color: #" + label.getColour() + ";";
	}

	private ListChangeListener<TurboLabel> createLabelsChangeListener(){
		WeakReference<IssuePanelCard> that = new WeakReference<IssuePanelCard>(this);
		ListChangeListener<TurboLabel> listener = new ListChangeListener<TurboLabel>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TurboLabel> arg0) {
				if(that.get() != null){
					that.get().updateDetails();
				}
			}
		};
		changeListeners.add(listener);
		return listener;
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
	
	private ChangeListener<Number> createParentsChangeListener(){
		WeakReference<IssuePanelCard> that = new WeakReference<IssuePanelCard>(this);
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> integerProperty,
					Number oldValue, Number newValue) {
				if(that.get() != null){
					that.get().updateDetails();
				}
			}
		};
		changeListeners.add(changeListener);
		return changeListener;
	}
	
	private ChangeListener<String> createMilestoneChangeListener() {
		WeakReference<IssuePanelCard> that = new WeakReference<IssuePanelCard>(this);
		ChangeListener<String> changeListener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				if(that.get() != null){
					that.get().updateDetails();
				}
			}
		};
		changeListeners.add(changeListener);
		return changeListener;
	}

	private ChangeListener<String> createIssueTitleListener(TurboIssue issue, Text issueName){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		ChangeListener<String> titleChangeListener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				TurboIssue issue = issueRef.get();
				if(issue != null){
					issueName.setText("#" + issue.getId() + " " + newValue);
				}
			}
		};
		changeListeners.add(titleChangeListener);
		return titleChangeListener;
	}
}
