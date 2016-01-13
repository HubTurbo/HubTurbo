package util.events;

import backend.resource.TurboUser;
import filter.expression.FilterExpression;
import ui.GuiElement;

import java.util.List;
import java.util.Map;

public class ModelUpdatedEvent extends Event {
    public final Map<FilterExpression, List<GuiElement>> elementsToShow;
    public final List<TurboUser> users;

    /**
     * Each filter expression is matched with the list of GuiElements that were filtered and sorted based on its
     * rules. Each GuiElement, in turn, corresponds to a ListPanelCard to be displayed in a panel containing said
     * filter expression on the GUI.
     *
     * The list of current users on the repos in the MultiModel is required as their names are added to the
     * autocomplete suggestions.
     *
     * @param elementsToShow
     * @param users
     */
    public ModelUpdatedEvent(Map<FilterExpression, List<GuiElement>> elementsToShow,
                             List<TurboUser> users) {
        this.elementsToShow = elementsToShow;
        this.users = users;
    }
}
