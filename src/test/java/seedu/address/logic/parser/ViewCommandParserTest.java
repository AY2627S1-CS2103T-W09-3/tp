package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ViewCommand;

/**
 * Tests the view command's format and index validation.
 */
public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_missingOrMalformedIndex_throwsFormatError() {
        for (String input : List.of("", "   ", "abc", "1 2", "1 extra", "1.5", "+1", "1 n/Alice")) {
            assertParseFailure(parser, input, "Invalid command format. Use: view INDEX.");
        }
    }

    @Test
    public void parse_nonPositiveOrOverflowIndex_throwsInvalidIndexError() {
        for (String input : List.of("0", "-1", "-0", "2147483648", "999999999999999999999")) {
            assertParseFailure(parser, input, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }

    @Test
    public void parse_validIndex_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "2", new ViewCommand(INDEX_SECOND_PERSON));
        assertParseSuccess(parser, "2147483647", new ViewCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_validIndexWithWhitespace_returnsViewCommand() {
        assertParseSuccess(parser, " \t2  ", new ViewCommand(INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
