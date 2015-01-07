package filter.expression;

public class NumberRange {
	private final Integer start;
	private final Integer end;
	private final boolean strictly;
	
	public NumberRange(Integer start, Integer end) {
		this.start = start;
		this.end = end;
		this.strictly = false;
	}
	
	public NumberRange(Integer start, Integer end, boolean strict) {
		this.start = start;
		this.end = end;
		this.strictly = strict;
	}

	public boolean encloses(int number) {
		if (start == null) {
			// * .. end
			return strictly ? number < end : number <= end;
		} else if (end == null) {
			// start .. *
			return strictly ? number > end : number >= end;
		} else {
			// start .. end
			return strictly
					? number > start && number < end
					: number >= start && number <= end;
		}
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
		return "NumberRange [start=" + start + ", end=" + end + ", strictly=" + strictly + "]";
	}
}
