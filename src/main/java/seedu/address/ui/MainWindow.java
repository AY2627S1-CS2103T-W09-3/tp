package seedu.address.ui;

import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final PseudoClass COMPACT_LAYOUT = PseudoClass.getPseudoClass("compact");
    // Use the content height, not the stage height which includes platform-dependent decorations.
    private static final double COMPACT_CONTENT_HEIGHT = 500;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private DetailPane detailPane;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;

    @FXML
    private VBox mainContainer;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private MenuItem historyMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane detailPanePlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());
        configureResponsiveLayout();

        refreshHistoryMenuItems();
        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void configureResponsiveLayout() {
        mainContainer.heightProperty().addListener(observable -> updateLayoutDensity());
        updateLayoutDensity();
    }

    private void updateLayoutDensity() {
        mainContainer.pseudoClassStateChanged(COMPACT_LAYOUT, mainContainer.getHeight() < COMPACT_CONTENT_HEIGHT);
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
        undoMenuItem.setAccelerator(KeyCombination.valueOf("Shortcut+Z"));
        redoMenuItem.setAccelerator(redoAcceleratorFor(System.getProperty("os.name")));
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleHistoryShortcut);
    }

    /** Keeps the Edit menu in sync with the model's undo and redo stacks. */
    private void refreshHistoryMenuItems() {
        undoMenuItem.setDisable(!logic.canUndo());
        redoMenuItem.setDisable(!logic.canRedo());
    }

    static KeyCombination redoAcceleratorFor(String osName) {
        return KeyCombination.valueOf(osName.startsWith("Mac") ? "Meta+Shift+Z" : "Ctrl+Y");
    }

    private void handleHistoryShortcut(KeyEvent event) {
        if (event.isAltDown()) {
            return;
        }
        boolean ctrlOnly = event.isControlDown() && !event.isMetaDown();
        boolean metaOnly = event.isMetaDown() && !event.isControlDown();
        if (event.getCode() == KeyCode.Z && !event.isShiftDown() && (ctrlOnly || metaOnly)) {
            handleUndo();
        } else if ((event.getCode() == KeyCode.Y && ctrlOnly && !event.isShiftDown())
                || (event.getCode() == KeyCode.Z && metaOnly)) {
            handleRedo();
        } else {
            return;
        }
        event.consume();
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        detailPane = new DetailPane();
        detailPanePlaceholder.getChildren().add(detailPane.getRoot());
        personListPanel.selectedPersonProperty().addListener((observable, oldPerson, newPerson) ->
                detailPane.setPerson(newPerson));

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath, logic.getFilteredPersonList());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        Platform.runLater(commandBox::requestFocus);
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    @FXML
    private void handleUndo() {
        commandBox.executeCommand("undo");
    }

    @FXML
    private void handleRedo() {
        commandBox.executeCommand("redo");
    }

    @FXML
    private void handleHistory() {
        commandBox.executeCommand("history");
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    public DetailPane getDetailPane() {
        return detailPane;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            if (!commandResult.isAwaitingConfirmation() && !commandResult.isShowHelp()) {
                commandResult.getViewIndex().ifPresentOrElse(personListPanel::selectPerson,
                        personListPanel::clearSelection);
            }
            logger.info("Result: " + commandResult.getFeedbackToUser());
            if (commandResult.isAwaitingConfirmation()) {
                resultDisplay.setPendingFeedbackToUser(commandResult.getFeedbackToUser());
            } else {
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            }

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setErrorFeedbackToUser(e.getMessage());
            throw e;
        } finally {
            refreshHistoryMenuItems();
        }
    }
}
