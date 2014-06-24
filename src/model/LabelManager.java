package model;

import java.util.ArrayList;
import java.util.List;

public class LabelManager {
	private List<TurboLabel> labels; 
	
	LabelManager() {
		this.labels = new ArrayList<TurboLabel>();
	}
	
	public List<TurboLabel> getLabels() {
		return this.labels;
	}
	
}
