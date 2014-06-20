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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((githubName == null) ? 0 : githubName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TurboCollaborator other = (TurboCollaborator) obj;
		if (githubName == null) {
			if (other.githubName != null)
				return false;
		} else if (!githubName.equals(other.githubName))
			return false;
		return true;
	}
}
