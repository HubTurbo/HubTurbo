package filter;

import java.util.ArrayList;

public class Parser {

	public static void main(String[] args) {
//		System.out.println(Parser.parse("e(f) (a(b) or c(d))"));
//		System.out.println(Parser.parse("(title(one) or parent(issue)) ~milestone(0.1)"));
	}
	
	private Parser(ArrayList<Token> input) {
		this.input = input;
	}
	public static Expression parse(String input) {
		if (input == null || input.isEmpty()) return null;
		return new Parser(new Lexer(input).lex()).parseExpression(0);
	}
		
	private ArrayList<Token> input;
	private int position = 0;
	
	public Token consume(TokenType type) {
		if (input.get(position).getType() == type) {
			return input.get(position++);
		} else {
			throw new RuntimeException("Invalid token " + input.get(position) + " where " + type + " expected");
		}
	}
	
	public Token consume() {
		return input.get(position++);
	}

	public Token lookAhead() {
		return input.get(position);
	}
		
	private Expression parseExpression(int precedence) {
		Token token = consume();
		assert token.getType() != TokenType.EOF;
		
		// Prefix
		
		Expression left;
		
		switch (token.getType()) {
		case LBRACKET:
			left = parseGroup(token);
			break;
		case NEGATE:
			left = parseNegation(token);
			break;
		case SYMBOL:
			left = parsePredicate(token);
			break;
		default:
			throw new RuntimeException("Invalid prefix token " + token);
		}
		
		token = lookAhead();
		if (token.getType() == TokenType.EOF) return left;

		// Infix
		
		while (precedence < getInfixPrecedence()) {
			switch (token.getType()) {
			case AND:
				consume();
				left = parseConjunction(left, token);
				break;
			case OR:
				consume();
				left = parseDisjunction(left, token);
				break;
			case SYMBOL:
			case NEGATE:
			case LBRACKET:
				// Implicit conjunction
				// Every token that could appear in a prefix position will trigger this path
				left = parseConjunction(left, token);
				break;
			default:
				throw new RuntimeException("Invalid infix token " + token);
			}
		}
		
		return left;
	}

	private int getInfixPrecedence() {
		switch (lookAhead().getType()) {
		case AND:
		case SYMBOL:
		case LBRACKET:
			// Implicit conjunction
			return Precedence.CONJUNCTION;
		case OR:
			return Precedence.DISJUNCTION;
		case NEGATE:
			return Precedence.PREFIX;
		default:
			return 0;
		}
	}

	private Expression parsePredicate(Token token) {
		String name = token.getValue();
		consume(TokenType.LBRACKET);
		String content = consume().getValue();
		consume(TokenType.RBRACKET);
		return new Predicate(name, content);
	}
	
	private Expression parseGroup(Token token) {
		Expression expr = parseExpression(0);
		consume(TokenType.RBRACKET);
		return expr;
	}
	
	private Expression parseDisjunction(Expression left, Token token) {
		return new Disjunction(left, parseExpression(Precedence.DISJUNCTION));
	}

	private Expression parseConjunction(Expression left, Token token) {
		return new Conjunction(left, parseExpression(Precedence.CONJUNCTION));
	}
	
	private Expression parseNegation(Token token) {
		return new Negation(parseExpression(Precedence.PREFIX));
	}
}
