package com.myccnice.util.concurrent.memorizer;

public interface Computable<K, V>{

    V compute(K arg) throws InterruptedException;

}
