package logic;

import org.eclipse.egit.github.core.Label;

public class TurboLabel {
	private Label ghLabel;
	private String name;
	
	
	public TurboLabel(Label label) {
		this.ghLabel = label;
		this.name = label.getName();
	}
	
	public Label getGhLabel() {
		return ghLabel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
