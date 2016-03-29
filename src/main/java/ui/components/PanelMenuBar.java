package ui.components;

import static ui.issuepanel.AbstractPanel.PANEL_WIDTH;
import static ui.issuepanel.AbstractPanel.OCTICON_TICK_MARK;
import static ui.issuepanel.AbstractPanel.OCTICON_UNDO;
import static ui.issuepanel.AbstractPanel.OCTICON_RENAME_PANEL;
import static ui.issuepanel.AbstractPanel.OCTICON_CLOSE_PANEL;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import ui.GUIController;
import ui.IdGenerator;
import ui.UI;
import ui.issuepanel.FilterPanel;
import util.events.ShowRenamePanelEvent;

/**
 * An HTPanelMenuBar allows for the creation of a high level panel menu bar interface that
 * contains logic for renaming the panel menu, handling the button and keyboard events
 * and augmenting the children of panel menu bar defined in FilterPanel.java during the
 * renaming process. The class is instantiated in FilterPanel.java.
 */
public class PanelMenuBar extends HBox {

    private final HBox menuBarUndoButton;
    private final HBox menuBarConfirmButton;
    private final HBox menuBarRenameButton;
    private final HBox menuBarCloseButton;
    private TextField renameableTextField;
    private final HBox menuBarNameArea;
    private HBox nameBox;
    private Label renameButton;
    private final FilterPanel panel;
    private Label closeButton;
    private final UI ui;
    private String panelName = DEFAULT_PANEL_NAME;
    private Text nameText;

    public static final String DEFAULT_PANEL_NAME = "Panel";
    public static final int NAME_DISPLAY_WIDTH = PANEL_WIDTH - 80;
    public static final int NAME_AREA_WIDTH = PANEL_WIDTH - 65;
    public static final int TOOLTIP_WRAP_WIDTH = 220; //prefWidth for longer tooltip

    public PanelMenuBar(FilterPanel panel, UI ui) {
        this.ui = ui;
        this.panel = panel;
        this.setSpacing(2);
        this.setMinWidth(PANEL_WIDTH);
        this.setMaxWidth(PANEL_WIDTH);
        menuBarNameArea = createNameArea();
        menuBarRenameButton = createRenameButton();
        menuBarCloseButton = createCloseButton();
        menuBarConfirmButton = createOcticonButton(OCTICON_TICK_MARK, "confirmButton");
        menuBarUndoButton = createOcticonButton(OCTICON_UNDO, "undoButton");

        menuBarRenameButton.setMaxWidth(27);
        menuBarRenameButton.setMinWidth(27);
        menuBarConfirmButton.setMaxWidth(27);
        menuBarConfirmButton.setMinWidth(27);
        this.getChildren().addAll(menuBarNameArea, menuBarRenameButton, menuBarCloseButton);
        this.setPadding(new Insets(0, 0, 8, 0));
    }

    private HBox createNameArea() {
        HBox nameArea = new HBox();

        nameText = new Text(panelName);
        nameText.setId(IdGenerator.getPanelNameAreaId(panel.panelIndex));
        nameText.setWrappingWidth(NAME_DISPLAY_WIDTH);

        nameBox = new HBox();
        nameBox.getChildren().add(nameText);
        nameBox.setMinWidth(NAME_DISPLAY_WIDTH);
        nameBox.setMaxWidth(NAME_DISPLAY_WIDTH);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        nameBox.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
                    && mouseEvent.getClickCount() == 2) {

                mouseEvent.consume();
                activateInplaceRename();
            }
        });
        Tooltip.install(nameArea, new Tooltip("Double click to edit the name of this panel"));

        nameArea.getChildren().add(nameBox);
        nameArea.setMinWidth(NAME_AREA_WIDTH);
        nameArea.setMaxWidth(NAME_AREA_WIDTH);
        nameArea.setPadding(new Insets(0, 10, 0, 5));
        return nameArea;
    }

    private void activateInplaceRename() {
        ui.triggerEvent(new ShowRenamePanelEvent(panel.panelIndex));
    }

    private HBox createRenameButton() {
        HBox renameBox = new HBox();
        renameButton = new Label(OCTICON_RENAME_PANEL);

        renameButton.getStyleClass().addAll("octicon", "label-button");
        renameButton.setId(IdGenerator.getPanelRenameButtonId(panel.panelIndex));
        renameButton.setOnMouseClicked(e -> {
            e.consume();
            activateInplaceRename();
        });
        Tooltip.install(renameBox, new Tooltip("Edit the name of this panel"));

        renameBox.getChildren().add(renameButton);
        return renameBox;
    }

    private HBox createCloseButton() {
        HBox closeArea = new HBox();
        closeButton = new Label(OCTICON_CLOSE_PANEL);
        closeButton.setId(IdGenerator.getPanelCloseButtonId(panel.panelIndex));
        closeButton.getStyleClass().addAll("octicon", "label-button");
        closeButton.setOnMouseClicked((e) -> {
            e.consume();
            panel.parentPanelControl.closePanel(panel.panelIndex);
        });
        Tooltip.install(closeArea, new Tooltip("Close this panel"));

        closeArea.getChildren().add(closeButton);

        return closeArea;
    }

    private HBox createOcticonButton(String octString, String cssName) {
        HBox buttonArea = new HBox();

        Label buttonType = new Label(octString);
        buttonType.getStyleClass().addAll("octicon", "label-button");
        buttonType.setId(IdGenerator.getOcticonButtonId(panel.panelIndex, cssName));
        buttonArea.getChildren().add(buttonType);

        return buttonArea;
    }

    /**
     * Called in FilterPanel.java when the ShowRenamePanelEventHandler is invoked.
     * A renameableTextField is generated in which the user inputs the new panel name.
     */
    public void initRenameableTextFieldAndEvents() {
        renameableTextField = new TextField();
        renameableTextField.setId(IdGenerator.getPanelRenameTextFieldId(panel.panelIndex));
        Platform.runLater(() -> {
            renameableTextField.requestFocus();
            renameableTextField.selectAll();
        });
        renameableTextField.setText(panelName);
        augmentRenameableTextField();
        buttonAndKeyboardEventHandler();
        renameableTextField.setPrefColumnCount(30);
    }

    /**
     * Handles the button and the keyboard events when the panle is in the rename stage
     */
    private void buttonAndKeyboardEventHandler() {
        // for button events
        menuBarUndoButton.setOnMouseClicked((e) -> {
            closeRenameableTextField();
            e.consume();
        });
        menuBarConfirmButton.setOnMouseClicked((e) -> {
            panelNameValidator();
            closeRenameableTextField();
            e.consume();
        });
        // for keyboard events
        setOnKeyReleased(e -> {

            if (e.getCode() == KeyCode.ESCAPE) {
                closeRenameableTextField();
            } else if (e.getCode() == KeyCode.ENTER) {
                panelNameValidator();
                closeRenameableTextField();
            }

            e.consume();
        });
    }

    /**
     * Augments components of PanelMenuBar when the renaming of the panel happens.
     * The confirm button and the undo button are added to the panel.
     */
    private void augmentRenameableTextField() {
        menuBarNameArea.getChildren().remove(nameBox);
        this.getChildren().removeAll(menuBarRenameButton, menuBarCloseButton);
        menuBarNameArea.getChildren().addAll(renameableTextField);
        Tooltip.install(menuBarConfirmButton, new Tooltip("Confirm this name change"));
        Tooltip undoTip = new Tooltip("Abandon this name change and go back to the previous name");
        undoTip.setPrefWidth(TOOLTIP_WRAP_WIDTH);
        Tooltip.install(menuBarUndoButton, undoTip);
        this.getChildren().addAll(menuBarConfirmButton, menuBarUndoButton);
    }

    /**
     * Closes the renameableTextField. The pencil and the close button are
     * added back in.
     */
    private void closeRenameableTextField() {
        menuBarNameArea.getChildren().remove(renameableTextField);
        this.getChildren().removeAll(menuBarConfirmButton, menuBarUndoButton);
        menuBarNameArea.getChildren().add(nameBox);
        this.getChildren().addAll(menuBarRenameButton, menuBarCloseButton);
        panel.requestFocus();
    }

    private void panelNameValidator() {
        String newName = renameableTextField.getText().trim();
        if (!newName.equals("")) {
            setPanelName(newName);
        }
    }

    public void setNameText(String name) {
        this.nameText.setText(name);
    }

    public Text getNameText() {
        return this.nameText;
    }

    public void setPanelName(String panelName) {
        this.panelName = panelName;
        setNameText(panelName);
    }

    public String getPanelName() {
        return this.panelName;
    }

    public Label getRenameButton() {
        return this.renameButton;
    }

    public Label getCloseButton() {
        return this.closeButton;
    }
}
