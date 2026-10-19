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

    /** Index of the client to view, or null if no view is requested. */
    private final Index viewIndex;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null);
    }

    /**
     * Constructs a {@code CommandResult} with an optional view index.
     * A null {@code viewIndex} means no client view is requested.
     */
    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, Index viewIndex) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.viewIndex = viewIndex;
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
     * The index must not be null and refers to the currently displayed client list.
     */
    public CommandResult(String feedbackToUser, Index viewIndex) {
        this(feedbackToUser, false, false, requireNonNull(viewIndex));
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
     * Returns the requested client index, or an empty optional if no view is requested.
     */
    public Optional<Index> getViewIndex() {
        return Optional.ofNullable(viewIndex);
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
                && Objects.equals(viewIndex, otherCommandResult.viewIndex);
    }

    @Override
    public int hashCode() {
        // Index defines value equality but does not override hashCode, so hash its numeric value.
        Integer viewIndexValue = viewIndex == null ? null : viewIndex.getZeroBased();
        return Objects.hash(feedbackToUser, showHelp, exit, viewIndexValue);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("viewIndex", viewIndex)
                .toString();
    }

}
