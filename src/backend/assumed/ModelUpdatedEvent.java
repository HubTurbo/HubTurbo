package backend.assumed;

import backend.interfaces.IModel;
import backend.resource.MultiModel;
import util.events.Event;

public class ModelUpdatedEvent extends Event {
	public IModel model;

    public ModelUpdatedEvent(MultiModel model) {
	    this.model = model;
	}
}
