package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.person.Note;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_noteWithSpaces_success() {
        assertParseSuccess(parser, " 1 Call again next Monday ",
                new NoteCommand(INDEX_FIRST_PERSON, new Note("Call again next Monday")));
    }

    @Test
    public void parse_escapedNewline_decodesNewline() {
        assertParseSuccess(parser, "1 First line\\nSecond line",
                new NoteCommand(INDEX_FIRST_PERSON, new Note("First line\nSecond line")));
    }

    @Test
    public void parse_exactLowercaseClear_returnsClearCommand() {
        assertParseSuccess(parser, "1 clear", new NoteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_uppercaseClear_isOrdinaryText() {
        assertParseSuccess(parser, "1 CLEAR",
                new NoteCommand(INDEX_FIRST_PERSON, new Note("CLEAR")));
    }

    @Test
    public void parse_sentenceContainingClear_isOrdinaryText() {
        assertParseSuccess(parser, "1 Please clear the outstanding paperwork",
                new NoteCommand(INDEX_FIRST_PERSON, new Note("Please clear the outstanding paperwork")));
    }

    @Test
    public void parse_blankNote_failure() {
        assertParseFailure(parser, "1", Note.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1    ", Note.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_tooLongNote_failure() {
        assertParseFailure(parser, "1 " + "a".repeat(Note.MAX_LENGTH + 1), Note.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingIndex_failure() {
        assertParseFailure(parser, "", NoteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        String[] invalidInputs = {"0 note", "-1 note", "1.5 note", "abc note", "2147483648 note"};

        for (String input : invalidInputs) {
            assertParseFailure(parser, input, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }
}
