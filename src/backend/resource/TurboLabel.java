package backend.resource;


import backend.resource.serialization.SerializableLabel;
import org.eclipse.egit.github.core.Label;

import java.util.Optional;

@SuppressWarnings("unused")
public class TurboLabel {

	public static final String EXCLUSIVE_DELIMITER = ".";
	public static final String NONEXCLUSIVE_DELIMITER = "-";

	private void ______SERIALIZED_FIELDS______() {
	}

	private final boolean exclusive;
	private final String actualName;
	private final String colour;

	private void ______TRANSIENT_FIELDS______() {
	}

	private void ______CONSTRUCTORS______() {
	}

	public TurboLabel(String name) {
		this.actualName = name;
		this.colour = "#ffffff";
		this.exclusive = true;
	}

	public TurboLabel(String group, String name) {
		this.exclusive = true;
		this.actualName = join(group, name);
		this.colour = "#ffffff";
	}

	public TurboLabel(Label label) {
		this.exclusive = true;
		this.actualName = label.getName();
		this.colour = label.getColor();
	}

	public TurboLabel(SerializableLabel label) {
		this.exclusive = label.exclusive;
		this.actualName = label.actualName;
		this.colour = label.colour;
	}

	private void ______METHODS______() {
	}

	private String getDelimiter() {
		return exclusive ? EXCLUSIVE_DELIMITER : NONEXCLUSIVE_DELIMITER;
	}

	private boolean isDelimited(String labelName) {
		return labelName.contains(EXCLUSIVE_DELIMITER) || labelName.contains(NONEXCLUSIVE_DELIMITER);
	}

	private String join(String group, String name) {
		return group + getDelimiter() + name;
	}

	public Optional<String> getGroup() {
		if (isDelimited(actualName)) {
			return Optional.of(actualName.split(getDelimiter())[0]);
		} else {
			return Optional.empty();
		}
	}

	public String getName() {
		if (isDelimited(actualName)) {
			return actualName.split(getDelimiter())[1];
		} else {
			return actualName;
		}
	}

	private void ______BOILERPLATE______() {
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public String getColour() {
		return colour;
	}

	public String getActualName() {
		return actualName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TurboLabel that = (TurboLabel) o;

		if (exclusive != that.exclusive) return false;
		if (actualName != null ? !actualName.equals(that.actualName) : that.actualName != null) return false;
		if (colour != null ? !colour.equals(that.colour) : that.colour != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (exclusive ? 1 : 0);
		result = 31 * result + (actualName != null ? actualName.hashCode() : 0);
		result = 31 * result + (colour != null ? colour.hashCode() : 0);
		return result;
	}
}
