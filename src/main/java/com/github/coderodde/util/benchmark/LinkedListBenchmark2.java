package com.github.coderodde.util.benchmark;

import com.github.coderodde.util.IndexedLinkedList;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import org.apache.commons.collections4.list.TreeList;

public class LinkedListBenchmark2 {
    
    private static final Object ELEMENT = new Object();
    private static final Map<String, Long> DURATION_COUNTER_MAP =
            new HashMap<>();
    
    private static final Map<String, Long> ITERATOR_DURATION_COUNTER_MAP = 
            new HashMap<>();
    
    private static final int[] LIST_SIZES = {
        100_000,
        200_000,
        300_000,
        400_000,
        500_000,
        600_000,
        700_000,
        800_000,
        900_000,
        1_000_000,
    };
    
    private static final int[] LIST_SIZES_FOR_ITERATOR_MODFICATIONS = {
        10_000,
        20_000,
        30_000,
        40_000,
        50_000,
        60_000,
        70_000,
        80_000,
        90_000,
        100_000,
    };
    
    private static final class Bounds {
        static final int NUMBER_OF_ADDITIONS_AT_BEGINNING = 2_000;
        static final int NUMBER_OF_RANDOM_ADDS = 2_000;
        static final int NUMBER_OF_GETS = 500;
        static final int NUMBER_OF_REMOVE_FIRST_OPS = 5_000;
        static final int NUMBER_OF_REMOVE_LAST_OPS = 20_000;
        static final int NUMBER_OF_RANDOM_REMOVES = 10_000;
        static final int NUMBER_OF_COLLECTION_APPENDS = 50;
        static final int NUMBER_OF_COLLECTION_INSERTS = 50;
        static final int APPEND_COLLECTION_SIZE = 10_000;
        static final int INSERT_COLLECTION_SIZE = 3_500;
        static final int REMOVE_RANGE_SIZE = 500;
        static final double ITERATE_AND_MODIFY_ADD_THRESHOLD = 0.7;
        static final double ITERATE_AND_MODIFY_REMOVE_THRESHOLD = 0.3;
    }
    
    private static final String[] METHOD_NAMES = {
        "AddAtBeginning",
        "AddAtEnd",
        "AddRandom",
        "AppendCollection",
        "GetRandom",
        "InsertCollection",
        "Iterate",
        "PrependCollection",
        "RemoveFromBeginning",
        "RemoveFromEnd",
        "RemoveRandom",
        "RemoveRange",
    };
    
    private static final String[] LIST_TYPE_NAMES = {
        "arrayList",
        "indexedLinkedList",
        "linkedList",
        "treeList",
    };
    
    static {
        clearDurationCounterMap();
    }
    
    private static void clearDurationCounterMap() {
        for (int i = 0; i < LIST_TYPE_NAMES.length; i++) {
            DURATION_COUNTER_MAP.put(LIST_TYPE_NAMES[i], 0L);
            ITERATOR_DURATION_COUNTER_MAP.put(LIST_TYPE_NAMES[i], 0L);
        }
    }
    
    public static void main(String[] args) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        clearDurationCounterMap();
        warmup();
        benchmark();
        System.out.println("<<< Total durations >>>");
        printTotalDurations();
        System.out.println("<<< Modified iterator >>>");
        printModifiedIteratorDurations();
    }
    
    private static void warmup() {
        for (String methodName : METHOD_NAMES) {
            for (String listTypeName : LIST_TYPE_NAMES) {
                for (int listSize : LIST_SIZES) {
                    benchmark(methodName, listTypeName, listSize, false);
                }
            }
        }
        
        benchmarkModifiedIterator(false);
    }
    
    private static void benchmark() {
        clearDurationCounterMap(); // Clear from the warmup run.
        
        for (String methodName : METHOD_NAMES) {
            for (String listTypeName : LIST_TYPE_NAMES) {
                for (int listSize : LIST_SIZES) {
                    benchmark(methodName, listTypeName, listSize, true);
                }
            }
        }
        
        benchmarkModifiedIterator(true);
    }
    
    private static void printTotalDurations() {
        for (Map.Entry<String, Long> e : DURATION_COUNTER_MAP.entrySet()) {
            System.out.printf("%-" + "indexedLinkedList".length() + "s: %d\n", 
                    e.getKey(),
                    e.getValue());
        }
    }
    
    private static void printModifiedIteratorDurations() {
        for (Map.Entry<String, Long> e : 
                ITERATOR_DURATION_COUNTER_MAP.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
    }
    
    private static List<Object> getEmptyList(String listTypeName) {
        if (listTypeName.equals(LIST_TYPE_NAMES[0])) {
            return new ArrayList<>();
        } else if (listTypeName.equals(LIST_TYPE_NAMES[1])) {
            return new IndexedLinkedList<>();
        } else if (listTypeName.equals(LIST_TYPE_NAMES[2])) {
            return new LinkedList<>();
        } else if (listTypeName.equals(LIST_TYPE_NAMES[3])) {
            return new TreeList<>();
        } else {
            throw new IllegalArgumentException(
                    "Unknown list type name: " + listTypeName);
        }
    }
    
    private static String getListTypeName(List<Object> list) {
        String className = list.getClass().getSimpleName();
        
        switch (className) {
            case "ArrayList": 
                return "arrayList";
                
            case "IndexedLinkedList":
                return "indexedLinkedList";
                
            case "LinkedList":
                return "linkedList";
                
            case "TreeList":
                return "treeList";
                
            default:
                throw new IllegalArgumentException(
                        "Uknown List class: " + className);
        }
    }
    
    private static void benchmark(String methodName,
                                  String listTypeName,
                                  int listSize,
                                  boolean print) {
        List<Object> list = getEmptyList(listTypeName);
        loadList(list, listSize);
        long duration;
        System.gc();
        
        switch (methodName) {
            case "AddAtBeginning":
                duration = BenchmarkMethods.addAtBeginning(list, print);
                break;
                
            case "AddAtEnd":
                duration = BenchmarkMethods.addAtEnd(list, print);
                break;
                
            case "AddRandom":
                duration = BenchmarkMethods.addRandom(list,
                                                      print,
                                                      new Random(1L));
                break;
                
            case "AppendCollection":
                List<Object> listToAppend = 
                        new ArrayList<>(Bounds.APPEND_COLLECTION_SIZE);
                
                loadList(listToAppend, Bounds.APPEND_COLLECTION_SIZE);
                duration = BenchmarkMethods.appendCollection(list, 
                                                             listToAppend,
                                                             print);
                break;
                
            case "GetRandom":
                duration = BenchmarkMethods.getRandom(list, new Random(2L), print);
                break;
                
            case "InsertCollection":
                List<Object> listToInsert = 
                        new ArrayList<>(Bounds.INSERT_COLLECTION_SIZE);
                
                loadList(listToInsert, Bounds.INSERT_COLLECTION_SIZE);
                Random random = new Random(4L);
                duration = BenchmarkMethods.insertCollection(list, 
                                                             listToInsert, 
                                                             random, 
                                                             print);
                break;
                
            case "Iterate":
                duration = BenchmarkMethods.iterate(list, print);
                break;
                
            case "IterateAndModify":
                duration = BenchmarkMethods.iterateAndModify(list, 
                                                             new Random(2L), 
                                                             print);
                break;
                
            case "PrependCollection":
                List<Object> listToPrepend = 
                        new ArrayList<>(Bounds.APPEND_COLLECTION_SIZE);
                
                loadList(listToPrepend, Bounds.APPEND_COLLECTION_SIZE);
                duration = BenchmarkMethods.prependCollection(list, 
                                                              listToPrepend, 
                                                              print);
                break;
                
            case "RemoveFromBeginning":
                duration = BenchmarkMethods.removeFromBeginning(list, print);
                break;
                
            case "RemoveFromEnd":
                duration = BenchmarkMethods.removeFromEnd(list, print);
                break;
                        
            case "RemoveRandom":
                duration = BenchmarkMethods.removeRandom(list, 
                                                         print, 
                                                         new Random(3L));
                break;
                
            case "RemoveRange":
                duration = BenchmarkMethods.removeRange(list, 
                                                        print,
                                                        new Random(4L));
                break;
                
            default:
                throw new IllegalArgumentException(
                        "Unknown method name: " + methodName);
        }
        
        DURATION_COUNTER_MAP.put(listTypeName, 
                                 DURATION_COUNTER_MAP.get(listTypeName) 
                                         + duration);
    }
    
    private static void benchmarkModifiedIterator(boolean print) {
        for (String listTypeName : LIST_TYPE_NAMES) {
            for (int size : LIST_SIZES_FOR_ITERATOR_MODFICATIONS) {
                List<Object> list = getEmptyList(listTypeName);
                loadList(list, size);
                long duration = 
                        BenchmarkMethods.iterateAndModify(
                                list, 
                                new Random(5L), 
                                print);
                
                ITERATOR_DURATION_COUNTER_MAP.put(
                        listTypeName, 
                        ITERATOR_DURATION_COUNTER_MAP.get(listTypeName) 
                                + duration);
            }
        }
    }
    
    private static void loadList(List<Object> list, int listSize) {
        for (int i = 0; i < listSize; i++) {
            list.add(ELEMENT);
        }
    }
    
    private static final class BenchmarkMethods {
        
        static long addAtBeginning(List<Object> list, boolean print) {
            long startTime;
            long endTime;
            
            if (list instanceof Deque) {
                startTime = System.nanoTime();
                Deque<Object> deque = (Deque<Object>) list;
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_BEGINNING; i++) {
                    deque.addFirst(ELEMENT);
                }
                
                endTime = System.nanoTime();
            } else {
                startTime = System.nanoTime();
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_BEGINNING; i++) {
                    list.add(0, ELEMENT);
                }
                
                endTime = System.nanoTime();
            }
            
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                
                System.out.println(
                        listTypeName 
                        + "AddAtBeginning: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long addAtEnd(List<Object> list, boolean print) {
            long startTime;
            long endTime;
            
            if (list instanceof Deque) {
                startTime = System.nanoTime();
                Deque<Object> deque = (Deque<Object>) list;
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_BEGINNING; i++) {
                    deque.addLast(ELEMENT);
                }
                
                endTime = System.nanoTime();
            } else {
                startTime = System.nanoTime();
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_BEGINNING; i++) {
                    list.add(ELEMENT);
                }
                
                endTime = System.nanoTime();
            }
            
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "AddAtEnd: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long addRandom(List<Object> list, boolean print, Random random) {
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_RANDOM_ADDS; i++) {
                list.add(random.nextInt(list.size() + 1), ELEMENT);
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "AddRandom: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long appendCollection(List<Object> list, 
                                     List<Object> listToAdd,
                                     boolean print) {
            
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_APPENDS; i++) {
                list.addAll(listToAdd);
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "AppendCollection: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long getRandom(List<Object> list, Random random, boolean print) {
            int[] indices = new int[Bounds.NUMBER_OF_GETS];
            
            for (int i = 0; i < indices.length; i++) {
                indices[i] = random.nextInt(indices.length);
            }
            
            long startTime = System.nanoTime();

            for (int index : indices) {
                list.get(index);
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "GetRandom: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long insertCollection(List<Object> list, 
                                     List<Object> listToInsert,
                                     Random random, 
                                     boolean print) {
            long startTime = System.nanoTime();
            
            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_INSERTS; i++) {
                int index = random.nextInt(list.size() + 1);
                list.addAll(index, listToInsert);
            }
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "InsertCollection: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long iterate(List<Object> list, boolean print) {
            Iterator<Object> iterator = list.iterator();
            
            long startTime = System.nanoTime();

            while (iterator.hasNext()) {
                iterator.next();
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "Iterate: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long iterateAndModify(List<Object> list,
                                     Random random, 
                                     boolean print) {
            
            ListIterator<Object> iterator = list.listIterator();
            
            long startTime = System.nanoTime();

            while (iterator.hasNext()) {
                iterator.next();
                double value = random.nextDouble();
                
                if (value > Bounds.ITERATE_AND_MODIFY_ADD_THRESHOLD) {
                    iterator.add(ELEMENT);
                } else if (value < Bounds.ITERATE_AND_MODIFY_REMOVE_THRESHOLD) {
                    iterator.remove();
                }
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "IterateAndModify: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long prependCollection(List<Object> list, 
                                      List<Object> listToPrepend,
                                      boolean print) {
            
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_APPENDS; i++) {
                list.addAll(0, listToPrepend);
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "PrependCollection: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long removeFromBeginning(List<Object> list, boolean print) {
            
            long startTime;
            long endTime;
            long duration;
            
            if (list instanceof Deque) {
                Deque<Object> deque = (Deque<Object>) list;
                startTime = System.nanoTime();
                
                for (int i = 0; i < Bounds.NUMBER_OF_REMOVE_FIRST_OPS; i++) {
                    deque.removeFirst();
                }
                
                endTime = System.nanoTime();
            } else {
                startTime = System.nanoTime();
                
                for (int i = 0; i < Bounds.NUMBER_OF_REMOVE_FIRST_OPS; i++) {
                    list.remove(0);
                }
                
                endTime = System.nanoTime();
            }
            
            duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveFromBeginning: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long removeFromEnd(List<Object> list, boolean print) {
            
            long startTime;
            long endTime;
            
            if (list instanceof Deque) {
                Deque<Object> deque = (Deque<Object>) list;
                startTime = System.nanoTime();
                
                for (int i = 0;
                        i < Bounds.NUMBER_OF_REMOVE_LAST_OPS 
                        && !deque.isEmpty(); 
                        i++) {
                    deque.removeLast();
                }
                
                endTime = System.nanoTime();
            } else {
                startTime = System.nanoTime();
                
                for (int i = 0; 
                        i < Bounds.NUMBER_OF_REMOVE_LAST_OPS && !list.isEmpty();
                        i++) {
                    list.remove(list.size() - 1);
                }
                
                endTime = System.nanoTime();
            }
            
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveFromEnd: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long removeRandom(List<Object> list, 
                                 boolean print, 
                                 Random random) {
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_RANDOM_REMOVES; i++) {
                list.remove(random.nextInt(list.size()));
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveRandom: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long removeRange(List<Object> list,
                                boolean print, 
                                Random random) {
            long startTime;
            long endTime;
            
            int requestedSize = (4 * list.size()) / 5;
            
            startTime = System.nanoTime();
            
            while (list.size() > requestedSize) {
                int fromIndex = random.nextInt(list.size()) - 
                                Bounds.REMOVE_RANGE_SIZE;
                
                fromIndex = Math.max(fromIndex, 0);
                int toIndex = fromIndex + Bounds.REMOVE_RANGE_SIZE;
                list.subList(fromIndex, toIndex).clear();
            }
            
            endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveRange: " 
                        + duration);
            }
            
            return duration;
        }
    }
}
