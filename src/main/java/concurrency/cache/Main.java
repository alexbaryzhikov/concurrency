package concurrency.cache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final String[] targets = {
            "aaa", "bbb", "ccc", "foo", "bar", "baz", "qux", "mop", "abc", "tip",
            "dfa", "res", "vbf", "rew", "jju", "vdx", "xxx", "hjt", "ddw", "mmj",
    };

    NonceFinder nonceFinder = new NonceFinder();
    Computable<String, byte[]> nonceComputable = s -> nonceFinder.findNonce(s.getBytes(StandardCharsets.UTF_8));
    Computable<String, byte[]> cachingNonceComputable = new CachingComputableWrapper<>(nonceComputable);

    private void runComputations(int nThreads) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(nThreads);
        for (int i = 0; i < nThreads; i++) {
            new Thread(() -> {
                try {
                    computeNonces(startLatch, finishLatch);
                } catch (InterruptedException ignored) {
                } catch (Throwable e) {
                    System.err.println("[" + Thread.currentThread().getName() + "] " + e);
                }
            }).start();
        }
        long t0 = System.currentTimeMillis();
        startLatch.countDown();
        finishLatch.await();
        long t1 = System.currentTimeMillis();
        System.out.println("Time elapsed: " + Duration.ofMillis(t1 - t0));
    }

    private void computeNonces(CountDownLatch startLatch, CountDownLatch finishLatch) throws InterruptedException {
        startLatch.await();
        for (int j = 0; j < 5; j++) {
            String target = targets[ThreadLocalRandom.current().nextInt(targets.length)];
            byte[] nonce = cachingNonceComputable.compute(target);
            System.out.println("[" + Thread.currentThread().getName() + "] " + target + " " + Arrays.toString(nonce));
        }
        finishLatch.countDown();
    }

    public static void main(String[] args) {
        try {
            new Main().runComputations(Runtime.getRuntime().availableProcessors());
        } catch (InterruptedException ignored) {
        }
    }
}
