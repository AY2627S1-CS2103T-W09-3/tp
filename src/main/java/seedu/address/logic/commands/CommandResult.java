package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** Index in the post-command filtered list to select; see {@link #getViewIndex()} for empty-index handling. */
    private final Index viewIndex;

    /** The user must confirm before the command takes effect. */
    private final boolean awaitingConfirmation;

    /** The command leaves the displayed list and current selection unchanged. */
    private final boolean preserveSelection;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, false);
    }

    /**
     * Constructs a {@code CommandResult} with an explicit confirmation state.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean awaitingConfirmation) {
        this(feedbackToUser, showHelp, exit, awaitingConfirmation, null, false);
    }

    /**
     * Constructs a {@code CommandResult} with all result fields.
     */
    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit,
            boolean awaitingConfirmation, Index viewIndex, boolean preserveSelection) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.awaitingConfirmation = awaitingConfirmation;
        this.viewIndex = viewIndex;
        this.preserveSelection = preserveSelection;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /**
     * Constructs a {@code CommandResult} requesting a view of the client at {@code viewIndex}.
     * The index must not be null and refers to the filtered list after the command has finished.
     * Mutation commands must locate the updated client in that list, rather than reuse its old index.
     */
    public CommandResult(String feedbackToUser, Index viewIndex) {
        this(feedbackToUser, false, false, false, requireNonNull(viewIndex), false);
    }

    /** Returns a result for a read-only command that should keep the selected client visible. */
    public static CommandResult preservingSelection(String feedbackToUser) {
        return new CommandResult(feedbackToUser, false, false, false, null, true);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    /**
     * Returns the client index to select in the post-command filtered list.
     * Results awaiting confirmation, showing help, or requesting selection preservation leave the
     * current selection and detail panel unchanged. For other results, an empty optional instructs
     * the UI to clear its selection and detail panel.
     * After edit, note or role, the index identifies the updated client if it remains visible;
     * otherwise it is empty, even if a different client now occupies the old index.
     */
    public Optional<Index> getViewIndex() {
        return Optional.ofNullable(viewIndex);
    }

    public boolean isAwaitingConfirmation() {
        return awaitingConfirmation;
    }

    public boolean shouldPreserveSelection() {
        return preserveSelection;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && Objects.equals(viewIndex, otherCommandResult.viewIndex)
                && awaitingConfirmation == otherCommandResult.awaitingConfirmation
                && preserveSelection == otherCommandResult.preserveSelection;
    }

    @Override
    public int hashCode() {
        // Index defines value equality but does not override hashCode, so hash its numeric value.
        Integer viewIndexValue = viewIndex == null ? null : viewIndex.getZeroBased();
        return Objects.hash(feedbackToUser, showHelp, exit, viewIndexValue, awaitingConfirmation, preserveSelection);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("viewIndex", viewIndex)
                .add("awaitingConfirmation", awaitingConfirmation)
                .add("preserveSelection", preserveSelection)
                .toString();
    }

}
