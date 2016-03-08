package ui.components;

import filter.ParseException;
import filter.Parser;
import javafx.application.Platform;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A field augmented with the ability to revert edits, autocompletion, on-the-fly error
 * checking, and callbacks for various actions.
 */
public class FilterTextField extends TextField {

    // A character class that indicates the characters considered to be word boundaries.
    // Specialised to HubTurbo's filter syntax.
    private static final String WORD_BOUNDARY_REGEX = "[ (:)]";

    // Background colours of FilterTextField.
    // The background colour set depends on whether the text in the textfield is a valid filter.
    private static final String VALID_FILTER_STYLE = "-fx-control-inner-background: white";
    private static final String INVALID_FILTER_STYLE = "-fx-control-inner-background: #EE8993";

    // Callback functions
    private Runnable cancel = () -> {};
    private Function<String, String> confirm = (s) -> s;

    // For reverting edits
    private String previousText;

    // The list of keywords which will be used in completion
    private List<String> keywords = new ArrayList<>();

    // For on-the-fly parsing and checking
    private final ValidationSupport validationSupport = new ValidationSupport();

    public FilterTextField(String initialText) {
        super(initialText);
        previousText = initialText;
        setup();
    }

    private void setup() {
        setPrefColumnCount(30);
        validationSupport.registerValidator(this, (c, newValue) -> {
            boolean wasError = false;
            try {
                Parser.parse(getText());
                setStyleForValidFilter();
            } catch (ParseException e) {
                wasError = true;
                setStyleForInvalidFilter();
            }
            return ValidationResult.fromErrorIf(this, "Parse error", wasError);
        });
        setOnKeyTyped(e -> {
            boolean isModifierKeyPress = e.isAltDown() || e.isMetaDown() || e.isControlDown();
            String key = e.getCharacter();
            if (key == null || key.isEmpty() || isModifierKeyPress) {
                return;
            }
            char typed = e.getCharacter().charAt(0);
            // \b will allow us to detect deletion, but we can't find out the characters deleted
            if (typed == '\t') {
                e.consume();
                if (!getSelectedText().isEmpty()) {
                    confirmCompletion();
                }
            } else if (Character.isAlphabetic(typed) && shouldStartCompletion()) {
                startCompletion(e);
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
                confirmEdit();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                if (getText().equals(previousText)) {
                    cancel.run();
                } else {
                    revertEdit();
                }
            }
        });
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
     * Completion is only started when there's effectively nothing (only whitespace)
     * after the caret, or if there is selected text (indicating either that we are in
     * the midst of completion, or that the user does not want the selected text and is
     * typing to replace it)
     * @return true if completion should be started
     */
    private boolean shouldStartCompletion() {
        return getCharAfterCaret().trim().isEmpty() || !getSelectedText().isEmpty();
    }

    /**
     * Determines if the word being edited begins a registered completion word.
     * If so, performs completion.
     */
    private void startCompletion(KeyEvent e) {
        String editedWord = getCurrentWord() + e.getCharacter();
        for (String candidateWord : keywords) {
            if (candidateWord.startsWith(editedWord)) {
                performCompletionOfWord(e, editedWord, candidateWord);
                break;
            }
        }
    }

    /**
     * Low-level manipulation of field selection to implement completion
     */
    private void performCompletionOfWord(KeyEvent e, String word, String candidateWord) {
        e.consume();
        int caret = getCaretPosition();
        if (getSelectedText().isEmpty()) {
            String before = getText().substring(0, caret);
            String insertion = e.getCharacter();
            String after = getText().substring(caret, getText().length());
            String addition = candidateWord.substring(word.length());
            setText(before + insertion + addition + after);
            Platform.runLater(() -> selectRange(
                before.length() + insertion.length() + addition.length(),
                before.length() + insertion.length()));
        } else {
            IndexRange sel = getSelection();
            int start = Math.min(sel.getStart(), sel.getEnd());
            int end = Math.max(sel.getStart(), sel.getEnd());
            String before = getText().substring(0, start);
            String after = getText().substring(end, getText().length());
            String insertion = e.getCharacter();
            String addition = candidateWord.substring(word.length());
            setText(before + insertion + addition + after);
            Platform.runLater(() -> selectRange(
                before.length() + insertion.length() + addition.length(),
                before.length() + insertion.length()));
        }
    }

    /**
     * Confirms a completion by moving to the extreme right side of the selection
     */
    private void confirmCompletion() {
        positionCaret(Math.max(getSelection().getStart(), getSelection().getEnd()));
    }

    /**
     * Determines the word currently being edited.
     */
    private String getCurrentWord() {
        int caret = Math.min(getSelection().getStart(), getSelection().getEnd());
        int pos = regexLastIndexOf(getText().substring(0, caret), WORD_BOUNDARY_REGEX);
        if (pos == -1) {
            pos = 0;
        }
        return getText().substring(pos > 0 ? pos + 1 : pos, caret);
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
     * Returns the character following the caret as a string. The string returned
     * is guaranteed to be of length 1, unless the caret is at the end of the field,
     * in which case it is empty.
     */
    private String getCharAfterCaret() {
        if (getCaretPosition() < getText().length()) {
            return getText().substring(getCaretPosition(), getCaretPosition() + 1);
        }
        return "";
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
     * Commits the current contents of the field. This triggers its 'confirm' callback.
     */
    private void confirmEdit() {
        previousText = getText();
        String newText = confirm.apply(getText());
        int caretPosition = getCaretPosition();
        setText(newText);
        positionCaret(caretPosition);
    }

    /**
     * Sets the contents of the field and acts as if it was confirmed by the user.
     */
    public void setFilterText(String text) {
        setText(text);
        confirmEdit();
    }

    /**
     * Sets the 'cancel' callback, which will be called with Esc is pressed and there is
     * no edit to revert.
     */
    public FilterTextField setOnCancel(Runnable cancel) {
        this.cancel = cancel;
        return this;
    }

    /**
     * Sets the 'confirm' callback, which will be called when Enter is pressed, or when the
     * contents of the field are manually set with {@link #setFilterText}. The callback will
     * be passed the current contents of the field.
     */
    public FilterTextField setOnConfirm(Function<String, String> confirm) {
        this.confirm = confirm;
        return this;
    }

    /**
     * Sets the list of words to be used as completion candidates.
     */
    public void setCompletionKeywords(List<String> words) {
        keywords = new ArrayList<>(words);
    }
}
