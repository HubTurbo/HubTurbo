package ui;

import java.lang.ref.WeakReference;

import model.TurboComment;
import handler.IssueDetailsContentHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CommentsEditBox extends VBox{
	protected static final int ELEMENT_SPACING = 8;
	protected static final String COMMENT_BTN_TXT = "Comment";
	
	private IssueDetailsContentHandler commentHandler;
	
	private ChangeListener<String> commentFieldChangeListener;
	
	private Button commentButton;
	private String initialText = "";
	private TextArea commentTextField;
	private TurboComment editedComment;
	
	public CommentsEditBox(IssueDetailsContentHandler handler){
		this.commentHandler = handler;
		initialiseUIComponents();
		setupLayout();
	}
	
	public CommentsEditBox(IssueDetailsContentHandler handler, TurboComment editedComment){
		this.commentHandler = handler;
		this.editedComment = editedComment;
		if(editedComment != null){
			initialText = editedComment.getBody();
			System.out.println(initialText);
		}
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
		commentButton = new Button(COMMENT_BTN_TXT);
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
		commentHandler.createComment(commentTextField.getText());
		commentTextField.setText("");
	}
	
	private void handleCommentEdit(){
		boolean editRes = commentHandler.editComment(editedComment);
		if(editRes){
			commentHandler.setCommentEditStateFalse(editedComment);
		}
	}
}
