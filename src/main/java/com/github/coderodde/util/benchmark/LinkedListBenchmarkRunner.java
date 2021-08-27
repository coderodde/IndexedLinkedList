package com.github.coderodde.util.benchmark;

public class LinkedListBenchmarkRunner {

    public static void main(String[] args) {
        long seed = 1629956432123L; //System.currentTimeMillis();

        System.out.println("<<< LinkedList seed = " + seed + " >>>");
        System.out.println();

        LinkedListBenchmark benchmark = new LinkedListBenchmark(seed);

        benchmark.warmup();
        System.out.println();
        benchmark.benchmark();
    }
}
