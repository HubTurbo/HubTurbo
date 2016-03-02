package util.events;

import backend.resource.TurboUser;
import filter.expression.FilterExpression;

import java.util.List;
import java.util.Map;

public final class WarnUserEvent extends Event {
    // Each List of warning string matches with a panel identified by the filter expression.
    // Each warning list in the list is then displayed as a tooltip of warning list in the panel.
    public final Map<FilterExpression, List<String>> warningsToShow;

    public WarnUserEvent(Map<FilterExpression, List<String>> warningsToShow) {
        this.warningsToShow = warningsToShow;
    }
}
