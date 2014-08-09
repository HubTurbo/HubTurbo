package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import model.Model;
import model.TurboLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.DialogMessage;

public class LabelsHandler {
	private static final Logger logger = LogManager.getLogger(LabelsHandler.class.getName());
	private static final String CHARSET = "ISO-8859-1";
	
	WeakReference<Model> modelRef;
	
	public LabelsHandler(Model model){
		modelRef = new WeakReference<>(model);
	}
	
	public TurboLabel createLabel(TurboLabel newLabel) {
		Label ghLabel = newLabel.toGhResource();
		Label createdLabel = null;
		try {
			createdLabel = ServiceManager.getInstance().createLabel(ghLabel);
		} catch (SocketTimeoutException | UnknownHostException e) {
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Cannot connect to GitHub", 
						"Please check your internet connection and try again.");
			});
		} catch (RequestException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No repository permissions", 
						"Cannot create label for repository in Github.");
			});
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		TurboLabel returnedLabel = new TurboLabel(createdLabel);
		modelRef.get().addLabel(returnedLabel);
		modelRef.get().refresh();
		return returnedLabel;
	}
	
	public void deleteLabel(TurboLabel label) {
		try {
			ServiceManager.getInstance().deleteLabel(URLEncoder.encode(label.toGhName(), CHARSET));
			modelRef.get().deleteLabel(label);
			modelRef.get().refresh();
		} catch (SocketTimeoutException | UnknownHostException e) {
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Cannot connect to GitHub", 
						"Please check your internet connection and try again.");
			});
		} catch (RequestException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No repository permissions", 
						"Cannot delete label from Github.");
			});
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateLabel(TurboLabel editedLabel, String labelName) {
		Label ghLabel = editedLabel.toGhResource();
		try {
			ServiceManager.getInstance().editLabel(ghLabel, URLEncoder.encode(labelName, CHARSET));
			modelRef.get().refresh();
		} catch (SocketTimeoutException | UnknownHostException e) {
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Cannot connect to GitHub", 
						"Please check your internet connection and try again.");
			});
		} catch (RequestException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No repository permissions", 
						"Cannot edit label.");
			});
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public ObservableList<TurboLabel> getLabels(){
		return modelRef.get().getLabels();
	}
}
