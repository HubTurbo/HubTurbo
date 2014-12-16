package model;

import javafx.scene.image.Image;

import org.eclipse.egit.github.core.User;

import storage.DataManager;

public class TurboUser implements Listable {
	
	/*
	 * Attributes, Getters & Setters
	 */
	
	public String getAlias() {
		String name = DataManager.getInstance().getUserAliases().get(getGithubName());
		return name == null ? getGithubName() : name;
	}
	
	private String githubName = "";
	public String getGithubName() {
		return githubName;
	}
	
	public void setGithubName(String githubName) {
		this.githubName = githubName;
	}
	
	private String realName;
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	private String avatarUrl = "";
	public String getAvatarUrl(){
		return avatarUrl;
	}
	public void setAvatarUrl(String url){
		if(url != null){
			this.avatarUrl = url;
		}
	}
	
	private transient Image avatar;
	public Image getAvatar() {
		if(avatarUrl == ""){
			return null;
		}
		if(avatar == null){
			avatar = new Image(getAvatarUrl(), 12, 12, true, false);
		}
		return avatar;
	}
	
	/*
	 * Constructors and Public Methods
	 */
	
	public TurboUser(){
		super();
	}
	
	public TurboUser(User user) {
		assert user != null;

		this.githubName = user.getLogin();
		this.realName = user.getName();
		this.avatarUrl = user.getAvatarUrl();
	}
	
	public User toGhResource() {
		User ghUser = new User();
		ghUser.setLogin(githubName);
		return ghUser;
	}
	
	public void copyValues(Object other){
		if(other.getClass() == TurboUser.class){
			TurboUser obj = (TurboUser)other;
			setGithubName(obj.getGithubName());
			setRealName(obj.getRealName());
			setAvatarUrl(obj.getAvatarUrl());
		}
	}
	
	/*
	 * Overriden Methods
	 */
	
	@Override
	public String getListName() {
		return getAlias();
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
