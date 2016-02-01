package filter;

/**
 * Thrown to indicate failure while processing a filter expression
 * Exceptions that extend this class should provide an appropriate error message
 * and reason that it occurs
 */
@SuppressWarnings("serial")
public abstract class FilterException extends RuntimeException {
    public FilterException(String message) {
        super(message);
    }
}
