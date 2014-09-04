package ui.issuepanel.expanded.comments;

import handler.IssueDetailsContentHandler;

import java.lang.ref.WeakReference;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import model.TurboComment;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

import ui.EditableMarkupPopup;
import util.DialogMessage;

public class CommentCard extends IssueDetailsCard{
	protected static String EDIT_BTN_TXT = "\uf058";
	protected static String CANCEL_BTN_TXT = " \uf0a4 ";
	protected static String DELETE_BTN_TXT = "\uf0d0";
	protected static String POPUP_BTN_TXT = "\uf07f";
	
	protected IssueDetailsContentHandler handler;
	
	private TurboComment editedComment;
	
	private Label deleteButton;
	private Label editButton;
	private Label popupButton;
	
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
		initialiseDeleteButton();
		initialisePopupButton();
	}
	
	private void initialiseEditButton(){
		editButton = new Label();
		editButton.setText(EDIT_BTN_TXT);
		editButton.getStyleClass().addAll("button-github-octicon", "comments-label-button");
		WeakReference<CommentCard> selfRef = new WeakReference<CommentCard>(this);
		editButton.setOnMousePressed(e -> {
		    selfRef.get().handleEditButtonPressed();
		});
	}
	
	private void initialiseDeleteButton(){
		deleteButton = new Label();
		deleteButton.setText(DELETE_BTN_TXT);
		deleteButton.getStyleClass().addAll("button-github-octicon", "comments-label-button");
		WeakReference<CommentCard> selfRef = new WeakReference<CommentCard>(this);
		deleteButton.setOnMousePressed(e -> {
		    selfRef.get().handleDeleteButtonPressed();
		});
	}
	
	private void initialisePopupButton(){
		popupButton = new Label();
		popupButton.setText(POPUP_BTN_TXT);
		popupButton.getStyleClass().addAll("button-github-octicon");
		popupButton.setOnMousePressed(e -> {
			EditableMarkupPopup popup = createPopup();
			popup.show();
		});
	}
	
	private EditableMarkupPopup createPopup(){
		EditableMarkupPopup popup = new EditableMarkupPopup("Update");
		popup.setDisplayedText(editedComment.getBodyHtml(), editedComment.getBody());
		
		WeakReference<EditableMarkupPopup> ref = new WeakReference<>(popup);
		popup.setEditModeCompletion(() -> {
			editedComment.setBody(ref.get().getText());
			handler.editComment(editedComment);
		});
		return popup;
	}
	
	
	private void initialiseEditableCommentsText(){
		Runnable refreshDisplay = ()->{
			loadCommentsDisplay();
		};
		commentEditField = new CommentsEditBox(handler, editedComment, "Update", refreshDisplay);
	}
	
	private HBox createControlsBox(){
		HBox controls = new HBox();
		controls.setAlignment(Pos.BOTTOM_RIGHT);
		controls.getChildren().addAll(popupButton, editButton, deleteButton);
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
			topBar.getChildren().addAll(commentsDetailsDisp, controlsBox);
		}else{
			super.loadTopBar();
		}
	}
	
	@Override
	protected void loadCommentsDisplay(){
		commentsTextDisplay.getChildren().clear();
		updateEditButtonText();
		if(!handler.commentIsInEditState(originalComment)){
			commentEditField = null;
			super.loadCommentsDisplay();
		}else{
			loadCommentEditField();
		}
	}
	
	private void loadCommentEditField(){
		if(commentEditField == null){
			initialiseEditableCommentsText();
		}
		commentsTextDisplay.getChildren().add(commentEditField);
		commentEditField.requestFocus();
	}

	
	private void handleDeleteButtonPressed(){
		Action response = DialogMessage.showConfirmDialog("Delete Comment", 
										String.format("Are you sure you want to delete the comment:\n %s...?", 
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
