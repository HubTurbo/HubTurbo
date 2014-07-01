package filter;

import model.TurboIssue;

public interface Expression {
	public boolean isSatisfiedBy(TurboIssue issue);
}
