package tests;

import backend.resource.TurboUser;
import org.junit.Test;
import ui.components.pickers.AssigneePickerState;
import ui.components.pickers.PickerAssignee;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class AssigneePickerStateTest {

    @Test
    public void toggleAssignee_noAssignee_assigneeAssigned() {
        AssigneePickerState state = getUnassignedState();
        state.toggleAssignee("username1");
        assertTrue(state.getCurrentAssigneesList().get(0).isSelected());
    }

    @Test
    public void toggleAssignee_hasAssignee_assigneeChanged() {
        AssigneePickerState state = getAssignedState();
        state.toggleAssignee("username1");
        assertTrue(state.getCurrentAssigneesList().get(0).isSelected());
        assertFalse(state.getCurrentAssigneesList().get(1).isSelected());
    }

    @Test
    public void toggleAssignee_hasAssignee_assigneeUnassigned() {
        AssigneePickerState state = getAssignedState();
        state.toggleAssignee("username2");
        assertFalse(state.getCurrentAssigneesList().get(0).isSelected());
        assertFalse(state.getCurrentAssigneesList().get(1).isSelected());
    }

    private AssigneePickerState getUnassignedState() {
        List<PickerAssignee> userList = new ArrayList<>();
        PickerAssignee user1 = new PickerAssignee(new TurboUser("test/repo", "username1", "realname1"));
        userList.add(user1);
        return new AssigneePickerState(userList);
    }

    private AssigneePickerState getAssignedState() {
        List<PickerAssignee> userList = new ArrayList<>();
        PickerAssignee user1 = new PickerAssignee(new TurboUser("test/repo", "username1", "realname1"));
        PickerAssignee user2 = new PickerAssignee(new TurboUser("test/repo", "username2", "realname2"));
        user2.setSelected(true);
        userList.add(user1);
        userList.add(user2);
        return new AssigneePickerState(userList);
    }
}
