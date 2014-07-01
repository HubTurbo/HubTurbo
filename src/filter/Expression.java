package filter;

public interface Expression {
	
}

class Predicate implements Expression {
	private String name;
	private String content;
	public Predicate(String name, String content) {
		this.name = name;
		this.content = content;
	}
	@Override
	public String toString() {
		return name + "(" + content + ")";
	}
}

class Conjunction implements Expression {
	private Expression left;
	private Expression right;
	public Conjunction(Expression left, Expression right) {
		this.left = left;
		this.right = right;
	}
	@Override
	public String toString() {
		return "(" + left + " and " + right + ")";
	}
}

class Disjunction implements Expression {
	private Expression left;
	private Expression right;
	public Disjunction(Expression left, Expression right) {
		this.left = left;
		this.right = right;
	}
	@Override
	public String toString() {
		return "(" + left + " or " + right + ")";
	}
}

class Negation implements Expression {
	private Expression expr;
	public Negation(Expression expr) {
		this.expr = expr;
	}
	@Override
	public String toString() {
		return "~" + expr;
	}
}
