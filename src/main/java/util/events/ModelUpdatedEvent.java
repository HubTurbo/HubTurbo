package util.events;

import backend.interfaces.IModel;
import backend.resource.MultiModel;

public class ModelUpdatedEvent extends Event {
	public IModel model;

    public ModelUpdatedEvent(MultiModel model) {
	    this.model = model;
	}
}
