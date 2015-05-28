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

	public MetaQualifierInfo(List<Qualifier> qualifiers) {

		this.in = processInQualifier(qualifiers);
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

	public Optional<String> getIn() {
		return in;
	}
}