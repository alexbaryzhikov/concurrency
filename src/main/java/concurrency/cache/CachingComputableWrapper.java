package concurrency.cache;

import concurrency.annotations.ThreadSafe;

import java.util.HashMap;

@ThreadSafe
public class CachingComputableWrapper<A, V> implements Computable<A, V> {
    private final Computable<A, V> computable;
    private final HashMap<A, V> cache = new HashMap<>();

    public CachingComputableWrapper(Computable<A, V> computable) {
        this.computable = computable;
    }

    @Override
    synchronized public V compute(A arg) {
        if (!cache.containsKey(arg)) {
            cache.put(arg, computable.compute(arg));
        }
        return cache.get(arg);
    }
}
