package ui;

import model.TurboComment;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class CommentCard extends VBox{
	protected static String EDIT_BTN_TXT = "Edit";
	protected static String DELETE_BTN_TXT = "Delete";
	
	private TurboComment originalComment;
	private TurboComment editedComment;
	
	private Button deleteButton;
	private Button editButton;
	
	
	
	public CommentCard(TurboComment comment){
		this.originalComment = comment;
		this.editedComment = new TurboComment(comment);
	}
	
	private void initialiseEditButton(){
		editButton = new Button();
		editButton.setText(EDIT_BTN_TXT);
	}
	
	private void intialiseDeleteButton(){
		deleteButton = new Button();
		deleteButton.setText(DELETE_BTN_TXT);
	}
	
	private HBox createControlsBox(){
		HBox controls = new HBox();
		initialiseEditButton();
		intialiseDeleteButton();
		controls.setAlignment(Pos.CENTER_RIGHT);
		controls.getChildren().addAll(editButton, deleteButton);
		return controls;
	}
	
	private HBox createTopBar(){
		HBox top = new HBox();
		top.getChildren().addAll(createCommentsDetailsDisplay(), createControlsBox());
		return top;
	}
	
	private HBox createCommentsDetailsDisplay(){
		HBox details = new HBox();
		Text creator = new Text(originalComment.getCreator().getGithubName());
		Text creationDate = new Text(originalComment.getCreatedAt().toString());
		details.setAlignment(Pos.CENTER_LEFT);
		details.getChildren().addAll(creator, creationDate);
		return details;
	}
}
