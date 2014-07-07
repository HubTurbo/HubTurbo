package filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import model.Model;
import model.TurboIssue;

public class Conjunction implements FilterExpression {
	private FilterExpression left;
	private FilterExpression right;

	public Conjunction(FilterExpression left, FilterExpression right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return "(" + left + " and " + right + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Conjunction other = (Conjunction) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	public boolean isSatisfiedBy(TurboIssue issue, Model model) {
		return left.isSatisfiedBy(issue, model) && right.isSatisfiedBy(issue, model);
	}
	
	private boolean containsDuplicatePredicateNames() {
		List<String> predicateNames = getPredicateNames();
		HashSet<String> noDuplicates = new HashSet<>(predicateNames);
		return noDuplicates.size() != predicateNames.size();
	}
	
	@Override
	public boolean canBeAppliedToIssue() {
		return !containsDuplicatePredicateNames() && left.canBeAppliedToIssue() && right.canBeAppliedToIssue();
	}

	@Override
	public void applyTo(TurboIssue issue, Model model) throws PredicateApplicationException {
		left.applyTo(issue, model);
		right.applyTo(issue, model);
	}

	@Override
	public List<String> getPredicateNames() {
		ArrayList<String> list = new ArrayList<>();
		list.addAll(left.getPredicateNames());
		list.addAll(right.getPredicateNames());
		return list;
	}
}