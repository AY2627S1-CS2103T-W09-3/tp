package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;

/**
 * Sets or clears the note of an existing client.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets or clears the note of the client identified by the index number used in the displayed list.\n"
            + "Parameters: INDEX TEXT | INDEX clear\n"
            + "Example: " + COMMAND_WORD + " 1 Prefers a corner unit\n"
            + "Example: " + COMMAND_WORD + " 1 clear";

    public static final String MESSAGE_INVALID_COMMAND_FORMAT =
            String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, MESSAGE_USAGE);
    public static final String MESSAGE_NOTE_UPDATED = "Note updated for %1$s.";
    public static final String MESSAGE_NOTE_CLEARED = "Note cleared for %1$s.";

    private final Index index;
    private final Note note;

    /**
     * Creates a command that sets {@code note} on the client at {@code index}.
     */
    public NoteCommand(Index index, Note note) {
        this.index = requireNonNull(index);
        this.note = requireNonNull(note);
    }

    /**
     * Creates a command that clears the note of the client at {@code index}.
     */
    public NoteCommand(Index index) {
        this.index = requireNonNull(index);
        this.note = null;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = new Person(
                personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getRole(), note, personToEdit.getTags(),
                personToEdit.getBudget(), personToEdit.getAskingPrice(), personToEdit.getAreas(),
                personToEdit.getHousingTypes());

        model.setPerson(personToEdit, editedPerson);

        String message = note == null ? MESSAGE_NOTE_CLEARED : MESSAGE_NOTE_UPDATED;
        return new CommandResult(String.format(message, editedPerson.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        return other instanceof NoteCommand otherNoteCommand
                && index.equals(otherNoteCommand.index)
                && Objects.equals(note, otherNoteCommand.note);
    }

    @Override
    public int hashCode() {
        return index.hashCode() + Objects.hashCode(note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("note", note)
                .toString();
    }
}
