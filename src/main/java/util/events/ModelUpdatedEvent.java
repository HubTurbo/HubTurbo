package util.events;

import backend.interfaces.IModel;
import backend.resource.MultiModel;

import java.util.Optional;

public class ModelUpdatedEvent extends Event {
    public final IModel model;
    public final boolean hasMetadata;

    public ModelUpdatedEvent(MultiModel models, boolean hasMetadata) {
        this.model = models;
        this.hasMetadata = hasMetadata;
    }
}
