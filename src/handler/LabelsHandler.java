package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Label;

import service.ServiceManager;
import model.Model;
import model.TurboLabel;

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
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		TurboLabel returnedLabel = new TurboLabel(createdLabel);
		modelRef.get().createLabel(returnedLabel);
		modelRef.get().refresh();
		return returnedLabel;
	}
	
	public void deleteLabel(TurboLabel label) {
		try {
			ServiceManager.getInstance().deleteLabel(URLEncoder.encode(label.toGhName(), CHARSET));
			modelRef.get().deleteLabel(label);
			modelRef.get().refresh();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateLabel(TurboLabel editedLabel, String labelName) {
		Label ghLabel = editedLabel.toGhResource();
		try {
			ServiceManager.getInstance().editLabel(ghLabel, URLEncoder.encode(labelName, CHARSET));
			modelRef.get().refresh();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
}
