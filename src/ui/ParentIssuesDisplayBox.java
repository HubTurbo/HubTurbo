package ui;

import java.lang.ref.WeakReference;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ParentIssuesDisplayBox extends HBox {
	
	private ObservableList<Integer> issueNumbers = null;
	private ListChangeListener<Integer> listChangeListener;
	private boolean displayWhenEmpty;
	
	public ParentIssuesDisplayBox(ObservableList<Integer> items, boolean displayWhenEmpty) {
		setListableItems(items);
		this.displayWhenEmpty = displayWhenEmpty;
		setup();
	}
	
	private void setup() {
		if (displayWhenEmpty) {
			getStyleClass().add("faded-borders");
		}
		update();
	}

	private void setListableItems(ObservableList<Integer> issueNumbers) {
		this.issueNumbers = issueNumbers;
		initialiseChangeListener();
		issueNumbers.addListener(new WeakListChangeListener<Integer>(listChangeListener));
		
		update();
	}
	
	private void initialiseChangeListener(){
		if(listChangeListener != null){
			listChangeListener = null;
		}
		if(this.issueNumbers != null){
			WeakReference<ParentIssuesDisplayBox> that = new WeakReference<ParentIssuesDisplayBox>(this);
			listChangeListener = new ListChangeListener<Integer>() {
				@Override
				public void onChanged(ListChangeListener.Change<? extends Integer> arg0) {
					if(that.get() != null){
						that.get().update();
					}
				}
			};
		}
	}
	
	private void update() {
		getChildren().clear();

		Label label;
		if (displayWhenEmpty && issueNumbers.size() == 0) {
			label = new Label("Parents");
			label.setStyle(UI.STYLE_FADED + "-fx-padding: 5;");
			getChildren().add(label);
		} else {
			StringBuilder parentSB = new StringBuilder();
			for (Integer p : issueNumbers) {
				parentSB.append("#" + p);
				parentSB.append(", ");
			}
			if (parentSB.length() != 0) parentSB.delete(parentSB.length()-2, parentSB.length());

			if (displayWhenEmpty || (!displayWhenEmpty && !parentSB.toString().isEmpty())) {
				label = new Label(parentSB.toString());
				label.setStyle("-fx-padding: 5;");
				getChildren().add(label);
			}
		}
	}
	
}
