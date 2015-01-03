package ui.issuepanel.expanded.comments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import model.TurboComment;

import org.w3c.dom.Document;

import service.ServiceManager;

public class IssueDetailsCard extends VBox{
	protected static int PREF_WIDTH = 300;
	protected static int PREF_WEB_HEIGHT = 150;
	protected static int ELEMENTS_HORIZONTAL_SPACING = 10;
	protected static int ELEMENTS_VERTICAL_SPACING = 5;
	protected static int PADDING = 3;
	protected static int WEB_TEXT_PADDING = 30;
	
	public static final String EVENT_TYPE_CLICK = "click";
	protected static final String DEFAULT_CSS = "<style type=\"text/css\">"
			+ "img{"
			+ "max-width: 100%;"
			+ "}"
			+ "body {"
			+ "font-family: System;"
			+ "font-size: 12px"
			+ "}"
			+ "</style>";
	protected static final String HTML_CONTENT_WRAPPER = "<html><body>" 
	        +  "<div id=\"wrapper\" style = \"width: 280\">%1s</div>" +
	           "</body></html>";
	
	protected HBox topBar;
	protected WebView commentsBody;
	protected VBox commentsTextDisplay;
	
	protected TurboComment originalComment;
	
	protected ChangeListener<String> bodyChangeListener;
	protected ChangeListener<Document> webViewHeightListener;
	protected ChangeListener<Number> contentLoadListeners;
		
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
		initialiseCommentsBodyDisplay();
		initialiseCommentsTextDisplay();
	}
	
	protected void initialiseTopBar(){
		topBar = new HBox();
		topBar.setPrefWidth(PREF_WIDTH);
		topBar.setSpacing(ELEMENTS_HORIZONTAL_SPACING);
	}
	
	protected void initialiseCommentsBodyDisplay(){
		commentsBody = new WebView();
		commentsBody.setPrefWidth(PREF_WIDTH);
		commentsBody.setPrefHeight(PREF_WEB_HEIGHT);
		setupWebEngineHeightListener();
	}
	
	protected void setupCommentBodyChangeListener(){
		bodyChangeListener = new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String original, String change) {
				setDisplayedCommentText();
			}	
		};
		originalComment.getBodyHtmlProperty().addListener(new WeakChangeListener<String>(bodyChangeListener));
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
		Text creator = new Text(originalComment.getCreator().getAlias());
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
		commentsTextDisplay.getChildren().add(commentsBody);
	}
	
	private void setDisplayedCommentText(){
		String text = originalComment.getBodyHtml();
		String displayedText = DEFAULT_CSS + String.format(HTML_CONTENT_WRAPPER, text);
		commentsBody.getEngine().loadContent(displayedText);	
	}
	
	private void setupWebEngineHeightListener(){
		webViewHeightListener = new ChangeListener<Document>() {
	        @Override
	        public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
	            adjustWebEngineHeight();
	        }
		};
		commentsBody.getEngine().documentProperty().addListener(new WeakChangeListener<Document>(webViewHeightListener));
	}
	
	private void adjustWebEngineHeight(){
		try{
			Object res = commentsBody.getEngine().executeScript("document.getElementById('wrapper').offsetHeight");
	        if(res!= null && res instanceof Integer) {
	        	Integer height = (Integer)res + WEB_TEXT_PADDING;
	        	commentsBody.setPrefHeight(height);
	        }
		}catch(Exception e){
		}
		return;
	}
}
