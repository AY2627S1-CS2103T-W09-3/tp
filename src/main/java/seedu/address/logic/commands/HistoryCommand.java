package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.Model;
import seedu.address.model.history.HistoryEntry;

/** Displays the available undo and redo actions, with the next action first. */
public class HistoryCommand extends Command {
    public static final String COMMAND_WORD = "history";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Use: history.";
    public static final String MESSAGE_EMPTY_HISTORY = "There is no history to display.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<HistoryEntry> undoHistory = model.getUndoHistory();
        List<HistoryEntry> redoHistory = model.getRedoHistory();
        if (undoHistory.isEmpty() && redoHistory.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY_HISTORY);
        }
        return new CommandResult("Undo history:\n" + formatEntries(undoHistory)
                + "\nRedo history:\n" + formatEntries(redoHistory));
    }

    private static String formatEntries(List<HistoryEntry> entries) {
        if (entries.isEmpty()) {
            return "None";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                result.append('\n');
            }
            result.append(i + 1).append(". ").append(entries.get(i).getActionDescription());
        }
        return result.toString();
    }
}
