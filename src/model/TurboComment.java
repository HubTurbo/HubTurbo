package model;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Comment;

import service.ServiceManager;
import ui.issuepanel.expanded.comments.CommentListItem;

public class TurboComment implements CommentListItem {
	private Date createdAt;

	private Date updatedAt;

	private StringProperty body = new SimpleStringProperty();

	private StringProperty bodyHtml = new SimpleStringProperty();

	private StringProperty bodyText = new SimpleStringProperty();

	private long id = 0;

	private String url;

	private TurboUser creator;
		
	public TurboComment(Comment comment){
		createdAt = comment.getCreatedAt();
		updatedAt = comment.getUpdatedAt();
		setBody(comment.getBody());
		setBodyHtml(comment.getBodyHtml());
		setBodyText(comment.getBodyText());
		setId(comment.getId());
		setUrl(comment.getUrl());
		setCreator(new TurboUser(comment.getUser()));
	}
	
	public TurboComment(TurboComment comment){
		copyValues(comment);
	}
	
	public boolean isIssueLog(){
		String text = getBodyText();
		if(text == null){
			text = getBody();
		}
		return text.startsWith(ServiceManager.CHANGELOG_TAG);
	}
	
	public void setCreatedAt(Date date){
		createdAt = date;
	}
	public Date getCreatedAt(){
		return createdAt;
	}
	
	public void setUpdatedAt(Date date){
		updatedAt = date;
	}
	public Date getUpdatedAt(){
		return updatedAt;
	}
	
	public void setBody(String text){
		body.set(text);
	}
	public String getBody(){
		return body.get();
	}
	public StringProperty getBodyProperty(){
		return body;
	}
	
	public void setBodyHtml(String text){
		bodyHtml.set(text);
	}
	public String getBodyHtml(){
		return bodyHtml.get();
	}
	public StringProperty getBodyHtmlProperty(){
		return bodyHtml;
	}
	
	public void setBodyText(String text){
		bodyText.set(text);
	}
	public String getBodyText(){
		return bodyText.get();
	}
	public StringProperty getBodyTextProperty(){
		return bodyText;
	}
		
	public void setId(long id){
		this.id = id;
	}
	public long getId(){
		return id;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	public String getUrl(){
		return url;
	}
	
	public void setCreator(TurboUser user){
		creator = user;
	}
	public TurboUser getCreator(){
		return creator;
	}
	
	public void copyValues(Object other) {
		assert other != null;
		if(other.getClass() == TurboComment.class){
			TurboComment comment = (TurboComment)other;
			setCreatedAt(comment.getCreatedAt());
			setUpdatedAt(comment.getUpdatedAt());
			setBody(comment.getBody());
			setBodyHtml(comment.getBodyHtml());
			setBodyText(comment.getBodyText());
			setId(comment.getId());
			setUrl(comment.getUrl());
			setCreator(comment.getCreator());
		}
	}
	
	public Comment toGhComment(){
		Comment comment = new Comment();
		comment.setCreatedAt(createdAt);
		comment.setUpdatedAt(updatedAt);
		comment.setBody(getBody());
		comment.setBodyHtml(getBodyHtml());
		comment.setBodyHtml(getBodyText());
		comment.setId(id);
		comment.setUrl(url);
		comment.setUser(creator.toGhResource());
		return comment;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(!(obj instanceof TurboComment)){
			return false;
		}
		return ((TurboComment)obj).getId() == this.getId();
	}
	
	@Override
	public int hashCode() {
		Long idObj = new Long(id);
		return idObj.hashCode();
	}
	
	public Date getDate() {
		return getCreatedAt();
	}
}
