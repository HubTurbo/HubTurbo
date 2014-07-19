package ui;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.TurboComment;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class CommentCard extends VBox{
	protected static int PREF_WIDTH = 320;
	protected static int ELEMENTS_SPACING = 10;
	protected static int PADDING = 8;
	
	protected static String EDIT_BTN_TXT = "Edit";
	protected static String DELETE_BTN_TXT = "Delete";
	
	private TurboComment originalComment;
	private TurboComment editedComment;
	
	private Button deleteButton;
	private ToggleButton editButton;
	
	private Text commentsText;
	private TextArea editableCommentsText;
	private VBox commentsTextDisplay;
	
	private ChangeListener<String> bodyChangeListener;
	
	public CommentCard(TurboComment comment){
		this.originalComment = comment;
		this.editedComment = new TurboComment(comment);
		this.setSpacing(ELEMENTS_SPACING);
		this.setPrefWidth(PREF_WIDTH);
		initialiseUIComponents();
		loadCardComponents();
	}
	
	private void initialiseUIComponents(){
		initialiseEditButton();
		intialiseDeleteButton();
		initialiseCommentsText();
		initialiseEditableCommentsText();
		initialiseCommentsTextDisplay();
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
	
	private void initialiseCommentsText(){
		commentsText = new Text();
		commentsText.setWrappingWidth(PREF_WIDTH);
		initialiseCommentBodyChangeListener();
	}
	
	private void initialiseCommentBodyChangeListener(){
		bodyChangeListener = new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String original, String change) {
				commentsText.setText(originalComment.getBody());
			}	
		};
		originalComment.getBodyProperty().addListener(new WeakChangeListener<String>(bodyChangeListener));
	}
	
	private void initialiseEditableCommentsText(){
		editableCommentsText = new TextArea();
		editableCommentsText.setWrapText(true);
	}
	
	private void initialiseCommentsTextDisplay(){
		commentsTextDisplay = new VBox();
	}
	
	private HBox createControlsBox(){
		HBox controls = new HBox();
		controls.setAlignment(Pos.BASELINE_RIGHT);
		controls.getChildren().addAll(editButton, deleteButton);
		return controls;
	}
	
	
	private HBox createCommentsDetailsDisplay(){
		HBox details = new HBox();
		Text creator = new Text(originalComment.getCreator().getGithubName());
		Text creationDate = new Text(formatDisplayedDate(originalComment.getCreatedAt()));
		details.setAlignment(Pos.BASELINE_LEFT);
		details.setSpacing(ELEMENTS_SPACING);
		details.getChildren().addAll(creator, creationDate);
		return details;
	}
	
	private String formatDisplayedDate(Date date){
		SimpleDateFormat format = new SimpleDateFormat("d MMM yy, h:mm a");
		return format.format(date);
	}
	
	private VBox createTopBar(){
		VBox top = new VBox();
		top.setPrefWidth(PREF_WIDTH);
		top.setSpacing(ELEMENTS_SPACING);
		top.getChildren().addAll(createControlsBox(), createCommentsDetailsDisplay());
		return top;
	}
	
	private void loadCardComponents(){
		VBox topBar = createTopBar();
		loadCommentsBody();
		getChildren().addAll(topBar, commentsTextDisplay);
	}
	
	private void loadCommentsBody(){
		commentsTextDisplay.getChildren().clear();
		if(editButton.selectedProperty().get() == false){
			commentsText.setText(originalComment.getBody());
			commentsTextDisplay.getChildren().add(commentsText);
		}else{
			editableCommentsText.setText(originalComment.getBody());
			commentsTextDisplay.getChildren().add(editableCommentsText);
		}
		
	}
	
	private void handleEditButtonPressed(){
		System.out.println("here");
		if(editButton.selectedProperty().get()){
			
		}
		loadCommentsBody();
	}
	
}
