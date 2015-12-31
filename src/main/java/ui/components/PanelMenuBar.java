package ui.components;
import static ui.issuepanel.AbstractPanel.PANEL_WIDTH;
import static ui.issuepanel.AbstractPanel.OCTICON_TICK_MARK;
import static ui.issuepanel.AbstractPanel.OCTICON_UNDO;
import static ui.issuepanel.AbstractPanel.OCTICON_RENAME_PANEL;
import static ui.issuepanel.AbstractPanel.OCTICON_CLOSE_PANEL;
import backend.interfaces.IModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import ui.UI;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.events.ShowRenamePanelEvent;

/**
 * An HTPanelMenuBar allows for the creation of a high level panel menu bar interface that
 * contains logic for renaming the panel menu, handling the button and keyboard events
 * and augmenting the children of panel menu bar defined in FilterPanel.java during the
 * renaming process. The class is instantiated in FilterPanel.java.
 */
public class PanelMenuBar extends HBox {

    private HBox menuBarUndoButton;
    private HBox menuBarConfirmButton;
    private HBox menuBarCloseButton;
    private TextField renameableTextField;
    private HBox menuBarNameArea;
    private HBox nameBox;
    private Label renameButton;
    private FilterPanel panel;
    private IModel model;
    private Label closeButton;
    private UI ui;
    private String panelName = "Panel";
    private Text nameText;

    public static final int NAME_DISPLAY_WIDTH = PANEL_WIDTH - 70;
    public static final int NAME_AREA_WIDTH = PANEL_WIDTH - 40;
    public static final int NAME_AREA_EDIT_WIDTH = PANEL_WIDTH - 65; //width in the panel rename edit mode
	public static final int TOOLTIP_WRAP_WIDTH = 220; // prefWidth for longer tooltip

    public PanelMenuBar(FilterPanel panel, IModel model, UI ui){
        this.model = model;
        this.ui = ui;
        this.panel = panel;
        this.setSpacing(2);
        this.setMinWidth(PANEL_WIDTH);
        this.setMaxWidth(PANEL_WIDTH);
        menuBarNameArea = createNameArea();
        menuBarCloseButton = createCloseButton();
        menuBarConfirmButton = createOcticonButton(OCTICON_TICK_MARK, "confirmButton");
        menuBarUndoButton = createOcticonButton(OCTICON_UNDO, "undoButton");

        menuBarConfirmButton.setMaxWidth(27);
        menuBarConfirmButton.setMinWidth(27);
        this.getChildren().addAll(menuBarNameArea, menuBarCloseButton);
        this.setPadding(new Insets(0, 0, 8, 0));
    }

    private HBox createNameArea() {
        HBox nameArea = new HBox();

        nameText = new Text(panelName);
        nameText.setId(model.getDefaultRepo() + "_col" + panel.panelIndex + "_nameText");
        nameText.setWrappingWidth(NAME_DISPLAY_WIDTH);

        nameBox = new HBox();
        nameBox.getChildren().add(nameText);
        nameBox.setMinWidth(NAME_DISPLAY_WIDTH);
        nameBox.setMaxWidth(NAME_DISPLAY_WIDTH);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        nameBox.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    mouseEvent.consume();
                    activateInplaceRename();
                }
            }
        });
        Tooltip.install(nameArea, new Tooltip("Edit the name of this panel"));

        HBox renameBox = new HBox();
        renameButton = new Label(OCTICON_RENAME_PANEL);
        renameButton.getStyleClass().addAll("octicon", "label-button");
        renameButton.setId(model.getDefaultRepo() + "_col" + panel.panelIndex + "_renameButton");
        renameButton.setOnMouseClicked(e -> {
            e.consume();
            activateInplaceRename();
        });
        renameBox.getChildren().add(renameButton);
        renameBox.setAlignment(Pos.TOP_RIGHT);

        nameArea.getChildren().addAll(nameBox, renameButton);
        nameArea.setMinWidth(360);
        nameArea.setMaxWidth(360);
        return nameArea;
    }

    private void activateInplaceRename() {
        ui.triggerEvent(new ShowRenamePanelEvent(panel.panelIndex));
    }

    private HBox createCloseButton() {
        HBox closeArea = new HBox();
        closeButton = new Label(OCTICON_CLOSE_PANEL);
        closeButton.setId(model.getDefaultRepo() + "_col" + panel.panelIndex + "_closeButton");
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
        buttonType.getStyleClass().addAll("octicon", "issue-event-icon");
        buttonType.setId(model.getDefaultRepo() + "_col" + panel.panelIndex + "_" + cssName);
        buttonType.getStyleClass().add("label-button");
        buttonArea.getChildren().add(buttonType);

        return buttonArea;
    }

    /** Called in FilterPanel.java when the ShowRenamePanelEventHandler is invoked.
     * A renameableTextField is generated in which the user inputs the new panel name.
     */
    public void initRenameableTextFieldAndEvents() {
        renameableTextField = new TextField();
        renameableTextField.setId(model.getDefaultRepo() + "_col" + panel.panelIndex + "_renameTextField");
        Platform.runLater(() -> {
            renameableTextField.requestFocus();
            renameableTextField.selectAll();
        });
        renameableTextField.setText(panelName);
        augmentRenameableTextField();
        buttonAndKeyboardEventHandler();
        renameableTextField.setPrefColumnCount(30);
    }

    /** Handles the button and the keyboard events when the panle is in the rename stage
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

    /** Augments components of PanelMenuBar when the renaming of the panel happens.
     * The confirm button and the undo button are added to the panel.
     */
    private void augmentRenameableTextField(){
        menuBarNameArea.getChildren().removeAll(nameBox, renameButton);
        this.getChildren().remove(menuBarCloseButton);
        menuBarNameArea.getChildren().addAll(renameableTextField);
        menuBarNameArea.setMinWidth(NAME_AREA_EDIT_WIDTH);
        menuBarNameArea.setMaxWidth(NAME_AREA_EDIT_WIDTH);
        Tooltip.install(menuBarConfirmButton, new Tooltip("Confirm this name change of the panel"));
        Tooltip undoTip = new Tooltip("Abandon this name change and go back to the previous name");
        undoTip.setPrefWidth(TOOLTIP_WRAP_WIDTH);
        Tooltip.install(menuBarUndoButton, undoTip);
        this.getChildren().addAll(menuBarConfirmButton, menuBarUndoButton);
    }

    /** Closes the renameableTextField. The pencil and the close button are
     * added back in.
     */
    private void closeRenameableTextField() {
        menuBarNameArea.getChildren().remove(renameableTextField);
        this.getChildren().removeAll(menuBarConfirmButton, menuBarUndoButton);
        this.getChildren().add(menuBarCloseButton);
        menuBarNameArea.getChildren().addAll(nameBox, renameButton);
        menuBarNameArea.setMinWidth(NAME_AREA_WIDTH);
        menuBarNameArea.setMaxWidth(NAME_AREA_WIDTH);
        panel.requestFocus();
    }

    private void panelNameValidator(){
        String newName = renameableTextField.getText().trim();
        if (!newName.equals("")) {
            setPanelName(newName);
        }
    }
    public void setNameText(String name){
        this.nameText.setText(name);
    }

    public Text getNameText(){
        return this.nameText;
    }

    public void setPanelName(String panelName){
        this.panelName = panelName;
        setNameText(panelName);
    }

    public String getPanelName(){
        return this.panelName;
    }

    public Label getRenameButton(){
        return this.renameButton;
    }

    public Label getCloseButton(){
        return this.closeButton;
    }
}
