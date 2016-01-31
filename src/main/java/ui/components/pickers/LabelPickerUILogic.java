package ui.components.pickers;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;

public class LabelPickerUILogic {

    private Stack<LabelPickerState> history = new Stack<>();
    private LabelPickerState currentState;
    private final Set<String> repoLabels;

    public LabelPickerUILogic(LabelPickerState state, Set<String> repoLabels) {
        this.repoLabels = repoLabels;
        currentState = state;
    }

    /**
     * Get a new state of LabelPicker after the user presses keyEvent resulting in currentString
     * @param keyEvent
     * @param currentString
     * @return new state of LabelPicker
     */
    public LabelPickerState getNewState(KeyEvent keyEvent, String currentString) {
        LabelPickerState newState;
        if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.KP_UP) {
            newState = handleKeyUp(currentState);
        } else if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.KP_DOWN) {
            newState = handleKeyDown(currentState);
        } else if (keyEvent.getCode() == KeyCode.SPACE) {
            newState = handleSpace(currentState, currentString);
            history.add(currentState);
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
            newState = handleBackSpace();
        } else {
            newState = handleCharAddition(currentState, currentString);
            history.add(currentState);
        }
        assert newState != null;
        currentState = newState;
        return newState;
    }

    private LabelPickerState handleKeyUp(LabelPickerState currentState) {
        return currentState.previousSuggestion();
    }

    private LabelPickerState handleKeyDown(LabelPickerState currentState) {
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

    private LabelPickerState handleBackSpace() {
        if (!history.isEmpty()) {
            currentState = history.peek();
            history.pop();
        }
        return currentState;
    }

    private LabelPickerState handleCharAddition(LabelPickerState currentState, String currentString) {
        return currentState.updateMatchedLabels(repoLabels, currentString);
    }

}
