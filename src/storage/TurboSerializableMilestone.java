package storage;

import model.TurboMilestone;

class TurboSerializableMilestone {
	private int number;
	
	//TODO
	// Problem
	private String title;
	
	private String state;
	private String description;
	
	/** Problems **/
	private String dueOnString;
	private int closed;
	private int open;
	
	public TurboSerializableMilestone(TurboMilestone milestone) {
		this.number = milestone.getNumber();
		this.title = milestone.getTitle();
		this.state = milestone.getState();
		this.description = milestone.getDescription();
		
		this.dueOnString = milestone.getDueOnString();
		this.closed = milestone.getClosed();
		this.open = milestone.getOpen();
	}
	
	public TurboMilestone toTurboMilestone() {
		TurboMilestone tM = new TurboMilestone(this.title);
		
		tM.setNumber(number);
		tM.setState(state);
		tM.setDescription(description);
		
		tM.setDueOn(dueOnString);
		tM.setDueOnString(dueOnString);
		tM.setClosed(closed);
		tM.setOpen(open);
		
		return tM;
	}
}
