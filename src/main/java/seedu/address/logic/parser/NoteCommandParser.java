package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Note;

/**
 * Parses input arguments and creates a new {@code NoteCommand} object.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(NoteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }

        String[] arguments = trimmedArgs.split("\\s+", 2);
        if (!ParserUtil.hasValidIndexSyntax(arguments[0])) {
            throw new ParseException(NoteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(arguments[0]);
        } catch (ParseException pe) {
            throw new ParseException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, pe);
        }

        String rawText = arguments.length == 2 ? arguments[1] : "";
        Note note = ParserUtil.parseNote(rawText);
        if (note.value.equals("clear")) {
            return new NoteCommand(index);
        }

        return new NoteCommand(index, note);
    }
}
