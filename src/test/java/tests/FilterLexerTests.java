package tests;

import filter.ParseException;
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

        assertEquals(tokenise(""), Arrays.asList(
                new Token(TokenType.EOF, "")));

        assertEquals(tokenise("a' b' c'"), Arrays.asList(
                new Token(TokenType.SYMBOL, "a'"),
                new Token(TokenType.SYMBOL, "b'"),
                new Token(TokenType.SYMBOL, "c'"),
                new Token(TokenType.EOF, "")));
    }

    @Test
    public void lex_repoIds() {

        assertEquals(tokenise("test/test"),
                     Arrays.asList(new Token(TokenType.SYMBOL, "test/test"), new Token(TokenType.EOF, "")));
    }

    @Test
    public void lex_sortingKeys() {

        assertEquals(tokenise("sort: a, b "), Arrays.asList(
                new Token(TokenType.QUALIFIER, "sort:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));

        assertEquals(tokenise("sort-self-other: a, b "), Arrays.asList(
                new Token(TokenType.QUALIFIER, "sort-self-other:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));

        assertEquals(tokenise("---sort-self-other: a, b "), Arrays.asList(
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.QUALIFIER, "sort-self-other:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));

        assertEquals(tokenise("sort: a , - b"), Arrays.asList(
                new Token(TokenType.QUALIFIER, "sort:"),
                new Token(TokenType.SYMBOL, "a"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.NOT, "-"),
                new Token(TokenType.SYMBOL, "b"),
                new Token(TokenType.EOF, "")));
    }

    @Test
    public void lex_milestoneAliases() {

        assertEquals(tokenise("milestone:curr-1"), Arrays.asList(
                new Token(TokenType.QUALIFIER, "milestone:"),
                new Token(TokenType.SYMBOL, "curr-1"),
                new Token(TokenType.EOF, "")
        ));

        assertEquals(tokenise("milestone:curr+1"), Arrays.asList(
                new Token(TokenType.QUALIFIER, "milestone:"),
                new Token(TokenType.SYMBOL, "curr+1"),
                new Token(TokenType.EOF, "")
        ));
    }

    @Test
    public void lex_compoundId_tokenAdded() {

        assertEquals(tokenise("id:test/test#1"), Arrays.asList(
                new Token(TokenType.QUALIFIER, "id:"),
                new Token(TokenType.COMPOUND_ID_PREFIX, "test/test#"),
                new Token(TokenType.SYMBOL, "1"),
                new Token(TokenType.EOF, "")
        ));

    }

    @Test
    public void lex_invalidCompoundId_tokenNotAdded() {
        assertFalse(tokenise("id:#1").contains(new Token(TokenType.COMPOUND_ID_PREFIX, "#")));
        // test: missing "/" 
        assertFalse(tokenise("id:test#1").contains(new Token(TokenType.COMPOUND_ID_PREFIX, "test#")));
        // test: symbol after "/" needed
        assertFalse(tokenise("id:test/#1").contains(new Token(TokenType.COMPOUND_ID_PREFIX, "test/#")));
    }

    @Test(expected = ParseException.class)
    public void lex_invalidUsernameInCompoundId_throwParseException() {
        assertFalse(tokenise("id:-/#1").contains(new Token(TokenType.COMPOUND_ID_PREFIX, "test/#")));
    }

    /**
     * @param query
     * @return list of tokens after lexing
     */
    private List<Token> tokenise(String query) {
        return new Lexer(query).lex();
    }
}
