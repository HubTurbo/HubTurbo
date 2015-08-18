package filter.expression;

/**
 * Represents an open numerical interval. It may be strict, in which case it
 * excludes both endpoints. Provides an encloses method to check if a number
 * falls within it.
 */
public class NumberRange {
    private final Integer start;
    private final Integer end;
    private final boolean strictly;

    public NumberRange(Integer start, Integer end) {
        this.start = start;
        this.end = end;
        this.strictly = false;
        checkIntervalValidity();
    }

    public NumberRange(Integer start, Integer end, boolean strict) {
        this.start = start;
        this.end = end;
        this.strictly = strict;
        checkIntervalValidity();
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

    public boolean encloses(int number) {
        if (start == null) {
            // * .. end
            return strictly ? number < end : number <= end;
        } else if (end == null) {
            // start .. *
            return strictly ? number > start : number >= start;
        } else {
            // start .. end
            return strictly
                    ? number > start && number < end
                    : number >= start && number <= end;
        }
    }

    /**
     * Checks if an interval is valid. A valid interval should contain either
     * at least a start or end.
     */
    private void checkIntervalValidity() {
        assert !(start == null && end == null) : "Both can't be null";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + (strictly ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NumberRange other = (NumberRange) obj;
        if (end == null) {
            if (other.end != null)
                return false;
        } else if (!end.equals(other.end))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        if (strictly != other.strictly)
            return false;
        return true;
    }

    @Override
    public String toString() {
        checkIntervalValidity();

        if (start == null) {
            if (strictly) {
                return "<" + end;
            } else {
                return "<=" + end;
            }
        } else if (end == null) {
            if (strictly) {
                return ">" + start;
            } else {
                return ">=" + start;
            }
        } else {
            return start + " .. " + end;
        }
    }
}
