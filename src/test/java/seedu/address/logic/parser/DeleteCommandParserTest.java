package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;

/**
 * Tests valid indices and the distinction between malformed input and invalid numeric indices.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_missingOrMalformedIndex_throwsFormatError() {
        for (String input : List.of("", "   ", "abc", "1 2", "1.5", "+1")) {
            assertParseFailure(parser, input, "Invalid command format. Use: delete INDEX.");
        }
    }

    @Test
    public void parse_nonPositiveOrOverflowIndex_throwsInvalidIndexError() {
        for (String input : List.of("0", "-1", "-0", "2147483648", "999999999999999999999")) {
            assertParseFailure(parser, input, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }

    @Test
    public void parse_validIndexWithWhitespace_returnsDeleteCommand() {
        assertParseSuccess(parser, " \t1  ", new DeleteCommand(INDEX_FIRST_PERSON));
    }
}
