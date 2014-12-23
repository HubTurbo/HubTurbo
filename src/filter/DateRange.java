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
}
