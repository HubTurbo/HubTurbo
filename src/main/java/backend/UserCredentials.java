package backend;

public class UserCredentials {
	public final String username;
	public final String password;

	public UserCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UserCredentials that = (UserCredentials) o;

		if (password != null ? !password.equals(that.password) : that.password != null) return false;
		if (username != null ? !username.equals(that.username) : that.username != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = username != null ? username.hashCode() : 0;
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}
}
