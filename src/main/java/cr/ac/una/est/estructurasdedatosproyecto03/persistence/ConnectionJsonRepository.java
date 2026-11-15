package cr.ac.una.est.estructurasdedatosproyecto03.persistence;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentConnection;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConnectionJsonRepository{
    private static final Type connectionListType = new TypeToken<List<StudentConnection>>(){}.getType();
    private final Path filePath;
    private final JsonDocumentStore documentStore;

    // Connect connection data operations to one JSON file and the shared document store.
    // Inputs: connection file path and JSON document store; output: a ready connection repository.
    public ConnectionJsonRepository (Path filePath, JsonDocumentStore documentStore){
        this.filePath = Objects.requireNonNull(filePath, "Connection file path cannot be null.");
        this.documentStore = Objects.requireNonNull(documentStore, "JSON document store cannot be null.");
    }

    // Load all saved connections so startup can rebuild the in-memory graph.
    // Output: a fresh list containing the saved student connections.
    public List<StudentConnection> loadAll () throws IOException{
        List<StudentConnection> savedConnections = documentStore.readJson(
            filePath,
            connectionListType,
            ArrayList::new
        );

        return new ArrayList<>(savedConnections);
    }

    // Add one connection while checking existing records so the same mutual link is not stored twice.
    // Inputs: the connection to add; output: none, the updated collection is saved to JSON.
    public void add (StudentConnection connection) throws IOException{
        List<StudentConnection> connections = loadAll();
        connections.add(connection);
        saveAll(connections);
    }

    // Replace all saved connections so startup setup can write a complete initial data set.
    // Inputs: all connections to save; output: none, the validated collection replaces the JSON data.
    public void saveAll (Collection<StudentConnection> connections) throws IOException{
        Objects.requireNonNull(connections, "Connection collection cannot be null.");

        List<StudentConnection> connectionList = new ArrayList<>(connections);
        ensureUniqueConnections(connectionList);
        documentStore.writeJson(filePath, connectionList);
    }

    // Reject invalid, self, or duplicate links before writing the connection data.
    // Inputs: connection records to inspect; output: none, invalid collections raise an exception.
    private void ensureUniqueConnections (Collection<StudentConnection> connections){
        Set<String> connectionKeys = new HashSet<>();

        for (StudentConnection connection : connections){
            if (connection == null){
                throw new IllegalArgumentException("Connection collection cannot contain null records.");
            }

            String firstAccountId = connection.getFirstAccountId();
            String secondAccountId = connection.getSecondAccountId();

            if (firstAccountId == null || firstAccountId.trim().isEmpty()){
                throw new IllegalArgumentException("First account ID cannot be empty.");
            }
            if (secondAccountId == null || secondAccountId.trim().isEmpty()){
                throw new IllegalArgumentException("Second account ID cannot be empty.");
            }
            if (firstAccountId.trim().equals(secondAccountId.trim())){
                throw new IllegalArgumentException("An account cannot connect to itself.");
            }
            if (connection.getCreatedAt() == null){
                throw new IllegalArgumentException("Connection creation time cannot be null.");
            }

            String normalizedFirstId = firstAccountId.trim();
            String normalizedSecondId = secondAccountId.trim();
            String connectionKey;

            if (normalizedFirstId.compareTo(normalizedSecondId) <= 0){
                connectionKey = normalizedFirstId + "\u0000" + normalizedSecondId;
            } else{
                connectionKey = normalizedSecondId + "\u0000" + normalizedFirstId;
            }

            if (!connectionKeys.add(connectionKey)){
                throw new IllegalArgumentException("Student connections must be unique.");
            }
        }
    }
}
