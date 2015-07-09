package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

public class LabelPickerTests extends UITest {

    @Test
    public void showLabelPickerTest() {
        click("#dummy/dummy_col0_9");
        push(KeyCode.L);
        sleep(1000);
        assertNodeExists(hasText("Issue #9: Issue 9"));
        push(KeyCode.ENTER);
    }

}
