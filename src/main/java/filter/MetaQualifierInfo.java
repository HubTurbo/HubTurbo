package filter;

import filter.expression.Qualifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encapsulates information about the meta-qualifiers present in a filter expression,
 * which affects the semantics of other qualifiers.
 */
public class MetaQualifierInfo {

	private Optional<String> in = Optional.empty();
	private Optional<Boolean> ascending = Optional.empty();

	public MetaQualifierInfo(List<Qualifier> qualifiers) {

		this.in = processInQualifier(qualifiers);
		this.ascending = processOrderQualifier(qualifiers);
	}

	private Optional<Boolean> processOrderQualifier(List<Qualifier> qualifiers) {
		List<Qualifier> orderQualifiers = qualifiers.stream()
			.filter(q -> q.getName().equals("order"))
			.collect(Collectors.toList());

		if (orderQualifiers.isEmpty()) {
			return Optional.empty();
		} else if (orderQualifiers.size() > 1) {
			throw new ParseException("More than one meta-qualifier: order");
		} else {
			assert orderQualifiers.get(0).getContent().isPresent();
			if ("descending".startsWith(orderQualifiers.get(0).getContent().get())) {
				return Optional.of(false);
			} else {
				// Default to ascending if content is invalid
				return Optional.of(true);
			}
		}
	}

	private Optional<String> processInQualifier(List<Qualifier> qualifiers) {
		List<Qualifier> inQualifiers = qualifiers.stream()
			.filter(q -> q.getName().equals("in"))
			.collect(Collectors.toList());
		
		if (inQualifiers.isEmpty()) {
			return Optional.empty();
		} else if (inQualifiers.size() > 1) {
			throw new ParseException("More than one meta-qualifier: in");
		} else {
			return inQualifiers.get(0).getContent();
		}
	}

	public Optional<Boolean> isOrderAscending() {
		return ascending;
	}

	public Optional<String> getIn() {
		return in;
	}
}