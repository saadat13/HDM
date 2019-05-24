package com.apps.saadat.hdm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyThreadPool {

    public static ExecutorService executor;
    public static ExecutorService singleExecutor;

    public static void execute(Runnable runnable){
        executor.submit(runnable);
    }

    public static void initExecutor(){
        executor = Executors.newCachedThreadPool();
    }

    public static void initSingleExecutor(){
        singleExecutor = Executors.newFixedThreadPool(1);
    }

    public static void shutDownExecutor(){
        executor.shutdown();
    }

    public static void shutDownSingleExecutor(){
        singleExecutor.shutdown();
    }

}
