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
	private Function<String, Void> confirm = (s) -> null;
    private ValidationSupport validationSupport = new ValidationSupport();

	public FilterTextField(String initialText, int position) {
		super(initialText);
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
					cancel.run();
				}
			}
		});
	}

	private void confirmEdit() {
		confirm.apply(getText());
	}
	
	public void setFilterText(String text) {
		setText(text);
		confirmEdit();
	}

	public FilterTextField setOnCancel(Runnable cancel) {
		this.cancel = cancel;
		return this;
	}

	public FilterTextField setOnConfirm(Function<String, Void> confirm) {
		this.confirm = confirm;
		return this;
	}
}
