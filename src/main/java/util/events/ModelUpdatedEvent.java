package util.events;

import backend.resource.TurboUser;
import filter.expression.FilterExpression;
import ui.GuiElement;

import java.util.List;
import java.util.Map;

public class ModelUpdatedEvent extends Event {
    public final Map<FilterExpression, List<GuiElement>> elementsToShow;
    public final List<TurboUser> users;

    public ModelUpdatedEvent(Map<FilterExpression, List<GuiElement>> elementsToShow,
                             List<TurboUser> users) {
        this.elementsToShow = elementsToShow;
        this.users = users;
    }
}
