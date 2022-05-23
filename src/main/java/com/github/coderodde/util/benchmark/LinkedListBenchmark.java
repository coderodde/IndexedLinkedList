package com.github.coderodde.util.benchmark;

import com.github.coderodde.util.EnhancedLinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.collections4.list.TreeList;

final class LinkedListBenchmark {

    private static final int ADD_FIRST_OPERATIONS           = 100_000;
    private static final int ADD_LAST_OPERATIONS            = 100_000;
    private static final int ADD_AT_OPERATIONS              = 10_000;
    private static final int ADD_COLLECTION_AT_OPERATIONS   = 4_000;
    private static final int ADD_LAST_COLLECTION_OPERATIONS = 10_000;
    private static final int REMOVE_VIA_INDEX_OPERATIONS    = 10;
    private static final int REMOVE_OBJECT_OPERATIONS       = 1_000;
    private static final int GET_OPERATIONS                 = 2_000;
    private static final int REMOVE_FIRST_OPERATIONS        = 2_000;

    private static final int MAXIMUM_COLLECTION_SIZE = 20;

    private static final int MAXIMUM_INTEGER = 1_000;

    private final long seed;

    private Random randomJavaUtilLinkedList;
    private Random randomJavaUtilArrayList;
    private Random randomRoddeList;
    private Random randomTreeList;

    private EnhancedLinkedList<Integer> roddeList = new EnhancedLinkedList<>();
    private LinkedList<Integer> linkedList = new LinkedList<>();
    private ArrayList<Integer> arrayList = new ArrayList<>();
    private TreeList<Integer> treeList = new TreeList<>();

    private long totalMillisRoddeList  = 0L;
    private long totalMillisLinkedList = 0L;
    private long totalMillisArrayList  = 0L;
    private long totalMillisTreeList   = 0L;
    
    private final List<Integer>[] getLists = new ArrayList[5];

    LinkedListBenchmark(long seed) {
        this.seed = seed;
        
        for (int i = 0; i < getLists.length; i++) {
            getLists[i] = new ArrayList<>(GET_OPERATIONS);
        }
    }

    void warmup() {
        profile(BenchmarkChoice.WARMUP);
    }

    void benchmark() {
        profile(BenchmarkChoice.BENCHMARK);
    }
    
    // Simulate list.subList(fromIndex, toIndex) for those lists that do not
    // support the method:
    private static void clear(List<Integer> list, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; ++i) {
            list.remove(fromIndex);
        }
    }
    
    private static  Integer getRandomInteger(Random random) {
        return random.nextInt(MAXIMUM_INTEGER + 1);
    }

    private static List<Integer> createRandomCollection(Random random) {
        int size = 1 + random.nextInt(MAXIMUM_COLLECTION_SIZE);

        List<Integer> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(getRandomInteger(random));
        }

        return list;
    }

    private enum BenchmarkChoice { WARMUP, BENCHMARK }

    private void initRandomGenerators() {
        randomJavaUtilLinkedList = new Random(seed);
        randomJavaUtilArrayList  = new Random(seed);
        randomRoddeList          = new Random(seed);
        randomTreeList           = new Random(seed);
    }

    private void listsEqual() {
        listsEqual(roddeList,
                   linkedList, 
                   arrayList, 
                   treeList);
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

        profileAddFirst();
        profileAddLast();
        profileAddViaIndex();
        profileAppendCollection();
        profileAddCollection();
        profileGet();
        profileRemoveFirst();
        profileRemoveLast();
        profileRemoveViaIndex();
        profileRemoveObject();
        profileListIteratorAddition();
        profileListIteratorRemoval();
        profileStream();
        profileParallelStream();
        profileSubListClear();

        printTotalDurations();

        resetLists();
        zeroTimeDurationCounters();
    }

    private void zeroTimeDurationCounters() {
        totalMillisArrayList   = 0;
        totalMillisLinkedList  = 0;
        totalMillisRoddeList   = 0;
        totalMillisRoddeList = 0;
        totalMillisTreeList    = 0;
    }

    private void resetLists() {
        roddeList = new EnhancedLinkedList<>();
        linkedList  = new java.util.LinkedList<>();
        arrayList   = new ArrayList<>();
        treeList    = new TreeList<>();
    }

    private void profileAddFirst() {
        profileAddFirstRoddeListV2();
        profileAddFirstLinkedList();
        profileAddFirstArrayList();
        profileAddFirstTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileAddLast() {
        profileAddLastRoddeListV2();
        profileAddLastLinkedList();
        profileAddLastArrayList();
        profileAddLastTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileAddViaIndex() {
        profileAddIndexRoddeListV2();
        profileAddIndexLinkedList();
        profileAddIndexArrayList();
        profileAddIndexTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileAddCollection() {
        profileAddCollectionRoddeListV2();
        profileAddCollectionLinkedList();
        profileAddCollectionArrayList();
        profileAddCollectionTreeList();

        listsEqual();
        System.out.println();
    }
    
    private void profileGet() {
        profileGetRoddeListV2();
        profileGetLinkedList();
        profileGetArrayList();
        profileGetTreeList();
        
        listsEqual();
        
        System.out.println();
    }
    
    private void profileRemoveFirst() {
        profileRemoveFirstRoddeListV2();
        profileRemoveFirstLinkedList();
        profileRemoveFirstArrayList();
        profileRemoveFirstTreeList();
        
        listsEqual();
        System.out.println();
    }
    
    private void profileRemoveLast() {
        profileRemoveLastRoddeListV2();
        profileRemoveLastLinkedList();
        profileRemoveLastArrayList();
        profileRemoveLastTreeList();
        
        listsEqual();
        System.out.println();
    }

    private void profileAppendCollection() {
        profileAppendCollectionRoddeListV2();
        profileAppendCollectionLinkedList();
        profileAppendCollectionArrayList();
        profileAppendCollectionTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileRemoveViaIndex() {
        profileRemoveViaIndexRoddeListV2();
        profileRemoveViaIndexLinkedList();
        profileRemoveViaIndexArrayList();
        profileRemoveViaIndexTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileRemoveObject() {
        profileRemoveObjectRoddeListV2();
        profileRemoveObjectLinkedList();
        profileRemoveObjectArrayList();
        profileRemoveObjectTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileListIteratorAddition() {
        profileListIteratorAdditionRoddeListV2();
        profileListIteratorAdditionLinkedList();
        profileListIteratorAdditionArrayList();
        profileListIteratorAdditionTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileListIteratorRemoval() {
        profileListIteratorRemovalRoddeListV2();
        profileListIteratorRemovalLinkedList();
        profileListIteratorRemovalArrayList();
        profileListIteratorRemovalTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileStream() {
        profileStreamRoddeListV2();
        profileStreamLinkedList();
        profileStreamArrayList();
        profileStreamTreeList();

        listsEqual();
        System.out.println();
    }

    private void profileParallelStream() {
        profileParallelStreamRoddeListV2();
        profileParallelStreamLinkedList();
        profileParallelStreamArrayList();
        profileParallelStreamTreeList();

        Collections.sort(treeList);
        Collections.sort(roddeList);
        Collections.sort(arrayList);
        Collections.sort(linkedList);

        listsEqual();
        System.out.println();
    }
    
    private static List<Integer> getIntegerArray(int size) {
        List<Integer> integers = new ArrayList<>(size);
        
        for (int i = 0; i < size; ++i) {
            integers.add(Integer.valueOf(size % 900_000));
        }
        
        Collections.shuffle(integers);
        return integers;
    }
    
    private void profileSubListClear() {
        roddeList.clear();
        arrayList.clear();
        linkedList.clear();
        treeList.clear();
        
        roddeList.addAll(getIntegerArray(1_000_000));
        arrayList.addAll(roddeList);
        linkedList.addAll(roddeList);
        treeList.addAll(roddeList);
        
        profileSubListClearTreeList();
        profileSubListClearRoddeList();
        profileSubListClearArrayList();
        profileSubListClearLinkedList();
        
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

        System.out.println(
                treeList.getClass().getName() + 
                        " in (ms): " + 
                        totalMillisTreeList);
    }

    private long profileAddFirst(
            List<Integer> list, 
            int operations, 
            Random random) {

        long startMillis = System.currentTimeMillis();

        for (int i = 0; i < operations; i++) {
            list.add(0, getRandomInteger(random));
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
            list.add(list.size(), getRandomInteger(random));
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
            Integer value = getRandomInteger(random);
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
//            System.out.println("i = " + i);
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
    
    private long profileGet(List<Integer> list, int operations, Random random) {
        long startMillis = System.currentTimeMillis();
        
        for (int i = 0; i < operations; i++) {
            list.get(random.nextInt(list.size()));
        }
        
        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;
        
        System.out.println(
                list.getClass().getName() + ".get(int) in (ms): " +
                    durationMillis);
        
        return durationMillis;
    }
    
    private long profileRemoveFirst(List<Integer> list) {
        long startMillis = System.currentTimeMillis();
        
        if (!list.getClass().equals(Deque.class)) {
            for (int i = 0; i < REMOVE_FIRST_OPERATIONS; i++) {
                list.remove(0);
            }
        } else {
            for (int i = 0; i < REMOVE_FIRST_OPERATIONS; i++) {
                ((Deque<Integer>) list).removeFirst();
            }
        }
        
        
        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;
        
        System.out.println(
                list.getClass().getName() + ".removeFirst() in (ms): " +
                        durationMillis);
        
        return durationMillis;
    }
    
    private long profileRemoveLast(List<Integer> list) {
        long startMillis = System.currentTimeMillis();
        
        if (!list.getClass().equals(Deque.class)) {
            for (int i = 0; i < REMOVE_FIRST_OPERATIONS; i++) {
                list.remove(list.size() - 1);
            }
        } else {
            for (int i = 0; i < REMOVE_FIRST_OPERATIONS; i++) {
                ((Deque<Integer>) list).removeLast();
            }
        }
        
        
        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;
        
        System.out.println(
                list.getClass().getName() + ".removeLast() in (ms): " +
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

    private long profileRemoveObject(
            List<Integer> list, 
            int operations, 
            Random random) {

        long startMillis = System.currentTimeMillis();

        for (int i = 0; i < operations; i++) {
            list.remove(Integer.valueOf(getRandomInteger(random)));
        }

        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;

        System.out.println(
                list.getClass().getName() +
                        ".remove(Object) in (ms): " +
                        durationMillis);

        return durationMillis;
    }
    
    private long profileListIteratorRemoval(List<Integer> list) {
        long startMillis = System.currentTimeMillis();
        Iterator<Integer> iterator = list.iterator();
        int counter = 0;

        while (iterator.hasNext()) {
            iterator.next();

            // Remove every 2nd element:
            if (counter % 10 == 0) {
                try {
                    iterator.remove();
                } catch (AssertionError ae) {
                    System.err.println(ae.getMessage());
                    System.exit(1);
                }
            }

            counter++;
        }

        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;

        System.out.println(
                list.getClass().getName() +
                        ".iterator().remove() in (ms): " +
                        durationMillis);

        return durationMillis;
    }

    private long profileListIteratorAddition(
            List<Integer> list, Random random) {

        long startMillis = System.currentTimeMillis();
        ListIterator<Integer> iterator = list.listIterator(1);
        int counter = 0;

        while (iterator.hasNext()) {
            iterator.next();

            // Remove every 2nd element:
            if (counter % 10 == 0) {
                try {
                    Integer integer = Integer.valueOf(random.nextInt(10_000));
                    iterator.add(integer);
                } catch (AssertionError ae) {
                    System.err.println(ae.getMessage());
                    System.exit(1);
                }
            }

            counter++;
        }

        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;

        System.out.println(
                list.getClass().getName() +
                        ".iterator().add() in (ms): " +
                        durationMillis);

        return durationMillis;
    }

    private long profileStream(List<Integer> list) {
        long startMillis = System.currentTimeMillis();

        List<Integer> newList =
                list.stream().map(x -> 2 * x).collect(Collectors.toList());

        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;

        list.clear();
        list.addAll(newList);

        System.out.println(
                list.getClass().getName() +
                        ".stream() in (ms): " + durationMillis);

        return durationMillis;
    }

    private long profileParallelStream(List<Integer> list) {
        long startMillis = System.currentTimeMillis();

        List<Integer> newList = list.stream()
                        .parallel()
                        .map(x -> 2 * x)
                        .collect(Collectors.toList());

        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;

        list.clear();
        list.addAll(newList);

        System.out.println(
                list.getClass().getName() +
                        ".stream().parallel() in (ms): " + durationMillis);

        return durationMillis;
    }
    
    private long profileSubListClear(List<Integer> list) {
        Class<?> clazz = list.getClass();
        
        long startMillis;
        long endMillis;
        long durationMillis;
        
        int fromIndex;
        int toIndex;
        
        if (clazz.getSimpleName().equals("TreeList")) {
            fromIndex = list.size() / 2;
            toIndex = list.size() / 2 + 1;
            
            startMillis = System.currentTimeMillis();
            
            // Clear short range:
            clear(list, fromIndex, toIndex);
            
            fromIndex = 10;
            toIndex = list.size() - 9;
            
            // Clear long range:
            clear(list, fromIndex, toIndex);
            
            endMillis = System.currentTimeMillis();
        } else {
            fromIndex = list.size() / 2;
            toIndex = list.size() / 2 + 1;
            
            startMillis = System.currentTimeMillis();
            
            // Clear short range:
            list.subList(fromIndex, toIndex).clear();
            
            fromIndex = 10;
            toIndex = list.size() - 9;
            
            // Clear long range:
            list.subList(fromIndex, toIndex).clear();
            
            endMillis = System.currentTimeMillis();
        }
        
        durationMillis = endMillis - startMillis;
        
        System.out.println(
                list.getClass().getName() + 
                        ".subList(...).clear() in (ms): " + 
                        durationMillis);
        
        return durationMillis;
    }

    private void profileAddFirstRoddeListV2() {
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

    private void profileAddFirstTreeList() {
        totalMillisTreeList += 
                profileAddFirst(treeList,
                                ADD_FIRST_OPERATIONS,
                                randomTreeList);
    }

    private void profileAddLastRoddeListV2() {
        totalMillisRoddeList += 
            profileAddLast(roddeList, ADD_LAST_OPERATIONS, randomRoddeList);
    }

    private void profileAddLastLinkedList() {
        totalMillisLinkedList += 
                profileAddLast(
                        linkedList, 
                        ADD_LAST_OPERATIONS, 
                        randomJavaUtilLinkedList);
    }

    private void profileAddLastArrayList() {
        totalMillisArrayList += 
                profileAddLast(arrayList, 
                               ADD_LAST_OPERATIONS, 
                               randomJavaUtilArrayList);
    }

    private void profileAddLastTreeList() {
        totalMillisTreeList += 
                profileAddLast(treeList, 
                               ADD_LAST_OPERATIONS, 
                               randomTreeList);
    }

    private void profileAddIndexRoddeListV2() {
        totalMillisRoddeList += 
                profileAddIndex(roddeList,
                                ADD_AT_OPERATIONS, 
                                randomRoddeList);
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

    private void profileAddIndexTreeList() {
        totalMillisTreeList +=
                profileAddIndex(
                        treeList, 
                        ADD_AT_OPERATIONS, 
                        randomTreeList);
    }

    private void profileAddCollectionRoddeListV2() {
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

    private void profileAddCollectionTreeList() {
        totalMillisTreeList +=
                profileAddCollection(
                        treeList,
                        ADD_COLLECTION_AT_OPERATIONS,
                        randomTreeList);
    }
    
    private void profileGetRoddeListV2() {
        totalMillisRoddeList += 
                profileGet(roddeList, GET_OPERATIONS, randomRoddeList);
    }
    
    private void profileGetLinkedList() {
        totalMillisLinkedList += 
                profileGet(linkedList, 
                           GET_OPERATIONS, 
                           randomJavaUtilLinkedList);
    }
    
    private void profileGetArrayList() {
        totalMillisArrayList += 
                profileGet(arrayList, 
                           GET_OPERATIONS, 
                           randomJavaUtilArrayList);
    }
    
    private void profileGetTreeList() {
        totalMillisTreeList += 
                profileGet(treeList, GET_OPERATIONS, randomTreeList);
    }

    private void profileRemoveFirstRoddeListV2() {
        totalMillisRoddeList += profileRemoveFirst(roddeList);
    }

    private void profileRemoveFirstLinkedList() {
        totalMillisLinkedList += profileRemoveFirst(linkedList);
    }

    private void profileRemoveFirstArrayList() {
        totalMillisArrayList += profileRemoveFirst(arrayList);
    }

    private void profileRemoveFirstTreeList() {
        totalMillisTreeList += profileRemoveFirst(treeList);        
    }

    private void profileRemoveLastRoddeListV2() {
        totalMillisRoddeList += profileRemoveLast(roddeList);
    }

    private void profileRemoveLastLinkedList() {
        totalMillisLinkedList += profileRemoveLast(linkedList);
    }

    private void profileRemoveLastArrayList() {
        totalMillisArrayList += profileRemoveLast(arrayList);
    }

    private void profileRemoveLastTreeList() {
        totalMillisTreeList += profileRemoveLast(treeList);        
    }
    
    private void profileAppendCollectionRoddeListV2() {
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

    private void profileAppendCollectionTreeList() {
        totalMillisTreeList +=
                profileAppendCollection(
                        treeList, 
                        ADD_LAST_COLLECTION_OPERATIONS, 
                        randomTreeList);
    }

    private void profileRemoveViaIndexRoddeListV2() {
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

    private void profileRemoveViaIndexTreeList() {
        totalMillisTreeList += 
                profileRemoveViaIndex(
                        treeList, 
                        REMOVE_VIA_INDEX_OPERATIONS, 
                        randomTreeList);
    }

    private void profileRemoveObjectRoddeListV2() {
        totalMillisRoddeList += 
                profileRemoveObject(
                        roddeList, 
                        REMOVE_OBJECT_OPERATIONS, 
                        randomRoddeList);
    }

    private void profileRemoveObjectLinkedList() {    
        totalMillisLinkedList += 
                profileRemoveObject(
                        linkedList, 
                        REMOVE_OBJECT_OPERATIONS, 
                        randomJavaUtilLinkedList);
    }

    private void profileRemoveObjectArrayList() {
        totalMillisArrayList += 
                profileRemoveObject(
                        arrayList, 
                        REMOVE_OBJECT_OPERATIONS, 
                        randomJavaUtilArrayList);
    }

    private void profileRemoveObjectTreeList() {
        totalMillisTreeList += 
                profileRemoveObject(
                        treeList,
                        REMOVE_OBJECT_OPERATIONS, 
                        randomTreeList);
    }
    
    private void profileListIteratorRemovalRoddeListV2() {
        totalMillisRoddeList += profileListIteratorRemoval(roddeList);
    }

    private void profileListIteratorRemovalLinkedList() {
        totalMillisLinkedList += profileListIteratorRemoval(linkedList);
    }

    private void profileListIteratorRemovalArrayList() {
        totalMillisArrayList += profileListIteratorRemoval(arrayList);
    }

    private void profileListIteratorRemovalTreeList() {
        totalMillisTreeList += profileListIteratorRemoval(treeList);
    }
    
    private void profileListIteratorAdditionRoddeListV2() {
        totalMillisRoddeList += 
                profileListIteratorAddition(roddeList, randomRoddeList);
    }

    private void profileListIteratorAdditionLinkedList() {
        totalMillisLinkedList +=
                profileListIteratorAddition(
                        linkedList, 
                        randomJavaUtilLinkedList);
    }

    private void profileListIteratorAdditionArrayList() {
        totalMillisArrayList += 
                profileListIteratorAddition(
                        arrayList, 
                        randomJavaUtilArrayList);
    }

    private void profileListIteratorAdditionTreeList() {
        totalMillisTreeList += 
                profileListIteratorAddition(treeList, randomTreeList);
    }

    private void profileStreamRoddeListV2() {
        totalMillisRoddeList += profileStream(roddeList);
    }

    private void profileStreamLinkedList() {
        totalMillisLinkedList += profileStream(linkedList);
    }

    private void profileStreamArrayList() {
        totalMillisArrayList += profileStream(arrayList);
    }

    private void profileStreamTreeList() {
        totalMillisTreeList += profileStream(treeList);
    }

    private void profileParallelStreamRoddeListV2() {
        totalMillisRoddeList += profileParallelStream(roddeList);
    }

    private void profileParallelStreamLinkedList() {
        totalMillisLinkedList += profileParallelStream(linkedList);
    }

    private void profileParallelStreamArrayList() {
        totalMillisArrayList += profileParallelStream(arrayList);
    }

    private void profileParallelStreamTreeList() {
        totalMillisTreeList += profileParallelStream(treeList);
    }

    private void profileSubListClearRoddeList() {
        totalMillisRoddeList += profileSubListClear(roddeList);
    }

    private void profileSubListClearLinkedList() {
        totalMillisLinkedList += profileSubListClear(linkedList);
    }

    private void profileSubListClearArrayList() {
        totalMillisArrayList += profileSubListClear(arrayList);
    }

    private void profileSubListClearTreeList() {
        totalMillisTreeList += profileSubListClear(treeList);
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
    
    private void clearGetLists() {
        for (List<Integer> list : getLists) {
            list.clear();
        }
    }
}