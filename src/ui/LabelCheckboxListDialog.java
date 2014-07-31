package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.controlsfx.control.textfield.TextFields;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.TurboLabel;

public class LabelCheckboxListDialog extends Dialog<List<TurboLabel>> {

	private static final double WINDOW_WIDTH = 250;
	private static final double WINDOW_HEIGHT = 370;
	private static final int ROW_HEIGHT = 30;
	
	private ArrayList<TurboLabel> initialChecked;
	private ObservableList<TurboLabel> labels;
	private HashMap<String, BetterCheckListView> controls = new HashMap<>();
	private List<String> labelSuggestions = new ArrayList<>();
	
	private TextField autoCompleteBox;
	private Button close;
	
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
		if(tokens == null){
			checklist = controls.get(LabelManagementComponent.UNGROUPED_NAME);
			labelName = name;
		}else{
			checklist = controls.get(tokens[0]);
			labelName = tokens[1];
		}
		boolean result = checklist.checkItem(labelName);
		return result;
	}
	
	protected Parent content() {
		
		HashMap<String, ArrayList<TurboLabel>> groups = TurboLabel.groupLabels(labels, LabelManagementComponent.UNGROUPED_NAME);
		labelSuggestions = labels.stream().map(l -> l.toGhName()).collect(Collectors.toList());
		
		VBox layout = new VBox();
		layout.setSpacing(4);
		
		for (String groupName : groups.keySet()) {
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
		}
		
		ScrollPane sp = new ScrollPane();
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setContent(layout);
		
		VBox.setVgrow(sp, Priority.ALWAYS);
		return setupLayout(sp);
	}

	private Parent setupLayout(ScrollPane content) {
		close = new Button("Close");
		close.setOnAction((e) -> {
			respond();
			close();
		});
		
		setupAutoCompleteBox();
		
		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER_RIGHT);
		layout.setSpacing(5);
		layout.setPadding(new Insets(10));
		layout.getChildren().addAll(content, autoCompleteBox, close);

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
