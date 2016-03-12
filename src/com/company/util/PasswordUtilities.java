package com.company.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtilities {

    public static boolean isPasswordCorrect(String salt, String passwordHash, String passwordToCheck) {
        String hashedPasswordToCheck = getPasswordHash(passwordToCheck, salt);
        return passwordHash.equals(hashedPasswordToCheck);
    }

    public static String getRandomSalt() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(salt);
    }

    public static String getPasswordHash(String passwordToHash, String salt) {
        String generatedPassword = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt.getBytes());
            byte[] bytes = messageDigest.digest(passwordToHash.getBytes());
            Base64.Encoder encoder = Base64.getEncoder();
            generatedPassword = encoder.encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }
}
