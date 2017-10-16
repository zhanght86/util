package com.myccnice.util.concurrent.memorizer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class MemorizerTest {

    @Test
    public void test() {
        long t1 = System.currentTimeMillis();
        final Memorizer<Integer, Integer> memorizer = new Memorizer<>(new MyComputer());
        ExecutorService es = Executors.newCachedThreadPool();
        final CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(memorizer.compute(10));
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        es.shutdown();
        System.out.println(System.currentTimeMillis() -t1);
    }
}
