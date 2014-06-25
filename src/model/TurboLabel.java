package model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Label;

import ui.LabelTreeItem;

public class TurboLabel implements Listable, LabelTreeItem {
	
	public TurboLabel(String name) {
		setName(name);
		setColour("000000");
	}
	
	public TurboLabel(Label label) {
		assert label != null;
		
		String[] tokens = label.getName().split(".");
		
		if (tokens.length > 1) {
			setGroup(tokens[0]);
			setName(tokens[1]);
		} else {
			setName(label.getName());
		}
		setColour(label.getColor());
	}
	
	public Label toGhLabel() {
		Label ghLabel = new Label();
		String groupAppended = getGroup() + "." + getName();
		ghLabel.setName(groupAppended);
		ghLabel.setColor(getColour());
		return ghLabel;
	}
	
	public static List<Label> toGhLabels(List<TurboLabel> turboLabels) {
		List<Label> ghLabels = new ArrayList<Label>();
		
		if (turboLabels == null) return ghLabels;
		
		for (TurboLabel turboLabel : turboLabels) {
			Label label = new Label();
			label.setName(turboLabel.getName());
			ghLabels.add(label);
		}
		return ghLabels;
	}

    private StringProperty name = new SimpleStringProperty();
    public final String getName() {return name.get();}
    public final void setName(String value) {name.set(value);}
    public StringProperty nameProperty() {return name;}
	
    private StringProperty colour = new SimpleStringProperty();
    public final String getColour() {return colour.get();}
    public final void setColour(String value) {colour.set(value);}
    public StringProperty colourProperty() {return colour;}
    
    private StringProperty group = new SimpleStringProperty();
    public final String getGroup() {return group.get();}
    public final void setGroup(String value) {group.set(value);}
    public StringProperty groupProperty() {return group;}

	@Override
	public String getListName() {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getGroup() + "/" + getName() + "/" + getColour();
	}
	
	public String getValue() {
		return getName();
	}

}
