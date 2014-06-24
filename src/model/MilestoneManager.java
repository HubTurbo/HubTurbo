package model;

import java.util.ArrayList;
import java.util.List;

public class MilestoneManager {
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";
	
	private List<TurboMilestone> milestones;
	
	MilestoneManager() {
		this.milestones = new ArrayList<TurboMilestone>();
	}
	
	public List<TurboMilestone> getMilestones() {
		return this.milestones;
	}
	
}
