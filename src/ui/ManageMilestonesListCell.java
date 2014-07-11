package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Model;
import model.TurboMilestone;

public class ManageMilestonesListCell extends ListCell<TurboMilestone> {
	private final Model model;
	private final MilestoneManagementComponent parentDialog;
	
    private TextField textField;
	private VBox content;
	private ArrayList<ChangeListener<?>> changeListeners = new ArrayList<ChangeListener<?>>();

    public ManageMilestonesListCell(Model model, MilestoneManagementComponent parentDialog) {
		super();
		this.model = model;
		this.parentDialog = parentDialog;
	}
    
    private ChangeListener<Boolean> createTextFieldListener(){
    	WeakReference<ManageMilestonesListCell> that = new WeakReference<>(this);
    	ChangeListener<Boolean> textFieldListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> stringProperty,
					Boolean previouslyFocused, Boolean currentlyFocused) {
				assert previouslyFocused != currentlyFocused;
				ManageMilestonesListCell thisRef = that.get();
				if (thisRef != null && !currentlyFocused) {
                    commitEdit();
				}
			}
		};
		changeListeners.add(textFieldListener);
		return textFieldListener;
    }
    
    private void createTextField() {
        textField = new TextField(getItem().getTitle());
        textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit();
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
        
        textField.focusedProperty().addListener(new WeakChangeListener<Boolean>(createTextFieldListener()));
    }
    
    // This is NOT an overridden method.
    // The overridden one is, however, called in here.
    public void commitEdit() {
    	super.commitEdit(getItem());
    	getItem().setTitle(textField.getText());
    	model.updateMilestone(getItem());
    	parentDialog.refresh();
    }
    
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().getTitle());
        setGraphic(content);
    }
    
    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.setText(getItem().getTitle());
        textField.selectAll();
        textField.requestFocus();
    }
    
    private ChangeListener<String> createMilestoneTitleListener(TurboMilestone milestone, Text milestoneTitle){
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
    
	@Override
	public void updateItem(TurboMilestone milestone, boolean empty) {
		super.updateItem(milestone, empty);
		if (milestone == null)
			return;

		Text milestoneTitle = new Text(milestone.getTitle());
		milestone.titleProperty().addListener(new WeakChangeListener<String>(createMilestoneTitleListener(milestone, milestoneTitle)));

		VBox everything = new VBox();
		everything.setSpacing(2);
		everything.getChildren()
				.addAll(milestoneTitle);

		setGraphic(everything);
		content = everything;
		
		setContextMenu(createContextMenu(milestone));
	}

	private ContextMenu createContextMenu(TurboMilestone milestone) {
		WeakReference<ManageMilestonesListCell> that = new WeakReference<>(this); 
		MenuItem edit = new MenuItem("Edit Milestone");
		edit.setOnAction((event) -> {
			that.get().startEdit();
		});
		MenuItem delete = new MenuItem("Delete Milestone");
		delete.setOnAction((event) -> {
			model.deleteMilestone(milestone);
			parentDialog.refresh();
		});
		
		return new ContextMenu(new MenuItem[] {edit, delete});
	}
}
