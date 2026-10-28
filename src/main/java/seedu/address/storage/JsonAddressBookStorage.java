package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        Path destination = filePath.toAbsolutePath();
        Path parent = destination.getParent();
        Files.createDirectories(parent);
        if (Files.exists(destination) && !Files.isWritable(destination)) {
            throw new AccessDeniedException(destination.toString());
        }

        // Write beside the destination so replacing it can be atomic. A failed write or move leaves
        // the previously saved address book untouched.
        Path temporaryFile = Files.createTempFile(parent, ".addressbook-", ".tmp");
        try {
            Files.writeString(temporaryFile, json, StandardCharsets.UTF_8);
            replaceDataFile(temporaryFile, destination);
        } catch (IOException | RuntimeException saveFailure) {
            try {
                deleteTemporaryFile(temporaryFile);
            } catch (IOException | RuntimeException cleanupFailure) {
                saveFailure.addSuppressed(cleanupFailure);
            }
            throw saveFailure;
        }
    }

    /** Removes a temporary file after an unsuccessful save. */
    void deleteTemporaryFile(Path temporaryFile) throws IOException {
        Files.deleteIfExists(temporaryFile);
    }

    /** Replaces the saved file only after its complete replacement has been written. */
    void replaceDataFile(Path temporaryFile, Path destination) throws IOException {
        Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
    }

}
