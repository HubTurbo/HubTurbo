package ui;

import java.util.ArrayList;

import model.TurboLabel;

public class TurboLabelGroup implements LabelTreeItem {
	
	private String name;
	private ArrayList<TurboLabel> labels;
	private boolean exclusive = false;
	
	public TurboLabelGroup(String name) {
		this.name = name;
		this.labels = new ArrayList<>();
	}
		
	public TurboLabelGroup(ArrayList<TurboLabel> labels) {
		this.name = "unnamed";
		this.labels = labels;

		// determine if exclusive or not
		// a group is only exclusive if all labels inside it are
		// if a single label is not exclusive, the group is also not
		exclusive = true;
		for (TurboLabel label : labels) {
			exclusive = exclusive && label.isExclusive();
		}
	}

	public void addLabel(TurboLabel label) {
		labels.add(label);
		if (name.equals(LabelManagementComponent.UNGROUPED_NAME)) {
			label.setGroup(null);
		} else {
			label.setGroup(name);
		}
	}
	
	public String getName() {
		return getValue();
	}
	
	public void setName(String name) {
		setValue(name);
	}
	
	public String getValue() {
		return name;
	}
	public void setValue(String value) {
		this.name = value;

		if (this.name.equals(LabelManagementComponent.UNGROUPED_NAME)) {
			value = null;
		}
		
		for (TurboLabel label : labels) {
			label.setGroup(value);
		}
	}

	@Override
	public String toString() {
		return "TurboLabelGroup [name=" + name + ", labels=" + labels + "]";
	}

	public ArrayList<TurboLabel> getLabels() {
		return new ArrayList<TurboLabel>(labels);
	}

	public void setExclusive(boolean ex) {
		exclusive = ex;
		for (TurboLabel label : labels) {
			if(label.isExclusive() != ex){
				String oldName = label.toGhName();
				label.setExclusive(ex);
				//TODO:
			}
		}
	}

	public boolean isExclusive() {
		return exclusive;
	}
}
