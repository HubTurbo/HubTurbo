package guitests;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RepositorySelectorTest extends UITest {

    @Test
    public void repositorySelectorTest() {
        ComboBox<String> comboBox = find("#repositorySelector");
        assertEquals(1, comboBox.getItems().size());
        doubleClick(comboBox);
        doubleClick();
        type("dummy2/dummy2");
        push(KeyCode.ENTER);
        sleep(5000);
    }

}
