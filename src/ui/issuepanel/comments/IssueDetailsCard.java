package ui.issuepanel.comments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import service.ServiceManager;
import util.Browse;
import model.TurboComment;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;

public class IssueDetailsCard extends VBox{
	protected static int PREF_WIDTH = 300;
	protected static int ELEMENTS_HORIZONTAL_SPACING = 10;
	protected static int ELEMENTS_VERTICAL_SPACING = 5;
	protected static int PADDING = 3;
	protected static int WEB_TEXT_PADDING = 30;
	
	public static final String EVENT_TYPE_CLICK = "click";
	
	protected static final String HTML_CONTENT_WRAPPER = "<html><body>" +
	           "<div id=\"wrapper\">%1s</div>" +
	           "</body></html>";
	
	protected HBox topBar;
	protected WebView commentsText;
	protected VBox commentsTextDisplay;
	
	protected TurboComment originalComment;
	
	protected ChangeListener<String> bodyChangeListener;
	protected ChangeListener<Document> webViewHeightListener;
	protected ChangeListener<State> weblinkClickListeners;
	
	public IssueDetailsCard(){
		this.setSpacing(ELEMENTS_VERTICAL_SPACING);
		this.setPrefWidth(PREF_WIDTH);
		this.setPadding(new Insets(PADDING));
		this.getStyleClass().add("comments-list-cell");
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
		setupWeblinkClickListeners();
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
		commentsText = new WebView();
		commentsText.setPrefWidth(PREF_WIDTH);
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
		commentsTextDisplay.getChildren().add(commentsText);
	}
	
	private void setDisplayedCommentText(){
		String text = originalComment.getBodyHtmlMarkUp();
		String displayedText = String.format(HTML_CONTENT_WRAPPER, stripChangeLogHeader(text));
		commentsText.getEngine().loadContent(displayedText);
	}
	
	private void setupWebEngineHeightListener(){
		webViewHeightListener = new ChangeListener<Document>() {
	        @Override
	        public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
	            adjustWebEngineHeight();
	        }
		};
		commentsText.getEngine().documentProperty().addListener(new WeakChangeListener<Document>(webViewHeightListener));
	}
	
	private void setupWeblinkClickListeners(){		
//		weblinkClickListeners = new ChangeListener<State>() {
//            @Override
//            public void changed(ObservableValue ov, State oldState, State newState) {
//                if (newState == Worker.State.SUCCEEDED) {
//                	System.out.println("listener created");
//                    EventListener listener = new EventListener() {
//						@Override
//						public void handleEvent(Event evt) {
//							 String domEventType = evt.getType();
//							 System.out.println(evt.getType());
//	                            if (domEventType.equals(EVENT_TYPE_CLICK)) {
//	                                String href = ((Element)evt.getTarget()).getAttribute("href");
//	                                System.out.println(href);
////	                                Browse.browse(href);                            
//	                            } 
//						}
//                    };
//
//                    Document doc = commentsText.getEngine().getDocument();
//                    NodeList nodeList = doc.getElementsByTagName("a");
//                    for (int i = 0; i < nodeList.getLength(); i++) {
//                    	System.out.println("node");
//                        ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, false);
//                    }
//                }
//            }
//        };
//        commentsText.getEngine().getLoadWorker().stateProperty()
//        			.addListener(weblinkClickListeners);
	}
	
	private void adjustWebEngineHeight(){
		Object res = commentsText.getEngine().executeScript("document.getElementById('wrapper').offsetHeight");
        if(res!= null && res instanceof Integer) {
        	Integer height = (Integer)res + WEB_TEXT_PADDING;
        	commentsText.setPrefHeight(height);
        }
	}
	
	private String stripChangeLogHeader(String text){
		if(text == null || !originalComment.isIssueLog()){
			return text;
		}
		String regex = Pattern.quote(ServiceManager.CHANGELOG_TAG);
		return text.replaceFirst(regex, "").trim();
	}
}
