package com.github.coderodde.util.benchmark;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LinkedListBenchmark {

    private static final class Benchmark {

        private static final int ADD_FIRST_OPERATIONS = 100_000;
        
        private final Random randomJavaUtilLinkedList;
        private final Random randomJavaUtilArrayList;
        private final Random randomMyList;
        
        private final com.github.coderodde.util.LinkedList<Integer> roddeList = 
                new com.github.coderodde.util.LinkedList<>();
        
        private final LinkedList<Integer> linkedList = new LinkedList<>();
        private final ArrayList<Integer> arrayList = new ArrayList<>();
        
        
        private Benchmark(long seed) {
            this.randomJavaUtilLinkedList = new Random(seed);
            this.randomJavaUtilArrayList = new Random(seed);
            this.randomMyList = new Random(seed);
        }
        
        private enum BenchmarkChoice { WARMUP, BENCHMARK }
        
        private void listsEqual() {
            listsEqual(roddeList, linkedList, arrayList);
        }
        
        private static void listsEqual(List<Integer>... lists) {
            if (lists.length < 2) {
                throw new IllegalArgumentException("lists.length < 2");
            }

            for (int i = 0; i < lists.length - 1; i++) {
                if (lists[i].size() != lists[i + 1].size())  {
                    throw new IllegalStateException("Different size");
                }

                for (int j = 0; j < lists[0].size(); j++) {
                    if (!Objects.equals(lists[0].get(j), lists[i + 1].get(j))) {
                        throw new IllegalStateException("Different data");
                    }
                }
            }
        }
    
        private void warmup() {
            profile(BenchmarkChoice.WARMUP);
        }
        
        private void benchmark() {
            profile(BenchmarkChoice.BENCHMARK);
        }
        
        private void profile(BenchmarkChoice benchmarkChoice) {
            printTitle(benchmarkChoice);
            
            profileAddFirstRoddeList();
            profileAddFirstLinkedList();
            profileAddFirstArrayList();
            
            listsEqual();
            
            System.out.println();
            
            profileAddLastRoddeList();
            profileAddLastLinkedList();
            profileAddLastArrayList();
            
            listsEqual();
            
            System.out.println();
        }
        
        private long profileAddFirst(
                List<Integer> list, 
                int operations, 
                Random random) {
            
            long startMillis = System.currentTimeMillis();
            
            for (int i = 0; i < operations; i++) {
                list.add(0, random.nextInt());
            }
            
            long endMillis = System.currentTimeMillis();
            long durationMillis = endMillis - startMillis;
            
            System.out.println(
                    list.getClass().getName() + 
                            ".addFirst in (ms): " + 
                            durationMillis);
            
            return durationMillis;
        }
        
        private long profileAddLast(
                List<Integer> list, 
                int operations, 
                Random random) {
            
            long startMillis = System.currentTimeMillis();
            
            for (int i = 0; i < operations; i++) {
                list.add(list.size(), random.nextInt());
            }
            
            long endMillis = System.currentTimeMillis();
            long durationMillis = endMillis - startMillis;
            
            System.out.println(
                    list.getClass().getName() + 
                            ".addLast in (ms): " + 
                            durationMillis);
            
            return durationMillis;
        }
        
        private void profileAddFirstRoddeList() {
            profileAddFirst(roddeList, ADD_FIRST_OPERATIONS, randomMyList);
        }
        
        private void profileAddFirstLinkedList() {
            profileAddFirst(linkedList, 
                            ADD_FIRST_OPERATIONS, 
                            randomJavaUtilLinkedList);
        }
        
        private void profileAddFirstArrayList() {
            profileAddFirst(arrayList, 
                            ADD_FIRST_OPERATIONS, 
                            randomJavaUtilArrayList);
        }
        
        private void profileAddLastRoddeList() {
            profileAddLast(roddeList, ADD_FIRST_OPERATIONS, randomMyList);
        }
        
        private void profileAddLastLinkedList() {
            profileAddLast(linkedList, 
                            ADD_FIRST_OPERATIONS, 
                            randomJavaUtilLinkedList);
        }
        
        private void profileAddLastArrayList() {
            profileAddLast(arrayList, 
                           ADD_FIRST_OPERATIONS, 
                           randomJavaUtilArrayList);
        }
        
        private void printTitle(BenchmarkChoice benchmarkChoice) {
            switch (benchmarkChoice) {
                case WARMUP:
                    System.out.println("=== WARMUP RUN ===");
                    break;
                    
                case BENCHMARK:
                    System.out.println("=== BENCHMARK RUN ===");
                    break;
            }
        }
    }
    
    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        
        System.out.println("--- Seed = " + seed + " ---");
        System.out.println();
        
        Benchmark benchmark = new Benchmark(seed);
        benchmark.warmup();
        benchmark.benchmark();
    }
}
