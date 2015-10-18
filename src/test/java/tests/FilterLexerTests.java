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
            new Token(TokenType.EOF, "")));
        assertEquals(new Lexer("a' b' c'").lex(), Arrays.asList(
            new Token(TokenType.SYMBOL, "a'"),
            new Token(TokenType.SYMBOL, "b'"),
            new Token(TokenType.SYMBOL, "c'"),
            new Token(TokenType.EOF, "")));

        // Repo names
        assertEquals(new Lexer("test/test").lex(), Arrays.asList(
            new Token(TokenType.SYMBOL, "test/test"),
            new Token(TokenType.EOF, "")));

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
            new Token(TokenType.QUALIFIER, "sort:"),
            new Token(TokenType.SYMBOL, "a"),
            new Token(TokenType.COMMA, ","),
            new Token(TokenType.SYMBOL, "b"),
            new Token(TokenType.EOF, "")));
        assertEquals(new Lexer("sort-self-other: a, b ").lex(), Arrays.asList(
                new Token(TokenType.QUALIFIER, "sort-self-other:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));
        assertEquals(new Lexer("---sort-self-other: a, b ").lex(), Arrays.asList(
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.QUALIFIER, "sort-self-other:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));
        assertEquals(new Lexer("sort: a , - b").lex(), Arrays.asList(
            new Token(TokenType.QUALIFIER, "sort:"),
            new Token(TokenType.SYMBOL, "a"),
            new Token(TokenType.COMMA, ","),
            new Token(TokenType.NOT, "-"),
            new Token(TokenType.SYMBOL, "b"),
            new Token(TokenType.EOF, "")));

        // milestone aliases
        assertEquals(new Lexer("milestone:prev-1").lex(), Arrays.asList(
                new Token(TokenType.QUALIFIER, "milestone:"),
                new Token(TokenType.SYMBOL, "prev-1"),
                new Token(TokenType.EOF, "")
        ));
        assertEquals(new Lexer("milestone:next+1").lex(), Arrays.asList(
                new Token(TokenType.QUALIFIER, "milestone:"),
                new Token(TokenType.SYMBOL, "next+1"),
                new Token(TokenType.EOF, "")
        ));
    }

}
