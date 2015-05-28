package tests;

import filter.ParseException;
import filter.lexer.Lexer;
import filter.lexer.Token;
import filter.lexer.TokenType;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FilterLexerTests {
	@Test
	public void lexer() {
		assertEquals(new Lexer("").lex(), Arrays.asList(
			new Token(TokenType.EOF, "", 0)));
		assertEquals(new Lexer("a' b' c'").lex(), Arrays.asList(
			new Token(TokenType.SYMBOL, "a'", 0),
			new Token(TokenType.SYMBOL, "b'", 0),
			new Token(TokenType.SYMBOL, "c'", 0),
			new Token(TokenType.EOF, "", 0)));

		// Repo names
		assertEquals(new Lexer("test/test").lex(), Arrays.asList(
			new Token(TokenType.SYMBOL, "test/test", 0),
			new Token(TokenType.EOF, "", 0)));

		// Forward slashes cannot begin symbols, so this won't work
		try {
			new Lexer("repo:\"test / test\"").lex();
		} catch (ParseException ignored) {}

		// Sorting keys
		// Commas aren't valid symbol names and may only appear in the body of a `sort` qualifier
		try {
			new Lexer("a,b").lex();
		} catch (ParseException ignored) {}

		assertEquals(new Lexer("sort: a, b ").lex(), Arrays.asList(
			new Token(TokenType.QUALIFIER, "sort:", 0),
			new Token(TokenType.SYMBOL, "a", 0),
			new Token(TokenType.COMMA, ",", 0),
			new Token(TokenType.SYMBOL, "b", 0),
			new Token(TokenType.EOF, "", 0)));
		assertEquals(new Lexer("sort: a , - b").lex(), Arrays.asList(
			new Token(TokenType.QUALIFIER, "sort:", 0),
			new Token(TokenType.SYMBOL, "a", 0),
			new Token(TokenType.COMMA, ",", 0),
			new Token(TokenType.NOT, "-", 0),
			new Token(TokenType.SYMBOL, "b", 0),
			new Token(TokenType.EOF, "", 0)));
	}

}
