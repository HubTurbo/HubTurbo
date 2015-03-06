package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import org.eclipse.egit.github.core.Label;

public class TurboLabel implements Listable {
	
	/*
	 * Attributes, Getters & Setters
	 */
	public static final String EXCLUSIVE_DELIM = ".";
	public static final String NONEXCLUSIVE_DELIM = "-";
	
	public String getValue() {return getName();}
	public void setValue(String value) {setName(value);}
	
	private String name = "";
    public String getName() {
    	return name;
    }

    public void setName(String value) {
    	assert value != "";
    	assert value != null;
    	name = value;
    }
	
    private String colour = "";
    public final String getColour() {return colour;}
    public final void setColour(String value) {colour = value;}
    
    private String group = "";
    public final String getGroup() {return group;}
    public final void setGroup(String value) {group = value;}
    
	private boolean isExclusive; // exclusive: "." non-exclusive: "-"
	public boolean isExclusive() {return isExclusive;}
	public void setExclusive(boolean isExclusive) {
		if (getGroup() != null) this.isExclusive = isExclusive;
	}
	
	/*
	 * Constructors and Public Methods
	 */

	public TurboLabel(TurboLabel other){
		this(other.toGhResource());
	}

	public TurboLabel(){
		setColour("000000");
	}
	
	public TurboLabel(Label label) {
		assert label != null;
		
		String labelName = label.getName();
		Optional<String[]> tokens = TurboLabel.parseName(labelName);
		if(!tokens.isPresent()){
			setName(labelName);
		}else{
			setGroup(tokens.get()[0]);
			setName(tokens.get()[1]);
			setExclusive(tokens.get()[2].equals(EXCLUSIVE_DELIM));
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
	
	public String getGroupDelimiter(){
		String groupDelimiter = isExclusive ? EXCLUSIVE_DELIM : NONEXCLUSIVE_DELIM;
		return groupDelimiter;
	}
	
	public String toGhName() {
		String groupDelimiter = isExclusive ? EXCLUSIVE_DELIM : NONEXCLUSIVE_DELIM;
		String groupPrefix = (getGroup() == null || getGroup().isEmpty()) ? "" : getGroup() + groupDelimiter;
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

	public Node getNode() {
		javafx.scene.control.Label node = new javafx.scene.control.Label(getName());
		node.getStyleClass().add("labels");
		node.setStyle(getStyle());
		if (getGroup() != null) {
			Tooltip groupTooltip = new Tooltip(getGroup());
			node.setTooltip(groupTooltip);
		}
		return node;
	}
	
	/**
	 * Returns an array in the format:
	 * 
	 * {
	 *     label group,
	 *     label name,
	 *     separator
	 * }
	 * 
	 * May fail if the string is not in the format group.name or group-name.
	 */
	public static Optional<String[]> parseName(String name) {
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
			return Optional.empty();
		} else {
			result[0] = name.substring(0, pos);
			result[1] = name.substring(pos+1);
			result[2] = name.substring(pos, pos+1);
			return Optional.of(result);
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
		return "TurboLabel [name=" + name + ", group=" + group + "]";
	}
	
	/**
	 * A convenient string representation of this object, for purposes of readable logs.
	 * @return
	 */
	public String logString() {
		return toGhName();
	}
}
