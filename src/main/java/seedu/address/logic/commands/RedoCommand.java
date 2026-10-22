package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;

/** Reapplies the most recently undone change. */
public class RedoCommand extends Command {
    public static final String COMMAND_WORD = "redo";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Use: redo.";

    private HistoryEntry redoneEntry;

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        redoneEntry = null;
        try {
            redoneEntry = model.redo();
        } catch (HistoryOperationException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult("Redid: " + redoneEntry.getActionDescription());
    }

    /** Returns the exact entry transferred by the most recent successful execution. */
    public HistoryEntry getRedoneEntry() {
        return redoneEntry;
    }
}
