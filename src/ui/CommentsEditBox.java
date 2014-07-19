package ui;

import handler.IssueDetailsContentHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CommentsEditBox extends VBox{
	protected static final int ELEMENT_SPACING = 8;
	protected static final String COMMENT_BTN_TXT = "Comment";
	private IssueDetailsContentHandler commentHandler;
	private Button commentButton;
	private TextArea commentText;
	
	public CommentsEditBox(IssueDetailsContentHandler handler){
		this.commentHandler = handler;
		initialiseUIComponents();
		setupLayout();
	}
	
	private void initialiseUIComponents(){
		commentButton = new Button(COMMENT_BTN_TXT);
		commentText = new TextArea();
	}
	
	private void setupLayout(){
		this.setSpacing(ELEMENT_SPACING);
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		buttonBox.getChildren().add(commentButton);
		
		getChildren().addAll(commentText, buttonBox);
	}
}
