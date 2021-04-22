package com.github.coderodde.util.benchmark;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LinkedListBenchmark {

    private static final class Benchmark {

        private static final int ADD_FIRST_OPERATIONS = 100_000;
        private static final int ADD_AT_OPERATIONS = 10_000;
        private static final int ADD_COLLECTION_AT_OPERATIONS = 5_000;
        private static final int ADD_LAST_COLLECTION_OPERATIONS = 10_000;
        
        private static final int MAXIMUM_COLLECTION_SIZE = 2_000;
        
        private final Random randomJavaUtilLinkedList;
        private final Random randomJavaUtilArrayList;
        private final Random randomMyList;
        
        private final com.github.coderodde.util.LinkedList<Integer> roddeList = 
                new com.github.coderodde.util.LinkedList<>();
        
        private final LinkedList<Integer> linkedList = new LinkedList<>();
        private final ArrayList<Integer> arrayList = new ArrayList<>();
        
        private long totalMillisRoddeList  = 0L;
        private long totalMillisLinkedList = 0L;
        private long totalMillisArrayList  = 0L;
        
        private Benchmark(long seed) {
            this.randomJavaUtilLinkedList = new Random(seed);
            this.randomJavaUtilArrayList = new Random(seed);
            this.randomMyList = new Random(seed);
        }
    
        private static List<Integer> createRandomCollection(Random random) {
            int size = 1 + random.nextInt(MAXIMUM_COLLECTION_SIZE);

            List<Integer> list = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                list.add(random.nextInt());
            }

            return list;
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
        
        private void clearAllLists() {
            roddeList.clear();
            linkedList.clear();
            arrayList.clear();
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
            
            profileAddIndexRoddeList();
            profileAddIndexLinkedList();
            profileAddIndexArrayList();
            
            System.out.println();
            
            profileAddCollectionRoddeList();
            profileAddCollectionLinkedList();
            profileAddCollectionArrayList();
            
            System.out.println();
            
            profileAppendCollectionRoddeList();
            profileAppendCollectionLinkedList();
            profileAppendCollectionArrayList();
            
            System.out.println();
            
            printTotalDurations();
            
            clearAllLists();
        }
        
        private void printTotalDurations() {
            System.out.println("--- Total time elapsed ---");
            System.out.println(
                    roddeList.getClass().getName() + 
                            " in (ms): " + 
                            totalMillisRoddeList);
            
            System.out.println(
                    linkedList.getClass().getName() + 
                            " in (ms): " + 
                            totalMillisLinkedList);
            
            System.out.println(
                    arrayList.getClass().getName() + 
                            " in (ms): " + 
                            totalMillisArrayList);
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
        
        private long profileAddIndex(
                List<Integer> list, 
                int operations, 
                Random random) {
            
            long startMillis = System.currentTimeMillis();
            
            for (int i = 0; i < operations; i++) {
                int index = random.nextInt(list.size());
                Integer value = random.nextInt();
                list.add(index, value);
            }
            
            long endMillis = System.currentTimeMillis();
            long durationMillis = endMillis - startMillis;
            
            System.out.println(
                    list.getClass().getName() + 
                            ".add(int, E) in (ms): " + 
                            durationMillis);
            
            return durationMillis;
        }
        
        private long profileAddCollection(
                List<Integer> list, 
                int operations, 
                Random random) {
            
            long startMillis = System.currentTimeMillis();
            
            for (int i = 0; i < operations; i++) {
                List<Integer> collection = createRandomCollection(random);
                int index = random.nextInt(list.size());
                list.addAll(index, collection);
            }
            
            long endMillis = System.currentTimeMillis();
            long durationMillis = endMillis - startMillis;
            
            System.out.println(
                    list.getClass().getName() + 
                            ".addAll(int, Collection) in (ms): " +
                            durationMillis);
            
            return durationMillis;
        }
        
        private long profileAppendCollection(
                List<Integer> list, 
                int operations, 
                Random random) {
            
            long startMillis = System.currentTimeMillis();
            
            for (int i = 0; i < operations; i++) {
                List<Integer> collection = createRandomCollection(random);
                list.addAll(collection);
            }
            
            long endMillis = System.currentTimeMillis();
            long durationMillis = endMillis - startMillis;
            
            System.out.println(
                    list.getClass().getName() + 
                            ".addAll(Collection) in (ms): " +
                            durationMillis);
            
            return durationMillis;
        }
        
        private void profileAddFirstRoddeList() {
            totalMillisRoddeList += 
                    profileAddFirst(
                            roddeList, 
                            ADD_FIRST_OPERATIONS, 
                            randomMyList);
        }
        
        private void profileAddFirstLinkedList() {
            totalMillisLinkedList += 
                    profileAddFirst(linkedList, 
                                    ADD_FIRST_OPERATIONS, 
                                    randomJavaUtilLinkedList);
        }
        
        private void profileAddFirstArrayList() {
            totalMillisArrayList += 
                    profileAddFirst(arrayList, 
                                    ADD_FIRST_OPERATIONS, 
                                    randomJavaUtilArrayList);
        }
        
        private void profileAddLastRoddeList() {
            totalMillisRoddeList += 
                profileAddLast(roddeList, ADD_FIRST_OPERATIONS, randomMyList);
        }
        
        private void profileAddLastLinkedList() {
            totalMillisLinkedList += 
                    profileAddLast(
                            linkedList, 
                            ADD_FIRST_OPERATIONS, 
                            randomJavaUtilLinkedList);
        }
        
        private void profileAddLastArrayList() {
            totalMillisArrayList += 
                    profileAddLast(arrayList, 
                                   ADD_FIRST_OPERATIONS, 
                                   randomJavaUtilArrayList);
        }
        
        private void profileAddIndexRoddeList() {
            totalMillisRoddeList += 
                    profileAddIndex(roddeList, ADD_AT_OPERATIONS, randomMyList);
        }
        
        private void profileAddIndexLinkedList() {
            totalMillisLinkedList += 
                    profileAddIndex(
                            linkedList, 
                            ADD_AT_OPERATIONS, 
                            randomJavaUtilLinkedList);
        }
        
        private void profileAddIndexArrayList() {
            totalMillisArrayList +=
                    profileAddIndex(
                            arrayList, 
                            ADD_AT_OPERATIONS, 
                            randomJavaUtilArrayList);
        }
        
        private void profileAddCollectionRoddeList() {
            totalMillisRoddeList +=
                    profileAddCollection(
                            roddeList, 
                            ADD_COLLECTION_AT_OPERATIONS, 
                            randomMyList);
        }
       
        private void profileAddCollectionLinkedList() {
            totalMillisLinkedList += 
                    profileAddCollection(
                            linkedList,
                            ADD_COLLECTION_AT_OPERATIONS,
                            randomJavaUtilLinkedList);
        }
           
        private void profileAddCollectionArrayList() {
            totalMillisArrayList +=
                    profileAddCollection(
                            arrayList,
                            ADD_COLLECTION_AT_OPERATIONS,
                            randomJavaUtilArrayList);
        }
        
            
        private void profileAppendCollectionRoddeList() {
            totalMillisRoddeList +=
                    profileAppendCollection(
                            roddeList, 
                            ADD_LAST_COLLECTION_OPERATIONS, 
                            randomMyList);
        }
        
        private void profileAppendCollectionLinkedList() {
            totalMillisRoddeList +=
                    profileAppendCollection(
                            linkedList, 
                            ADD_LAST_COLLECTION_OPERATIONS, 
                            randomJavaUtilLinkedList);
        }
        
        private void profileAppendCollectionArrayList() {
            totalMillisRoddeList +=
                    profileAppendCollection(
                            arrayList, 
                            ADD_LAST_COLLECTION_OPERATIONS, 
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
        long seed = 1619010671495L; //System.currentTimeMillis();
        
        System.out.println("--- Seed = " + seed + " ---");
        System.out.println();
        
        Benchmark benchmark = new Benchmark(seed);
        benchmark.warmup();
        System.out.println();
        benchmark.benchmark();
    }
}
