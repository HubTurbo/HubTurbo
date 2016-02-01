package ui.components.pickers;

import java.util.*;

public class LabelPickerUILogic {

    public LabelPickerUILogic() {
    }

    public LabelPickerState determineState(LabelPickerState initialState, Set<String> repoLabels, String userInput) {
        String[] keywords = userInput.split("\\s+");
        LabelPickerState state = initialState;
        for (int i = 0; i < keywords.length; i++) {
            if (isConfirmedKeyword(keywords, userInput, i)) {
                state = state.toggleLabel(keywords[i]);
            } else {
                state = state.updateMatchedLabels(repoLabels, keywords[i]);
            }
        }
        return state;
    }

    private boolean isConfirmedKeyword(String[] keywords, String userInput, int keywordIndex) {
        return !(keywordIndex == keywords.length - 1 && !userInput.endsWith(" "));
    }

}
