package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryOperationException;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /** Whether the previous command was a {@code clear} that is waiting for {@code clear confirm}. */
    private boolean isClearPending;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command;
        if (isClearPending) {
            isClearPending = false;
            command = parseClearConfirmation(commandText);
            if (command == null) {
                logger.info("Pending clear cancelled by: " + commandText);
                return new CommandResult(ClearCommand.MESSAGE_CANCELLED);
            }
        } else {
            command = addressBookParser.parseCommand(commandText);
            if (command instanceof ClearCommand clearCommand && clearCommand.isConfirmed()) {
                throw new CommandException(ClearCommand.MESSAGE_NO_CLEAR_PENDING);
            }
        }

        AddressBookSnapshot beforeCommand = model.captureSnapshot();
        List<String> persistedBefore = persistedState(model);
        CommandResult commandResult = command.execute(model);
        AddressBookSnapshot afterCommand = model.captureSnapshot();

        // Compare every stored field of every client instead of comparing the model, so an edit that the model
        // considers equal, such as a tag that differs only in letter casing, is still saved.
        if (!persistedState(model).equals(persistedBefore)) {
            saveAddressBookOrRollback(command, beforeCommand);
        }

        if (command.isUndoable()) {
            model.recordChange(beforeCommand, afterCommand, command.getCommandWord(), command.getActionDescription());
        }

        isClearPending = commandResult.isAwaitingConfirmation();
        return commandResult;
    }

    /**
     * Saves the current address book, restoring data and history if saving fails.
     */
    private void saveAddressBookOrRollback(Command command, AddressBookSnapshot beforeCommand) throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            rollbackFailedSave(command, beforeCommand);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            rollbackFailedSave(command, beforeCommand);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
    }

    /**
     * Reverts the data and stack transition made before a failed save.
     */
    private void rollbackFailedSave(Command command, AddressBookSnapshot beforeCommand) {
        try {
            if (command instanceof UndoCommand undoCommand) {
                model.rollbackUndo(requireNonNull(undoCommand.getUndoneEntry(),
                        "Successful undo must retain its history entry"));
            } else if (command instanceof RedoCommand redoCommand) {
                model.rollbackRedo(requireNonNull(redoCommand.getRedoneEntry(),
                        "Successful redo must retain its history entry"));
            } else {
                model.restoreSnapshot(beforeCommand);
            }
        } catch (HistoryOperationException e) {
            throw new IllegalStateException("Could not restore history after a failed save", e);
        }
        logger.warning("Restored client data and history after a failed save");
    }

    /**
     * Returns the confirmed {@code ClearCommand} if {@code commandText} is {@code clear confirm},
     * or null if it is any other input, including input that cannot be parsed.
     */
    private Command parseClearConfirmation(String commandText) {
        try {
            Command command = addressBookParser.parseCommand(commandText);
            if (command instanceof ClearCommand clearCommand && clearCommand.isConfirmed()) {
                return clearCommand;
            }
        } catch (ParseException e) {
            // Input that cannot be parsed cancels the pending clear like any other input.
        }
        return null;
    }

    /**
     * Returns the JSON text of every client, so a command counts as a change whenever the text that would be written
     * to the data file changes, even if the model considers the before and after states equal.
     */
    private static List<String> persistedState(Model model) {
        return model.getAddressBook().getPersonList().stream()
                .map(JsonAddressBookStorage::toPersistedJson)
                .toList();
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public boolean canUndo() {
        return model.canUndo();
    }

    @Override
    public boolean canRedo() {
        return model.canRedo();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return model.getAddressBook().getPersonList();
    }

    @Override
    public ObservableValue<Boolean> personListFilteredProperty() {
        return model.personListFilteredProperty();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
