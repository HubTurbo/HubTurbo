package filter;

@SuppressWarnings("serial")
public class ParseException extends FilterException {
      public ParseException(String message) {
         super("Error in filter: " + message);
      }
 }
