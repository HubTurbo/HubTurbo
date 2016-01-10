package util.events;

import backend.resource.TurboUser;
import filter.expression.FilterExpression;
import ui.GUIElement;

import java.util.List;
import java.util.Map;

public class ModelUpdatedEvent extends Event {
    public final Map<FilterExpression, List<GUIElement>> elementsToShow;
    public final List<TurboUser> users;
    public final String defaultRepoId;

    public ModelUpdatedEvent(Map<FilterExpression, List<GUIElement>> elementsToShow,
                             List<TurboUser> users,
                             String defaultRepoId) {
        this.elementsToShow = elementsToShow;
        this.users = users;
        this.defaultRepoId = defaultRepoId;
    }
}
