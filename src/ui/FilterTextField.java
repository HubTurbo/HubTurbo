package ui;

import java.util.function.Function;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import filter.ParseException;
import filter.Parser;

public class FilterTextField extends TextField {

	private Runnable cancel = () -> {};
	private Function<String, String> confirm = (s) -> s;
    private ValidationSupport validationSupport = new ValidationSupport();
    private String previousText;

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
		
		setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
				if (e.getCode() == KeyCode.ENTER) {
					confirmEdit();
				} else {
					revertEdit();
				}
			}
		});
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
