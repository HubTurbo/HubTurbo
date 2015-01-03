package ui.issuepanel.expanded.comments;

import java.lang.ref.WeakReference;

import ui.components.StatusBar;
import util.DialogMessage;
import model.TurboComment;
import handler.IssueDetailsContentHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CommentsEditBox extends VBox{
	protected static final KeyCombination BUTTON_TRIGGER_SHORTCUT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
	protected static final int ELEMENT_SPACING = 8;

	private IssueDetailsContentHandler commentHandler;
	
	private ChangeListener<String> commentFieldChangeListener;
	
	private Button commentButton;
	private String initialText = "";
	private TextArea commentTextField;
	private TurboComment editedComment;
	
	protected String commentButtonText = "Comment";
	private Runnable refreshDisplayMethod;
	
	public CommentsEditBox(IssueDetailsContentHandler handler){
		this.commentHandler = handler;
		initialiseUIComponents();
		setupLayout();
		setupKeyboardShortcuts();
	}
	
	public CommentsEditBox(IssueDetailsContentHandler handler, TurboComment editedComment, Runnable refreshMethod){
		this.commentHandler = handler;
		this.editedComment = editedComment;
		this.refreshDisplayMethod = refreshMethod;
		if(editedComment != null){
			initialText = editedComment.getBody();
		}
		setupForEditing();
		setupKeyboardShortcuts();
	}
	
	public CommentsEditBox(IssueDetailsContentHandler handler, TurboComment editedComment, String commentBtnTxt, Runnable refreshMethod){
		this(handler, editedComment, refreshMethod);
		this.commentButtonText = commentBtnTxt;
	}
	
	private void setupKeyboardShortcuts(){
		WeakReference<CommentsEditBox> selfRef = new WeakReference<>(this);
		addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if(BUTTON_TRIGGER_SHORTCUT.match(e)){
				selfRef.get().handleCommentButtonPressed();
			}
		});
	}
	
	private void setupForEditing(){
		initialiseUIComponents();
		setupForCommentsEdit(editedComment);
		setupLayout();
	}
	
	private void setupForCommentsEdit(TurboComment comment){
		initialiseCommentFieldChangeListener();
		commentTextField.textProperty().addListener(new WeakChangeListener<String>(commentFieldChangeListener));
	}
	
	private void initialiseUIComponents(){
		initialiseCommentButton();
		commentTextField = new TextArea(initialText);
		commentTextField.setWrapText(true);
	}
	
	private void initialiseCommentButton(){
		WeakReference<CommentsEditBox> selfRef = new WeakReference<CommentsEditBox>(this);
		commentButton = new Button(commentButtonText);
		commentButton.setOnMousePressed(e -> {
		    if(selfRef.get() != null){
		    	handleCommentButtonPressed();
		    }
		});
	}
	
	private void setupLayout(){
		this.setSpacing(ELEMENT_SPACING);
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		buttonBox.getChildren().add(commentButton);
		
		getChildren().addAll(commentTextField, buttonBox);
	}
	
	private void initialiseCommentFieldChangeListener(){
		WeakReference<TurboComment> commentRef = new WeakReference<TurboComment>(editedComment);
		commentFieldChangeListener = (observable, oldValue, newValue) -> {
			TurboComment com = commentRef.get();
			if(com != null){
				com.setBody(newValue);
			}
		};
	}
	
	private void handleCommentButtonPressed(){
		if(editedComment == null){
			handleCommentAdd();
		}else{
			handleCommentEdit();
		}
	}
	
	private void handleCommentAdd(){
		Task<Boolean> bgTask = new Task<Boolean>(){
			@Override
			protected Boolean call() throws Exception {
				Boolean result = commentHandler.createComment(commentTextField.getText());
				return result;
			}
		};
		bgTask.setOnSucceeded(e -> {
			if(bgTask.getValue() == true){
				commentTextField.setText("");
			}else{
				StatusBar.displayMessage("An error occurred while adding issue comment.");
			}
		});
		bgTask.setOnFailed(e -> StatusBar.displayMessage("An error occurred while adding issue comment."));
		
		DialogMessage.showProgressDialog(bgTask, "Adding issue comment...");
		Thread backgroundThread = new Thread(bgTask);
		backgroundThread.start();
	}
	
	private void handleCommentEdit(){
		boolean editRes = commentHandler.editComment(editedComment);
		if(editRes){
			commentHandler.setCommentEditStateFalse(editedComment);
			if(refreshDisplayMethod != null){
				refreshDisplayMethod.run();
			}
		}
	}
	
	@Override
	public void requestFocus(){
		if(commentTextField != null){
			commentTextField.requestFocus();
		}else{
			super.requestFocus();
		}
	}
}
