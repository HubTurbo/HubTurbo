package ui;

import handler.IssueDetailsContentHandler;

import java.lang.ref.WeakReference;

import model.TurboComment;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class CommentCard extends IssueDetailsCard{
	protected static String EDIT_BTN_TXT = "Edit";
	protected static String CANCEL_BTN_TXT = "Cancel";
	protected static String DELETE_BTN_TXT = "Delete";
	
	
	protected IssueDetailsContentHandler handler;
	
	private TurboComment editedComment;
	
	private Button deleteButton;
	private Button editButton;
	
	private CommentsEditBox editableCommentsText;
		
	public CommentCard(IssueDetailsContentHandler handler){
		super();
		this.handler = handler;
	}
	
	@Override
	public void setDisplayedItem(TurboComment comment){
		super.setDisplayedItem(comment);
		this.editedComment = new TurboComment(comment);
	}
	
	@Override
	protected void initialiseUIComponents(){
		super.initialiseUIComponents();
		initialiseEditButton();
		intialiseDeleteButton();
	}
	
	private void initialiseEditButton(){
		editButton = new Button();
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
		editableCommentsText = new CommentsEditBox(handler, editedComment);
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
		if(!handler.commentIsInEditState(originalComment)){
			editableCommentsText = null;
			super.loadCommentsDisplay();
		}else{
			loadCommentEditField();
		}
		
	}
	
	private void loadCommentEditField(){
		if(editableCommentsText == null){
			initialiseEditableCommentsText();
		}
		commentsTextDisplay.getChildren().add(editableCommentsText);
	}

	private void handleEditButtonPressed(){
		handler.toggleCommentEditState(originalComment);
		updateEditButtonText();
		loadCommentsDisplay();
	}
	
	private void updateEditButtonText(){
		if(handler.commentIsInEditState(originalComment)){
			editButton.setText(CANCEL_BTN_TXT);
		}else{
			editButton.setText(EDIT_BTN_TXT);
		}
	}
}
