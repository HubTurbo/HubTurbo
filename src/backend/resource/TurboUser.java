package backend.resource;

import javafx.scene.image.Image;
import org.eclipse.egit.github.core.User;

@SuppressWarnings("unused")
public class TurboUser {

	private void ______SERIALIZED_FIELDS______() {
	}

	private final String loginName;
	private final String realName;

	private void ______TRANSIENT_FIELDS______() {
	}

	private final String avatarURL;
	private transient Image avatar = null;

	private void ______CONSTRUCTORS______() {
	}

	/**
	 * Default constructor
	 */
	public TurboUser(String loginName) {
		this.loginName = loginName;
		this.realName = "";

		this.avatarURL = "";
	}

	public TurboUser(User user) {
		this.loginName = user.getLogin();
		this.realName = user.getName();

		this.avatarURL = user.getAvatarUrl();
	}

	private void ______METHODS______() {
	}

	public Image getAvatar() {
		if (avatar == null) {
			avatar = new Image(avatarURL, 12, 12, true, true, false);
		}
		return avatar;
	}

	private void ______BOILERPLATE______() {
	}

	public String getRealName() {
		return realName;
	}

	public String getLoginName() {
		return loginName;
	}
}
