package util.events;

import backend.interfaces.IModel;
import backend.resource.MultiModel;

public class ModelUpdatedEvent extends Event {
	public final IModel model;
	public final boolean triggerMetadataUpdate;

    public ModelUpdatedEvent(MultiModel model, boolean triggerMetadataUpdate) {
	    this.model = model;
	    this.triggerMetadataUpdate = triggerMetadataUpdate;
	}
}
