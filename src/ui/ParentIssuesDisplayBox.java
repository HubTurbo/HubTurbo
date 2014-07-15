package ui;

import java.lang.ref.WeakReference;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.TurboIssue;

public class ParentIssuesDisplayBox extends HBox {
	
	private TurboIssue issue;
	private IntegerProperty issueNumber = null;
	private ChangeListener<Number> changeListener;
	
	public ParentIssuesDisplayBox(TurboIssue issue) {
		this.issue = issue;
		setListableItem(issue.parentIssueProperty());
		setup();
	}
	
	private void setup() {
		getStyleClass().add("faded-borders");
		update();
	}

	private void setListableItem(IntegerProperty issueNumber) {
		this.issueNumber = issueNumber;
		initialiseChangeListener();
		issueNumber.addListener(new WeakChangeListener<Number>(changeListener));
		
		update();
	}
	
	private void initialiseChangeListener(){
		if(changeListener != null){
			changeListener = null;
		}
		if(this.issueNumber != null){
			WeakReference<ParentIssuesDisplayBox> that = new WeakReference<ParentIssuesDisplayBox>(this);
			changeListener = new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					that.get().update();
				}
			};
		}
	}
	
	private void update() {
		getChildren().clear();

		Label label;
		if (issueNumber.get() <= 0) {
			label = new Label("Parent");
			label.getStyleClass().addAll("faded", "display-box-padding");
			getChildren().add(label);
		} else {
			String parentString = "#" + issueNumber.get() + " " + issue.parentReference().getTitle();
			label = new Label(parentString);
			label.getStyleClass().addAll("display-box-padding");
			getChildren().add(label);
		}
	}
	
}
