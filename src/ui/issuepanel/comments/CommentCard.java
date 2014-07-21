package ui.issuepanel.comments;

import handler.IssueDetailsContentHandler;

import java.lang.ref.WeakReference;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

import util.DialogMessage;
import model.TurboComment;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class CommentCard extends IssueDetailsCard{
	protected static String EDIT_BTN_TXT = "\uf058";
	protected static String CANCEL_BTN_TXT = " \uf0a4 ";
	protected static String DELETE_BTN_TXT = "\uf0d0";
	
	
	protected IssueDetailsContentHandler handler;
	
	private TurboComment editedComment;
	
	private Button deleteButton;
	private Button editButton;
	
	private CommentsEditBox commentEditField;
		
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
		editButton.getStyleClass().add("button-github-octicon");
		WeakReference<CommentCard> selfRef = new WeakReference<CommentCard>(this);
		editButton.setOnMousePressed(e -> {
		    selfRef.get().handleEditButtonPressed();
		});
	}
	
	private void intialiseDeleteButton(){
		deleteButton = new Button();
		deleteButton.setText(DELETE_BTN_TXT);
		deleteButton.getStyleClass().add("button-github-octicon");
		WeakReference<CommentCard> selfRef = new WeakReference<CommentCard>(this);
		deleteButton.setOnMousePressed(e -> {
		    selfRef.get().handleDeleteButtonPressed();
		});
	}
	
	
	private void initialiseEditableCommentsText(){
		commentEditField = new CommentsEditBox(handler, editedComment);
	}
	
	private HBox createControlsBox(){
		HBox controls = new HBox();
		controls.setAlignment(Pos.BOTTOM_RIGHT);
		controls.getChildren().addAll(editButton, deleteButton);
		controls.setSpacing(5);
		return controls;
	}
	
	@Override
	protected void loadTopBar(){
		//show comment editing options only when the comment is not a change log
		if(originalComment != null && !originalComment.isIssueLog()){
			HBox commentsDetailsDisp = createCommentsDetailsDisplay();
			HBox controlsBox = createControlsBox();
			HBox.setHgrow(controlsBox, Priority.ALWAYS);
			topBar.setSpacing(100);
			topBar.getChildren().addAll(commentsDetailsDisp, controlsBox);
		}else{
			super.loadTopBar();
		}
	}
	
	@Override
	protected void loadCommentsDisplay(){
		commentsTextDisplay.getChildren().clear();
		if(!handler.commentIsInEditState(originalComment)){
			commentEditField = null;
			super.loadCommentsDisplay();
		}else{
			loadCommentEditField();
		}
		updateEditButtonText();
	}
	
	private void loadCommentEditField(){
		if(commentEditField == null){
			initialiseEditableCommentsText();
		}
		commentsTextDisplay.getChildren().add(commentEditField);
	}

	
	private void handleDeleteButtonPressed(){
		Action response = DialogMessage.showConfirmDialog("Delete Comment", 
										String.format("Are you sure you want to delete the comment: %s...?", 
												getStartOfComment()));
		if(response == Dialog.Actions.OK){
			handler.deleteComment(editedComment);
		}
	}
	
	private void handleEditButtonPressed(){
		handler.toggleCommentEditState(originalComment);
		loadCommentsDisplay();
	}
	
	private void updateEditButtonText(){
		if(handler.commentIsInEditState(originalComment)){
			editButton.setText(CANCEL_BTN_TXT);
		}else{
			editButton.setText(EDIT_BTN_TXT);
		}
	}
	
	private String getStartOfComment(){
		String text = editedComment.getBody();
		return text.substring(0, (int)Math.min(15, text.length() - 1));
	}
}
