package tests;

import filter.lexer.Token;
import filter.lexer.TokenType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokenTest {

    @Test
    public void tokenTest() {
        Token token = new Token(TokenType.COLON, ":", 5);
        assertEquals(TokenType.COLON, token.getType());
        token.setType(TokenType.DOTDOT);
        assertEquals(TokenType.DOTDOT, token.getType());
        assertEquals(":", token.getValue());
        token.setValue("..");
        assertEquals("..", token.getValue());
        assertEquals(5, token.getPosition());
        token.setPosition(3);
        assertEquals(3, token.getPosition());
        Token token1 = new Token(TokenType.COLON, ":", 5);
        assertEquals(false, token.equals(token1));
        assertEquals(true, token.equals(token));
        assertEquals(false, token.equals(null));
        assertEquals(false, token.equals(""));
        token.setValue(null);
        assertEquals(false, token.equals(token1));
        token1.setType(TokenType.DOTDOT);
        assertEquals(false, token.equals(token1));
        token.setValue(":");
        assertEquals(true, token.equals(token1));
    }

}
