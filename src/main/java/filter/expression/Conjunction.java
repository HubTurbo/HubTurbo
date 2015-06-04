package filter.expression;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Conjunction implements FilterExpression {
	public FilterExpression left;
	public FilterExpression right;

	public Conjunction(FilterExpression left, FilterExpression right) {
		this.left = left;
		this.right = right;
	}

	/**
     * This method is used to serialise qualifiers. Thus whatever form returned
     * should be syntactically valid.
     * Since AND has the highest precedence, the parentheses here aren't needed.
     */
	@Override
	public String toString() {
		return left + " " + right;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Conjunction that = (Conjunction) o;

		if (left != null ? !left.equals(that.left) : that.left != null) return false;
		if (right != null ? !right.equals(that.right) : that.right != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = left != null ? left.hashCode() : 0;
		result = 31 * result + (right != null ? right.hashCode() : 0);
		return result;
	}

	@Override
	public boolean isSatisfiedBy(IModel model, TurboIssue issue, MetaQualifierInfo info) {
		return left.isSatisfiedBy(model, issue, info) && right.isSatisfiedBy(model, issue, info);
	}
	
	private boolean containsDuplicateQualifierNames() {
		List<String> nonLabelQualifierNames = getQualifierNames().stream()
			.filter(pn -> !pn.equals("label"))
			.collect(Collectors.toList());
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
	public void applyTo(TurboIssue issue, IModel model) throws QualifierApplicationException {
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
