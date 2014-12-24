package filter.expression;

import java.util.List;

import filter.QualifierApplicationException;
import model.Model;
import model.TurboIssue;

public class Negation implements FilterExpression {
	private FilterExpression expr;

	public Negation(FilterExpression expr) {
		this.expr = expr;
	}

	@Override
	public String toString() {
		return "NOT" + expr;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Negation other = (Negation) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}
	
	public boolean isSatisfiedBy(TurboIssue issue, Model model) {
		return !expr.isSatisfiedBy(issue, model);
	}

	@Override
	public boolean canBeAppliedToIssue() {
		return false;
	}

	@Override
	public void applyTo(TurboIssue issue, Model model) throws QualifierApplicationException {
		assert false;
	}
	
	@Override
	public List<String> getQualifierNames() {
		return expr.getQualifierNames();
	}
}