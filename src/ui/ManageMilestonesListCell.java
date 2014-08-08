package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboMilestone;


public class ManageMilestonesListCell extends ListCell<TurboMilestone> {
	private static final Logger logger = LogManager.getLogger(ManageMilestonesListCell.class.getName());
	private final Stage stage;
	private final Model model;
	private final MilestoneManagementComponent parentDialog;
	
	private ArrayList<ChangeListener<?>> changeListeners = new ArrayList<ChangeListener<?>>();

    public ManageMilestonesListCell(Stage stage, Model model, MilestoneManagementComponent parentDialog) {
		super();
		this.stage = stage;
		this.model = model;
		this.parentDialog = parentDialog;
	}

	@Override
	public void updateItem(TurboMilestone milestone, boolean empty) {
		super.updateItem(milestone, empty);
		if (milestone == null)
			return;
		
		setPadding(new Insets(0, 10, 0, 10));
		setGraphic(createMilestoneItem(milestone));
		setContextMenu(createContextMenu(milestone));
		
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.COPY);
			ClipboardContent content = new ClipboardContent();
			DragData dd = new DragData(DragData.Source.MILESTONE_TAB, milestone.getTitle());
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
			event.consume();
		});
	}

	private Node createMilestoneItem(TurboMilestone milestone) {
		Label title = new Label(milestone.getTitle());
		milestone.titleProperty().addListener(new WeakChangeListener<String>(createTitleListener(milestone, title)));
		
		HBox titleContainer = new HBox();
		HBox.setHgrow(titleContainer, Priority.ALWAYS);
		titleContainer.setAlignment(Pos.BASELINE_LEFT);
		titleContainer.getChildren().add(title);
		
		HBox top = new HBox();
		HBox.setHgrow(top, Priority.ALWAYS);
		top.getChildren().add(titleContainer);
		
		if (milestone.getDueOn() != null) {
			// TODO ADD CHANGE LISTENER
			Label dueDate = new Label("by " + milestone.getDueOnString());
			Tooltip realtiveDueDate = new Tooltip(milestone.relativeDueDateInString());
			dueDate.setTooltip(realtiveDueDate);
			milestone.dueOnStringProperty().addListener(new WeakChangeListener<String>(createDueDateListener(milestone, dueDate)));
			HBox dueDateContainer = new HBox();
			HBox.setHgrow(dueDateContainer, Priority.ALWAYS);
			dueDateContainer.setAlignment(Pos.BASELINE_RIGHT);
			dueDateContainer.getChildren().add(dueDate);
			top.getChildren().add(dueDateContainer);
		}
		
		StackPane progressStack = new StackPane();
		HBox.setHgrow(progressStack, Priority.ALWAYS);
		progressStack.setAlignment(Pos.TOP_CENTER);
		Label progressLabel = new Label(milestone.getClosed() + " of " + (milestone.getOpen() + milestone.getClosed()));
		ProgressBar progressBar = new ProgressBar(milestone.getProgress());
		progressBar.prefWidthProperty().bind(progressStack.widthProperty());
		progressStack.getChildren().addAll(progressBar, progressLabel);
		
		milestone.closedProperty().addListener(new WeakChangeListener<Number>(createProgressListener(milestone, progressStack)));
		milestone.openProperty().addListener(new WeakChangeListener<Number>(createProgressListener(milestone, progressStack)));
		
		Separator seperator = new Separator();
		
		VBox milestoneItem = new VBox();
		milestoneItem.setSpacing(3);
		milestoneItem.getChildren().addAll(top, progressStack, seperator);

		return milestoneItem;
	}
	
    private ChangeListener<String> createDueDateListener(TurboMilestone milestone, Label dueDate) {
    	WeakReference<TurboMilestone> milestoneRef = new WeakReference<TurboMilestone>(milestone);
		ChangeListener<String> changeListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> integerProperty,
					String oldValue, String newValue) {
				TurboMilestone milestone = milestoneRef.get();
				if(milestone != null){
					dueDate.setText(milestone.getDueOnString());
					dueDate.getTooltip().setText(milestone.relativeDueDateInString());
				}
			}
		};
		changeListeners.add(changeListener);
		return changeListener;
	}

	private ChangeListener<Number> createProgressListener(TurboMilestone milestone, StackPane progressStack) {
    	WeakReference<TurboMilestone> milestoneRef = new WeakReference<TurboMilestone>(milestone);
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> integerProperty,
					Number oldValue, Number newValue) {
				TurboMilestone milestone = milestoneRef.get();
				if(milestone != null){
					for(Node node : progressStack.getChildren()){
						if (node instanceof ProgressBar) {
							ProgressBar progressBar = (ProgressBar) node;
							progressBar.setProgress(milestone.getProgress());
						} else if (node instanceof Label) {
							Label progressLabel = (Label) node;
							progressLabel.setText(milestone.getClosed() + " of " + (milestone.getOpen() + milestone.getClosed()));
						}
					}
				}
			}
		};
		changeListeners.add(changeListener);
		return changeListener;
	}

	private ChangeListener<String> createTitleListener(TurboMilestone milestone, Label milestoneTitle){
    	WeakReference<TurboMilestone> milestoneRef = new WeakReference<TurboMilestone>(milestone);
    	ChangeListener<String> listener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				TurboMilestone milestone = milestoneRef.get();
				if(milestone != null){
					milestoneTitle.setText(milestone.getTitle());
				}
			}
		};
		changeListeners.add(listener);
		return listener;
    }

	private ContextMenu createContextMenu(TurboMilestone milestone) {
		MenuItem edit = new MenuItem("Edit Milestone");
		edit.setOnAction((event) -> {
			(new EditMilestoneDialog(stage, milestone)).show().thenApply(response -> {
				model.updateMilestone(response);
				return true;
			}).exceptionally(e -> {
				logger.error(e.getLocalizedMessage(), e);
				return false;
			});
		});
		MenuItem delete = new MenuItem("Delete Milestone");
		delete.setOnAction((event) -> {
			model.deleteMilestone(milestone);
			parentDialog.refresh();
		});
		
		return new ContextMenu(new MenuItem[] {edit, delete});
	}
}
