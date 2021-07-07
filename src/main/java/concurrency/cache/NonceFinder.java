package concurrency.cache;

import concurrency.annotations.ThreadSafe;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ThreadSafe
public class NonceFinder {

    public byte[] findNonce(byte[] target) {
        MessageDigest digest = getSha256Digest();
        byte[] nonce = new byte[10];
        byte[] hash = digest.digest(nonce);
        while (!startsWith(hash, target)) {
            nextNonce(nonce);
            hash = digest.digest(nonce);
        }
        return nonce;
    }

    private MessageDigest getSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean startsWith(byte[] array, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) return false;
        }
        return true;
    }

    private void nextNonce(byte[] nonce) {
        for (int i = 0; i < nonce.length; i++) {
            if (nonce[i] < Byte.MAX_VALUE) {
                nonce[i]++;
                return;
            }
            nonce[i] = 0;
        }
        throw new IllegalStateException("Nonce overflowed");
    }
}