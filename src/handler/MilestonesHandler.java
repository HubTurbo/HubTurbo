package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javafx.collections.ObservableList;
import model.Model;
import model.TurboMilestone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Milestone;

import service.ServiceManager;

public class MilestonesHandler {
	private static final Logger logger = LogManager.getLogger(MilestonesHandler.class.getName());
	WeakReference<Model> modelRef;
	
	public MilestonesHandler(Model model){
		modelRef = new WeakReference<>(model);
	}
	
	public TurboMilestone createMilestone(TurboMilestone newMilestone) {
		Milestone ghMilestone = newMilestone.toGhResource();
		Milestone createdMilestone = null;
		try {
			createdMilestone = ServiceManager.getInstance().createMilestone(ghMilestone);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		} 
		TurboMilestone returnedMilestone = new TurboMilestone(createdMilestone);
		modelRef.get().addMilestone(returnedMilestone);
		modelRef.get().refresh();
		return returnedMilestone;
	}
	
	public void deleteMilestone(TurboMilestone milestone) {
		try {
			ServiceManager.getInstance().deleteMilestone(milestone.getNumber());
			modelRef.get().deleteMilestone(milestone);
			modelRef.get().refresh();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateMilestone(TurboMilestone editedMilestone) {
		Milestone ghMilestone = editedMilestone.toGhResource();
		try {
			ServiceManager.getInstance().editMilestone(ghMilestone);
			modelRef.get().refresh();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public ObservableList<TurboMilestone> getMilestones(){
		return modelRef.get().getMilestones();
	}
}
