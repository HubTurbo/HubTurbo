package backend.resource.serialization;

import backend.resource.TurboUser;

/**
 * Warnings are suppressed to prevent complaints about fields not being final.
 * They are this way to give them default values.
 */
@SuppressWarnings("PMD")
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
