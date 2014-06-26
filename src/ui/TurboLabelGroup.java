package ui;

import java.util.ArrayList;

import model.TurboLabel;

public class TurboLabelGroup implements LabelTreeItem {
	
	private String name;
	private ArrayList<TurboLabel> labels = new ArrayList<>();
	private boolean exclusive = false;
	
	public TurboLabelGroup(String name) {
		this.name = name;
	}
	
	public void addLabel(TurboLabel label) {
		labels.add(label);
		if (name.equals(ManageLabelsDialog.UNGROUPED_NAME)) {
			label.setGroup(null);
		} else {
			label.setGroup(name);
		}
	}
	
	public String getValue() {
		return name;
	}
	public void setValue(String value) {
		this.name = value;

		if (this.name.equals(ManageLabelsDialog.UNGROUPED_NAME)) {
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
			label.setExclusive(ex);
		}
	}

	public boolean getExclusive() {
		return exclusive;
	}
}
