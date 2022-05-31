package com.github.coderodde.util.benchmark;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides the entry point to the benchmark.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 * @since 1.6
 */
public final class LinkedListBenchmarkRunner {

    private static final String ALL_BENCHMARK_METHODS_FLAG = "--all";
    private static final String BENCHMARK_SUBLIST_CLEAR_FLAG = "--clear";
    private static final String BENCHMARK_REMOVE_ALL = "--remove-all";
    private static final String BENCHMARK_SORT = "--sort";
    private static final String HELP = "-h";
    
    private LinkedListBenchmarkRunner() {
        
    }
    
    /**
     * Runs the benchmark.
     * 
     * @param args the command line arguments. Ignored. 
     */
    public static void main(String[] args) {
        boolean runSubListClear = false;
        boolean runRemoveAll = false;
        boolean runSort = false;
        
        Set<String> commandLineArgumentSet = new HashSet<>(3);
        
        for (String arg : args) {
            commandLineArgumentSet.add(arg);
        }
        
        if (commandLineArgumentSet.contains(HELP)) {
            printHelp();
            return;
        }
        
        if (commandLineArgumentSet.contains(ALL_BENCHMARK_METHODS_FLAG)) {
            runSubListClear = true;
            runRemoveAll = true;
            runSort = true;
        } else {
            if (commandLineArgumentSet.contains(BENCHMARK_SUBLIST_CLEAR_FLAG)) {
                runSubListClear = true;
            }
            
            if (commandLineArgumentSet.contains(BENCHMARK_REMOVE_ALL)) {
                runRemoveAll = true;
            }
            
            if (commandLineArgumentSet.contains(BENCHMARK_SORT)) {
                runSort = true;
            }
        }
        
        long seed = System.currentTimeMillis();

        System.out.println("<<< Benchmark seed = " + seed + " >>>");
        System.out.println();
        
        System.out.println("<<< Flags >>>");
        System.out.println("runSubListClear: " + runSubListClear);
        System.out.println("runRemoveAll   : " + runRemoveAll);
        System.out.println("runSort        : " + runSort);
        System.out.println();

        LinkedListBenchmark benchmark = 
                new LinkedListBenchmark(seed, 
                                        runSubListClear,
                                        runRemoveAll,
                                        runSort);

        benchmark.warmup();
        System.out.println();
        benchmark.benchmark();
    }
    
    private static void printHelp() {
        String jarFileName = getJarFileName();
        
        if (jarFileName == null) {
            System.out.println("Could not read the name of the JAR file.");
            return;
        }
        
        String text = 
                "Usage: java -jar " 
                + jarFileName
                + " ["  
                + HELP
                + " | " 
                + ALL_BENCHMARK_METHODS_FLAG 
                + " | " 
                + BENCHMARK_SUBLIST_CLEAR_FLAG
                + " | " 
                + BENCHMARK_REMOVE_ALL + "]\n";
        
        System.out.println(text);
    }
    
    private static String getJarFileName() {
        try {
            String jarPath =
                    LinkedListBenchmarkRunner
                            .class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
                            .getPath();
            
            return jarPath.substring(jarPath.lastIndexOf("/") + 1);
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}
