package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Listable;

public class CheckboxListDialog extends Dialog<List<Integer>> {

	private ObservableList<String> objectNames;
	private TextField autoCompleteBox;
	private BetterCheckListView checkListView;
	private Button close;
	
	public CheckboxListDialog(Stage parentStage, ObservableList<Listable> objects) {
		super(parentStage);
		ObservableList<String> stringRepresentations = FXCollections
				.observableArrayList(objects.stream()
						.map((obj) -> obj.getListName())
						.collect(Collectors.toList()));

		this.objectNames = stringRepresentations;
	}
	
	@Override
	public CompletableFuture<List<Integer>> show() {
		CompletableFuture<List<Integer>> response = super.show();
		autoCompleteBox.requestFocus();
		return response;
	}
	
	private void handleCloseButtonAction(){
		completeResponse(checkListView);
		close();
	}
	
	private void setupCloseButton(){
		WeakReference<CheckboxListDialog> selfRef = new WeakReference<>(this);
		close = new Button("Close");
		close.setOnAction((e) -> {
			selfRef.get().handleCloseButtonAction();
		});
		close.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.ENTER){
				selfRef.get().handleCloseButtonAction();
			}
		});
	}
	
	@Override
	protected Parent content() {
		
		checkListView = new BetterCheckListView(objectNames);
		checkListView.setSingleSelection(!multipleSelection);
		initialCheckedState.forEach((i) -> checkListView.setChecked(i, true));
		
		setupCloseButton();
		
		createAutoCompleteTextField();

		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER_RIGHT);
		layout.getChildren().addAll(checkListView, autoCompleteBox, close);
		layout.setSpacing(5);
		layout.setPadding(new Insets(10));

		setSize(400, 300);
		
		return layout;
	}
	
	private void createAutoCompleteTextField(){
		autoCompleteBox = new TextField();
		TextFields.bindAutoCompletion(autoCompleteBox, objectNames);
		WeakReference<CheckboxListDialog> selfRef = new WeakReference<>(this);
		WeakReference<TextField> fieldRef = new WeakReference<>(autoCompleteBox);
		autoCompleteBox.setOnKeyReleased(e -> {
			KeyCode code = e.getCode();
			CheckboxListDialog self = selfRef.get();
			switch (code) {
            case ENTER:
                if(self != null){
                	if(self.checkCheckItemWithName(fieldRef.get().getText())){
                		fieldRef.get().setText("");
                		close.requestFocus();
                	}
                }
                break;
            default:
                break;
            }
		});
	}
	
	private boolean checkCheckItemWithName(String name){
		return checkListView.checkItem(name);
	}

	private void completeResponse(BetterCheckListView checkListView) {
		completeResponse(checkListView.getCheckedIndices());
	}
	
	List<Integer> initialCheckedState = new ArrayList<>();

	public List<Integer> getInitialCheckedState() {
		return initialCheckedState;
	}

	public CheckboxListDialog setInitialCheckedState(
			List<Integer> initialCheckedState) {
		this.initialCheckedState = initialCheckedState;
		return this;
	}

	public CheckboxListDialog setWindowTitle(String windowTitle) {
		setTitle(windowTitle);
		return this;
	}

	boolean multipleSelection = true;

	public boolean getMultipleSelection() {
		return multipleSelection;
	}

	public CheckboxListDialog setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
		return this;
	}
}
