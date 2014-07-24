package ui.issuepanel.comments;

import java.lang.ref.WeakReference;

import ui.StatusBar;
import util.DialogMessage;
import model.TurboComment;
import handler.IssueDetailsContentHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CommentsEditBox extends VBox{
	protected static final int ELEMENT_SPACING = 8;

	private IssueDetailsContentHandler commentHandler;
	
	private ChangeListener<String> commentFieldChangeListener;
	
	private Button commentButton;
	private String initialText = "";
	private TextArea commentTextField;
	private TurboComment editedComment;
	
	protected String commentButtonText = "Comment";
	
	public CommentsEditBox(IssueDetailsContentHandler handler){
		this.commentHandler = handler;
		initialiseUIComponents();
		setupLayout();
	}
	
	public CommentsEditBox(IssueDetailsContentHandler handler, TurboComment editedComment, String commentBtnTxt){
		this.commentButtonText = commentBtnTxt;
		this.commentHandler = handler;
		this.editedComment = editedComment;
		if(editedComment != null){
			initialText = editedComment.getBody();
		}
		setupForEditing();
	}
	
	public CommentsEditBox(IssueDetailsContentHandler handler, TurboComment editedComment){
		this.commentHandler = handler;
		this.editedComment = editedComment;
		if(editedComment != null){
			initialText = editedComment.getBody();
		}
		setupForEditing();
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
		if(commentHandler == null){
			System.out.println("null here");
		}
		boolean editRes = commentHandler.editComment(editedComment);
		if(editRes){
			commentHandler.setCommentEditStateFalse(editedComment);
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
