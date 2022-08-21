package com.github.coderodde.util.benchmark;

import com.github.coderodde.util.IndexedLinkedList;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import org.apache.commons.collections4.list.TreeList;

public class LinkedListBenchmark2 {
    
    private static final Object ELEMENT = new Object();
    
    private static final int[] LIST_SIZES = {
//        10_000,
//        20_000,
//        30_000,
        40_000,
//        50_000,
//        60_000,
//        70_000,
//        80_000,
//        90_000,
//        100_000,
//        110_000,
//        120_000,
//        130_000,
//        140_000,
//        150_000,
//        160_000,
//        170_000,
//        180_000,
//        190_000,
//        200_000,
    };
    
    private static final class Bounds {
        static final int NUMBER_OF_ADDITIONS_AT_BEGINNING = 2_000;
        static final int NUMBER_OF_RANDOM_ADDS = 2_000;
        static final int NUMBER_OF_GETS = 2_000;
        static final int NUMBER_OF_REMOVE_FIRST_OPS = 5_000;
        static final int NUMBER_OF_REMOVE_LAST_OPS = 20_000;
        static final int NUMBER_OF_RANDOM_REMOVES = 10_000;
        static final int NUMBER_OF_COLLECTION_APPENDS = 50;
        static final int APPEND_COLLECTION_SIZE = 10_000;
        static final int REMOVE_RANGE_SIZE = 500;
        static final double ITERATE_AND_MODIFY_ADD_THRESHOLD = 0.7;
        static final double ITERATE_AND_MODIFY_REMOVE_THRESHOLD = 0.3;
    }
    
    private static final String[] METHOD_NAMES = {
//        "AddAtBeginning",
//        "AddAtEnd",
//        "AddRandom",
//        "AppendCollection",
//        "GetRandom",
//        "InsertCollection",
//        "Iterate",
//        "IterateAndModify",
//        "PrependCollection",
//        "RemoveFromBeginning",
//        "RemoveFromEnd",
//        "RemoveRandom",
        "RemoveRange",
    };
    
    private static final String[] LIST_TYPE_NAMES = {
        "arrayList",
        "indexedLinkedList",
        "linkedList",
        "treeList",
    };
    
    public static void main(String[] args) {
//        warmup();
        benchmark();
    }
    
    private static void warmup() {
        for (String methodName : METHOD_NAMES) {
            for (String listTypeName : LIST_TYPE_NAMES) {
                for (int listSize : LIST_SIZES) {
                    benchmark(methodName, listTypeName, listSize, false);
                }
            }
        }
    }
    
    private static void benchmark() {
        for (String methodName : METHOD_NAMES) {
            for (String listTypeName : LIST_TYPE_NAMES) {
                for (int listSize : LIST_SIZES) {
                    benchmark(methodName, listTypeName, listSize, true);
                }
            }
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
        
        switch (methodName) {
            case "AddAtBeginning":
                BenchmarkMethods.addAtBeginning(list, print);
                return;
                
            case "AddAtEnd":
                BenchmarkMethods.addAtEnd(list, print);
                return;
                
            case "AddRandom":
                BenchmarkMethods.addRandom(list, print, new Random(1L));
                return;
                
            case "AppendCollection":
                List<Object> listToAppend = 
                        new ArrayList<>(Bounds.APPEND_COLLECTION_SIZE);
                
                loadList(listToAppend, Bounds.APPEND_COLLECTION_SIZE);
                BenchmarkMethods.appendCollection(list, listToAppend, print);
                return;
                
            case "GetRandom":
                BenchmarkMethods.getRandom(list, new Random(2L), print);
                return;
                
            case "Iterate":
                BenchmarkMethods.iterate(list, print);
                return;
                
            case "IterateAndModify":
                BenchmarkMethods.iterateAndModify(list, new Random(2L), print);
                return;
                
            case "PrependCollection":
                List<Object> listToPrepend = 
                        new ArrayList<>(Bounds.APPEND_COLLECTION_SIZE);
                
                loadList(listToPrepend, Bounds.APPEND_COLLECTION_SIZE);
                BenchmarkMethods.prependCollection(list, listToPrepend, print);
                return;
                
            case "RemoveFromBeginning":
                BenchmarkMethods.removeFromBeginning(list, print);
                return;
                
            case "RemoveFromEnd":
                BenchmarkMethods.removeFromEnd(list, print);
                return;
                        
            case "RemoveRandom":
                BenchmarkMethods.removeRandom(list, print, new Random(3L));
                return;
                
            case "RemoveRange":
                BenchmarkMethods.removeRange(list, print, new Random(4L));
                return;
                
            default:
//                throw new IllegalArgumentException(
//                        "Unknown method name: " + methodName);
        }
    }
    
    private static void loadList(List<Object> list, int listSize) {
        for (int i = 0; i < listSize; i++) {
            list.add(ELEMENT);
        }
    }
    
    private static final class BenchmarkMethods {
        
        static void addAtBeginning(List<Object> list, boolean print) {
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
            
            if (print) {
                String listTypeName = getListTypeName(list);
                
                System.out.println(
                        listTypeName 
                        + "AddAtBeginning: " 
                        + (endTime - startTime));
            }
        }
        
        static void addAtEnd(List<Object> list, boolean print) {
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
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "AddAtEnd: " 
                        + (endTime - startTime));
            }
        }
        
        static void addRandom(List<Object> list, boolean print, Random random) {
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_RANDOM_ADDS; i++) {
                list.add(random.nextInt(list.size() + 1), ELEMENT);
            }

            long endTime = System.nanoTime();
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "AddRandom: " 
                        + (endTime - startTime));
            }
        }
        
        static void appendCollection(List<Object> list, 
                                     List<Object> listToAdd,
                                     boolean print) {
            
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_APPENDS; i++) {
                list.addAll(listToAdd);
            }

            long endTime = System.nanoTime();
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "AppendCollection: " 
                        + (endTime - startTime));
            }
        }
        
        static void getRandom(List<Object> list, Random random, boolean print) {
            int[] indices = new int[Bounds.NUMBER_OF_GETS];
            
            for (int i = 0; i < indices.length; i++) {
                indices[i] = random.nextInt(indices.length);
            }
            
            long startTime = System.nanoTime();

            for (int index : indices) {
                list.get(index);
            }

            long endTime = System.nanoTime();
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "GetRandom: " 
                        + (endTime - startTime));
            }
        }
        
        static void iterate(List<Object> list, boolean print) {
            Iterator<Object> iterator = list.iterator();
            
            long startTime = System.nanoTime();

            while (iterator.hasNext()) {
                iterator.next();
            }

            long endTime = System.nanoTime();
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "Iterate: " 
                        + (endTime - startTime));
            }
        }
        
        static void iterateAndModify(List<Object> list,
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
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "IterateAndModify: " 
                        + (endTime - startTime));
            }
        }
        
        static void prependCollection(List<Object> list, 
                                      List<Object> listToPrepend,
                                      boolean print) {
            
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_APPENDS; i++) {
                list.addAll(0, listToPrepend);
            }

            long endTime = System.nanoTime();
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "PrependCollection: " 
                        + (endTime - startTime));
            }
        }
        
        static void removeFromBeginning(List<Object> list, boolean print) {
            
            long startTime;
            long endTime;
            
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
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveFromBeginning: " 
                        + (endTime - startTime));
            }
        }
        
        static void removeFromEnd(List<Object> list, boolean print) {
            
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
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveFromEnd: " 
                        + (endTime - startTime));
            }
        }
        
        static void removeRandom(List<Object> list, boolean print, Random random) {
            long startTime = System.nanoTime();

            for (int i = 0; i < Bounds.NUMBER_OF_RANDOM_REMOVES; i++) {
                list.remove(random.nextInt(list.size()));
            }

            long endTime = System.nanoTime();
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveRandom: " 
                        + (endTime - startTime));
            }
        }
        
        static void removeRange(List<Object> list,
                                boolean print, 
                                Random random) {
            long startTime;
            long endTime;
            
            startTime = System.nanoTime();
            
            int requestedSize = (4 * list.size()) / 5;
            int ops = 0;
            
            while (list.size() > requestedSize) {
                int fromIndex = random.nextInt(list.size()) - 
                                Bounds.REMOVE_RANGE_SIZE;
                
                fromIndex = Math.max(fromIndex, 0);
                int toIndex = fromIndex + Bounds.REMOVE_RANGE_SIZE;
                
                if (list.getClass().getSimpleName()
                        .equals(IndexedLinkedList.class.getSimpleName())) {
                    
                    System.out.println(fromIndex + " -> " + toIndex);
//                    System.out.println("ops: " + ops);
                    ops++;
                }
                
                
                list.subList(fromIndex, toIndex).clear();
            }
            
            endTime = System.nanoTime();
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "RemoveRange: " 
                        + (endTime - startTime));
            }
        }
    }
}
