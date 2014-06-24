package model;

import java.util.ArrayList;
import java.util.List;

public class CollaboratorManager {
	private List<TurboCollaborator> collaborators;
	
	CollaboratorManager() {
		this.collaborators = new ArrayList<TurboCollaborator>();
	}
	
	public List<TurboCollaborator> getCollaborators() {
		return this.collaborators;
	}
	
}
