package cr.ac.una.est.estructurasdedatosproyecto03.persistence;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.estructurasdedatosproyecto03.model.WallEntry;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WallEntryJsonRepository{
    private static final Type wallEntryListType = new TypeToken<List<WallEntry>>(){}.getType();
    private final Path filePath;
    private final JsonDocumentStore documentStore;

    // Connect post data operations to one JSON file and the shared document store.
    // Inputs: post file path and JSON document store; output: a ready post repository.
    public WallEntryJsonRepository (Path filePath, JsonDocumentStore documentStore){
        this.filePath = Objects.requireNonNull(filePath, "Post file path cannot be null.");
        this.documentStore = Objects.requireNonNull(documentStore, "JSON document store cannot be null.");
    }

    // Load all saved posts so the timeline service can build feeds in memory.
    // Output: a fresh list containing the saved wall entries.
    public List<WallEntry> loadAll () throws IOException{
        List<WallEntry> savedEntries = documentStore.readJson(
            filePath,
            wallEntryListType,
            ArrayList::new
        );

        return new ArrayList<>(savedEntries);
    }

    // Add one post while checking existing records so its ID remains unique.
    // Inputs: the post to add; output: none, the updated collection is saved to JSON.
    public void add (WallEntry wallEntry) throws IOException{
        List<WallEntry> wallEntries = loadAll();
        wallEntries.add(wallEntry);
        saveAll(wallEntries);
    }

    // Replace all saved posts so startup setup can write a complete initial data set.
    // Inputs: all wall entries to save; output: none, the validated collection replaces the JSON data.
    public void saveAll (Collection<WallEntry> wallEntries) throws IOException{
        Objects.requireNonNull(wallEntries, "Post collection cannot be null.");

        List<WallEntry> wallEntryList = new ArrayList<>(wallEntries);
        ensureUniqueWallEntries(wallEntryList);
        documentStore.writeJson(filePath, wallEntryList);
    }

    // Reject invalid posts and duplicate IDs before writing post data.
    // Inputs: wall entries to inspect; output: none, invalid collections raise an exception.
    private void ensureUniqueWallEntries (Collection<WallEntry> wallEntries){
        Set<String> wallEntryIds = new HashSet<>();

        for (WallEntry wallEntry : wallEntries){
            if (wallEntry == null){
                throw new IllegalArgumentException("Post collection cannot contain null records.");
            }
            if (wallEntry.getId() == null || wallEntry.getId().trim().isEmpty()){
                throw new IllegalArgumentException("Post ID cannot be empty.");
            }
            if (wallEntry.getAuthorId() == null || wallEntry.getAuthorId().trim().isEmpty()){
                throw new IllegalArgumentException("Post author ID cannot be empty.");
            }
            if (wallEntry.getMessage() == null || wallEntry.getMessage().trim().isEmpty()){
                throw new IllegalArgumentException("Post message cannot be empty.");
            }
            if (wallEntry.getCreatedAt() == null){
                throw new IllegalArgumentException("Post creation time cannot be null.");
            }

            if (!wallEntryIds.add(wallEntry.getId().trim())){
                throw new IllegalArgumentException("Post IDs must be unique.");
            }
        }
    }
}
