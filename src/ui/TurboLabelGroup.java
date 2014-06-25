package ui;

public class TurboLabelGroup implements LabelTreeItem {
	
	private String name;
	
	public TurboLabelGroup(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return name;
	}
}
