package tests;

import filter.lexer.Lexer;
import filter.lexer.Token;
import filter.lexer.TokenType;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FilterLexerTests {

    @Test
    public void lex_generalInputs() {

        assertEquals(getTokens(""), Arrays.asList(
            new Token(TokenType.EOF, "")));

        assertEquals(getTokens("a' b' c'"), Arrays.asList(
            new Token(TokenType.SYMBOL, "a'"),
            new Token(TokenType.SYMBOL, "b'"),
            new Token(TokenType.SYMBOL, "c'"),
            new Token(TokenType.EOF, "")));
    }

    @Test
    public void lex_repoIds() {

        assertEquals(getTokens("test/test"), 
            Arrays.asList(new Token(TokenType.SYMBOL, "test/test"), new Token(TokenType.EOF, "")));
    }

    @Test
    public void lex_sortingKeys() {

        assertEquals(getTokens("sort: a, b "), Arrays.asList(
            new Token(TokenType.QUALIFIER, "sort:"),
            new Token(TokenType.SYMBOL, "a"),
            new Token(TokenType.COMMA, ","),
            new Token(TokenType.SYMBOL, "b"),
            new Token(TokenType.EOF, "")));

        assertEquals(getTokens("sort-self-other: a, b "), Arrays.asList(
                new Token(TokenType.QUALIFIER, "sort-self-other:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));

        assertEquals(getTokens("---sort-self-other: a, b "), Arrays.asList(
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.QUALIFIER, "sort-self-other:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));

        assertEquals(getTokens("sort: a , - b"), Arrays.asList(
            new Token(TokenType.QUALIFIER, "sort:"),
            new Token(TokenType.SYMBOL, "a"),
            new Token(TokenType.COMMA, ","),
            new Token(TokenType.NOT, "-"),
            new Token(TokenType.SYMBOL, "b"),
            new Token(TokenType.EOF, "")));
    }

    @Test
    public void lex_milestoneAliases() {

        assertEquals(getTokens("milestone:curr-1"), Arrays.asList(
                new Token(TokenType.QUALIFIER, "milestone:"),
                new Token(TokenType.SYMBOL, "curr-1"),
                new Token(TokenType.EOF, "")
        ));

        assertEquals(getTokens("milestone:curr+1"), Arrays.asList(
                new Token(TokenType.QUALIFIER, "milestone:"),
                new Token(TokenType.SYMBOL, "curr+1"),
                new Token(TokenType.EOF, "")
        ));
    }

    @Test
    public void lex_compoundId() {

        assertEquals(getTokens("id:test/test#1"), Arrays.asList(
                new Token(TokenType.QUALIFIER, "id:"),
                new Token(TokenType.COMPOUND_ID, "test/test#"),
                new Token(TokenType.SYMBOL, "1"),
                new Token(TokenType.EOF, "")
        ));

        // test: at least one symbol preceedes "#"
        assertFalse(getTokens("id:#1").contains(new Token(TokenType.COMPOUND_ID, "#")));
    }

    /**
     * @param query
     * @return list of tokens after lexing
     */
    private List<Token> getTokens(String query) {
        return new Lexer(query).lex();
    }
}
