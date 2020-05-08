package com.github.freeacs.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncHandler {
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void execute(Runnable runnable){
        executor.execute(runnable);

    }
}
