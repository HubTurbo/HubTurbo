package storage;

import model.TurboLabel;

class SerializableLabel {
	private String name;
	private String colour;
	private String group;
	private boolean isExclusive;
	
	public SerializableLabel(TurboLabel label) {
		this.name = label.getName();
		this.colour = label.getColour();
		this.group = label.getGroup();
		this.isExclusive = label.isExclusive();
	}
	
	public TurboLabel toTurboLabel() {
		TurboLabel tL = new TurboLabel();
		
		tL.setName(name);
		tL.setColour(colour);
		tL.setGroup(group);
		tL.setExclusive(isExclusive);
		
		return tL;
	}
}
