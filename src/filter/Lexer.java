package filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

	private final boolean SKIP_WHITESPACE = true;
	private final Pattern NO_WHITESPACE = Pattern.compile("\\S");
	
	private List<Rule> rules = Arrays.asList(
			new Rule("[~A-Za-z0-9][0-9A-Za-z.]*", TokenType.SYMBOL),
			new Rule("\\(", TokenType.LBRACKET),
			new Rule("\\)", TokenType.RBRACKET)
		);

	private String input;
	private int position;
	
	public Lexer(String input) {
		this.input = input;
		this.position = 0;
	}

	private Token nextToken() {

		if (SKIP_WHITESPACE) {
			Matcher matcher = NO_WHITESPACE.matcher(input).region(position, input.length());
			matcher.find();
			position = matcher.start();
		}
		
		for (Rule r : rules) {
			Matcher matcher = r.getPattern().matcher(input).region(position, input.length());

			if (matcher.lookingAt()) {
				String match = matcher.group();
				System.out.println(match);
				position += match.length();

				return new Token(r.getTokenType(), match, matcher.start());
			}
		}
		throw new IllegalArgumentException("unrecognised token " + input.charAt(position) + " at " + position);
	}
	
	public ArrayList<Token> lex() {
		ArrayList<Token> result = new ArrayList<>();
		
		while (position < input.length()) {
			result.add(nextToken());
		}

		return result;
	}

}
