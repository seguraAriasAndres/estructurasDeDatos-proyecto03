package cr.ac.una.est.estructurasdedatosproyecto03.model;

public class StudentAccount{
    private String id;
    private String fullName;
    private String username;
    private String passwordHash;
    private String careerId;
    private String campusId;

    // Prepare an empty account shell so Gson can restore fields from saved JSON.
    // Inputs: none; output: an account shell with empty field values.
    public StudentAccount (){
        this.id = "";
        this.fullName = "";
        this.username = "";
        this.passwordHash = "";
        this.careerId = "";
        this.campusId = "";
    }

    // Create a validated student account so services can store complete account data.
    // Inputs: account ID, name, username, password hash, career ID, and campus ID; output: a populated account.
    public StudentAccount (
        String id,
        String fullName,
        String username,
        String passwordHash,
        String careerId,
        String campusId
    ){
        if (id == null || id.trim().isEmpty()){
            throw new IllegalArgumentException("Account ID cannot be empty.");
        }
        if (fullName == null || fullName.trim().isEmpty()){
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (username == null || username.trim().isEmpty()){
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()){
            throw new IllegalArgumentException("Password hash cannot be empty.");
        }
        if (careerId == null || careerId.trim().isEmpty()){
            throw new IllegalArgumentException("Career ID cannot be empty.");
        }
        if (campusId == null || campusId.trim().isEmpty()){
            throw new IllegalArgumentException("Campus ID cannot be empty.");
        }

        this.id = id.trim();
        this.fullName = fullName.trim();
        this.username = username.trim();
        this.passwordHash = passwordHash.trim();
        this.careerId = careerId.trim();
        this.campusId = campusId.trim();
    }

    // Return the account ID so other records can refer to this student reliably.
    // Output: this account's ID.
    public String getId (){
        return id;
    }

    // Return the student's full name so the interface can identify the account.
    // Output: this student's full name.
    public String getFullName (){
        return fullName;
    }

    // Return the username so authentication and search can identify the account.
    // Output: this account's username.
    public String getUsername (){
        return username;
    }

    // Return the stored password hash so authentication can verify a sign-in attempt.
    // Output: the encoded password hash, never the original password.
    public String getPasswordHash (){
        return passwordHash;
    }

    // Return the career ID so the student's selected program can be stored and displayed.
    // Output: this student's career ID, or an empty value for an older account.
    public String getCareerId (){
        return careerId;
    }

    // Return the campus ID so the student's selected location can be stored and displayed.
    // Output: this student's campus ID, or an empty value for an older account.
    public String getCampusId (){
        return campusId;
    }
}
