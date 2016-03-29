package util.events;

import filter.expression.FilterExpression;

import java.util.List;

/**
 * This class indicates that there are warnings for a particular FilterExpression
 */
public class FilterWarningEvent extends Event {
    public final FilterExpression filterExpr;
    public final List<String> warnings;

    public FilterWarningEvent(FilterExpression filterExpr, List<String> warnings) {
        this.filterExpr = filterExpr;
        this.warnings = warnings;
        assert !warnings.isEmpty();
    }
}
