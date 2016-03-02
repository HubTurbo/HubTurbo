package filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Thrown to warn the users about potential errors in the filter.
 * It does not inherit from FilterException since it does not really
 * indicate a failure in processing the filter.
 */
public class WarningException extends Exception {

    private final List<String> warnings;
    private final boolean filterResult;

    public WarningException(String warning, boolean filterResult) {
        this.warnings = new ArrayList<>(Arrays.asList(warning));
        this.filterResult = filterResult;
    }

    public WarningException(List<String> warningList, boolean filterResult) {
        this.warnings = new ArrayList<>(warningList);
        this.filterResult = filterResult;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean getFilterResult() {
        return filterResult;
    }
}
