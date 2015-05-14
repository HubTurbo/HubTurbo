package backend.resource.serialization;

import backend.resource.TurboLabel;

public class SerializableLabel {

	public final boolean exclusive;
	public final String actualName;
	public final String colour;

	public SerializableLabel(TurboLabel label) {
		this.actualName = label.getActualName();
		this.colour = label.getColour();
		this.exclusive = label.isExclusive();
	}
}
