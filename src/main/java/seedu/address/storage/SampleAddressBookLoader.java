package seedu.address.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.util.SampleDataUtil;

/**
 * Loads bundled sample clients, falling back to samples defined in code.
 */
public class SampleAddressBookLoader {

    private static final Logger logger = LogsCenter.getLogger(SampleAddressBookLoader.class);
    private static final String SAMPLE_RESOURCE = "/data/sample-addressbook.json";

    private SampleAddressBookLoader() {}

    /**
     * Returns the bundled sample client book, or the fallback if loading fails.
     */
    public static ReadOnlyAddressBook load() {
        return load(SampleAddressBookLoader.class.getResourceAsStream(SAMPLE_RESOURCE));
    }

    /**
     * Loads samples from the supplied stream and closes it.
     * A null stream represents a missing resource.
     */
    static ReadOnlyAddressBook load(InputStream input) {
        if (input == null) {
            logger.warning("Bundled sample data is missing. Using fallback samples.");
            return SampleDataUtil.getSampleAddressBook();
        }

        try (InputStream stream = input) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            JsonSerializableAddressBook data = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

            if (data == null) {
                throw new IOException("Bundled sample data must contain a client book.");
            }

            return data.toModelType();
        } catch (IOException | IllegalValueException | IllegalArgumentException | NullPointerException e) {
            // Existing JSON adapters can reject malformed records with unchecked exceptions.
            logger.warning("Bundled sample data could not be loaded. Using fallback samples. " + e);
            return SampleDataUtil.getSampleAddressBook();
        }
    }

}
