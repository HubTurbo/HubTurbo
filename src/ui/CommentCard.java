package ui;

import java.lang.ref.WeakReference;


import model.TurboComment;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
public class CommentCard extends IssueDetailsCard{
	protected static String EDIT_BTN_TXT = "Edit";
	protected static String DELETE_BTN_TXT = "Delete";
	
	
	private TurboComment editedComment;
	
	private Button deleteButton;
	private ToggleButton editButton;
	
	
	private TextArea editableCommentsText;
	
	public CommentCard(TurboComment comment){
		super(comment);
		this.editedComment = new TurboComment(comment);
	}
	
	@Override
	protected void initialiseUIComponents(){
		super.initialiseUIComponents();
		initialiseEditButton();
		intialiseDeleteButton();
		initialiseEditableCommentsText();
	}
	
	private void initialiseEditButton(){
		editButton = new ToggleButton();
		editButton.setText(EDIT_BTN_TXT);
		WeakReference<CommentCard> selfRef = new WeakReference<CommentCard>(this);
		editButton.setOnMousePressed(e -> {
		    selfRef.get().handleEditButtonPressed();
		});
	}
	
	private void intialiseDeleteButton(){
		deleteButton = new Button();
		deleteButton.setText(DELETE_BTN_TXT);
	}
	
	
	private void initialiseEditableCommentsText(){
		editableCommentsText = new TextArea();
		editableCommentsText.setWrapText(true);
	}
	
	private HBox createControlsBox(){
		HBox controls = new HBox();
		controls.setAlignment(Pos.BASELINE_RIGHT);
		controls.getChildren().addAll(editButton, deleteButton);
		return controls;
	}
	
	@Override
	protected void loadTopBar(){
		topBar.getChildren().addAll(createControlsBox(), createCommentsDetailsDisplay());
	}
	
	@Override
	protected void loadCommentsDisplay(){
		commentsTextDisplay.getChildren().clear();
		if(editButton.selectedProperty().get() == false){
			super.loadCommentsDisplay();
		}else{
			editableCommentsText.setText(originalComment.getBody());
			commentsTextDisplay.getChildren().add(editableCommentsText);
		}
		
	}
	
	private void handleEditButtonPressed(){
		//TODO:
		System.out.println("here");
		if(editButton.selectedProperty().get()){
			
		}
		loadCommentsDisplay();
	}
	
}
