package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;

/** Reverses the most recent recorded change. */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Use: undo.";

    private HistoryEntry undoneEntry;

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        undoneEntry = null;
        try {
            undoneEntry = model.undo();
        } catch (HistoryOperationException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult("Undid: " + undoneEntry.getActionDescription());
    }

    /** Returns the exact entry transferred by the most recent successful execution. */
    public HistoryEntry getUndoneEntry() {
        return undoneEntry;
    }
}
