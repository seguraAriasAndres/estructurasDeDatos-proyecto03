package cr.ac.una.est.estructurasdedatosproyecto03.persistence;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class AccountJsonRepository{
    private static final Type accountListType = new TypeToken<List<StudentAccount>>(){}.getType();
    private final Path filePath;
    private final JsonDocumentStore documentStore;
    private final List<StudentAccount> accounts;

    // Load the saved accounts into a dynamic in-memory list so searches use startup data.
    // Inputs: account file path and JSON document store; output: a ready account repository.
    public AccountJsonRepository (Path filePath, JsonDocumentStore documentStore) throws IOException{
        this.filePath = Objects.requireNonNull(filePath, "Account file path cannot be null.");
        this.documentStore = Objects.requireNonNull(documentStore, "JSON document store cannot be null.");

        List<StudentAccount> savedAccounts = this.documentStore.readJson(
            this.filePath,
            accountListType,
            ArrayList::new
        );

        this.accounts = new ArrayList<>(savedAccounts);
        ensureUniqueAccounts(this.accounts);
    }

    // Return a copy of the in-memory account catalog so callers cannot change repository state directly.
    // Output: a fresh list containing the saved student accounts.
    public List<StudentAccount> loadAll () throws IOException{
        return new ArrayList<>(accounts);
    }

    // Find one account by ID so services can verify account references.
    // Inputs: an account ID; output: the matching account or null when it is not found.
    public StudentAccount findById (String accountId) throws IOException{
        if (accountId == null || accountId.trim().isEmpty()){
            return null;
        }

        String normalizedAccountId = accountId.trim();

        for (StudentAccount account : accounts){
            if (account != null && normalizedAccountId.equals(account.getId())){
                return account;
            }
        }

        return null;
    }

    // Find a username without case sensitivity so capitalization does not create separate accounts.
    // Inputs: a username; output: the matching account or null when it is not found.
    public StudentAccount findByUsername (String username) throws IOException{
        if (username == null || username.trim().isEmpty()){
            return null;
        }

        String normalizedUsername = username.trim();

        for (StudentAccount account : accounts){
            if (account != null
                && account.getUsername() != null
                && normalizedUsername.equalsIgnoreCase(account.getUsername().trim())){
                return account;
            }
        }

        return null;
    }

    // Add one account while checking the existing records so duplicate credentials are rejected.
    // Inputs: the account to add; output: none, the updated collection is saved to JSON and memory.
    public void add (StudentAccount account) throws IOException{
        List<StudentAccount> updatedAccounts = loadAll();
        updatedAccounts.add(account);
        saveAll(updatedAccounts);
    }

    // Replace the account catalog in JSON and memory after validating the complete collection.
    // Inputs: all accounts to save; output: none, the validated collection replaces the stored data.
    public void saveAll (Collection<StudentAccount> accounts) throws IOException{
        Objects.requireNonNull(accounts, "Account collection cannot be null.");

        List<StudentAccount> accountList = new ArrayList<>(accounts);
        ensureUniqueAccounts(accountList);
        documentStore.writeJson(filePath, accountList);

        this.accounts.clear();
        this.accounts.addAll(accountList);
    }

    // Reject invalid records and duplicate IDs or usernames before writing account data.
    // Inputs: account records to inspect; output: none, invalid collections raise an exception.
    private void ensureUniqueAccounts (Collection<StudentAccount> accounts){
        Set<String> accountIds = new HashSet<>();
        Set<String> usernames = new HashSet<>();

        for (StudentAccount account : accounts){
            if (account == null){
                throw new IllegalArgumentException("Account collection cannot contain null records.");
            }
            if (account.getId() == null || account.getId().trim().isEmpty()){
                throw new IllegalArgumentException("Account ID cannot be empty.");
            }
            if (account.getFullName() == null || account.getFullName().trim().isEmpty()){
                throw new IllegalArgumentException("Account full name cannot be empty.");
            }
            if (account.getUsername() == null || account.getUsername().trim().isEmpty()){
                throw new IllegalArgumentException("Account username cannot be empty.");
            }
            if (account.getPasswordHash() == null || account.getPasswordHash().trim().isEmpty()){
                throw new IllegalArgumentException("Account password hash cannot be empty.");
            }

            String normalizedId = account.getId().trim();
            String normalizedUsername = account.getUsername().trim().toLowerCase(Locale.ROOT);

            if (!accountIds.add(normalizedId)){
                throw new IllegalArgumentException("Account IDs must be unique.");
            }
            if (!usernames.add(normalizedUsername)){
                throw new IllegalArgumentException("Account usernames must be unique.");
            }
        }
    }
}
