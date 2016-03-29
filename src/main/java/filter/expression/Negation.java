package filter.expression;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Negation implements FilterExpression {

    private final FilterExpression expr;

    public Negation(FilterExpression expr) {
        this.expr = expr;
    }

    /**
     * This method is used to serialise qualifiers. Thus whatever form returned
     * should be syntactically valid.
     */
    @Override
    public String toString() {
        return "NOT " + expr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Negation negation = (Negation) o;
        return !(expr != null ? !expr.equals(negation.expr) : negation.expr != null);
    }

    @Override
    public int hashCode() {
        return expr != null ? expr.hashCode() : 0;
    }

    @Override
    public boolean isSatisfiedBy(IModel model, TurboIssue issue, MetaQualifierInfo info) {
        return !expr.isSatisfiedBy(model, issue, info);
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
    public List<String> getWarnings(IModel model, TurboIssue issue) {
        return expr.getWarnings(model, issue);
    }

    @Override
    public List<QualifierType> getQualifierTypes() {
        return expr.getQualifierTypes();
    }

    @Override
    public FilterExpression filter(Predicate<Qualifier> pred) {
        FilterExpression expr = this.expr.filter(pred);
        if (expr.isEmpty()) {
            return Qualifier.EMPTY;
        } else {
            return new Negation(expr);
        }
    }

    @Override
    public List<Qualifier> find(Predicate<Qualifier> pred) {
        List<Qualifier> expr = this.expr.find(pred);
        ArrayList<Qualifier> result = new ArrayList<>();
        result.addAll(expr);
        return result;
    }

    @Override
    public FilterExpression map(Function<Qualifier, Qualifier> func) {
        FilterExpression expr = this.expr.map(func);
        if (expr.isEmpty()) {
            return Qualifier.EMPTY;
        } else {
            return new Negation(expr);
        }
    }

    @Override
    public boolean isEmpty() {
        return expr.isEmpty();
    }
}
