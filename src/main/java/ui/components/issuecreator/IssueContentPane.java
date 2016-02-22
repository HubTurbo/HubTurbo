package ui.components.issuecreator;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.PopupAlignment;
import org.json.JSONObject;
import org.pegdown.PegDownProcessor;


import util.Utility;
import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.WindowEvent;

/**
 * Models GitHub comment box with toggleable Markdown preview
 */
public class IssueContentPane extends StackPane {

    public static final String HTML_TEMPLATE =
            "<!DOCTYPE html><html><head></head><body>%s</body></html>";
    public static final String REFERENCE_ISSUE = "#%d %s";
    public static final int MAX_SUGGESTION_ENTRIES = 5;
    public static final String CONTENT_ID = "body";

    public static final KeyCodeCombination PREVIEW = new KeyCodeCombination(KeyCode.P, 
            KeyCodeCombination.ALT_DOWN);
    public static final KeyCodeCombination MENTION = new KeyCodeCombination(KeyCode.DIGIT2, 
            KeyCodeCombination.SHIFT_DOWN);
    public static final KeyCodeCombination REFERENCE = new KeyCodeCombination(KeyCode.DIGIT3,
            KeyCodeCombination.SHIFT_DOWN);
    public static final KeyCodeCombination PASTE = new KeyCodeCombination(KeyCode.V,
            KeyCodeCombination.CONTROL_DOWN);
    public static final KeyCodeCombination HIDE_SUGGGESTIONS =
            new KeyCodeCombination(KeyCode.SPACE);

    private static final String IMAGE_LINK = "![screenshot](%s)";
    private static final String LINK_STYLE = "-fx-font-weight: bold; -fx-fill: red";
    private static final double CONTENT_WIDTH = 200;
    private static final String MENTION_KEY = "@";
    private static final String REFERENCE_KEY = "#";

    private final PegDownProcessor markdownProcessor;
    private final InlineCssTextArea body;
    private final WebView preview;
    private final SuggestionMenu suggestions;
    private final IssueCreatorPresenter presenter;

    // Caret position of @mention or #reference
    private int initialCaretPosition;

    public IssueContentPane(String content, IssueCreatorPresenter presenter) {
        this.presenter = presenter;
        suggestions = new SuggestionMenu(MAX_SUGGESTION_ENTRIES);
        markdownProcessor = initMarkdownProcessor();

        // Order of these methods are important
        body = initContent();
        preview = initWebView();

        body.replaceText(content);
        initialCaretPosition = body.getCaretPosition();
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
        setOnMouseClicked(this::mouseClickHandler);
        body.setOnKeyPressed(this::bodyKeyPressHandler);
        body.setOnKeyReleased(this::querySuggestionsHandler);
        suggestions.setOnHiding(this::onHidingHandler);
    }


    // ==============
    // Event handlers
    // ==============

    @SuppressWarnings("PMD")
    private void mouseClickHandler(MouseEvent e) {
        body.toFront();
        body.requestFocus();
    }

    /**
     * Triggers context menu for every keyword
     */
    private void bodyKeyPressHandler(KeyEvent e) {
        if (PASTE.match(e)) {
            Optional<Image> paste = getImageFromClipboard();
            if (paste.isPresent()) {
                new UploadImageTask(paste.get()).response.ifPresent(json -> {
                    try {
                        body.appendText(" " + String.format(
                            IMAGE_LINK, ((JSONObject) json.get("data")).getString("link")));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }
        }

        if (PREVIEW.match(e)) {
            generatePreview(body.getText());
            togglePane();
            e.consume();
        }
        
        if (canShowSuggestions(MENTION, e)) {
            showSuggestions(getMatchedUsers(presenter.getUsers(), ""));
        }
        
        if (canShowSuggestions(REFERENCE, e)) {
            showSuggestions(getMatchedIssues(presenter.getIssues(), ""));
        }
    }

    @SuppressWarnings("PMD")
    private void querySuggestionsHandler(KeyEvent event) {
        if (body.getCaretPosition() > initialCaretPosition 
            && !(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN)
            && !canMatchSequence(body.getCaretPosition(), 1, " ")) {

            if (!suggestions.isShowing()) suggestions.show(this.getScene().getWindow());

            querySuggestions(body.getText(initialCaretPosition, body.getCaretPosition()));
        } 
        
        if (HIDE_SUGGGESTIONS.match(event)) suggestions.hide();
    }

    @SuppressWarnings("PMD")
    private void onHidingHandler(WindowEvent event) {
        suggestions.getSelectedContent().ifPresent(c -> {
            String content = processSelectedContent(c) + " ";
            body.replaceText(initialCaretPosition + 1, body.getCaretPosition(), content);
            body.setStyle(initialCaretPosition, body.getCaretPosition() - 1, LINK_STYLE);
        });
    }

    // ===========
    // Suggestions
    // ===========

    private void showSuggestions(List<String> content) {
        // For extraction of query text
        initialCaretPosition = body.getCaretPosition();
        suggestions.loadSuggestions(content);
        suggestions.show(this.getScene().getWindow());
    }

    private void querySuggestions(String input) {
        String symbol = input.substring(0, 1);
        String query = input.substring(1, input.length());
        
        if (MENTION_KEY.equals(symbol)) {
            suggestions.loadSuggestions(getMatchedUsers(presenter.getUsers(), query));
        }

        if (REFERENCE_KEY.equals(symbol)) {
            suggestions.loadSuggestions(getMatchedIssues(presenter.getIssues(), query));
        }
    }

    /**
     * Only trigger suggestions after "@" or "#" is pressed at the beginning of 
     * a new token i.e suggestions will not be triggered for "#hello#'
     */
    private boolean canShowSuggestions(KeyCodeCombination key, KeyEvent event) {
        int caretPosition = body.getCaretPosition();

        return canMatchSequence(caretPosition, 1, " ") && key.match(event);
    }

    private String processSelectedContent(String input) {
        if (isReferenceContent(input)) return extractIssueId(input);
        return input;
    }

    private String extractIssueId(String input) {
        return input.split(" ")[0].substring(1);
    }

    private boolean isReferenceContent(String input) {
        return Utility.containsIgnoreCase(input, "#");
    }

    /**
     * Get issues with title or id that matches the query.
     * Supports only single word query 
     * @param issues
     * @param query
     * @return list of matching issues title
     */
    private List<String> getMatchedIssues(List<TurboIssue> issues, String query) {
        return issues.stream().sorted(Comparator.comparing(TurboIssue::getId))
                .map(i -> String.format(REFERENCE_ISSUE, i.getId(), i.getTitle()))
                .filter(str -> Utility.containsIgnoreCase(str, query))
                .collect(Collectors.toList());
    }

    /**
     * Get users which matches the query
     * @param users
     * @param query
     * @return list of matching users 
     */
    private List<String> getMatchedUsers(List<TurboUser> users, String query) {
        return users.stream().map(TurboUser::getLoginName).sorted()
            .filter(str -> Utility.containsIgnoreCase(str, query))
            .collect(Collectors.toList());
    }

    // ===============
    // Textarea helper 
    // ===============
    
    /**
     * Checks last n characters from given caret position with a query
     * @param pos position of caret in text area
     */
    private boolean canMatchSequence(int pos, int n, String query) {
        return body.getText(pos - n, pos).equals(query);
    }

    // =================
    // UI initialization
    // =================

    /**
     * Initializes WebView
     */
    private WebView initWebView() {
        WebView preview = new WebView();
        preview.setPrefWidth(CONTENT_WIDTH);
        this.getChildren().add(preview);
        return preview;
    }

    /**
     * Initializes Styled Text Area
     */
    private InlineCssTextArea initContent() {
        InlineCssTextArea content = new InlineCssTextArea();
        content.setId(CONTENT_ID);
        content.setPrefWidth(CONTENT_WIDTH);
        content.setWrapText(true);
        content.setPopupWindow(suggestions);
        content.setPopupAlignment(PopupAlignment.CARET_BOTTOM);
        this.getChildren().add(content);
        return content;
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

    private Optional<Image> getImageFromClipboard() {
        Clipboard cb = Clipboard.getSystemClipboard();
        return Optional.ofNullable(cb.getImage());
    }
}
