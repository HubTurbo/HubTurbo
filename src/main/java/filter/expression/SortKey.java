package filter.expression;

public class SortKey {
	public final String key;
	public final boolean inverted;

	public SortKey(String key, boolean inverted) {
		this.key = key;
		this.inverted = inverted;
	}
}
