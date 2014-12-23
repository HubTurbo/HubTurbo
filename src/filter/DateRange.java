package filter;

import java.time.LocalDate;

public class DateRange {
	private final LocalDate start;
	private final LocalDate end;
	
	public DateRange(LocalDate start, LocalDate end) {
		this.start = start;
		this.end = end;
	}
	
	public boolean encloses(LocalDate date) {
		if (start == null) {
			// * .. end
			return date.isBefore(start);
		} else if (end == null) {
			// start .. *
			return date.isAfter(start);
		} else {
			// start .. end
			return date.isAfter(start) && date.isBefore(end);
		}
	}
	
	public boolean enclosesStrict(LocalDate date) {
		if (start == null) {
			// * .. end
			return date.isBefore(start) && !date.isEqual(start);
		} else if (end == null) {
			// start .. *
			return date.isAfter(start) && !date.isEqual(end);
		} else {
			// start .. end
			return date.isAfter(start) && date.isBefore(end)
					&& !date.isEqual(start) && !date.isEqual(end);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		return true;
	}
}
