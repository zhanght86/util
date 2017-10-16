package com.myccnice.util.concurrent.memorizer;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @category 最终存储器
 *
 * create in 2017年7月20日
 * @author wangpeng
 */
public class Memorizer<A, V> implements Computable<A, V>{

    private final ConcurrentHashMap<A, Future<V>> cache = new ConcurrentHashMap<>();
    private final Computable<A, V> c;

    public Memorizer(Computable<A, V> c) {
        this.c = c;
    }

    @Override
    public V compute(final A arg) throws InterruptedException {
        while (true) {
            Future<V> f = cache.get(arg);
            if (f == null) {
                Callable<V> eval = new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        return c.compute(arg);
                    }
                };
                FutureTask<V> ft = new FutureTask<>(eval);
                f = cache.putIfAbsent(arg, ft);
                // 缓存中没有相应的缓存数据
                if (f == null) {
                    f = ft;
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch(CancellationException e) {
                cache.remove(arg, f);
            } catch (ExecutionException e) {
                throw new InterruptedException(e.getMessage());
            }
        }
    }

}
