package cr.ac.una.est.estructurasdedatosproyecto03.service;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.AccountJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.security.CredentialHasher;
import cr.ac.una.est.estructurasdedatosproyecto03.session.AccessSession;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class AuthenticationService{
    private static final int minimumPasswordLength = 8;
    private final AccountJsonRepository accountRepository;
    private final CredentialHasher credentialHasher;
    private final AccessSession accessSession;

    // Connect account storage, password hashing, and session state.
    // Inputs: account repository, credential hasher, and access session; output: a ready authentication service.
    public AuthenticationService (
        AccountJsonRepository accountRepository,
        CredentialHasher credentialHasher,
        AccessSession accessSession
    ){
        if (accountRepository == null){
            throw new IllegalArgumentException("Account repository cannot be null.");
        }
        if (credentialHasher == null){
            throw new IllegalArgumentException("Credential hasher cannot be null.");
        }
        if (accessSession == null){
            throw new IllegalArgumentException("Access session cannot be null.");
        }

        this.accountRepository = accountRepository;
        this.credentialHasher = credentialHasher;
        this.accessSession = accessSession;
    }

    // Validate and save a new account so a student can later sign in with unique credentials.
    // Inputs: student's full name, chosen username, and plain-text password; output: the newly saved account.
    public StudentAccount register (String fullName, String username, String rawPassword) throws IOException{
        validateRegistrationData(fullName, username, rawPassword);

        String normalizedUsername = normalizeUsername(username);
        StudentAccount existingAccount = accountRepository.findByUsername(normalizedUsername);

        if (existingAccount != null){
            throw new IllegalArgumentException("That username is already in use.");
        }

        String accountId = UUID.randomUUID().toString();
        String passwordHash = credentialHasher.hashPassword(rawPassword);
        StudentAccount newAccount = new StudentAccount(
            accountId,
            fullName.trim(),
            normalizedUsername,
            passwordHash
        );

        accountRepository.add(newAccount);
        return newAccount;
    }

    // Start a session only after checking the submitted password against the stored hash.
    // Inputs: username and attempted password; output: the signed-in account, or null for invalid credentials.
    public StudentAccount authenticate (String username, String rawPassword) throws IOException{
        if (username == null || username.trim().isEmpty() || rawPassword == null || rawPassword.isEmpty()){
            return null;
        }

        String normalizedUsername = normalizeUsername(username);
        StudentAccount account = accountRepository.findByUsername(normalizedUsername);

        if (account == null || !credentialHasher.verifyPassword(rawPassword, account.getPasswordHash())){
            return null;
        }

        accessSession.startSession(account);
        return account;
    }

    // End the active session so the student must authenticate again before using signed-in features.
    // Output: none; the active session is cleared.
    public void signOut (){
        accessSession.endSession();
    }

    // Simple validation and exception handling method.
    // Inputs: full name, username, and plain-text password; output: none, invalid details raise an exception.
    private void validateRegistrationData (String fullName, String username, String rawPassword){
        if (fullName == null || fullName.trim().isEmpty()){
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (username == null || username.trim().isEmpty()){
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (rawPassword == null || rawPassword.length() < minimumPasswordLength){
            throw new IllegalArgumentException(
                "Password must contain at least " + minimumPasswordLength + " characters."
            );
        }
    }

    // Simple normalization method.
    // Inputs: a username; output: a trimmed lowercase username.
    private String normalizeUsername (String username){
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
