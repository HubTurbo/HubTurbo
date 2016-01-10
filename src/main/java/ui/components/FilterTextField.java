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

public class FilterTextField extends TextField {

    // Callback functions
    private Runnable cancel = () -> {};
    private Function<String, String> confirm = (s) -> s;

    // For reversion of edits
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
            } catch (ParseException e) {
                wasError = true;
            }
            return ValidationResult.fromErrorIf(this, "Parse error", wasError);
        });
        setOnKeyTyped(e -> {
            boolean isModifierKeyPress = e.isAltDown() || e.isMetaDown() || e.isControlDown();
            String key = e.getCharacter();
            if (key == null || key.isEmpty() || isModifierKeyPress){
                return;
            }
            char typed = e.getCharacter().charAt(0);
            // \b will allow us to detect deletion, but we can't find out the characters deleted
            if (typed == '\t') {
                e.consume();
                if (!getSelectedText().isEmpty()) {
                    confirmCompletion();
                }
            } else if (Character.isAlphabetic(typed)) {
                // Only trigger completion when there's effectively nothing
                // (only whitespace) after the caret
                boolean shouldTriggerCompletion = getCharAfterCaret().trim().length() == 0;
                if (shouldTriggerCompletion) {
                    performCompletion(e);
                }
            }
        });
        setOnKeyPressed(e -> {
             if (e.getCode() == KeyCode.TAB) {
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
                    selectAll();
                }
            }
        });
    }

    private void performCompletion(KeyEvent e) {
        String word = getCurrentWord() + e.getCharacter();
        for (String candidateWord : keywords) {
            if (candidateWord.startsWith(word)) {
                performCompletionOfWord(e, word, candidateWord);
                break;
            }
        }
    }

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

    private void confirmCompletion() {
        // Confirm a completion by moving to the extreme right side
        positionCaret(Math.max(getSelection().getStart(), getSelection().getEnd()));
    }

    private String getCurrentWord() {
        int caret = Math.min(getSelection().getStart(), getSelection().getEnd());
        int pos = regexLastIndexOf(getText().substring(0, caret), "[ (:)]");
        if (pos == -1) {
            pos = 0;
        }
        return getText().substring(pos > 0 ? pos + 1 : pos, caret);
    }

    // Caveat: algorithm only works for character-class regexes
    private int regexLastIndexOf(String inString, String charClassRegex) {
        String reversed = new StringBuilder(inString).reverse().toString();
        Pattern pattern = Pattern.compile(charClassRegex);
        Matcher m = pattern.matcher(reversed);
        if (m.find()) {
            return reversed.length() - (m.start() + 1);
        } else {
            return -1;
        }
    }

    private String getCharAfterCaret() {
        if (getCaretPosition() < getText().length()) {
            return getText().substring(getCaretPosition(), getCaretPosition() + 1);
        }
        return "";
    }

    private void revertEdit() {
        setText(previousText);
        positionCaret(getLength());
    }

    private void confirmEdit() {
        previousText = getText();
        String newText = confirm.apply(getText());
        int caretPosition = getCaretPosition();
        setText(newText);
        positionCaret(caretPosition);
    }

    public void setFilterText(String text) {
        setText(text);
        confirmEdit();
    }

    public FilterTextField setOnCancel(Runnable cancel) {
        this.cancel = cancel;
        return this;
    }

    public FilterTextField setOnConfirm(Function<String, String> confirm) {
        this.confirm = confirm;
        return this;
    }

    public void setKeywords(List<String> words) {
        keywords = new ArrayList<>(words);
    }
}
