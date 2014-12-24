package filter.expression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import model.Model;
import model.TurboIssue;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;

public class Conjunction implements FilterExpression {
	private FilterExpression left;
	private FilterExpression right;

	public Conjunction(FilterExpression left, FilterExpression right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return "(" + left + " " + right + ")";
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

	public boolean isSatisfiedBy(TurboIssue issue, MetaQualifierInfo info) {
		return left.isSatisfiedBy(issue, info) && right.isSatisfiedBy(issue, info);
	}
	
	private boolean containsDuplicateQualifierNames() {
		List<String> nonLabelQualifierNames = getQualifierNames().stream().filter(pn -> !pn.equals("label")).collect(Collectors.toList());
		HashSet<String> noDuplicates = new HashSet<>(nonLabelQualifierNames);
		return noDuplicates.size() != nonLabelQualifierNames.size();
	}
	
	@Override
	public boolean canBeAppliedToIssue() {
		return !containsDuplicateQualifierNames()
				&& left.canBeAppliedToIssue()
				&& right.canBeAppliedToIssue();
	}

	@Override
	public void applyTo(TurboIssue issue, Model model) throws QualifierApplicationException {
		left.applyTo(issue, model);
		right.applyTo(issue, model);
	}

	@Override
	public List<String> getQualifierNames() {
		ArrayList<String> list = new ArrayList<>();
		list.addAll(left.getQualifierNames());
		list.addAll(right.getQualifierNames());
		return list;
	}

	@Override
	public FilterExpression filter(Predicate<Qualifier> pred) {
		FilterExpression left = this.left.filter(pred);
		FilterExpression right = this.right.filter(pred);
		if (left == Qualifier.EMPTY) {
			return right;
		} else if (right == Qualifier.EMPTY) {
			return left;
		} else {
			return new Conjunction(left, right);
		}
	}

	@Override
	public List<Qualifier> find(Predicate<Qualifier> pred) {
		List<Qualifier> left = this.left.find(pred);
		List<Qualifier> right = this.right.find(pred);
		ArrayList<Qualifier> result = new ArrayList<>();
		result.addAll(left);
		result.addAll(right);
		return result;
	}
}