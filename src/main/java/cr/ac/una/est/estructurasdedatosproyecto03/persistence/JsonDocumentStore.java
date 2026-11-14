package cr.ac.una.est.estructurasdedatosproyecto03.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Supplier;

public class JsonDocumentStore{
    private final Gson gson;

    // Configure readable JSON output.
    // Output: a JSON document store configured with Gson.
    public JsonDocumentStore (){
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    // Read and deserialize saved data so repositories can restore application state from disk.
    // Inputs: file path, expected JSON type, and a supplier for a missing or empty file; output: the restored value or supplied default.
    public <T> T readJson (Path filePath, Type targetType, Supplier<T> defaultValueSupplier) throws IOException{
        Objects.requireNonNull(filePath, "File path cannot be null.");
        Objects.requireNonNull(targetType, "Target type cannot be null.");
        Objects.requireNonNull(defaultValueSupplier, "Default value supplier cannot be null.");

        Path normalizedFilePath = filePath.toAbsolutePath().normalize();

        if (Files.notExists(normalizedFilePath)){
            return defaultValueSupplier.get();
        }

        try (Reader reader = Files.newBufferedReader(normalizedFilePath, StandardCharsets.UTF_8)){
            T savedValue = gson.fromJson(reader, targetType);

            if (savedValue == null){
                return defaultValueSupplier.get();
            }

            return savedValue;
        } catch (JsonParseException exception){
            throw new IOException("The JSON file is malformed: " + normalizedFilePath, exception);
        }
    }

    // Write JSON through a temporary file for security.
    // Inputs: destination file path and the object to serialize; output: none, the JSON will replace the destination file.
    public void writeJson (Path filePath, Object value) throws IOException{
        Objects.requireNonNull(filePath, "File path cannot be null.");
        Objects.requireNonNull(value, "Value to save cannot be null.");

        Path normalizedFilePath = filePath.toAbsolutePath().normalize();
        ensureParentDirectory(normalizedFilePath);

        String temporaryPrefix = normalizedFilePath.getFileName().toString();

        if (temporaryPrefix.length() < 3){
            temporaryPrefix = (temporaryPrefix + "___").substring(0, 3);
        }

        Path temporaryFilePath = Files.createTempFile(
            normalizedFilePath.getParent(),
            temporaryPrefix,
            ".tmp"
        );

        try{
            try (Writer writer = Files.newBufferedWriter(
                temporaryFilePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )){
                gson.toJson(value, writer);
            }

            try{
                Files.move(
                    temporaryFilePath,
                    normalizedFilePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException exception){
                Files.move(
                    temporaryFilePath,
                    normalizedFilePath,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally{
            Files.deleteIfExists(temporaryFilePath);
        }
    }

    // Create the destination folder when needed so the first save can succeed.
    // Inputs: the normalized destination file path; output: none, its parent directory exists afterward.
    private void ensureParentDirectory (Path filePath) throws IOException{
        Path parentDirectory = filePath.getParent();

        if (parentDirectory != null){
            Files.createDirectories(parentDirectory);
        }
    }
}
