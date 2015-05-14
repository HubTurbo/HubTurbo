package backend.resource.serialization;

import backend.resource.TurboUser;

public class SerializableUser {
	public final String loginName;
	public final String realName;
	public final String avatarURL;

	public SerializableUser(TurboUser user) {
		this.loginName = user.getLoginName();
		this.realName = user.getRealName();
		this.avatarURL = user.getAvatarURL();
	}
}
