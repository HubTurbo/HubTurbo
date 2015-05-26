package util;

import javafx.concurrent.Task;
import org.controlsfx.dialog.Dialogs;

public class DialogMessage {
	public static void showWarningDialog(String title, String message){
		Dialogs.create()
			   .title(title)
			   .message(message)
			   .showWarning();
	}
	
//	public static Action showConfirmDialog(String title, String message){
//		Action response = Dialogs.create()
//		        .title(title)
//		        .message(message)
//		        .actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
//		        .showConfirm();
//		return response;
//	}
	
	public static void showProgressDialog(Task<?> task, String progressTitle){
		Dialogs.create()
			   .title(progressTitle)
			   .showWorkerProgress(task);
	}
}
