package concurrency.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NonceFinder implements Computable<String, byte[]> {
    private final MessageDigest digest = getSha256Digest();

    private MessageDigest getSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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

    private boolean startsWith(byte[] array, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) return false;
        }
        return true;
    }

    public byte[] findNonce(byte[] target) {
        byte[] nonce = new byte[10];
        byte[] hash = digest.digest(nonce);
        while (!startsWith(hash, target)) {
            nextNonce(nonce);
            hash = digest.digest(nonce);
        }
        return nonce;
    }

    @Override
    public byte[] compute(String arg) {
        if (arg.length() > 32) {
            throw new IllegalArgumentException("Target must be no longer than 32 bytes");
        }
        return findNonce(arg.getBytes(StandardCharsets.UTF_8));
    }
}
