package filter.lexer;

import filter.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    private static final boolean SKIP_WHITESPACE = true;
    private static final Pattern NO_WHITESPACE = Pattern.compile("\\S");

    private final List<Rule> rules = Arrays.asList(
            new Rule("AND|&&?", TokenType.AND),
            new Rule("OR|\\|\\|?", TokenType.OR),
            new Rule("NOT|~|!|-", TokenType.NOT),

            // These have higher priority than Symbol
            new Rule("\\d{4}-\\d{1,2}-\\d{1,2}", TokenType.DATE), // YYYY-MM?-DD?
            new Rule("[A-Za-z]+(-[A-Za-z]+)*\\s*:", TokenType.QUALIFIER),
            new Rule(";", TokenType.SEMICOLON),
            new Rule("[A-Za-z0-9#][/A-Za-z0-9.'+-]*", TokenType.SYMBOL),

            new Rule("\\(", TokenType.LBRACKET),
            new Rule("\\)", TokenType.RBRACKET),
            new Rule("\\\"", TokenType.QUOTE),
            new Rule(",", TokenType.COMMA),
            new Rule("%", TokenType.PERCENT),
            new Rule("\\.\\.", TokenType.DOTDOT),

            // These have higher priority than < and >
            new Rule("<=", TokenType.LTE),
            new Rule(">=", TokenType.GTE),
            new Rule("<", TokenType.LT),
            new Rule(">", TokenType.GT),

            new Rule("\\*", TokenType.STAR)
        );

    private final String input;
    private int position;

    public Lexer(String input) {
        this.input = stripTrailingWhitespace(input);
        this.position = 0;
    }

    private final Pattern trailingWhitespace = Pattern.compile("\\s+$");
    private String stripTrailingWhitespace(String input) {
        return trailingWhitespace.matcher(input).replaceAll("");
    }

    private Token nextToken() {

        if (position >= input.length()) {
            return new Token(TokenType.EOF, "");
        }

        if (SKIP_WHITESPACE) {
            Matcher matcher = NO_WHITESPACE.matcher(input).region(position, input.length());
            boolean found = matcher.find();
            if (!found) {
                return new Token(TokenType.EOF, "");
            }
            position = matcher.start();
        }

        for (Rule r : rules) {
            Matcher matcher = r.getPattern().matcher(input).region(position, input.length());

            if (matcher.lookingAt()) {
                String match = matcher.group();
                position += match.length();

                return new Token(r.getTokenType(), match);
            }
        }
        throw new ParseException("Unrecognised token " + input.charAt(position) + " at " + position);
    }

    public ArrayList<Token> lex() {
        ArrayList<Token> result = new ArrayList<>();

        Token previous = null;
        while (position < input.length()
                && (previous == null || previous.getType() != TokenType.EOF)) {
            previous = nextToken();
            result.add(previous);
        }
        result.add(nextToken()); // EOF

        return result;
    }

}
