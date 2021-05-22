package com.github.coderodde.util.benchmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class LinkedListDebugger {
    
    private static final int TEST_LOOP_ITERATIONS = 1_000;
    
    private final Random random;
    
    public LinkedListDebugger() {
        long seed = 1621691929766L; //System.currentTimeMillis();
        System.out.println("seed <- " + seed);
        random = new Random(seed);
    }
    
    public void run() {
        List<Integer> roddeList = new com.github.coderodde.util.LinkedList<>();
        List<Integer> arrayList = new ArrayList<>();
        
        populate(roddeList, arrayList);
        checkEqualLists(roddeList, arrayList);
        
        int op = 0;
        
        while (true) {
            System.out.println("Operation " + op);
            
            List<Integer> randomList = getRandomList();
            int index = random.nextInt(roddeList.size());
            System.out.println("INDEX: " + index);
            
            roddeList.addAll(index, randomList);
            arrayList.addAll(index, randomList);
            
            checkEqualLists(roddeList, arrayList);
            op++;
        }
    }
    
    private static void checkEqualLists(List<Integer> list1, 
                                        List<Integer> list2) {
        
        Iterator<Integer> iterator1 = list1.iterator();
        Iterator<Integer> iterator2 = list2.iterator();
        int index = 0;
        
        while (iterator1.hasNext() && iterator2.hasNext()) {
            Integer integer1 = iterator1.next();
            Integer integer2 = iterator2.next();
            
            if (!integer1.equals(integer2)) {
                throw new IllegalStateException(
                        integer1 + " vs. " + integer2 + " at index " + index);
            }
            
            index++;
        }
        
        if (iterator1.hasNext() || iterator2.hasNext()) {
            throw new IllegalStateException("Bad iterators");
        }
    }
    
    private List<Integer> getRandomList() {
        int size = random.nextInt(3);
        List<Integer> list = new ArrayList<>(size);
        
        for (int i = 0; i < size; i++) {
            list.add(random.nextInt() % 1000);
        }
        
        return list;
    }
    
    private void populate(List<Integer> roddeList, List<Integer> arrayList) {
        for (int op = 0; op < 5; op++) {
            List<Integer> coll = getRandomList();
            roddeList.addAll(coll);
            arrayList.addAll(coll);
            
            if (!roddeList.equals(arrayList)) {
                throw new IllegalStateException("op = " + op);
            }
        }
    }
}
