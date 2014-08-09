package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import model.Model;
import model.TurboMilestone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.DialogMessage;

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
		} catch (SocketTimeoutException | UnknownHostException e) {
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Cannot connect to GitHub", 
						"Please check your internet connection and try again.");
			});
		} catch (RequestException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No repository permissions", 
						"Cannot create milestone for repository in Github.");
			});
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
		} catch (SocketTimeoutException | UnknownHostException e) {
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Cannot connect to GitHub", 
						"Please check your internet connection and try again.");
			});
		} catch (RequestException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No repository permissions", 
						"Cannot delete milestone from Github.");
			});
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateMilestone(TurboMilestone editedMilestone) {
		Milestone ghMilestone = editedMilestone.toGhResource();
		try {
			ServiceManager.getInstance().editMilestone(ghMilestone);
			modelRef.get().refresh();
		} catch (SocketTimeoutException | UnknownHostException e) {
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Cannot connect to GitHub", 
						"Please check your internet connection and try again.");
			});
		} catch (RequestException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No repository permissions", 
						"Cannot update milestone.");
			});
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public ObservableList<TurboMilestone> getMilestones(){
		return modelRef.get().getMilestones();
	}
}
