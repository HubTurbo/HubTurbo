package util.events;

import filter.expression.FilterExpression;

/**
 * This class is meant to indicate that there is an exception thrown during the filtering of issues
 */
public class FilterExceptionEvent extends Event {
    public final FilterExpression filterExpr;
    public final String exceptionMessage;

    public FilterExceptionEvent(FilterExpression filterExpr, String exceptionMessage) {
        this.filterExpr = filterExpr;
        this.exceptionMessage = exceptionMessage;
    }
}
