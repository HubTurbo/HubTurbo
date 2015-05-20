package backend.assumed;

import backend.resource.MultiModel;
import util.events.Event;

public class ModelUpdatedEvent extends Event {
	public MultiModel models;

    public ModelUpdatedEvent(MultiModel models) {
	    this.models = models;
	}
}
