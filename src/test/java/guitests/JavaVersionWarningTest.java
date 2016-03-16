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
    public void javaVersionWarning_PureUiTesting_WarningDialogAppears() {
        UI ui = TestController.getUI();

        JavaVersion runtime = new JavaVersion(0, 0, 0, 0, 0);
        JavaVersion required = new JavaVersion(1, 0, 0, 0, 0);

        Method methodShowJavaVersionOutdatedWarning;

        try {
            methodShowJavaVersionOutdatedWarning =
                    UI.class.getDeclaredMethod("showJavaVersionOutdatedWarning", JavaVersion.class, JavaVersion.class);
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

        String message = String.format(UI.WARNING_MSG_OUTDATED_JAVA_VERSION, required.toString(), runtime.toString());

        waitUntilNodeAppears(hasText(message));
        click("OK");
    }

    @Test
    public void javaVersionError_PureUiTesting_ErrorDialogAppears() {
        UI ui = TestController.getUI();

        String javaRuntimeVersionString = "1.unknown.format.1";

        Method methodShowJavaRuntimeVersionParsingError;

        try {
            methodShowJavaRuntimeVersionParsingError =
                    UI.class.getDeclaredMethod("showJavaRuntimeVersionNotCompatible", String.class);
            methodShowJavaRuntimeVersionParsingError.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail("Can't reflect method");
            return;
        }

        Platform.runLater(() -> {
            try {
                methodShowJavaRuntimeVersionParsingError.invoke(ui, javaRuntimeVersionString);
            } catch (IllegalAccessException | InvocationTargetException e) {
                fail("Failed to call method");
            }
        });

        String message = String.format(UI.ERROR_MSG_JAVA_RUNTIME_VERSION_PARSING, javaRuntimeVersionString);

        waitUntilNodeAppears(hasText(message));
        click("OK");
    }
}
