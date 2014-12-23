package filter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import filter.lexer.Lexer;
import filter.lexer.Token;
import filter.lexer.TokenType;

public class Parser {

	public static void main(String[] args) {
		FilterExpression p = Parser.parse("what hello");
//		ArrayList<Token> p = new Lexer("in:title hello").lex();
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
		case QUALIFIER:
			left = parseQualifier(token);
			break;
		case SYMBOL:
			left = parseKeyword(token);
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

	private FilterExpression parseKeyword(Token token) {
		return new Predicate("keyword", token.getValue());
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
	
	private static final Pattern datePattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
	private static Optional<LocalDate> parseDate(Token token) {
		Matcher matcher = datePattern.matcher(token.getValue());
		if (matcher.find()) {
			int year = Integer.parseInt(matcher.group(1));
			int month = Integer.parseInt(matcher.group(2));
			int day = Integer.parseInt(matcher.group(3));
			return Optional.of(LocalDate.of(year, month, day));
		} else {
			return Optional.empty();
		}
	}

	private FilterExpression parseQualifier(Token token) {
		String name = token.getValue();
		name = name.substring(0, name.length()-1);
		
		TokenType type = lookAhead().getType();

		if (type == TokenType.GT || type == TokenType.LT || type == TokenType.LTE || type == TokenType.GTE) {
			// < > <= >= [number | date range]
			return parseRangeOperator(name, lookAhead());
		} else if (isNumberOrDateToken(lookAhead())) {
			// [date] | [date] .. [date]
			return parseDateOrDateRange(name);
		} else {
			// Keyword
			Token t = consume();
			return new Predicate(name, t.getValue());
		}
	}
	
	private FilterExpression parseDateOrDateRange(String name) {
		Token left = consume();
		Optional<LocalDate> leftDate = parseDate(left);
		if (!leftDate.isPresent()) {
			throw new ParseException("Left operand of .. must be a date");
		}
		
		if (lookAhead().getType() == TokenType.DOTDOT) {
			// [date] .. [date]
			consume(TokenType.DOTDOT);
			Token right = consume();
			
			if (isNumberOrDateToken(right)) {
				Optional<LocalDate> rightDate = parseDate(right);
				if (rightDate.isPresent()) {
					return new Predicate(name, new DateRange(leftDate.get(), rightDate.get()));
				} else {
					assert false : "Possible problem with lexer processing date";
				}
			} else if (right.getType() == TokenType.STAR) {
				return new Predicate(name, new DateRange(leftDate.get(), null));
			} else {
				throw new ParseException("Right operand of .. must be a date or *");
			}
		}
		else {
			// Just one date, not a range
			return new Predicate(name, leftDate.get());
		}
		assert false : "Should never reach here";
		return null;
	}

	private FilterExpression parseRangeOperator(String name, Token token) {
		String operator = token.getValue();
		
		consume(token.getType());
		if (isNumberOrDateToken(lookAhead())) {
			Token dateToken = consume();
			Optional<LocalDate> date = parseDate(dateToken);
			if (date.isPresent()) {
				// Date
				return new Predicate(name, date.get());
			} else {
				// Number
				try {
//					int num = Integer.parseInt(info.getValue());
//					return new Predicate(name, num);
					throw new ParseException("Not yet implemented");
				} catch (NumberFormatException e) {
					throw new ParseException(String.format("Operator %s can only be applied to number or date", operator));
				}
			}
		} else {
			throw new ParseException(String.format("Operator %s can only be applied to number or date", operator));
		}
	}
	
	private boolean isNumberOrDateToken(Token token) {
		return token.getType() == TokenType.NUMBER || token.getType() == TokenType.DATE;
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
