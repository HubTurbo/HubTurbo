package logic;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Label;

public class TurboLabel implements Listable {
	private Label ghLabel;
	
	public TurboLabel(String name) {
		setName(name);
		setColour("000000");
	}
	
	public TurboLabel(Label label) {
		assert label != null;
		
		this.ghLabel = label;
		
		setName(label.getName());
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

	@Override
	public String getListName() {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ghLabel == null) ? 0 : ghLabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TurboLabel other = (TurboLabel) obj;
		if (ghLabel == null) {
			if (other.ghLabel != null)
				return false;
		} else if (!ghLabel.equals(other.ghLabel))
			return false;
		return true;
	}
}
