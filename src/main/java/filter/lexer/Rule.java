package filter.lexer;

import java.util.regex.Pattern;

public class Rule {
	private TokenType tokenType;
	private Pattern pattern;
	
	public Rule(String pattern, TokenType type) {
		this.tokenType = type;
		this.pattern = Pattern.compile(pattern);
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
