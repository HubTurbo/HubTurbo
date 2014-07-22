package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import filter.ParseException;
import filter.Parser;

public class FilterTextField extends TextField {

	private Runnable cancel = () -> {};
	private Function<String, String> confirm = (s) -> s;
    private ValidationSupport validationSupport = new ValidationSupport();
    private String previousText;
    private ArrayList<String> words = new ArrayList<>(Arrays.asList(
    		"assignee", "milestone", "title", "id",
    		"parent", "label", "has", "state",
    		"open", "closed"));

	public FilterTextField(String initialText, int position) {
		super(initialText);
		previousText = initialText;
		Platform.runLater(() -> {
			requestFocus();
			positionCaret(position);
		});
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
			
			char typed = e.getCharacter().charAt(0);
			
			if (typed == ')') {
				if (getCharAfterCaret().equals(")")) {
					e.consume();
					positionCaret(getCaretPosition()+1);
				}
			} else if (typed == '(') {
				e.consume();
				insertMatchingBracket();
			} else if (typed == '\t') {
				e.consume();
				if (getSelectedText().isEmpty()) {
					movePastRemainingBrackets();
				}
				else {
					confirmCompletion();
				}
			} else if (typed == '\b') {
				// Can't find out the characters deleted...
			} else if (Character.isAlphabetic(typed)) {
				performCompletion(e);
			}
		});
		
		setOnKeyPressed(e -> {
			 if (e.getCode() == KeyCode.TAB) {
				 e.consume();
			 }
		});
		
		setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				confirmEdit();
			} else if (e.getCode() == KeyCode.ESCAPE) {
				revertEdit();
			}
		});
	}

	private void insertMatchingBracket() {
		if (getSelectedText().isEmpty()) {
			int caret = getCaretPosition();
			String before = getText().substring(0, caret);
			String after = getText().substring(caret, getText().length());
			setText(before + "()" + after);
			Platform.runLater(() -> {
				positionCaret(caret+1);
			});
		}
	}

	private void performCompletion(KeyEvent e) {
		String word = getCurrentWord() + e.getCharacter();
		
		for (int i=0; i<words.size(); i++) {
			if (words.get(i).startsWith(word)) {
				
				e.consume();
				
				int caret = getCaretPosition();
				
				if (getSelectedText().isEmpty()) {
					String before = getText().substring(0, caret);
					String insertion = e.getCharacter();
					String after = getText().substring(caret, getText().length());
					
					String addition = words.get(i).substring(word.length());
					
					setText(before + insertion + addition + after);
					Platform.runLater(() -> {
						selectRange(
								before.length() + insertion.length() + addition.length(),
								before.length() + insertion.length());
					});
				} else {
					IndexRange sel = getSelection();
//							boolean additionAfter = sel.getEnd() == caret;
					int start = Math.min(sel.getStart(), sel.getEnd());
					int end = Math.max(sel.getStart(), sel.getEnd());
					
					String before = getText().substring(0, start);
					String after = getText().substring(end, getText().length());
//							String selection = getText().substring(start, end);
					String insertion = e.getCharacter();
					
					String addition = words.get(i).substring(word.length());
					
					setText(before + insertion + addition + after);

					Platform.runLater(() -> {
						selectRange(
								before.length() + insertion.length() + addition.length(),
								before.length() + insertion.length());
					});
				}
				break;
			}
		}
	}

	private void confirmCompletion() {
		// Confirm a completion by moving to the extreme right side
		positionCaret(Math.max(getSelection().getStart(), getSelection().getEnd()));
	}

	private void movePastRemainingBrackets() {
		// The default place to move to is the end of input field
		int j = getText().length();
		
		for (int i=getCaretPosition(); i<getText().length(); i++) {
			// Stop at the first non-) character
			if (getText().charAt(i) != ')') {
				j = i;
				break;
			}
		}
		positionCaret(j);
	}
	
	private String getCurrentWord() {
//		int caret = getCaretPosition();
		int caret = Math.min(getSelection().getStart(), getSelection().getEnd());
		int pos = getText().substring(0, caret).lastIndexOf(" "); // TODO colon should also be here
		if (pos == -1) {
			pos = 0;
		}
		return getText().substring(pos > 0 ? pos+1 : pos, caret);
	}

	private String getCharAfterCaret() {
		if (getCaretPosition() < getText().length()) {
			return getText().substring(getCaretPosition(), getCaretPosition()+1);
		}
		return "";
	}
	
	private void revertEdit() {
		setText(previousText);
		positionCaret(getLength());
		cancel.run();
	}

	private void confirmEdit() {
		previousText = getText();
		String newText = confirm.apply(getText());
		setText(newText);
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
}
