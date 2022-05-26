package com.github.coderodde.util.benchmark;

/**
 * This class provides the entry point to the benchmark.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 * @since 1.6
 */
public final class LinkedListBenchmarkRunner {

    private static final String ALL_BENCHMARK_METHODS_FLAG = "--all";
    
    private LinkedListBenchmarkRunner() {
        
    }
    
    /**
     * Runs the benchmark.
     * 
     * @param args the command line arguments. Ignored. 
     */
    public static void main(String[] args) {
        boolean runAllBenchmarkMethods = false;
        
        if (args.length == 1 && args[0].equals(ALL_BENCHMARK_METHODS_FLAG)) {
            runAllBenchmarkMethods = true;
        }
        
        long seed = System.currentTimeMillis();

        System.out.println("<<< Benchmark seed = " + seed + " >>>");
        System.out.println();

        LinkedListBenchmark benchmark = 
                new LinkedListBenchmark(seed, runAllBenchmarkMethods);

        benchmark.warmup();
        System.out.println();
        benchmark.benchmark();
    }
}
