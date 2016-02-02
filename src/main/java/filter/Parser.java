package filter;

import filter.expression.*;
import filter.lexer.Lexer;
import filter.lexer.Token;
import filter.lexer.TokenType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Parser {

    private Parser(ArrayList<Token> input) {
        this.input = input;
    }
    public static FilterExpression parse(String input) {
        if (input == null || input.isEmpty()) return Qualifier.EMPTY;
        return new Parser(new Lexer(input).lex()).parseExpression(0);
    }

    private final ArrayList<Token> input;
    private int position = 0;
    private int sourcePosition = 0;

    private Token consume(TokenType type) {
        if (input.get(position).getType() == type) {
            return input.get(position++);
        } else {
            throw new ParseException("Invalid token " + input.get(position)
                + " where " + type + " expected");
        }
    }

    private Token consume() {
        Token token = input.get(position++);
        sourcePosition += token.getValue().length();
        return token;
    }

    private Token lookAhead() {
        return input.get(Math.min(position, input.size() - 1));
    }

    private FilterExpression parseExpression(int precedence) {
        Token token = consume();
        if (token.getType() == TokenType.EOF) {
            throw new ParseException("Unexpected EOF while parsing at "
                + sourcePosition + " (token " + position + ")");
        }

        // Prefix

        FilterExpression left;

        switch (token.getType()) {
        case LBRACKET:
            left = parseGroup();
            break;
        case NOT:
            left = parseNegation();
            break;
        case QUALIFIER:
            left = parseQualifier(token);
            break;
        case QUOTE:
            left = parseQuotedKeywords();
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
                left = parseConjunction(left);
                break;
            case OR:
                consume();
                left = parseDisjunction(left);
                break;
            case QUALIFIER:
            case SYMBOL:
            case NOT:
            case LBRACKET:
                // Implicit conjunction
                // Every token that could appear in a prefix position will trigger this path
                left = parseConjunction(left);
                break;
            default:
                throw new ParseException("Invalid infix token " + token);
            }
        }

        return left;
    }

    private FilterExpression parseQuotedKeywords() {
        FilterExpression result = parseQuotedContent(QualifierType.KEYWORD);
        consume(TokenType.QUOTE);
        return result;
    }

    private FilterExpression parseQuotedContent(QualifierType type) {
        StringBuilder sb = new StringBuilder();
        while (!isQuoteToken(lookAhead())) {
            sb.append(consume().getValue()).append(' ');
        }
        return new Qualifier(type, sb.toString().trim());
    }

    private FilterExpression parseKeyword(Token token) {
        return new Qualifier(QualifierType.KEYWORD, token.getValue());
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

    private Optional<Integer> parseNumber(Token token) {
        switch (token.getType()) {
        case SYMBOL:
            if (isNumberToken(token)) {
                return Optional.of(Integer.parseInt(token.getValue()));
            } else {
                return Optional.empty();
            }
        default:
            return Optional.empty();
        }
    }

    private FilterExpression parseQualifier(Token token) {
        String qualifierName = token.getValue();

        // Strip the : at the end, then trim
        qualifierName = qualifierName.substring(0, qualifierName.length() - 1).trim();

        QualifierType type = QualifierType.parse(qualifierName).orElse(QualifierType.FALSE);
        return parseSeparatedContent(() -> parseQualifierContent(type, false));
    }

    private FilterExpression parseSeparatedContent(Supplier<FilterExpression> contentParser) {
        FilterExpression expr = contentParser.get();
        while (lookAhead().getType() == TokenType.SEMICOLON) {
            consume(TokenType.SEMICOLON);
            expr = new Disjunction(expr, contentParser.get());
        }
        return expr;
    }

    private FilterExpression parseQualifierContent(QualifierType type, boolean allowMultipleKeywords) {
        if (type == QualifierType.SORT) {
            return parseSortKeys();
        } else if (isRangeOperatorToken(lookAhead())) {
            // < > <= >= [number range | date range]
            return parseRangeOperator(type, lookAhead());
        } else if (isDateToken(lookAhead())) {
            // [date] | [date] .. [date]
            return parseDateOrDateRange(type);
        } else if (isNumberToken(lookAhead())) {
            // [number] | [number] .. [number]
            return parseNumberOrNumberRange(type);
        } else if (isQuoteToken(lookAhead())) {
            // " [content] "
            consume(TokenType.QUOTE);
            FilterExpression result = parseQualifierContent(type, true);
            consume(TokenType.QUOTE);
            return result;
        } else if (isKeywordToken(lookAhead())) {
            if (allowMultipleKeywords) {
                // We're within quotes, with the ability to parse multiple consecutive keywords
                return parseQuotedContent(type);
            } else {
                // Everything else
                return new Qualifier(type, consume().getValue());
            }
        } else {
            throw new ParseException(String.format("Invalid content for qualifier %s: %s",
                type, lookAhead()));
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

    private boolean isNumberToken(Token token) {
        switch (token.getType()) {
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

    private boolean isDateToken(Token token) {
        switch (token.getType()) {
        case DATE:
            return true;
        default:
            return false;
        }
    }

    private boolean isNumberOrDateToken(Token token) {
        return isNumberToken(token) || isDateToken(token);
    }

    private FilterExpression parseNumberOrNumberRange(QualifierType type) {
        Token left = consume();

        Optional<Integer> leftDate = parseNumber(left);
        if (!leftDate.isPresent()) {
            throw new ParseException("Expected a number as the input to " + type);
        }

        if (lookAhead().getType() == TokenType.DOTDOT) {
            // [number] .. [number]
            consume(TokenType.DOTDOT);
            Token right = consume();

            if (isNumberToken(right)) {
                Optional<Integer> rightNumber = parseNumber(right);
                if (rightNumber.isPresent()) {
                    return new Qualifier(type, new NumberRange(leftDate.get(), rightNumber.get()));
                } else {
                    assert false : "Possible problem with lexer processing number";
                }
            } else if (right.getType() == TokenType.STAR) {
                return new Qualifier(type, new NumberRange(leftDate.get(), null));
            } else {
                throw new ParseException("Right operand of .. must be a number or *");
            }
        } else {
            // Just one number, not a range
            return new Qualifier(type, leftDate.get());
        }
        assert false : "Should never reach here";
        return null;
    }

    private FilterExpression parseDateOrDateRange(QualifierType type) {
        Token left = consume();
        Optional<LocalDate> leftDate = parseDate(left);
        if (!leftDate.isPresent()) {
            throw new ParseException("Expected a date as the input to " + type);
        }

        if (lookAhead().getType() == TokenType.DOTDOT) {
            // [date] .. [date]
            consume(TokenType.DOTDOT);
            Token right = consume();

            if (isDateToken(right)) {
                Optional<LocalDate> rightDate = parseDate(right);
                if (rightDate.isPresent()) {
                    return new Qualifier(type, new DateRange(leftDate.get(), rightDate.get()));
                } else {
                    assert false : "Possible problem with lexer processing date";
                }
            } else if (right.getType() == TokenType.STAR) {
                return new Qualifier(type, new DateRange(leftDate.get(), null));
            } else {
                throw new ParseException("Right operand of .. must be a date or *");
            }
        } else {
            // Just one date, not a range
            return new Qualifier(type, leftDate.get());
        }
        assert false : "Should never reach here";
        return null;
    }

    private FilterExpression parseSortKeys() {
        List<SortKey> keys = new ArrayList<>();
        keys.add(parseSortKey());

        while (lookAhead().getType() == TokenType.COMMA) {
            consume(TokenType.COMMA);
            keys.add(parseSortKey());
        }
        return new Qualifier(QualifierType.SORT, keys);
    }

    private SortKey parseSortKey() {
        if (lookAhead().getType() == TokenType.NOT) {
            consume(TokenType.NOT);
            Token key = consume(TokenType.SYMBOL);
            return new SortKey(key.getValue(), true);
        } else {
            Token key = consume(TokenType.SYMBOL);
            return new SortKey(key.getValue(), false);
        }
    }

    private FilterExpression parseRangeOperator(QualifierType type, Token token) {
        String operator = token.getValue();

        consume(token.getType());
        if (isNumberOrDateToken(lookAhead())) {
            Token contentToken = consume();
            Optional<LocalDate> date = parseDate(contentToken);
            if (date.isPresent()) {
                // Date
                switch (token.getType()) {
                case GT:
                    return new Qualifier(type, new DateRange(date.get(), null, true));
                case GTE:
                    return new Qualifier(type, new DateRange(date.get(), null));
                case LT:
                    return new Qualifier(type, new DateRange(null, date.get(), true));
                case LTE:
                    return new Qualifier(type, new DateRange(null, date.get()));
                default:
                    assert false : "Should not happen";
                    break;
                }
                assert false : "Should not happen";
                return null;
            } else {
                // May be a number or something else
                try {
                    Integer.parseInt(contentToken.getValue());
                } catch (NumberFormatException e) {
                    // Exit with an exception if it's not a number
                    throw new ParseException(String.format(// NOPMD
                        "Operator %s can only be applied to number or date", operator));
                }

                // Must be a number
                int num = Integer.parseInt(contentToken.getValue());

                switch (token.getType()) {
                case GT:
                    return new Qualifier(type, new NumberRange(num, null, true));
                case GTE:
                    return new Qualifier(type, new NumberRange(num, null));
                case LT:
                    return new Qualifier(type, new NumberRange(null, num, true));
                case LTE:
                    return new Qualifier(type, new NumberRange(null, num));
                default:
                    assert false : "Should not happen";
                    break;
                }
                assert false : "Should not happen";
                return null;
            }
        } else {
            throw new ParseException(String.format(
                "Operator %s can only be applied to number or date, got %s", operator, lookAhead()));
        }
    }

    private FilterExpression parseGroup() {
        FilterExpression expr = parseExpression(0);
        consume(TokenType.RBRACKET);
        return expr;
    }

    private FilterExpression parseDisjunction(FilterExpression left) {
        return new Disjunction(left, parseExpression(Precedence.DISJUNCTION));
    }

    private FilterExpression parseConjunction(FilterExpression left) {
        return new Conjunction(left, parseExpression(Precedence.CONJUNCTION));
    }

    private FilterExpression parseNegation() {
        return new Negation(parseExpression(Precedence.PREFIX));
    }
}
