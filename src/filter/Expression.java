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
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Predicate other = (Predicate) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
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
}
