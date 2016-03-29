package ui.components;

import filter.expression.QualifierType;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ui.components.KeyboardShortcuts.SHOW_DOCS;

/**
 * A field augmented with the ability to revert edits, autocompletion, on-the-fly error
 * checking, and callbacks for various actions.
 */
public class FilterTextField extends TextField {

    private static final int MAX_SUGGESTIONS = 20;

    // A character class that indicates the characters considered to be word boundaries.
    // Specialised to HubTurbo's filter syntax.
    private static final String WORD_BOUNDARY_REGEX = "[ (:)]";

    // Background colours of FilterTextField.
    // The background colour set depends on whether the text in the textfield is a valid filter.
    public static final String VALID_FILTER_STYLE = "-fx-control-inner-background: white";
    public static final String INVALID_FILTER_STYLE = "-fx-control-inner-background: #EE8993";

    // For on-the-fly parsing and checking
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final SuggestionsMenu suggestions;

    // Callback functions
    private Runnable onCancel = () -> {
    };
    private Runnable onShowDocs = () -> {
    };
    private Function<String, String> onConfirm = (s) -> s;

    // For reverting edits
    private String previousText = "";

    // Shows that is navigating the suggestion menu
    private boolean isNavigating = false;

    // The list of keywords which will be used in completion
    private List<String> keywords = initCompletionKeywords();


    public FilterTextField(Predicate<String> validation) {
        super("");
        suggestions = setupSuggestion();
        setup(validation);
    }

    private List<String> initCompletionKeywords() {
        return QualifierType.getCompletionKeywords().stream().sorted().collect(Collectors.toList());
    }

    private void setup(Predicate<String> validation) {
        setPrefColumnCount(30);
        validationSupport.registerValidator(this, (c, newValue) -> {
            boolean isError = !validation.test(getText());

            if (isError) {
                setStyleForInvalidFilter();
            } else {
                setStyleForValidFilter();
            }

            return ValidationResult.fromErrorIf(this, "Parse error", isError);
        });
        setOnKeyTyped(e -> {
            boolean isModifierKeyPress = e.isAltDown() || e.isMetaDown() || e.isControlDown();
            String key = e.getCharacter();
            if (key == null || key.isEmpty() || isModifierKeyPress) {
                return;
            }
            
            char typed = key.charAt(0);
            
            if (typed == '\t' && suggestions.isShowing()) {
                suggestions.hide();
                suggestions.getSelectedContent().ifPresent(this::completeWord);
            } else {
                suggestions.loadSuggestions(getMatchingKeywords(getCurrentWord()));
                if (!suggestions.isShowing()) suggestions.show(this, Side.BOTTOM, 0, 0);
            }
        });
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.TAB) {
                // Disable tab for UI traversal
                e.consume();
            }
        });
        setOnKeyReleased(e -> {
            e.consume();

            if (e.getCode() == KeyCode.ENTER) {
                handleOnEnter();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                handleOnEscape();
            }

            isNavigating = e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN;

            if (suggestions.isShowing() && !isNavigating) {
                suggestions.loadSuggestions(getMatchingKeywords(getCurrentWord()));
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (SHOW_DOCS.match(event)) {
                onShowDocs.run();
            }
        });
    }

    private void handleOnEnter() {
        suggestions.hide();
        if (isNavigating) {
            suggestions.getSelectedContent().ifPresent(this::completeWord);
        } else {
            confirmEdit();
        }
    }

    private void handleOnEscape() {
        if (getText().equals(previousText)) {
            onCancel.run();
        } else if (suggestions.isShowing()){
            suggestions.hide();
        } else {
            revertEdit();
        }
    }


    /**
     * Sets the style of FilterTextField to the style for valid filter
     */
    public final void setStyleForValidFilter() {
        this.setStyle(VALID_FILTER_STYLE);
    }

    /**
     * Sets the style of FilterTextField to the style for invalid filter
     */
    public final void setStyleForInvalidFilter() {
        this.setStyle(INVALID_FILTER_STYLE);
    }

    /**
     * @param query
     * @return suggested keyword that contains a given query
     */
    private List<String> getMatchingKeywords(String query) {
        return keywords.stream().filter(keyword -> keyword.contains(query)).collect(Collectors.toList());
    }


    /**
     * Determines the word currently being edited.
     */
    private String getCurrentWord() {
        int caret = Math.min(getSelection().getStart(), getSelection().getEnd());
        return getText().substring(getInitialCaretPosition(caret), caret);
    }

    /**
     * @param caret
     * @return index before first char in current word 
     */
    private int getInitialCaretPosition(int caret) {
        int pos = regexLastIndexOf(getText().substring(0, caret), WORD_BOUNDARY_REGEX);
        if (pos == -1) {
            pos = 0;
        }
        return pos > 0 ? pos + 1 : pos;
    }

    /**
     * Given a string and a regex, determines the last occurrence of a substring matching
     * that regex in the string.
     * Caveat: algorithm only works for character-class regexes
     */
    private static int regexLastIndexOf(String inString, String charClassRegex) {
        String reversed = new StringBuilder(inString).reverse().toString();
        Pattern pattern = Pattern.compile(charClassRegex);
        Matcher m = pattern.matcher(reversed);
        if (m.find()) {
            return reversed.length() - (m.start() + 1);
        } else {
            return -1;
        }
    }


    /**
     * Reverts the contents of the field to its last confirmed value.
     */
    private void revertEdit() {
        setText(previousText);
        positionCaret(getLength());
        selectAll();
    }

    /**
     * Commits the current contents of the field. This triggers its 'onConfirm' callback.
     */
    private void confirmEdit() {
        previousText = getText();
        String newText = onConfirm.apply(getText());
        int caretPosition = getCaretPosition();
        setText(newText);
        positionCaret(caretPosition);
    }

    // SuggestionMenu 
    
    private SuggestionsMenu setupSuggestion() {
        SuggestionsMenu suggestion = new SuggestionsMenu(MAX_SUGGESTIONS).setActionHandler(this::menuItemHandler);
        suggestion.loadSuggestions(keywords);
        return suggestion;
    }

    /**
     * Replaces current word with suggested word
     * @param suggestedWord
     */
    private void completeWord(String suggestedWord) {
        int caret = getCaretPosition();
        selectRange(getInitialCaretPosition(caret), caret);
        replaceSelection(suggestedWord);
    }


    private void menuItemHandler(ActionEvent event) {
        SuggestionsMenu.getTextOnAction(event).ifPresent(this::completeWord);
        suggestions.hide();
    }

    /**
     * Sets the contents of the field and acts as if it was confirmed by the user.
     */
    public void setFilterText(String text) {
        setText(text);
        confirmEdit();
    }

    /**
     * Sets the 'onCancel' callback, which will be called with Esc is pressed and there is
     * no edit to revert.
     */
    public FilterTextField setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    /**
     * Sets the 'onConfirm' callback, which will be called when Enter is pressed, or when the
     * contents of the field are manually set with {@link #setFilterText}. The callback will
     * be passed the current contents of the field.
     */
    public FilterTextField setOnConfirm(Function<String, String> onConfirm) {
        this.onConfirm = onConfirm;
        return this;
    }

    /**
     * Sets the 'onShowDocs' callback, which will be called when the user triggers the show_docs
     * keyboard shortcut event.
     */
    public FilterTextField setOnShowDocs(Runnable onShowDocs) {
        this.onShowDocs = onShowDocs;
        return this;
    }

    /**
     * Sets the list of words to be used as completion candidates.
     */
    public void setCompletionKeywords(List<String> words) {
        keywords = new ArrayList<>(words);
    }
}
