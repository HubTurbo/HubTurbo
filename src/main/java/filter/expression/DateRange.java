package filter.expression;

import java.time.LocalDate;

/**
 * Represents an open date interval. It may be strict, in which case it
 * excludes one or both endpoints. Provides an encloses method to check if a date
 * falls within it.
 */
public class DateRange {
    private final LocalDate start;
    private final LocalDate end;
    private final boolean strictly;

    public DateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
        this.strictly = false;
        checkIntervalValidity();
    }

    public DateRange(LocalDate start, LocalDate end, boolean strict) {
        this.start = start;
        this.end = end;
        this.strictly = strict;
        checkIntervalValidity();
    }

    public boolean encloses(LocalDate date) {
        if (start == null) {
            // * .. end
            return date.isBefore(end) || (!strictly && date.isEqual(end));
        } else if (end == null) {
            // start .. *
            return date.isAfter(start) || (!strictly && date.isEqual(start));
        } else {
            // start .. end
            return date.isAfter(start) && date.isBefore(end)
                    || (!strictly && (date.isEqual(start) || date.isEqual(end)));
        }
    }

    /**
     * A valid interval has either a start and end, or both.
     */
    private void checkIntervalValidity() {
        assert !(start == null && end == null);
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
        DateRange other = (DateRange) obj;
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
        return strictly == other.strictly;
    }

    @Override
    public String toString() {
        checkIntervalValidity();

        if (end == null) {
            assert start != null;
            if (strictly) {
                return ">" + start;
            } else {
                return ">=" + start;
            }
        } else if (start == null) {
            if (strictly) {
                return "<" + end;
            } else {
                return "<=" + end;
            }
        } else {
            return start + " .. " + end;
        }
    }
}
