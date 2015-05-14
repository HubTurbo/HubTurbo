package backend.resource.serialization;

import backend.resource.TurboLabel;

public class SerializableLabel {

	private final boolean exclusive;
	private final String actualName;
	private final String colour;

	public SerializableLabel(TurboLabel label) {
		this.actualName = label.getActualName();
		this.colour = label.getColour();
		this.exclusive = label.isExclusive();
	}
}
