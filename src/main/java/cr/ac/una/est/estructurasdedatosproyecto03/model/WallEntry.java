package cr.ac.una.est.estructurasdedatosproyecto03.model;

import java.time.Instant;

public class WallEntry{
    private String id;
    private String authorId;
    private String message;
    private Instant createdAt;

    // Prepare an empty post shell so Gson can restore fields from saved JSON.
    // Output: a post shell with empty values and the epoch timestamp.
    public WallEntry (){
        this.id = "";
        this.authorId = "";
        this.message = "";
        this.createdAt = Instant.EPOCH;
    }

    // Create a validated post so each saved entry has an author, content, and timestamp.
    // Inputs: post ID, author ID, message, and creation time; output: a populated post.
    public WallEntry (String id, String authorId, String message, Instant createdAt){
        if (id == null || id.trim().isEmpty()){
            throw new IllegalArgumentException("Post ID cannot be empty.");
        }
        if (authorId == null || authorId.trim().isEmpty()){
            throw new IllegalArgumentException("Author ID cannot be empty.");
        }
        if (message == null || message.trim().isEmpty()){
            throw new IllegalArgumentException("Post message cannot be empty.");
        }
        if (createdAt == null){
            throw new IllegalArgumentException("Creation time cannot be null.");
        }

        this.id = id.trim();
        this.authorId = authorId.trim();
        this.message = message.trim();
        this.createdAt = createdAt;
    }

    // Return the post ID so repositories and UI components can refer to this entry.
    // Output: this post's ID.
    public String getId (){
        return id;
    }

    // Return the author ID so the feed can check whether the post belongs in its results.
    //Output: the ID of the student who wrote this post.
    public String getAuthorId (){
        return authorId;
    }

    // Return the post text so the feed can display the student's message.
    // Output: the post message.
    public String getMessage (){
        return message;
    }

    // Return the creation time so the timeline can sort posts chronologically.
    // Output: the post creation timestamp.
    public Instant getCreatedAt (){
        return createdAt;
    }
}
