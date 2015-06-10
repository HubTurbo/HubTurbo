package filter.expression;

public class SortKey {
	public final String key;
	public final boolean inverted;

	public SortKey(String key, boolean inverted) {
		this.key = key;
		this.inverted = inverted;
	}

	@Override
	public String toString() {
		return (inverted ? "~" : "") + key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SortKey sortKey = (SortKey) o;

		if (inverted != sortKey.inverted) return false;
		if (key != null ? !key.equals(sortKey.key) : sortKey.key != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = key != null ? key.hashCode() : 0;
		result = 31 * result + (inverted ? 1 : 0);
		return result;
	}
}
