package cr.ac.una.est.estructurasdedatosproyecto03.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class CredentialHasher{
    private static final String algorithmName = "PBKDF2WithHmacSHA256";
    private static final String formatName = "pbkdf2-sha256";
    private static final int hashIterations = 262144;
    private static final int maximumAllowedIterations = 999999;
    private static final int saltLength = 16;
    private static final int derivedKeyLength = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    // Generate a unique random salt for each account password.
    // Output: a credential hasher ready to create and verify password hashes.
    public CredentialHasher (){}

    // Encode a salted password hash.
    // Inputs: the student's plain-text password; output: an encoded algorithm, iteration, salt, and hash string.
    public String hashPassword (String rawPassword){
        if (rawPassword == null || rawPassword.isEmpty()){
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        byte[] salt = new byte[saltLength];
        secureRandom.nextBytes(salt);

        try{
            byte[] derivedKey = deriveKey(rawPassword, salt, hashIterations);

            return formatName
                + "$" + hashIterations
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derivedKey);
        } catch (GeneralSecurityException exception){
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        }
    }

    // Recreate the stored hash so authentication can check a password without saving it.
    // Inputs: the attempted password and its stored encoded hash; output: true when they match, otherwise false.
    public boolean verifyPassword (String rawPassword, String encodedHash){
        if (rawPassword == null || rawPassword.isEmpty() || encodedHash == null){
            return false;
        }

        String[] hashParts = encodedHash.split("\\$", -1);

        if (hashParts.length != 4 || !formatName.equals(hashParts[0])){
            return false;
        }

        int storedIterations;

        try{
            storedIterations = Integer.parseInt(hashParts[1]);
        } catch (NumberFormatException exception){
            return false;
        }

        if (storedIterations <= 0 || storedIterations > maximumAllowedIterations){
            return false;
        }

        byte[] salt;
        byte[] expectedKey;

        try{
            salt = Base64.getDecoder().decode(hashParts[2]);
            expectedKey = Base64.getDecoder().decode(hashParts[3]);
        } catch (IllegalArgumentException exception){
            return false;
        }

        if (salt.length != saltLength || expectedKey.length != derivedKeyLength){
            return false;
        }

        try{
            byte[] actualKey = deriveKey(rawPassword, salt, storedIterations);
            return MessageDigest.isEqual(expectedKey, actualKey);
        } catch (GeneralSecurityException exception){
            throw new IllegalStateException("Password verification is unavailable.", exception);
        }
    }

    // Derive the password key with the requested salt and work factor for hashing or verification.
    // Inputs: password, salt bytes, and iteration count; output: the derived key bytes.
    private byte[] deriveKey (String rawPassword, byte[] salt, int iterations) throws GeneralSecurityException{
        char[] passwordCharacters = rawPassword.toCharArray();
        PBEKeySpec keySpecification = new PBEKeySpec(
            passwordCharacters,
            salt,
            iterations,
            derivedKeyLength * 8
        );

        try{
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithmName);
            return keyFactory.generateSecret(keySpecification).getEncoded();
        } finally{
            keySpecification.clearPassword();
            Arrays.fill(passwordCharacters, '\0');
        }
    }
}
