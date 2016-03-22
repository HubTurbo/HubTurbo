package filter;

/**
 * Thrown to indicate failure to parse a filter expression due to
 * unsupported syntax
 */
@SuppressWarnings("serial")
public class ParseException extends FilterException {
    public ParseException(String message) {
        super("Error in filter: " + message);
    }
}
