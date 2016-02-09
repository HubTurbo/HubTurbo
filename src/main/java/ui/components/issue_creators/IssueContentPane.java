package ui.components.issue_creators;

import org.pegdown.PegDownProcessor;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

/**
 * Models GitHub comment box with toggleable Markdown preview
 */
public class IssueContentPane extends StackPane {

    public static final KeyCodeCombination PREVIEW = 
        new KeyCodeCombination(KeyCode.P, KeyCodeCombination.ALT_DOWN);
    public static final String HTML_TEMPLATE = "<!DOCTYPE html><html><head></head><body>%s</body></html>";
    
    private static final int PREF_COL = 30;

    private final PegDownProcessor markdownProcessor; 
    private final TextArea body;
    private final WebView preview;

    public IssueContentPane(String content) {
        markdownProcessor = initMarkdownProcessor();

        // Order of these methods are important
        body = initBody();
        preview = initWebView();

        body.setText(content);
        generatePreview(content);
        setupHandlers();
    }
    
    /**
     * Returns content in text area
     */
    public String getContent() {
        return body.getText();
    }

    private void generatePreview(String content) {
        String bodyContent = markdownProcessor.markdownToHtml(content);
        String html = String.format(HTML_TEMPLATE, bodyContent);
        preview.getEngine().loadContent(html, "text/html");
    }

    private void setupHandlers() {
        this.setOnMouseClicked(e -> {
            body.toFront();
        });
        setupKeyEvents();
    }

    private void setupKeyEvents() {
        this.setOnKeyPressed(e -> {
            if (PREVIEW.match(e)) {
                generatePreview(body.getText());
                togglePane();
                e.consume();
            }
        });
    }

    private void togglePane() {
        preview.toFront();
    }

    /**
     * Setups Markdown processor to handle GitHub flavored markdown syntax
     */
    private PegDownProcessor initMarkdownProcessor() {
        return new PegDownProcessor();
    }

    /**
     * Initializes WebView and set index 
     */
    private WebView initWebView() {
        WebView preview = new WebView();
        this.getChildren().add(1, preview);
        return preview;
    }

    /**
     * Initializes TextArea and set index 
     */
    private TextArea initBody() {
        TextArea body = new TextArea();
        body.setPrefColumnCount(PREF_COL);
        body.setWrapText(true);
        this.getChildren().add(0, body);
        return body;
    }
    
}
