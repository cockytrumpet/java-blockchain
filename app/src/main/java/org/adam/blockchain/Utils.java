package org.adam.blockchain;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * PKI and other helpers.
 */
public class Utils {
    /**
     * Computes the SHA-256 hash of the given byte array and returns it as a
     * hexadecimal string.
     *
     * @param input the byte array to hash
     * @return the SHA-256 hash represented as a hexadecimal string
     */
    public static String hashFromBytes(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input);
            StringBuilder hexString = new StringBuilder();
            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.err.println("Error while hashing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies the digital signature of a BlockEntry and returns a boolean result.
     *
     * @param BlockEntry a BlockEntry with a signature
     * @return boolean result of verification
     */
    public static boolean verifySignature(BlockEntry blockEntry) {
        if (blockEntry instanceof EmptyBlockEntry) {
            return true;
        }

        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(blockEntry.publicKey());
            sig.update(blockEntry.toString().getBytes());
            return sig.verify(blockEntry.getSignature());
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            System.out.println(blockEntry.getClass().getSimpleName() + ".isValid failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate and return KeyPair
     *
     * @return KeyPair
     */
    public static KeyPair generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(512);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Util.generateKeys failed");
            return null;
        }
    }

    /**
     * Sign BlockEntry
     *
     * @param BlockEntry to sign
     * @param PrivateKey used to sign
     * @return BlockEntry with signature populated
     */
    public static BlockEntry signBlockEntry(BlockEntry blockEntry, PrivateKey privateKey) {
        try {
            Signature rsa = Signature.getInstance("SHA1withRSA");
            rsa.initSign(privateKey);
            rsa.update(blockEntry.toString().getBytes());
            blockEntry.setSignature(rsa.sign());
            return blockEntry;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println("Util.signBlockEntry failed");
            return blockEntry;
        }
    }

    /**
     * Escape codes for terminal output.
     */
    class ColoredOutput {
        public static final String RESET = "\u001B[0m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
    }

}
