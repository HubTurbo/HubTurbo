package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import filter.lexer.Token;
import filter.lexer.TokenType;

public class TokenTest {

    @Test
    public void equalityOnValue() {
        Token token = new Token(TokenType.QUALIFIER, ":");
        Token token2 = new Token(TokenType.COLON, ":");
        assertNotEquals(token, token2);
        assertNotEquals(token.hashCode(), token2.hashCode());
    }

    @Test
    public void equalityOnType() {
        Token token = new Token(TokenType.COLON, "a");
        Token token2 = new Token(TokenType.COLON, ":");
        assertNotEquals(token, token2);
        assertNotEquals(token.hashCode(), token2.hashCode());
    }

    @Test
    public void equality() {
        Token token = new Token(TokenType.COLON, ":");
        Token token2 = new Token(TokenType.COLON, ":");
        assertEquals(token, token2);
        assertEquals(token.hashCode(), token2.hashCode());
    }
}
