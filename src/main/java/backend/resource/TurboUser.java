package backend.resource;

import backend.resource.serialization.SerializableUser;
import javafx.scene.image.Image;
import org.eclipse.egit.github.core.User;

@SuppressWarnings("unused")
public class TurboUser {

	private void ______SERIALIZED_FIELDS______() {
	}

	private final String loginName;
	private final String realName;
	private final String avatarURL;

	private void ______TRANSIENT_FIELDS______() {
	}

	private final String repoId;
	private transient Image avatar = null;

	private void ______CONSTRUCTORS______() {
	}

	/**
	 * Default constructor
	 */
	public TurboUser(String repoId, String loginName) {
		this.loginName = loginName;
		this.realName = "";
		this.avatarURL = "";
		this.repoId = repoId;
	}

	public TurboUser(String repoId, String loginName, String realName) {
		this.loginName = loginName;
		this.realName = realName;
		this.avatarURL = "";
		this.repoId = repoId;
	}

	public TurboUser(String repoId, User user) {
		this.loginName = user.getLogin();
		this.realName = user.getName();
		this.avatarURL = user.getAvatarUrl();
		this.repoId = repoId;
	}

	public TurboUser(String repoId, SerializableUser user) {
		this.loginName = user.getLoginName();
		this.realName = user.getRealName();
		this.avatarURL = user.getAvatarURL();
		this.repoId = repoId;
	}

	private void ______METHODS______() {
	}

	@Override
	public String toString() {
		return loginName;
	}

	public Image getAvatar() {
		if (avatar == null) {
			avatar = new Image(avatarURL, 12, 12, true, true, true);
		}
		return avatar;
	}

	private void ______BOILERPLATE______() {
	}

	public String getRepoId() {
		return repoId;
	}
	public String getRealName() {
		return realName;
	}
	public String getLoginName() {
		return loginName;
	}
	public String getAvatarURL() {
		return avatarURL;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TurboUser that = (TurboUser) o;

		if (!loginName.equals(that.loginName)) return false;
		if (!realName.equals(that.realName)) return false;
		if (!avatarURL.equals(that.avatarURL)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = loginName.hashCode();
		result = 31 * result + realName.hashCode();
		result = 31 * result + avatarURL.hashCode();
		return result;
	}
}
