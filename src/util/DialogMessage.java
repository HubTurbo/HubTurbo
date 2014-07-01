package util;

import org.controlsfx.dialog.Dialogs;

public class DialogMessage {
	public static void showWarningDialog(String title, String message){
		Dialogs.create()
        .title(title)
        .message(message)
        .showWarning();
	}
}
