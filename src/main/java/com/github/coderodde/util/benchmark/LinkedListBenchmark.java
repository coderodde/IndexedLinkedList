package com.github.coderodde.util.benchmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

final class LinkedListBenchmark {

    private static final int ADD_FIRST_OPERATIONS = 100_000;
    private static final int ADD_LAST_OPERATIONS = 100_000;
    private static final int ADD_AT_OPERATIONS = 10_000;
    private static final int ADD_COLLECTION_AT_OPERATIONS = 10;
    private static final int ADD_LAST_COLLECTION_OPERATIONS = 10;
    private static final int REMOVE_VIA_INDEX_OPERATIONS = 1_000;

    private static final int MAXIMUM_COLLECTION_SIZE = 20;

    private final long seed;

    private Random randomJavaUtilLinkedList;
    private Random randomJavaUtilArrayList;
    private Random randomRoddeList;

    private com.github.coderodde.util.LinkedList<Integer> roddeList = 
            new com.github.coderodde.util.LinkedList<>();

    private LinkedList<Integer> linkedList = new LinkedList<>();
    private ArrayList<Integer> arrayList = new ArrayList<>();

    private long totalMillisRoddeList  = 0L;
    private long totalMillisLinkedList = 0L;
    private long totalMillisArrayList  = 0L;

    LinkedListBenchmark(long seed) {
        this.seed = seed;
    }

    void warmup() {
        profile(BenchmarkChoice.WARMUP);
    }

    void benchmark() {
        profile(BenchmarkChoice.BENCHMARK);
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

    private void initRandomGenerators() {
        randomJavaUtilLinkedList = new Random(seed);
        randomJavaUtilArrayList  = new Random(seed);
        randomRoddeList          = new Random(seed);
    }

    private void listsEqual() {
        listsEqual(roddeList, linkedList, arrayList);
    }

    private static void listsEqual(List<Integer>... lists) {
        if (lists.length < 2) {
            throw new IllegalArgumentException("lists.length < 2");
        }

        for (int i = 0; i < lists.length - 1; i++) {
            if (lists[i].size() != lists[lists.length - 1].size()) {
                throw new IllegalStateException("Different size");
            }

            Iterator<Integer> iterator1 = lists[i].iterator();
            Iterator<Integer> iterator2 = lists[lists.length - 1].iterator();

            int elementIndex = 0;

            while (iterator1.hasNext() && iterator2.hasNext()) {
                Integer integer1 = iterator1.next();
                Integer integer2 = iterator2.next();

                if (!integer1.equals(integer2)) {
                    throw new IllegalStateException(
                            "Data mismatch: " + integer1 + " vs. " + 
                            integer2 + " at list " + i + 
                            ", element index: " + elementIndex);
                }

                elementIndex++;
            }

            if (iterator1.hasNext() || iterator2.hasNext()) {
                throw new IllegalStateException("Bad iterators");
            }
        }
    }

    private void profile(BenchmarkChoice benchmarkChoice) {

        printTitle(benchmarkChoice);
        initRandomGenerators();

//            profileAddFirst();
//            profileAddLast();
//            profileAddViaIndex();
        profileAppendCollection();
        profileAddCollection();
//        profileRemoveViaIndex();
//        printTotalDurations();

        resetLists();
        zeroTimeDurationCounters();
    }

    private void zeroTimeDurationCounters() {
        totalMillisArrayList  = 0;
        totalMillisLinkedList = 0;
        totalMillisRoddeList  = 0;
    }

    private void resetLists() {
        roddeList = new com.github.coderodde.util.LinkedList<>();
        linkedList = new java.util.LinkedList<>();
        arrayList = new ArrayList<>();
    }

    private void profileAddFirst() {
        profileAddFirstRoddeList();
        profileAddFirstLinkedList();
        profileAddFirstArrayList();

        listsEqual();
        System.out.println();
    }

    private void profileAddLast() {
        profileAddLastRoddeList();
        profileAddLastLinkedList();
        profileAddLastArrayList();

        listsEqual();
        System.out.println();
    }

    private void profileAddViaIndex() {
        profileAddIndexRoddeList();
        profileAddIndexLinkedList();
        profileAddIndexArrayList();

        listsEqual();
        System.out.println();
    }

    private void profileAddCollection() {
        long seed = System.currentTimeMillis();
        seed = 1621688256515L;
        
        System.out.println("---> seed = " + seed);
        Random random = new Random(seed);
        
        try {
            listsEqual();
        } catch (IllegalStateException ex) {
            System.out.println("SHIT HAPPENED");
            System.exit(-1);
        }
        
        for (int op = 0; op < ADD_COLLECTION_AT_OPERATIONS; op++) {
            List<Integer> coll = createRandomCollection(random);
            int index = random.nextInt(roddeList.size());
            
            linkedList.addAll(index, coll);
            roddeList.addAll(index, coll);
            arrayList.addAll(index, coll);
            
            System.out.println("(" + index + ", op = " + op + ", [" + coll + "])");
            
            try {
                listsEqual();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
        }
        
//        profileAddCollectionRoddeList();
//        profileAddCollectionLinkedList();
//        profileAddCollectionArrayList();
//
//        listsEqual();
//        System.out.println();
    }

    private void profileAppendCollection() {
        profileAppendCollectionRoddeList();
        profileAppendCollectionLinkedList();
        profileAppendCollectionArrayList();

        listsEqual();
        System.out.println();
    }

    private void profileRemoveViaIndex() {
        profileRemoveViaIndexRoddeList();
        profileRemoveViaIndexLinkedList();
        profileRemoveViaIndexArrayList();

        listsEqual();
        System.out.println();
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

    private long profileRemoveViaIndex(
            List<Integer> list, 
            int operations, 
            Random random) {

        long startMillis = System.currentTimeMillis();

        for (int i = 0; i < operations; i++) {
            list.remove(random.nextInt(list.size()));
        }

        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;

        System.out.println(
                list.getClass().getName() +
                        ".remove(int) in (ms): " +
                        durationMillis);

        return durationMillis;
    }

    private void profileAddFirstRoddeList() {
        totalMillisRoddeList += 
                profileAddFirst(
                        roddeList, 
                        ADD_FIRST_OPERATIONS, 
                        randomRoddeList);
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
            profileAddLast(roddeList, ADD_FIRST_OPERATIONS, randomRoddeList);
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
                profileAddIndex(roddeList, ADD_AT_OPERATIONS, randomRoddeList);
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
                        randomRoddeList);
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
                        randomRoddeList);
    }

    private void profileAppendCollectionLinkedList() {
        totalMillisLinkedList +=
                profileAppendCollection(
                        linkedList, 
                        ADD_LAST_COLLECTION_OPERATIONS, 
                        randomJavaUtilLinkedList);
    }

    private void profileAppendCollectionArrayList() {
        totalMillisArrayList +=
                profileAppendCollection(
                        arrayList, 
                        ADD_LAST_COLLECTION_OPERATIONS, 
                        randomJavaUtilArrayList);
    }


    private void profileRemoveViaIndexRoddeList() {
        totalMillisRoddeList += 
                profileRemoveViaIndex(
                        roddeList, 
                        REMOVE_VIA_INDEX_OPERATIONS, 
                        randomRoddeList);
    }

    private void profileRemoveViaIndexLinkedList() {    
        totalMillisLinkedList += 
                profileRemoveViaIndex(
                        linkedList, 
                        REMOVE_VIA_INDEX_OPERATIONS, 
                        randomJavaUtilLinkedList);
    }

    private void profileRemoveViaIndexArrayList() {
        totalMillisArrayList += 
                profileRemoveViaIndex(
                        arrayList, 
                        REMOVE_VIA_INDEX_OPERATIONS, 
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