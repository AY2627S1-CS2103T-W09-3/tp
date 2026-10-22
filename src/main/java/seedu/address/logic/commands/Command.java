package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns whether a successful execution is eligible for history. This does not indicate execution or save
     * success; the caller must check both, and equal before/after snapshots are still excluded by the model.
     */
    public boolean isUndoable() {
        return false;
    }

    /** Returns the history command word; read only for an undoable command after successful execution. */
    public String getCommandWord() {
        return "";
    }

    /** Returns a specific history description; read only for an undoable command after successful execution. */
    public String getActionDescription() {
        return "";
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
