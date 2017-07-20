package com.myccnice.util.concurrent.memorizer;

import java.util.Random;

public class MyComputer implements Computable<Integer, Integer>{

    private static final Random RANDOM = new Random();

    @Override
    public Integer compute(Integer arg) throws InterruptedException {
        int next = RANDOM.nextInt(10000);
        System.out.println(next);
        Thread.sleep(next);
        return arg;
    }

}
