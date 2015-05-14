package backend;

public class SerializableUser {
	public final String loginName;
	public final String realName;

	public SerializableUser(TurboUser user) {
		this.loginName = user.getLoginName();
		this.realName = user.getRealName();
	}
}
