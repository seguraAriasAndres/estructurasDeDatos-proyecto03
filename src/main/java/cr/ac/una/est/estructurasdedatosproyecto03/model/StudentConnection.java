package cr.ac.una.est.estructurasdedatosproyecto03.model;

import java.time.Instant;

public class StudentConnection{
    private String firstAccountId;
    private String secondAccountId;
    private Instant createdAt;

    // Prepare an empty connection shell so Gson can restore fields from the saved JSON.
    // Output: a connection shell with empty IDs and the epoch timestamp.
    public StudentConnection (){
        this.firstAccountId = "";
        this.secondAccountId = "";
        this.createdAt = Instant.EPOCH;
    }

    // Creates a normalized friendship link so both account orders represent the same connection.
    // Inputs: two distinct account IDs and creation time; output: a validated connection.
    public StudentConnection (String firstAccountId, String secondAccountId, Instant createdAt){
        if (firstAccountId == null || firstAccountId.trim().isEmpty()){
            throw new IllegalArgumentException("First account ID cannot be empty.");
        }
        if (secondAccountId == null || secondAccountId.trim().isEmpty()){
            throw new IllegalArgumentException("Second account ID cannot be empty.");
        }
        if (firstAccountId.trim().equals(secondAccountId.trim())){
            throw new IllegalArgumentException("An account cannot connect to itself.");
        }
        if (createdAt == null){
            throw new IllegalArgumentException("Creation time cannot be null.");
        }

        String normalizedFirstId = firstAccountId.trim();
        String normalizedSecondId = secondAccountId.trim();

        if (normalizedFirstId.compareTo(normalizedSecondId) <= 0){
            this.firstAccountId = normalizedFirstId;
            this.secondAccountId = normalizedSecondId;
        } else{
            this.firstAccountId = normalizedSecondId;
            this.secondAccountId = normalizedFirstId;
        }

        this.createdAt = createdAt;
    }

    // Return the first normalized account ID so connections have a consistent stored order.
    // Output: the first account ID.
    public String getFirstAccountId (){
        return firstAccountId;
    }

    // Return the second normalized account ID so connections have a consistent stored order.
    // Output: the second account ID.
    public String getSecondAccountId (){
        return secondAccountId;
    }

    // Return the creation time so the application can display or sort connection records.
    // Inputs: none; output: the connection creation timestamp.
    public Instant getCreatedAt (){
        return createdAt;
    }

    // Find the opposite endpoint so graph traversal can move from one student to the other.
    // Inputs: an account ID in this connection; output: the connected account ID.
    public String getOtherAccountId (String accountId){
        if (accountId == null){
            throw new IllegalArgumentException("Account ID cannot be null.");
        }

        String normalizedAccountId = accountId.trim();

        if (firstAccountId.equals(normalizedAccountId)){
            return secondAccountId;
        }
        if (secondAccountId.equals(normalizedAccountId)){
            return firstAccountId;
        }

        throw new IllegalArgumentException("The account is not part of this connection.");
    }
}
