package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    public static final String PENDING_STYLE_CLASS = "pending";
    public static final String PENDING_PROMPT_TEXT = "Pending confirmation: type " + ClearCommand.COMMAND_WORD + " "
            + ClearCommand.CONFIRM_WORD + " to proceed, or any other non-empty input to cancel.";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private String defaultPromptText;

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        defaultPromptText = commandTextField.getPromptText();
    }

    /**
     * Moves keyboard focus to the command input field.
     */
    public void requestFocus() {
        commandTextField.requestFocus();
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        executeCommand(commandText, true);
    }

    /**
     * Runs a command from a menu action or shortcut without submitting the text in the command box.
     */
    void executeCommand(String commandText) {
        executeCommand(commandText, false);
    }

    private void executeCommand(String commandText, boolean isSubmittedInput) {
        try {
            CommandResult commandResult = commandExecutor.execute(commandText);
            if (isSubmittedInput) {
                commandTextField.setText("");
            }
            setPendingConfirmationStyle(commandResult.isAwaitingConfirmation());
        } catch (CommandException | ParseException e) {
            setPendingConfirmationStyle(false);
            if (isSubmittedInput) {
                setStyleToIndicateCommandFailure();
            }
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Shows or hides the style and prompt text that indicate a confirmation is pending.
     */
    private void setPendingConfirmationStyle(boolean isPending) {
        ObservableList<String> styleClass = commandTextField.getStyleClass();
        styleClass.remove(PENDING_STYLE_CLASS);

        if (isPending) {
            styleClass.add(PENDING_STYLE_CLASS);
            commandTextField.setPromptText(PENDING_PROMPT_TEXT);
        } else {
            commandTextField.setPromptText(defaultPromptText);
        }
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
