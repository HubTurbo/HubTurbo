package filter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import filter.expression.Conjunction;
import filter.expression.DateRange;
import filter.expression.Disjunction;
import filter.expression.FilterExpression;
import filter.expression.Negation;
import filter.expression.NumberRange;
import filter.expression.Qualifier;
import filter.lexer.Lexer;
import filter.lexer.Token;
import filter.lexer.TokenType;

public class Parser {

	public static void main(String[] args) {
		String input = "\"a b\"";
		input = "id:1";
		FilterExpression p = Parser.parse(input);
//		ArrayList<Token> p = new Lexer(input).lex();
		System.out.println(p);
	}
	
	private Parser(ArrayList<Token> input) {
		this.input = input;
	}
	public static FilterExpression parse(String input) {
		if (input == null) return null;
		else if (input.isEmpty()) return Qualifier.EMPTY;
		return new Parser(new Lexer(input).lex()).parseExpression(0);
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
		return input.get(Math.min(position, input.size()-1));
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
		case QUOTE:
			left = parseQuotedKeywords(token);
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
			case QUALIFIER:
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

	private FilterExpression parseQuotedKeywords(Token token) {
		FilterExpression result = parseKeywords();
		consume(TokenType.QUOTE);
		return result;
	}

	private FilterExpression parseKeywords() {
		return parseKeywords("keyword");
	}
	
	private FilterExpression parseKeywords(String qualifierName) {
		StringBuilder sb = new StringBuilder();
		while (!isQuoteToken(lookAhead())) {
			sb.append(consume().getValue()).append(" ");
		}
		return new Qualifier(qualifierName, sb.toString().trim());
	}

	private FilterExpression parseKeyword(Token token) {
		return new Qualifier("keyword", token.getValue());
	}

	private int getInfixPrecedence(Token token) {
		switch (token.getType()) {
		case AND:
		case QUALIFIER:
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
	
	private static final Pattern datePattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
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
		String qualifierName = token.getValue();

		// Strip the : at the end, then trim
		qualifierName = qualifierName.substring(0, qualifierName.length()-1).trim();

		return parseQualifierContent(qualifierName, false);
	}

	private FilterExpression parseQualifierContent(String qualifierName, boolean allowMultipleKeywords) {
		if (isRangeOperatorToken(lookAhead())) {
			// < > <= >= [number range | date range]
			return parseRangeOperator(qualifierName, lookAhead());
		}
		else if (isNumberOrDateToken(lookAhead())) {
			// [date] | [date] .. [date]
			return parseDateOrDateRange(qualifierName);
		}
		else if (isQuoteToken(lookAhead())) {//!allowMultipleKeywords &&
			// " [content] "
			consume(TokenType.QUOTE);
			FilterExpression result = parseQualifierContent(qualifierName, true);
			consume(TokenType.QUOTE);
			return result;
		}
		else if (isKeywordToken(lookAhead())) {
			// Keyword(s)
			if (allowMultipleKeywords) {
				return parseKeywords(qualifierName);
			} else {
				return new Qualifier(qualifierName, consume().getValue());
			}
		} else {
			throw new ParseException(String.format("Invalid content for qualifier %s: %s", qualifierName, lookAhead()));
		}
	}

	private boolean isKeywordToken(Token token) {
		return token.getType() == TokenType.SYMBOL;
	}

	private boolean isQuoteToken(Token token) {
		return token.getType() == TokenType.QUOTE;
	}

	private boolean isRangeOperatorToken(Token token) {
		switch (token.getType()) {
		case GT:
		case LT:
		case GTE:
		case LTE:
			return true;
		default:
			return false;
		}
	}
	
	private boolean isNumberOrDateToken(Token token) {
		switch (token.getType()) {
		case DATE:
			return true;
		case SYMBOL:
			try {
				Integer.parseInt(token.getValue());
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		default:
			return false;
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
					return new Qualifier(name, new DateRange(leftDate.get(), rightDate.get()));
				} else {
					assert false : "Possible problem with lexer processing date";
				}
			} else if (right.getType() == TokenType.STAR) {
				return new Qualifier(name, new DateRange(leftDate.get(), null));
			} else {
				throw new ParseException("Right operand of .. must be a date or *");
			}
		}
		else {
			// Just one date, not a range
			return new Qualifier(name, leftDate.get());
		}
		assert false : "Should never reach here";
		return null;
	}

	private FilterExpression parseRangeOperator(String name, Token token) {
		String operator = token.getValue();
		
		consume(token.getType());
		if (isNumberOrDateToken(lookAhead())) {
			Token contentToken = consume();
			Optional<LocalDate> date = parseDate(contentToken);
			if (date.isPresent()) {
				// Date
				switch (token.getType()) {
				case GT:
					return new Qualifier(name, new DateRange(date.get(), null, true));
				case GTE:
					return new Qualifier(name, new DateRange(date.get(), null));
				case LT:
					return new Qualifier(name, new DateRange(null, date.get(), true));
				case LTE:
					return new Qualifier(name, new DateRange(null, date.get()));
				default:
					assert false : "Should not happen";
				}
				assert false : "Should not happen";
				return null;
			} else {
				// May be a number or something else
				try {
					Integer.parseInt(contentToken.getValue());
				} catch (NumberFormatException e) {
					// Exit with an exception if it's not a number
					throw new ParseException(String.format("Operator %s can only be applied to number or date", operator));
				}
				
				// Must be a number
                int num = Integer.parseInt(contentToken.getValue());
                		
				switch (token.getType()) {
				case GT:
					return new Qualifier(name, new NumberRange(num, null, true));
				case GTE:
					return new Qualifier(name, new NumberRange(num, null));
				case LT:
					return new Qualifier(name, new NumberRange(null, num, true));
				case LTE:
					return new Qualifier(name, new NumberRange(null, num));
				default:
					assert false : "Should not happen";
				}
				assert false : "Should not happen";
				return null;
			}
		} else {
			throw new ParseException(String.format("Operator %s can only be applied to number or date, got %s", operator, lookAhead()));
		}
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
