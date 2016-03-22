package util.events;

import backend.resource.TurboUser;
import filter.expression.FilterExpression;
import ui.GuiElement;

import java.util.List;
import java.util.Map;

public class ModelUpdatedEvent extends Event {
    // Each List of GuiElements matches with a panel identified by the filter expression.
    // Each GuiElement in the list is then displayed as an issue card in the panel.
    public final Map<FilterExpression, List<GuiElement>> elementsToShow;
    public final List<TurboUser> users; // User names are used as autocomplete keywords for filters.

    public ModelUpdatedEvent(Map<FilterExpression, List<GuiElement>> elementsToShow,
            List<TurboUser> users) {
        this.elementsToShow = elementsToShow;
        this.users = users;
    }
}
