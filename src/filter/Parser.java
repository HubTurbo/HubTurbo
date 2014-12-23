package filter;

import java.util.ArrayList;
import java.util.List;

import filter.lexer.Lexer;
import filter.lexer.Token;
import filter.lexer.TokenType;

public class Parser {

	public static void main(String[] args) {
		FilterExpression p = Parser.parse("~");
		System.out.println(p);
	}
	
	private Parser(ArrayList<Token> input) {
		this.input = input;
	}
	public static FilterExpression parse(String input) {
		if (input == null) return null;
		else if (input.isEmpty()) return Predicate.EMPTY;
		return new Parser(new Lexer(input).lex()).parseExpression(0);
	}
	public static boolean isListOfSymbols(String input) {
		assert input != null;
		if (input.isEmpty()) return false;
		
		List<Token> tokens = new Lexer(input).lex();
		
		// Last token should be an EOF; get rid of it
		assert tokens.get(tokens.size()-1).getType() == TokenType.EOF;
		tokens.remove(tokens.size()-1);
		
		for (int i=0; i<tokens.size(); i++) {
			if (tokens.get(i).getType() != TokenType.SYMBOL) {
				return false;
			}
		}
		return true;
	}

	private ArrayList<Token> input;
	private int position = 0;
	
	private Token consume(TokenType type) {
		if (input.get(position).getType() == type) {
			return input.get(position++);
		} else {
			throw new ParseException("Invalid token " + input.get(position) + " where " + type + " expected");
		}
	}
	
	private Token consume() {
		return input.get(position++);
	}

	private Token lookAhead() {
		return input.get(position);
	}
		
	private FilterExpression parseExpression(int precedence) {
		Token token = consume();
		if (token.getType() == TokenType.EOF) {
			throw new ParseException("Unexpected EOF while parsing at " + position);
		}
		
		// Prefix
		
		FilterExpression left;
		
		switch (token.getType()) {
		case LBRACKET:
			left = parseGroup(token);
			break;
		case NOT:
			left = parseNegation(token);
			break;
		case SYMBOL:
			left = parsePredicate(token);
			break;
		default:
			throw new ParseException("Invalid prefix token " + token);
		}
		
		if (lookAhead().getType() == TokenType.EOF) return left;

		// Infix
		
		while (precedence < getInfixPrecedence(token = lookAhead())) {
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
			case NOT:
			case LBRACKET:
				// Implicit conjunction
				// Every token that could appear in a prefix position will trigger this path
				left = parseConjunction(left, token);
				break;
			default:
				throw new ParseException("Invalid infix token " + token);
			}
		}
		
		return left;
	}

	private int getInfixPrecedence(Token token) {
		switch (token.getType()) {
		case AND:
		case SYMBOL: // Implicit conjunction
		case LBRACKET:
			return Precedence.CONJUNCTION;
		case OR:
			return Precedence.DISJUNCTION;
		case NOT:
			return Precedence.PREFIX;
		default:
			return 0;
		}
	}

	private FilterExpression parsePredicate(Token token) {
		String name = token.getValue();
		String content = "";
		
		// Predicates look like the following:

		// symbol(content with spaces)
		// symbol: contentWithoutSpaces
		
		if (lookAhead().getType() == TokenType.COLON) {
			consume(TokenType.COLON);
			
			// Consume one symbol after the colon
			content = consume(TokenType.SYMBOL).getValue();
		}
		else {
			consume(TokenType.LBRACKET);
			
			// Consume any number of space-delimited symbols
			while (lookAhead().getType() == TokenType.SYMBOL) {
				content += consume(TokenType.SYMBOL).getValue() + " ";
			}
			content = content.trim();
			
			consume(TokenType.RBRACKET);
		}
		return new Predicate(name, content);
	}
	
	private FilterExpression parseGroup(Token token) {
		FilterExpression expr = parseExpression(0);
		consume(TokenType.RBRACKET);
		return expr;
	}
	
	private FilterExpression parseDisjunction(FilterExpression left, Token token) {
		return new Disjunction(left, parseExpression(Precedence.DISJUNCTION));
	}

	private FilterExpression parseConjunction(FilterExpression left, Token token) {
		return new Conjunction(left, parseExpression(Precedence.CONJUNCTION));
	}
	
	private FilterExpression parseNegation(Token token) {
		return new Negation(parseExpression(Precedence.PREFIX));
	}
}
