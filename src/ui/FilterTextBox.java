package ui;

import java.util.function.Function;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class FilterTextBox extends TextField {

	private Runnable cancel = () -> {};
	private Function<String, Void> confirm = (s) -> null;
//	private String previousText;
	
	public FilterTextBox(String initialText, int position) {
//		previousText = initialText;
		super(initialText);
		positionCaret(position);
		setup();
	}

	private void setup() {
		setPrefColumnCount(30);

		setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
				if (e.getCode() == KeyCode.ENTER) {
					confirmEdit();
				} else {
//					revertEdit();
					cancel.run();
				}
			}
		});
	}

//	private void revertEdit() {
//		setText(previousText);
//	}

	private void confirmEdit() {
//		previousText = getText();
		confirm.apply(getText());
	}
	
	public void setFilterText(String text) {
		setText(text);
		confirmEdit();
	}

	public FilterTextBox setOnCancel(Runnable cancel) {
		this.cancel = cancel;
		return this;
	}

	public FilterTextBox setOnConfirm(Function<String, Void> confirm) {
		this.confirm = confirm;
		return this;
	}
}
