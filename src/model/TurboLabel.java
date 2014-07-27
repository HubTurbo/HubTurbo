package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Label;

import ui.LabelTreeItem;

public class TurboLabel implements Listable, LabelTreeItem {
	
	/*
	 * Attributes, Getters & Setters
	 */
	public static final String EXCLUSIVE_DELIM = ".";
	public static final String NONEXCLUSIVE_DELIM = "-";
	
	public String getValue() {return getName();}
	public void setValue(String value) {setName(value);}
	
	private StringProperty name = new SimpleStringProperty();
    public final String getName() {return name.get();}
    public final void setName(String value) {
    	assert value != "";
    	assert value != null;
    	name.set(value);
    	}
    public StringProperty nameProperty() {return name;}
	
    private StringProperty colour = new SimpleStringProperty();
    public final String getColour() {return colour.get();}
    public final void setColour(String value) {colour.set(value);}
    public StringProperty colourProperty() {return colour;}
    
    private StringProperty group = new SimpleStringProperty();
    public final String getGroup() {return group.get();}
    public final void setGroup(String value) {group.set(value);}
    public StringProperty groupProperty() {return group;}
    
	private boolean isExclusive; // exclusive: "." non-exclusive: "-"
	public boolean isExclusive() {return isExclusive;}
	public void setExclusive(boolean isExclusive) {
		if (getGroup() != null) this.isExclusive = isExclusive;
	}
	
	/*
	 * Constructors and Public Methods
	 */
	
//	public TurboLabel(String name) {
//		setName(name);
//		setColour("000000");
//	}
	
	public TurboLabel(){
		setColour("000000");
	}
	
	public TurboLabel(Label label) {
		assert label != null;
		
		String labelName = label.getName();
		String[] tokens = TurboLabel.parseName(labelName);
//		if (labelName.contains(".")) {
//			tokens = labelName.split("\\.", 2);
//			setGroup(tokens[0]);
//			setName(tokens[1]);
//			setExclusive(true);
//		} else if (labelName.contains("-")) {
//			tokens = labelName.split("-", 2);
//			setGroup(tokens[0]);
//			setName(tokens[1]);
//			setExclusive(false);
//		} else {
//			setName(labelName);
//		}
		if(tokens == null){
			setName(labelName);
		}else{
			setGroup(tokens[0]);
			setName(tokens[1]);
			setExclusive(tokens[2].equals(EXCLUSIVE_DELIM));
		}
		setColour(label.getColor());
	}
	
	public void copyValues(Object other){
		if(other.getClass() == TurboLabel.class){
			TurboLabel obj = (TurboLabel)other;
			setName(obj.getName());
			setColour(obj.getColour());
			setGroup(obj.getGroup());
			setExclusive(obj.isExclusive);
		}
	}
	
	public Label toGhResource() {
		Label ghLabel = new Label();
		ghLabel.setName(toGhName());
		ghLabel.setColor(getColour());
		return ghLabel;
	}
	
	public String toGhName() {
		String groupDelimiter = isExclusive ? "." : "-";
		String groupPrefix = getGroup() == null ? "" : getGroup() + groupDelimiter;
		String groupAppended = groupPrefix + getName();
		return groupAppended;
	}
	
	public static List<Label> toGhLabels(List<TurboLabel> turboLabels) {
		List<Label> ghLabels = new ArrayList<Label>();
		
		if (turboLabels == null) return ghLabels;
		
		for (TurboLabel turboLabel : turboLabels) {
			Label label = new Label();
			label.setName(turboLabel.toGhName());
			label.setColor(turboLabel.getColour());
			ghLabels.add(label);
		}
		return ghLabels;
	}
	
	public String getStyle() {
		String colour = getColour();
		int R = Integer.parseInt(colour.substring(0, 2), 16);
		int G = Integer.parseInt(colour.substring(2, 4), 16);
		int B = Integer.parseInt(colour.substring(4, 6), 16);
		double L = 0.2126 * R + 0.7152 * G + 0.0722 * B;
		boolean bright = L > 128;
		return "-fx-background-color: #" + getColour() + "; -fx-text-fill: " + (bright ? "black" : "white");
	}
	
	public static String[] parseName(String name) {
		String[] result = new String[3];
		int dotPos = name.indexOf(EXCLUSIVE_DELIM);
		int dashPos = name.indexOf(NONEXCLUSIVE_DELIM);
		int pos = -1;
		if(dotPos == -1){
			pos = dashPos;
		}else if(dashPos == -1){
			pos = dotPos;
		}else{
			pos = Math.min(dashPos, dotPos);
		}
		
		if (pos == -1) {
			return null;
		} else {
			result[0] = name.substring(0, pos);
//			result[1] = name.substring(dotPos+1).replaceAll("\\.", "");
			result[1] = name.substring(pos+1);
			result[2] = name.substring(pos, pos+1);
			return result;
		}
	}
	
	public static HashMap<String, ArrayList<TurboLabel>> groupLabels(Collection<TurboLabel> labels, String ungroupedName) {
		HashMap<String, ArrayList<TurboLabel>> groups = new HashMap<>();
		for (TurboLabel l : labels) {
			String groupName = l.getGroup() == null ? ungroupedName : l.getGroup();

			if (groups.get(groupName) == null) {
				groups.put(groupName, new ArrayList<TurboLabel>());
			}
			groups.get(groupName).add(l);
		}
		return groups;
	}
	
	/*
	 * Overriden Methods
	 */

	@Override
	public String getListName() {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : toGhName().hashCode());
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
			return other.name == null;
		}
		return this.toGhName().equals(other.toGhName());
	}

	@Override
	public String toString() {
        String groupDelimiter = isExclusive ? "." : "-";
        return getGroup() + groupDelimiter + getName();
    }
	
}
