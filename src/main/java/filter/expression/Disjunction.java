package filter.expression;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Disjunction implements FilterExpression {

    private final FilterExpression left;
    private final FilterExpression right;

    public Disjunction(FilterExpression left, FilterExpression right) {
        this.left = left;
        this.right = right;
    }

    /**
     * This method is used to serialise qualifiers. Thus whatever form returned
     * should be syntactically valid.
     * Since OR has lower precedence than AND, the parentheses are needed to
     * disambiguate expressions like: (a OR b) AND c
     */
    @Override
    public String toString() {
        return "(" + left + " OR " + right + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disjunction that = (Disjunction) o;
        return !(left != null ? !left.equals(that.left) : that.left != null) &&
                !(right != null ? !right.equals(that.right) : that.right != null);
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public boolean isSatisfiedBy(IModel model, TurboIssue issue, MetaQualifierInfo info) {
        return left.isSatisfiedBy(model, issue, info)
                || right.isSatisfiedBy(model, issue, info);
    }

    @Override
    public boolean canBeAppliedToIssue() {
        return false;
    }

    @Override
    public void applyTo(TurboIssue issue, IModel model) throws QualifierApplicationException {
        assert false;
    }

    @Override
    public List<QualifierType> getQualifierTypes() {
        ArrayList<QualifierType> list = new ArrayList<>();
        list.addAll(left.getQualifierTypes());
        list.addAll(right.getQualifierTypes());
        return list;
    }

    @Override
    public FilterExpression filter(Predicate<Qualifier> pred) {
        FilterExpression left = this.left.filter(pred);
        FilterExpression right = this.right.filter(pred);
        if (left.isEmpty()) {
            return right;
        } else if (right.isEmpty()) {
            return left;
        } else {
            return new Disjunction(left, right);
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

    @Override
    public FilterExpression map(Function<Qualifier, Qualifier> func) {
        FilterExpression left = this.left.map(func);
        FilterExpression right = this.right.map(func);
        if (left.isEmpty()) {
            return right;
        } else if (right.isEmpty()) {
            return left;
        } else {
            return new Disjunction(left, right);
        }
    }
    @Override
    public boolean isEmpty() {
        return left.isEmpty() && right.isEmpty();
    }
}
