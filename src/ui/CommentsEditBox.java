package ui;

import handler.IssueDetailsContentHandler;
import javafx.scene.control.TextArea;
import javafx.scene.web.HTMLEditor;

public class CommentsEditBox extends TextArea{
	private IssueDetailsContentHandler commentHandler;
	public CommentsEditBox(IssueDetailsContentHandler handler){
		this.commentHandler = handler;
	}
	
}
