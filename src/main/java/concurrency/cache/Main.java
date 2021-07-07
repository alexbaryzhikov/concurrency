package concurrency.cache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Main {
    private static final String[] targets = {
            "aaa", "bbb", "ccc", "foo", "bar", "baz", "qux", "mop", "foo", "tip",
            "aaa", "toc", "zzz", "arc", "abc", "tic", "toc", "ccc", "sup", "ups",
            "tic", "ohh", "zzz", "arc", "abc", "ccc", "aaa", "baz", "two", "one",
            "ten", "mop", "tic", "foo", "aaa", "qux", "bbb", "ccc", "ohh", "ten",
    };

    NonceFinder nonceFinder = new NonceFinder();
    Computable<String, byte[]> nonceComputable = s -> nonceFinder.findNonce(s.getBytes(StandardCharsets.UTF_8));
    Computable<String, byte[]> cachingNonceComputable = new CachingComputableWrapper<>(nonceComputable);

    private void runComputations() {
        int nCores = Runtime.getRuntime().availableProcessors();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(nCores);

        for (int i = 0; i < nCores; i++) {
            String target = targets[i];
            new Thread(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    return;
                }
                byte[] nonce = cachingNonceComputable.compute(target);
                System.out.println("[" + Thread.currentThread().getName() + "] " + target + " " + Arrays.toString(nonce));
                finishLatch.countDown();
            }).start();
        }

        long t0 = System.currentTimeMillis();
        startLatch.countDown();
        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            return;
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Time elapsed: " + Duration.ofMillis(t1 - t0));
    }

    public static void main(String[] args) {
        new Main().runComputations();
    }
}
