package filter.expression;

import java.util.List;
import java.util.function.Predicate;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;

public interface FilterExpression {

    // Determines if an issue satisfies this filter expression.
    // If so, it is shown in the issue panel.

    boolean isSatisfiedBy(IModel model, TurboIssue issue, MetaQualifierInfo info, String currentUser);

    // Filter expressions may only be applied if they contain no ambiguity
    // => they must contain only qualifiers or conjunctions thereof. Disjunctions
    // and negations can't be interpreted in order to be applied.

    boolean canBeAppliedToIssue();

    // Applies the traits that this filter expression expresses to
    // an issue. This should be invoked for disjunctions and negations
    // (i.e. call the above method to check first).

    void applyTo(TurboIssue issue, IModel model) throws QualifierApplicationException;

    // Walks the syntax tree to get all the qualifier names that appear.

    List<String> getQualifierNames();

    // A given FilterExpression has a string form that, when parsed, is
    // guaranteed to produce an equivalent FilterExpression. It can thus
    // be used to serialise FilterExpressions.

    String toString();

    // Filters the syntax tree with a given predicate, returning a new FilterExpression.
    // New FilterExpressions are copied shallowly.

    FilterExpression filter(Predicate<Qualifier> pred);

    // Gets references to all qualifiers in a syntax tree matching a given predicate.

    List<Qualifier> find(Predicate<Qualifier> pred);
}
