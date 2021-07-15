package concurrency.cache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final String[] args = {
            "aaa", "bbb", "ccc", "foo", "bar", "baz", "qux", "mop", "abc", "tip",
            "dfa", "res", "vbf", "rew", "jju", "vdx", "xxx", "hjt", "ddw", "mmj",
    };

    NonceFinder nonceFinder = new NonceFinder();
    Computable<String, byte[]> nonceComputable = s -> nonceFinder.findNonce(s.getBytes(StandardCharsets.UTF_8));
    Computable<String, byte[]> cachingNonceComputable = new CachingComputableWrapper<>(nonceComputable);

    private void runComputations(int workers) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(workers);
        startWorkers(workers, startLatch, finishLatch, () -> {
            for (int j = 0; j < 5; j++) {
                String target = args[ThreadLocalRandom.current().nextInt(args.length)];
                byte[] nonce = cachingNonceComputable.compute(target);
                System.out.println("[" + Thread.currentThread().getName() + "] " + target + " " + Arrays.toString(nonce));
            }
        });
        long t0 = System.currentTimeMillis();
        startLatch.countDown();
        finishLatch.await();
        long t1 = System.currentTimeMillis();
        System.out.println("Time elapsed: " + Duration.ofMillis(t1 - t0));
    }

    private void startWorkers(int workers, CountDownLatch startLatch, CountDownLatch finishLatch, Task task) {
        for (int i = 0; i < workers; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    task.run();
                } catch (Throwable e) {
                    System.err.println("[" + Thread.currentThread().getName() + "] " + e);
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        try {
            new Main().runComputations(Runtime.getRuntime().availableProcessors());
        } catch (InterruptedException ignored) {
        }
    }

    private interface Task {
        void run() throws InterruptedException;
    }
}
