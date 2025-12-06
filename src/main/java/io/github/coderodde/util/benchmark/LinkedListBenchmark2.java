package io.github.coderodde.util.benchmark;

import io.github.coderodde.util.IndexedLinkedList;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import net.openhft.affinity.AffinityLock;
import org.apache.commons.collections4.list.TreeList;

public class LinkedListBenchmark2 {
    
    private static final Object ELEMENT = new Object();
    private static final Map<String, Long> DURATION_COUNTER_MAP =
            new HashMap<>();
    
    private static final Map<String, Long> PER_OPERATION_DURATION_COUNTER_MAP = 
            new TreeMap<>();
    
    private static final Map<String, String>
            BENCHMARK_OPERATION_NAME_TO_GNUPLOT_NAME = new LinkedHashMap<>();
    
    private static final Map<String, String> COLOR_MAP = new HashMap<>();
    
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
    
    private static final class Bounds {
        static final int NUMBER_OF_ADDITIONS_AT_BEGINNING = 2_000;
        static final int NUMBER_OF_RANDOM_ADDS = 2_000;
        static final int NUMBER_OF_ADDITIONS_AT_END = 10_000;
        static final int NUMBER_OF_GETS = 500;
        static final int NUMBER_OF_REMOVE_FIRST_OPS = 5_000;
        static final int NUMBER_OF_REMOVE_LAST_OPS = 20_000;
        static final int NUMBER_OF_RANDOM_REMOVES = 10_000;
        static final int NUMBER_OF_COLLECTION_APPENDS = 50;
        static final int NUMBER_OF_COLLECTION_INSERTS = 50;
        static final int APPEND_COLLECTION_SIZE = 10_000;
        static final int INSERT_COLLECTION_SIZE = 3_500;
        static final int REMOVE_RANGE_SIZE = 500;
    }
    
    private static final String[] METHOD_NAMES = {
        "AddAtBeginning",
        "AddAtEnd",
        "AddRandom",
        "PrependCollection",
        "AppendCollection",
        "InsertCollection",
        "GetRandom",
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
        initializeOperationNames();
        initializePerOperationMap();
        initializeColorMap();
    }
    
    private static void clearDurationCounterMap() {
        for (int i = 0; i < LIST_TYPE_NAMES.length; i++) {
            DURATION_COUNTER_MAP.put(LIST_TYPE_NAMES[i], 0L);;
        }
    }
    
    private static void clearPerOperationCounterMap() {
        for (final String listTypeName : LIST_TYPE_NAMES) {
            for (final String methodName : METHOD_NAMES) {
                final String line = 
                        String.format(
                                "%s%s", 
                                listTypeName, 
                                methodName);
                
                PER_OPERATION_DURATION_COUNTER_MAP.put(line, 0L);
            }
        }
    }
    
    private static void initializeOperationNames() {
        Map<String, String> m = BENCHMARK_OPERATION_NAME_TO_GNUPLOT_NAME;
        
        m.put("AddAtBeginning",      "Push-Front");
        m.put("AddAtEnd",            "Push-Back");
        m.put("AddRandom",           "Insert");
        m.put("PrependCollection",   "Push-Front-Collection");
        m.put("AppendCollection",    "Push-Back-Collection");
        m.put("InsertCollection",    "Insert-Collection");
        m.put("GetRandom",           "Search");
        m.put("RemoveFromBeginning", "Pop-Front");
        m.put("RemoveFromEnd",       "Pop-Back");
        m.put("RemoveRandom",        "Delete");
        m.put("RemoveRange",         "Delete-Range");
    }
    
    private static void initializeColorMap() {
        final String insertColor = "0x289e37";
        final String searchColor = "0x28579e";
        final String deleteColor = "0xa83232";
        
        COLOR_MAP.put("Push-Front",            insertColor);
        COLOR_MAP.put("Push-Back",             insertColor);
        COLOR_MAP.put("Insert",                insertColor);
        COLOR_MAP.put("Push-Front-Collection", insertColor);
        COLOR_MAP.put("Push-Back-Collection",  insertColor);
        COLOR_MAP.put("Insert-Collection",     insertColor);
        
        COLOR_MAP.put("Search",                searchColor);
        
        COLOR_MAP.put("Pop-Front",             deleteColor);
        COLOR_MAP.put("Pop-Back",              deleteColor);
        COLOR_MAP.put("Delete",                deleteColor);
        COLOR_MAP.put("Delete-Range",          deleteColor);
    }
    
    private static void initializePerOperationMap() {
        // Initialize the per operation duration map:
        for (String listName : LIST_TYPE_NAMES) {
            for (String methodName : METHOD_NAMES) {
                PER_OPERATION_DURATION_COUNTER_MAP
                        .put(String.format(
                                "%s%s", 
                                listName,
                                methodName), 
                                0L);
            }
        }  
    }
    
    public static void main(String[] args) {
        
        try (AffinityLock al = AffinityLock.acquireLock()) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            clearDurationCounterMap();
            warmup();
            benchmark();
            System.out.println("<<< Total durations >>>");
            printTotalDurations();
        }
    }
    
    private static void warmup() {
        for (String methodName : METHOD_NAMES) {
            for (String listTypeName : LIST_TYPE_NAMES) {
                for (int listSize : LIST_SIZES) {
                    benchmark(methodName,
                              listTypeName, 
                              listSize,
                              false);
                }
            }
        }
        
        printTotalDurations();
        clearPerOperationCounterMap();
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
        
        showPerOperationStatistics();
    }
    
    private static void showPerOperationStatistics() {
        System.out.println();
        System.out.println();
        
        for (final String listTypeName : LIST_TYPE_NAMES) {
            System.out.printf(">>> List type name: %s\n", listTypeName);
            long listDuration = 0L;
            
            for (final String methodName : METHOD_NAMES) {
                final String line =
                        String.format(
                                "%s%s",
                                listTypeName, 
                                methodName);
                
                final String algorithmName =
                        BENCHMARK_OPERATION_NAME_TO_GNUPLOT_NAME
                                .get(methodName);
                
                final String fmt = 
                        String.format(
                                "%%%ds %%10d %%8s\n",
                                "\"Push-Front-Collection\"".length());
                
                System.out.printf(
                        fmt,
                        String.format("\"%s\"", algorithmName),
                        milliseconds(
                                PER_OPERATION_DURATION_COUNTER_MAP.get(line)),
                        COLOR_MAP.get(algorithmName)
                        );
                
                listDuration += PER_OPERATION_DURATION_COUNTER_MAP.get(line);
            }
            
            System.out.printf("    List duration: %d microseconds.\n\n",
                              listDuration);
        }
        
        System.out.println();
        System.out.println();
    }
    
    private static void printTotalDurations() {
        for (Map.Entry<String, Long> e : DURATION_COUNTER_MAP.entrySet()) {
            System.out.printf("%-" + "indexedLinkedList".length() + "s: %d\n", 
                    e.getKey(),
                    milliseconds(e.getValue()));
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
    
    private static String getListTypeName(List list) {
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
        System.gc();
        List<Object> list = getEmptyList(listTypeName);
        loadList(list, listSize);
        long duration;
        
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
                                                      new Random(13L));
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
                duration = BenchmarkMethods.getRandom(list, 
                                                      new Random(26L),
                                                      print);
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
        
        final String line = String.format("%s%s", listTypeName, methodName);
        
        PER_OPERATION_DURATION_COUNTER_MAP.put(
                line, 
                PER_OPERATION_DURATION_COUNTER_MAP.get(line) + duration);
        
        DURATION_COUNTER_MAP.put(listTypeName, 
                                 DURATION_COUNTER_MAP.get(listTypeName) 
                                         + duration);
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
                startTime = microseconds();
                Deque<Object> deque = (Deque<Object>) list;
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_BEGINNING; i++) {
                    deque.addFirst(ELEMENT);
                }
                
                endTime = microseconds();
            } else {
                startTime = microseconds();
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_BEGINNING; i++) {
                    list.add(0, ELEMENT);
                }
                
                endTime = microseconds();
            }
            
            long duration = endTime - startTime;
            
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
                startTime = microseconds();
                Deque<Object> deque = (Deque<Object>) list;
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_END; i++) {
                    deque.addLast(ELEMENT);
                }
                
                endTime = microseconds();
            } else {
                startTime = microseconds();
                
                for (int i = 0; i < Bounds.NUMBER_OF_ADDITIONS_AT_END; i++) {
                    list.add(ELEMENT);
                }
                
                endTime = microseconds();
            }
            
            long duration = endTime - startTime;
            
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
            long startTime = microseconds();

            for (int i = 0; i < Bounds.NUMBER_OF_RANDOM_ADDS; i++) {
                list.add(random.nextInt(list.size() + 1), ELEMENT);
            }

            long endTime = microseconds();
            long duration = endTime - startTime;
            
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
                                     
            long startTime = microseconds();

            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_APPENDS; i++) {
                list.addAll(listToAdd);
            }

            long endTime = microseconds();
            long duration = endTime - startTime;
            
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
            
            long startTime = microseconds();

            for (int index : indices) {
                list.get(index);
            }

            long endTime = microseconds();
            long duration = endTime - startTime;
            
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
            long startTime = microseconds();
            
            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_INSERTS; i++) {
                int index = random.nextInt(list.size() + 1);
                list.addAll(index, listToInsert);
            }
            
            long endTime = microseconds();
            long duration = endTime - startTime;
            
            if (print) {
                String listTypeName = getListTypeName(list);
                System.out.println(
                        listTypeName 
                        + "InsertCollection: " 
                        + duration);
            }
            
            return duration;
        }
        
        static long prependCollection(List<Object> list, 
                                      List<Object> listToPrepend,
                                      boolean print) {
            
            long startTime = microseconds();

            for (int i = 0; i < Bounds.NUMBER_OF_COLLECTION_APPENDS; i++) {
                list.addAll(0, listToPrepend);
            }

            long endTime = microseconds();
            long duration = endTime - startTime;
            
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
                startTime = microseconds();
                
                for (int i = 0; i < Bounds.NUMBER_OF_REMOVE_FIRST_OPS; i++) {
                    deque.removeFirst();
                }
                
                endTime = microseconds();
            } else {
                startTime = microseconds();
                
                for (int i = 0; i < Bounds.NUMBER_OF_REMOVE_FIRST_OPS; i++) {
                    list.remove(0);
                }
                
                endTime = microseconds();
            }
            
            duration = endTime - startTime;
            
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
                startTime = microseconds();
                
                for (int i = 0;
                        i < Bounds.NUMBER_OF_REMOVE_LAST_OPS 
                        && !deque.isEmpty(); 
                        i++) {
                    deque.removeLast();
                }
                
                endTime = microseconds();
            } else {
                startTime = microseconds();
                
                for (int i = 0; 
                        i < Bounds.NUMBER_OF_REMOVE_LAST_OPS && !list.isEmpty();
                        i++) {
                    list.remove(list.size() - 1);
                }
                
                endTime = microseconds();
            }
            
            long duration = endTime - startTime;
            
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
            long startTime = microseconds();

            for (int i = 0; i < Bounds.NUMBER_OF_RANDOM_REMOVES; i++) {
                list.remove(random.nextInt(list.size()));
            }

            long endTime = microseconds();
            long duration = endTime - startTime;
            
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
            
            int requestedSize = (3 * list.size()) / 5;
            
            startTime = microseconds();
            
            while (list.size() > requestedSize) {
                int fromIndex = random.nextInt(list.size()) - 
                                Bounds.REMOVE_RANGE_SIZE;
                
                fromIndex = Math.max(fromIndex, 0);
                int toIndex = fromIndex + Bounds.REMOVE_RANGE_SIZE;
                list.subList(fromIndex, toIndex).clear();
            }
            
            endTime = microseconds();
            long duration = endTime - startTime;
            
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
    
    private static long microseconds() {
        return System.nanoTime() / 1000L;
    }
    
    private static long milliseconds(long microseconds) {
        return (long)(Math.round((double) microseconds) / 
                                 (double) 1000.0);
    }
}
