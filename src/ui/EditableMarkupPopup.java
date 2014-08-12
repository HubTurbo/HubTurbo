package ui;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import service.ServiceManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditableMarkupPopup extends Stage{	
	private static final Logger logger = LogManager.getLogger(EditableMarkupPopup.class.getName());
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
	private ToggleButton modeButton;
	private VBox container;
	private Runnable editModeCompletion;
	private Button completionButton;
	
	private final String buttonText;
	
	
	public EditableMarkupPopup(String buttonText){
		this.buttonText = buttonText;
		
		setupContents();
		Scene scene = new Scene(container);
		scene.getStylesheets().add(EditableMarkupPopup.class.getResource("hubturbo.css").toString());
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
		
		setupToggleButton();
		setupMarkupDisplay();
		setupEditableDisplayView();
		
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.BASELINE_RIGHT);
		buttonContainer.getChildren().add(modeButton);
		
		container.getChildren().addAll(buttonContainer, markupDisplay);
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
	
	private void setupToggleButton(){
		modeButton = new ToggleButton();
		modeButton.setText(EDIT_BTN_TXT);
		modeButton.getStyleClass().addAll("button-github-octicon", "borderless-toggle-button");
		WeakReference<ToggleButton> btnRef = new WeakReference<>(modeButton);
		modeButton.setOnAction((ActionEvent e) -> {
			container.getChildren().remove(1);
			if(btnRef.get().isSelected()){
				btnRef.get().setText(BACK_BTN_TXT);
				container.getChildren().add(editableDisplayView);
			}else{
				btnRef.get().setText(EDIT_BTN_TXT);
				loadContentForWebView(getDisplayedContentMarkup());
				container.getChildren().add(markupDisplay);
			}
		});
	}
	
	private void setupCompleteButton(){
		completionButton = new Button();
		completionButton.setText(buttonText);
		WeakReference<EditableMarkupPopup> selfRef = new WeakReference<>(this);
		completionButton.setOnAction((ActionEvent e) -> {
			EditableMarkupPopup self = selfRef.get();
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
