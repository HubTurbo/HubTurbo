package filter;

public class Token {
	private TokenType type;
	private String value;
	private int position;
	
	public Token(TokenType type, String value, int position) {
		this.type = type;
		this.value = value;
		this.position = position;
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", value=" + value + ", position="
				+ position + "]";
	}
}
