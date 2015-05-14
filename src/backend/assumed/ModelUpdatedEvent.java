package backend.assumed;

import backend.resource.Model;
import util.events.Event;

public class ModelUpdatedEvent extends Event {
	public Model model;

    public ModelUpdatedEvent(Model model) {
	    this.model = model;
	}
}
