package ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import service.ServiceManager;
import model.TurboComment;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class IssueDetailsCard extends VBox{
	protected static int PREF_WIDTH = 320;
	protected static int ELEMENTS_SPACING = 10;
	protected static int PADDING = 8;
	
	protected VBox topBar;
	protected Text commentsText;
	protected VBox commentsTextDisplay;
	
	protected TurboComment originalComment;
	
	protected ChangeListener<String> bodyChangeListener;
	
	public IssueDetailsCard(TurboComment comment){
		this.originalComment = comment;
		this.setSpacing(ELEMENTS_SPACING);
		this.setPrefWidth(PREF_WIDTH);
		initialiseUIComponents();
		loadCardComponents();
	}
	
	protected void initialiseUIComponents(){
		initialiseTopBar();
		initialiseCommentsText();
		initialiseCommentsTextDisplay();
	}
	
	protected void initialiseTopBar(){
		topBar = new VBox();
		topBar.setPrefWidth(PREF_WIDTH);
		topBar.setSpacing(ELEMENTS_SPACING);
	}
	
	protected void initialiseCommentsText(){
		commentsText = new Text();
		commentsText.setWrappingWidth(PREF_WIDTH);
		initialiseCommentBodyChangeListener();
	}
	
	protected void initialiseCommentBodyChangeListener(){
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
		Text creationDate = new Text(formatDisplayedDate(originalComment.getCreatedAt()));
		details.setAlignment(Pos.BASELINE_LEFT);
		details.setSpacing(ELEMENTS_SPACING);
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
			System.out.println("returned "+text);
			return text;
		}
		return text.replaceFirst(Pattern.quote(ServiceManager.CHANGELOG_TAG), "");
	}
}
