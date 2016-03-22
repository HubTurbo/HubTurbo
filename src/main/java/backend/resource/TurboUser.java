package backend.resource;

import static util.Utility.replaceNull;

import javafx.scene.image.Image;

import org.eclipse.egit.github.core.User;

import backend.resource.serialization.SerializableUser;

@SuppressWarnings("unused")
public class TurboUser {

    private void ______SERIALIZED_FIELDS______() {}

    private final String loginName;
    private final String realName;
    private final String avatarURL;

    private void ______TRANSIENT_FIELDS______() {}

    private final String repoId;
    private transient Image avatar = null;

    private void ______CONSTRUCTORS______() {}

    /**
     * Default constructor.
     */
    public TurboUser(String repoId, String loginName) {
        this.loginName = replaceNull(loginName, "");
        this.realName = "";
        this.avatarURL = "";
        this.repoId = replaceNull(repoId, "");
    }

    /**
     * Copy constructor
     */
    public TurboUser(TurboUser user) {
        this.loginName = user.getLoginName();
        this.realName = user.getRealName();
        this.avatarURL = user.getAvatarURL();
        this.avatar = getAvatarImageFromAvatarUrl();
        this.repoId = user.getRepoId();
    }

    public TurboUser(String repoId, String loginName, String realName) {
        this.loginName = replaceNull(loginName, "");
        this.realName = replaceNull(realName, "");
        this.avatarURL = "";
        this.avatar = getAvatarImageFromAvatarUrl();
        this.repoId = replaceNull(repoId, "");
    }

    public TurboUser(String repoId, User user) {
        this.loginName = replaceNull(user.getLogin(), "");
        this.realName = replaceNull(user.getName(), "");
        this.avatarURL = replaceNull(user.getAvatarUrl(), "");
        this.avatar = getAvatarImageFromAvatarUrl();
        this.repoId = replaceNull(repoId, "");
    }

    public TurboUser(String repoId, SerializableUser user) {
        this.loginName = replaceNull(user.getLoginName(), "");
        this.realName = replaceNull(user.getRealName(), "");
        this.avatarURL = replaceNull(user.getAvatarURL(), "");
        this.avatar = getAvatarImageFromAvatarUrl();
        this.repoId = replaceNull(repoId, "");
    }

    private void ______METHODS______() {}

    @Override
    public String toString() {
        return loginName;
    }

    public Image getAvatarImage() {
        return this.avatar;
    }

    private void ______BOILERPLATE______() {}

    public String getRepoId() {
        return repoId;
    }

    public String getRealName() {
        return realName;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getFriendlierName() {
        return (!realName.isEmpty() ? realName : loginName);
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TurboUser that = (TurboUser) o;
        return loginName.equals(that.loginName) &&
                realName.equals(that.realName) &&
                avatarURL.equals(that.avatarURL);
    }

    @Override
    public int hashCode() {
        int result = loginName.hashCode();
        result = 31 * result + realName.hashCode();
        result = 31 * result + avatarURL.hashCode();
        return result;
    }

    private Image getAvatarImageFromAvatarUrl() {
        if (avatarURL.isEmpty()) return null;
        return new Image(avatarURL, 12, 12, true, true, true);
    }
}
