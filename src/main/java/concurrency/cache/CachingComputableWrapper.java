package concurrency.cache;

import concurrency.annotations.ThreadSafe;

import java.util.concurrent.*;

@ThreadSafe
public class CachingComputableWrapper<A, V> implements Computable<A, V> {
    private final Computable<A, V> computable;
    private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();

    public CachingComputableWrapper(Computable<A, V> computable) {
        this.computable = computable;
    }

    @Override
    public V compute(A arg) throws InterruptedException {
        Future<V> f = cache.get(arg);
        if (f == null) {
            FutureTask<V> ft = new FutureTask<>(() -> computable.compute(arg));
            f = cache.putIfAbsent(arg, ft);
            if (f == null) {
                f = ft;
                ft.run();
            }
        }
        try {
            return f.get();
        } catch (CancellationException e) {
            cache.remove(arg);
            throw new RuntimeException("Computation was cancelled");
        } catch (ExecutionException e) {
            throw launderThrowable(e.getCause());
        }
    }

    private RuntimeException launderThrowable(Throwable e) {
        if (e instanceof Error) {
            throw (Error) e;
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else { // checked exception
            return new RuntimeException(e);
        }
    }
}
