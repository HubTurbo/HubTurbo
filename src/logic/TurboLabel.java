package logic;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Label;

public class TurboLabel {
	private Label ghLabel;
	
	public TurboLabel(String name) {
		setName(name);
		setColour("#000000");
	}
	
	public TurboLabel(Label label) {
		this.ghLabel = label;
		if (label != null) {
			setName(label.getName());
		}
		setColour(label.getColor());
	}
	
	public Label getGhLabel() {
		return ghLabel;
	}

    private StringProperty name = new SimpleStringProperty();
    public final String getName() {return name.get();}
    public final void setName(String value) {name.set(value);}
    public StringProperty nameProperty() {return name;}
	
    private StringProperty colour = new SimpleStringProperty();
    public final String getColour() {return colour.get();}
    public final void setColour(String value) {colour.set(value);}
    public StringProperty colourProperty() {return colour;}
	
}
