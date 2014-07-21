package ui.issue.comments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import service.ServiceManager;
import model.TurboComment;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class IssueDetailsCard extends VBox{
	protected static int PREF_WIDTH = 300;
	protected static int ELEMENTS_HORIZONTAL_SPACING = 10;
	protected static int ELEMENTS_VERTICAL_SPACING = 5;
	protected static int PADDING = 3;
	
	protected HBox topBar;
	protected Text commentsText;
	protected VBox commentsTextDisplay;
	
	protected TurboComment originalComment;
	
	protected ChangeListener<String> bodyChangeListener;
	
	public IssueDetailsCard(){
		this.setSpacing(ELEMENTS_VERTICAL_SPACING);
		this.setPrefWidth(PREF_WIDTH);
		this.setPadding(new Insets(PADDING));
		initialiseUIComponents();
	}
	
	public void setDisplayedItem(TurboComment comment){
		this.originalComment = comment;
		reload();
	}
	
	protected void reload(){
		resetComponents();
		loadComponents();
	}
	
	protected void resetComponents(){
		topBar.getChildren().clear();
		commentsTextDisplay.getChildren().clear();
		this.getChildren().clear();
	}
	
	protected void loadComponents(){
		setupCommentBodyChangeListener();
		loadCardComponents();
	}
	
	protected void initialiseUIComponents(){
		initialiseTopBar();
		initialiseCommentsText();
		initialiseCommentsTextDisplay();
	}
	
	protected void initialiseTopBar(){
		topBar = new HBox();
		topBar.setPrefWidth(PREF_WIDTH);
		topBar.setSpacing(ELEMENTS_HORIZONTAL_SPACING);
	}
	
	protected void initialiseCommentsText(){
		commentsText = new Text();
		commentsText.setWrappingWidth(PREF_WIDTH);
	}
	
	protected void setupCommentBodyChangeListener(){
		bodyChangeListener = new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String original, String change) {
				setDisplayedCommentText();
			}	
		};
		originalComment.getBodyProperty().addListener(new WeakChangeListener<String>(bodyChangeListener));
	}
	
	protected void initialiseCommentsTextDisplay(){
		commentsTextDisplay = new VBox();
	}
	
	protected String formatDisplayedDate(Date date){
		SimpleDateFormat format = new SimpleDateFormat("d MMM yy, h:mm a");
		return format.format(date);
	}
	
	protected HBox createCommentsDetailsDisplay(){
		HBox details = new HBox();
		Text creator = new Text(originalComment.getCreator().getGithubName());
		creator.getStyleClass().add("issue-comment-details");
		Text creationDate = new Text(formatDisplayedDate(originalComment.getCreatedAt()));
		creationDate.getStyleClass().add("issue-comment-details");
		
		details.setAlignment(Pos.BOTTOM_LEFT);
		details.setSpacing(ELEMENTS_HORIZONTAL_SPACING);
		details.getChildren().addAll(creator, creationDate);
		return details;
	}
	
	protected void loadTopBar(){
		topBar.getChildren().add(createCommentsDetailsDisplay());
	}
	
	protected void loadCardComponents(){
		loadTopBar();
		loadCommentsDisplay();
		getChildren().addAll(topBar, commentsTextDisplay);
	}
	
	protected void loadCommentsDisplay(){
		setDisplayedCommentText();
		commentsTextDisplay.getChildren().add(commentsText);
	}
	
	private void setDisplayedCommentText(){
		String text = originalComment.getBody();
		commentsText.setText(stripChangeLogHeader(text));
	}
	
	private String stripChangeLogHeader(String text){
		if(text == null || !originalComment.isIssueLog()){
			return text;
		}
		return text.replaceFirst(Pattern.quote(ServiceManager.CHANGELOG_TAG), "").trim();
	}
}
