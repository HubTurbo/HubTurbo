package ui;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import service.ServiceManager;

public class MarkupPopup extends Stage{	
	private static final Logger logger = LogManager.getLogger(MarkupPopup.class.getName());
	protected static final String EDIT_BTN_TXT = "\uf058";
	protected static final String BACK_BTN_TXT = " \uf0a4 ";
	protected static final String DEFAULT_CSS = "<style type=\"text/css\">"
			+ "img{"
			+ "max-width: 100%;"
			+ "}"
			+ "body {"
			+ "font-family: System;"
			+ "font-size: 12px"
			+ "}"
			+ "</style>";
	
	private WebView markupDisplay;
	private VBox editableDisplayView;
	private HTMLEditor editableDisplay;
	private VBox container;
	private Runnable editModeCompletion;
	private Button completionButton;
	
	private final String buttonText;
	
	
	public MarkupPopup(String buttonText){
		this.buttonText = buttonText;
		
		setupContents();
		Scene scene = new Scene(container);
		scene.getStylesheets().add(MarkupPopup.class.getResource("hubturbo.css").toString());
		scene.setFill(Color.WHITE);
		
		this.setScene(scene);
		this.initModality(Modality.APPLICATION_MODAL);
		this.initOwner(null);
	}
	
	public String getText(){
		return editableDisplay.getHtmlText();
	}
	
	public String getDisplayedContentMarkup(){
		try {
			return ServiceManager.getInstance().getContentMarkup(getText());
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			return getText();
		}
	}
	
	public void loadURL(String url){
		markupDisplay.getEngine().load(url);
	}
	
	public void setDisplayedText(String markup){
		loadContentForWebView(markup);
		editableDisplay.setHtmlText(markup);
	}
	
	public void setDisplayedText(String markup, String original){
		loadContentForWebView(markup);
		editableDisplay.setHtmlText(original);
	}
	
	private void loadContentForWebView(String markup){
		String content = DEFAULT_CSS + markup;
		markupDisplay.getEngine().loadContent(content);
	}
	
	private void setupContents(){
		container = new VBox();
		container.setPadding(new Insets(10));
		
		setupMarkupDisplay();
		setupEditableDisplayView();

		container.getChildren().addAll(markupDisplay);
	}
	
	private void setupMarkupDisplay(){
		markupDisplay = new WebView();
	}
	
	private void setupEditableDisplayView(){
		editableDisplayView = new VBox();
		editableDisplayView.setSpacing(5);
		editableDisplay = new HTMLEditor();
		
		setupCompleteButton();
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.BASELINE_RIGHT);
		buttonContainer.getChildren().add(completionButton);
		
		editableDisplayView.getChildren().addAll(editableDisplay, buttonContainer);
	}
	
	private void setupCompleteButton(){
		completionButton = new Button();
		completionButton.setText(buttonText);
		WeakReference<MarkupPopup> selfRef = new WeakReference<>(this);
		completionButton.setOnAction((ActionEvent e) -> {
			MarkupPopup self = selfRef.get();
			if(self != null){
				if(editModeCompletion != null){
					editModeCompletion.run();
				}
				self.close();
			}
		});
	}
	
	public void setEditModeCompletion(Runnable completion){
		editModeCompletion = completion;
	}

}
