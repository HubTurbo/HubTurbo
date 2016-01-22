package filter;

import filter.expression.QualifierType;

@SuppressWarnings("serial")
public class SemanticException extends FilterException {
    
    public static final String ERR_MSG = "\"%s\" expects %s";
    public SemanticException(QualifierType type, String details) {
        super(String.format(ERR_MSG, type, details));
        System.out.println(String.format(ERR_MSG, type, details));
    }

}
