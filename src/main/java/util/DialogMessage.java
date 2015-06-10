package util;

import org.controlsfx.dialog.Dialogs;

import javafx.concurrent.Task;

public class DialogMessage {
    public static void showWarningDialog(String title, String message){
        Dialogs.create()
               .title(title)
               .message(message)
               .showWarning();
    }

    public static void showProgressDialog(Task<?> task, String progressTitle){
        Dialogs.create()
               .title(progressTitle)
               .showWorkerProgress(task);
    }
}
