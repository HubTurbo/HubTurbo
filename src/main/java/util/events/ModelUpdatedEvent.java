package util.events;

import backend.interfaces.IModel;
import backend.resource.MultiModel;

public class ModelUpdatedEvent extends Event {
    public final IModel model;
    public final boolean hasMetadata;

    public ModelUpdatedEvent(MultiModel model, boolean hasMetadata) {
        this.model = model;
        this.hasMetadata = hasMetadata;
    }
}
