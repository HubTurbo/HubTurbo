package guitests;

import javafx.application.Platform;
import org.junit.Test;
import ui.TestController;
import ui.UI;
import util.JavaVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;
import static org.loadui.testfx.controls.Commons.hasText;

public class JavaVersionWarningTest extends UITest {

    @Test
    public void javaVersionWarning_OutdatedJavaRuntime_WarningDialogAppears() {
        UI ui = TestController.getUI();

        JavaVersion runtime = new JavaVersion(0, 0, 0, 0, 0);
        JavaVersion required = new JavaVersion(1, 0, 0, 0, 0);

        Method methodShowJavaVersionOutdatedWarning;

        try {
            methodShowJavaVersionOutdatedWarning = UI.class.getDeclaredMethod("showJavaVersionOutdatedWarning",
                    JavaVersion.class, JavaVersion.class);
            methodShowJavaVersionOutdatedWarning.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail("Can't reflect method");
            return;
        }

        Platform.runLater(() -> {
            try {
                methodShowJavaVersionOutdatedWarning.invoke(ui, runtime, required);
            } catch (IllegalAccessException | InvocationTargetException e) {
                fail("Failed to call method");
            }
        });

        String message = String.format(JavaVersion.OUTDATED_JAVA_VERSION_MESSAGE,
                required.toString(), runtime.toString());

        waitUntilNodeAppears(hasText(message));
        click("OK");
    }
}
