package logic;

import org.eclipse.egit.github.core.User;

public class TurboCollaborator implements Listable {
	private User ghUser;
	private String githubName;
	private String realName;
	
	public TurboCollaborator(User user) {
		assert user != null;

		this.ghUser = user;
		this.githubName = user.getLogin();
		this.realName = user.getName();
	}
	
	public String getGithubName() {
		return githubName;
	}
	public void setGithubName(String githubName) {
		this.githubName = githubName;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}

	@Override
	public String getListName() {
		return getGithubName();
	}
	
	
}
