package logic;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Label;

public class TurboLabel {
	private Label ghLabel;
	
	public TurboLabel(String name) {
		setName(name);
	}
	
	public TurboLabel(Label label) {
		this.ghLabel = label;
		if (label != null) {
			setName(label.getName());
		}
	}
	
	public Label getGhLabel() {
		return ghLabel;
	}

    private StringProperty name = new SimpleStringProperty();
    public final String getName() {return name.get();}
    public final void setName(String value) {name.set(value);}
    public StringProperty nameProperty() {return name;}
	
	
}
