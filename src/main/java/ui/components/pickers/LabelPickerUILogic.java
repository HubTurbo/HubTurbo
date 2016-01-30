package ui.components.pickers;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;

public class LabelPickerUILogic {

    private Stack<LabelPickerState> history = new Stack<>();
    private Set<String> repoLabels;

    public LabelPickerUILogic(LabelPickerState state, Set<String> repoLabels) {
        this.repoLabels = repoLabels;
        history.add(state);
    }

    /**
     * Get a new state of LabelPicker after the user presses keyEvent resulting in currentString
     * @param keyEvent
     * @param currentString
     * @return new state of LabelPicker
     */
    public LabelPickerState getNewState(KeyEvent keyEvent, String currentString) {
        LabelPickerState currentState = history.peek();
        LabelPickerState newState;
        if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.KP_UP) {
            newState = handleKeyUp(currentState);
            history.add(newState);
        } else if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.KP_DOWN) {
            newState = handleKeyDown(currentState);
            history.add(newState);
        } else if (keyEvent.getCode() == KeyCode.SPACE) {
            newState = handleSpace(currentState, currentString);
            history.add(newState);
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
            newState = handleBackSpace(currentString);
        } else {
            newState = handleCharAddition(currentState, currentString);
            history.add(newState);
        }
        assert newState != null;
        return newState;
    }

    private LabelPickerState handleKeyUp(LabelPickerState currentState) {
        history.pop();
        return currentState.previousSuggestion();
    }

    private LabelPickerState handleKeyDown(LabelPickerState currentState) {
        history.pop();
        return currentState.nextSuggestion();
    }

    private LabelPickerState handleSpace(LabelPickerState currentState, String currentString) {
        assert currentString.charAt(currentString.length() - 1) == ' ';
        String[] keywords = currentString.split("\\s+");
        if (keywords.length > 0 && !Character.isSpaceChar(currentString.charAt(currentString.length() - 2))) {
            //just confirmed a word
            return currentState.clearMatchedLabels().toggleLabel(keywords[keywords.length - 1]);
        }
        return currentState;
    }

    private LabelPickerState handleBackSpace(String currentString) {
        if (history.size() > 1) {
            history.pop();
        } else {
            assert currentString.length() == 0;
        }
        return history.peek();
    }

    private LabelPickerState handleCharAddition(LabelPickerState currentState, String currentString) {
        return currentState.updateMatchedLabels(repoLabels, currentString);
    }

}
