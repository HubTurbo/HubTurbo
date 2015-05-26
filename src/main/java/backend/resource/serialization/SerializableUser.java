package backend.resource.serialization;

import backend.resource.TurboUser;

public class SerializableUser {
	private String loginName = "";
	private String realName = "";
	private String avatarURL = "";

	public SerializableUser(TurboUser user) {
		this.loginName = user.getLoginName();
		this.realName = user.getRealName();
		this.avatarURL = user.getAvatarURL();
	}

	public String getLoginName() {
		return loginName;
	}
	public String getRealName() {
		return realName;
	}
	public String getAvatarURL() {
		return avatarURL;
	}
}
