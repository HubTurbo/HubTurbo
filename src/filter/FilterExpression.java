package filter;

import model.TurboIssue;

public interface FilterExpression {
	public boolean isSatisfiedBy(TurboIssue issue);
}
