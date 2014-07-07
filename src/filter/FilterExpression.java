package filter;

import java.util.List;

import model.Model;
import model.TurboIssue;

public interface FilterExpression {
	
	// A predicate that determines if an issue satisfies this filter
	// expression. If it satisfies, it is shown in the issue panel.
	
	public boolean isSatisfiedBy(TurboIssue issue, Model model);
	
	// Filter expressions may only be applied if they contain no ambiguity
	// => they must contain only conjunctions or predicates. Disjunctions
	// and negations can't be interpreted in order to be applied.
	
	public boolean canBeAppliedToIssue();

	// Applies the traits that this filter expression expresses to
	// an issue. This should be invoked for disjunctions and negations
	// (i.e. call the above method to check first).
	
	public void applyTo(TurboIssue issue, Model model) throws PredicateApplicationException;
	
	// Walks the tree of predicates to get all the names that appear.
	
	public List<String> getPredicateNames();
}
