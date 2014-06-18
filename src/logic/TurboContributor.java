package logic;

import org.eclipse.egit.github.core.User;

public class TurboContributor {
	private User ghUser;
	private String githubName;
	private String realName;
	
	public TurboContributor(User user) {
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
	
	
}
