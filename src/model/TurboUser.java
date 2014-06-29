package model;

import org.eclipse.egit.github.core.User;

public class TurboUser implements Listable {
	
	/*
	 * Attributes, Getters & Setters
	 */
	
	private String githubName;
	public String getGithubName() {return githubName;}
	public void setGithubName(String githubName) {this.githubName = githubName;}
	
	private String realName;
	public String getRealName() {return realName;}
	public void setRealName(String realName) {this.realName = realName;}
	
	/*
	 * Constructors and Public Methods
	 */
	
	public TurboUser(User user) {
		assert user != null;

		this.githubName = user.getLogin();
		this.realName = user.getName();
	}
	
	public User toGhResource() {
		User ghUser = new User();
		ghUser.setLogin(githubName);
		return ghUser;
	}
	
	/*
	 * Overriden Methods
	 */
	
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
		TurboUser other = (TurboUser) obj;
		if (githubName == null) {
			if (other.githubName != null)
				return false;
		} else if (!githubName.equals(other.githubName))
			return false;
		return true;
	}

}
