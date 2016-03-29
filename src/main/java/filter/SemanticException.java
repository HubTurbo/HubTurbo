package filter;

import filter.expression.QualifierType;

/**
 * Thrown to indicate failure to interpret a filter expression which may
 * be resulted from invalid qualifier inputs, multiple qualifiers which
 * should only appear once or simply an empty input.
 */
@SuppressWarnings("serial")
public class SemanticException extends FilterException {

    public static final String ERROR_MESSAGE = "\"%s\" expects %s";

    public SemanticException(QualifierType type) {
        super(String.format(ERROR_MESSAGE, type, type.getDescriptionOfValidInputs()));
    }

    public SemanticException(String message) {
        super(message);
    }

}
