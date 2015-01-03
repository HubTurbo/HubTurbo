package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.TurboLabel;

import org.controlsfx.control.textfield.TextFields;

import ui.components.BetterCheckListView;
import ui.components.Dialog;
import ui.labelmanagement.LabelManagementComponent;

public class LabelCheckboxListDialog extends Dialog<List<TurboLabel>> {

	private static final double WINDOW_WIDTH = 250;
	private static final double WINDOW_HEIGHT = 370;
	private static final int ROW_HEIGHT = 30;
	
	HashMap<String, ArrayList<TurboLabel>> groups;
	private ArrayList<TurboLabel> initialChecked;
	private ObservableList<TurboLabel> labels;
	private HashMap<String, BetterCheckListView> controls = new HashMap<>();
	private List<String> labelSuggestions = new ArrayList<>();
	
	private TextField autoCompleteBox;
	private Button done;
	private ListView<VBox> display = new ListView<>();
	
	public LabelCheckboxListDialog(Stage parentStage, ObservableList<TurboLabel> labels) {
		super(parentStage);
		this.labels = labels;
	}
	
	private void setupAutoCompleteBox(){
		autoCompleteBox = new TextField();
		TextFields.bindAutoCompletion(autoCompleteBox, labelSuggestions);
		WeakReference<LabelCheckboxListDialog> selfRef = new WeakReference<>(this);
		WeakReference<TextField> fieldRef = new WeakReference<>(autoCompleteBox);
		autoCompleteBox.setOnKeyReleased(e -> {
			KeyCode code = e.getCode();
			LabelCheckboxListDialog self = selfRef.get();
			switch (code) {
            case ENTER:
                if(self != null){
                	if(self.checkLabelWithGhName(fieldRef.get().getText())){
                		fieldRef.get().setText("");
                	}
                }
                break;
            default:
                break;
            }
		});
	}
	
	private boolean checkLabelWithGhName(String name){
		String[] tokens = TurboLabel.parseName(name);
		BetterCheckListView checklist;
		String labelName;
		String groupName;
		if(tokens == null){
			groupName = LabelManagementComponent.UNGROUPED_NAME;
			labelName = name;
		}else{
			groupName = tokens[0];
			labelName = tokens[1];
		}
		checklist = controls.get(groupName);
		boolean result = checklist.checkItem(labelName);
		scrollToGroup(groupName);
		return result;
	}
	
	private void scrollToGroup(String groupName){
		int index = getIndexOfGroup(groupName);
		if(index >= 0){
			display.scrollTo(getIndexOfGroup(groupName));
		}
	}
	
	private int getIndexOfGroup(String groupName){
		int index = 0;
		for(String grp : groups.keySet()){
			if(grp.equalsIgnoreCase(groupName)){
				return index;
			}
			index += 1;
		}
		return -1;
	}
	
	private VBox createLabelGroupDisplay(String groupName){
		VBox layout = new VBox();
		layout.setSpacing(4);
		
		List<String> labelNames = groups.get(groupName).stream().map(l -> l.getValue()).collect(Collectors.toList());
		
		boolean isExclusive = new TurboLabelGroup(groups.get(groupName)).isExclusive();
		
		BetterCheckListView control = new BetterCheckListView(FXCollections.observableArrayList(labelNames));
		if (isExclusive) control.setSingleSelection(true);
		control.setPrefHeight(labelNames.size() * ROW_HEIGHT + 2);
		control.setPrefWidth(WINDOW_WIDTH - 29);
		control.setUserData(groups.get(groupName));
		controls.put(groupName, control);	

		// deal with initially checked items
		int selected = 0;
		for (int i=0; i<groups.get(groupName).size(); i++) {
			if (initialChecked.contains(groups.get(groupName).get(i))) {
				control.setChecked(i, true);
				selected++;
			}
		}
		assert !isExclusive || selected == 1 || selected == 0;
		
		// layout
		Label name = new Label(groupName);
		layout.getChildren().addAll(name, control);
		return layout;
	}
	
	protected Parent content() {
		
		groups = TurboLabel.groupLabels(labels, LabelManagementComponent.UNGROUPED_NAME);
		labelSuggestions = labels.stream().map(l -> l.toGhName()).collect(Collectors.toList());
		
		ObservableList<VBox> displayItems = FXCollections.observableArrayList();
		
		for (String groupName : groups.keySet()) {
			VBox layout = createLabelGroupDisplay(groupName);
			displayItems.addAll(layout);
		}
		display.setItems(displayItems);
		return setupLayout();
	}

	private void setupDoneButton(){
		WeakReference<LabelCheckboxListDialog> selfRef = new WeakReference<>(this);
		done = new Button("Done");
		done.setOnAction((e) -> {
			selfRef.get().handleDoneButtonAction();
		});
		done.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.ENTER){
				selfRef.get().handleDoneButtonAction();
			}
		});
	}
	
	private void handleDoneButtonAction(){
		respond();
		close();
	}
	
	private Parent setupLayout() {
		setupDoneButton();
		
		setupAutoCompleteBox();
		
		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER_RIGHT);
		layout.setSpacing(5);
		layout.setPadding(new Insets(10));
		layout.getChildren().addAll(display, autoCompleteBox, done);

		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setTitle("Choose Labels");
		
		return layout;
	}

	private void respond() {
		ArrayList<TurboLabel> result = new ArrayList<>();
		for (BetterCheckListView clv : controls.values()) {
			@SuppressWarnings("unchecked")
			ArrayList<TurboLabel> labels = ((ArrayList<TurboLabel>) clv.getUserData());
			result.addAll(clv.getCheckedIndices().stream().map(i -> labels.get(i)).collect(Collectors.toList()));
			
		}
		completeResponse(result);
	}

	public LabelCheckboxListDialog setInitialChecked(List<TurboLabel> initialChecked) {
		this.initialChecked = new ArrayList<>(initialChecked);
		return this;
	}
	
	@Override
	public CompletableFuture<List<TurboLabel>> show() {
		CompletableFuture<List<TurboLabel>> response = super.show();
		autoCompleteBox.requestFocus();
		return response;
	}
}
