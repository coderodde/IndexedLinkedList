package com.github.coderodde.util.benchmark;

public class LinkedListBenchmarkRunner {
    
    public static void main(String[] args) {
//        new LinkedListDebugger().run();
//        System.exit(0);
        long seed = 1619010671495L; //System.currentTimeMillis();
        seed = System.currentTimeMillis();
        
        System.out.println("--- Seed = " + seed + " ---");
        System.out.println();
        
        LinkedListBenchmark benchmark = new LinkedListBenchmark(seed);
        
        benchmark.warmup();
        System.out.println();
        benchmark.benchmark();
    }
}
