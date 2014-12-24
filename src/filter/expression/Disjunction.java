package filter.expression;

import java.util.ArrayList;
import java.util.List;

import filter.PredicateApplicationException;
import model.Model;
import model.TurboIssue;

public class Disjunction implements FilterExpression {
	private FilterExpression left;
	private FilterExpression right;

	public Disjunction(FilterExpression left, FilterExpression right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return "(" + left + " OR " + right + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Disjunction other = (Disjunction) obj;
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
		return left.isSatisfiedBy(issue, model) || right.isSatisfiedBy(issue, model);
	}

	@Override
	public boolean canBeAppliedToIssue() {
		return false;
	}

	@Override
	public void applyTo(TurboIssue issue, Model model) throws PredicateApplicationException {
		assert false;
	}
	
	@Override
	public List<String> getPredicateNames() {
		ArrayList<String> list = new ArrayList<>();
		list.addAll(left.getPredicateNames());
		list.addAll(right.getPredicateNames());
		return list;
	}
}