package com.github.coderodde.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.function.Consumer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class LinkedListTest {

    private final LinkedList<Integer> list = new LinkedList<>();
    
    @Before
    public void setUp() {
        list.clear();
    }

    @Test
    public void testAdd() {
        
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        
        list.add(1);
        
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        
        assertEquals(Integer.valueOf(1), list.get(0));
        
        list.add(2);
        
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
    }
    
    @Test
    public void testAddFirst() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        
        list.addFirst(1);
        
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        
        assertEquals(Integer.valueOf(1), list.get(0));
        
        list.addFirst(2);
        
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testThrowsOnAccessingEmptyList() {
        list.get(0);
    }
    
    @Test(expected = IndexOutOfBoundsException.class) 
    public void testOnNegativeIndexInEmptyList() {
        list.get(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class) 
    public void testOnNegativeIndexInNonEmptyList() {
        list.addFirst(10);
        list.get(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class) 
    public void testOnTooLargeIndex() {
        list.addFirst(10);
        list.addLast(20);
        list.get(2);
    }
    
    @Test
    public void testAddIndexAndElement() {
        list.add(0, 1);
        assertEquals(Integer.valueOf(1), list.get(0));
        
        list.add(0, 2);
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        
        list.add(2, 10);
        
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        assertEquals(Integer.valueOf(10), list.get(2));
        
        list.add(2, 100);
        
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        assertEquals(Integer.valueOf(100), list.get(2));
        assertEquals(Integer.valueOf(10), list.get(3));
    }
    
    @Test
    public void testAddCollectionOneElementToEmptyList() {
        List<Integer> c = new ArrayList<>();
        c.add(100);
        
        list.addAll(c);
        
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(100), list.get(0));
    }
    
    @Test
    public void testAddCollectionThreeElementsToEmptyList() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        
        List<Integer> c = Arrays.asList(1, 2, 3);
        
        list.addAll(c);
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());
        
        for (int i = 0; i < list.size(); i++) {
            assertEquals(Integer.valueOf(i + 1), list.get(i));
        }
    }
    
    @Test
    public void testAddCollectionAtIndex() {
        list.addAll(0, Arrays.asList(2, 3)); // setAll
        list.addAll(0, Arrays.asList(0, 1)); // prependAll
        list.addAll(4, Arrays.asList(6, 7)); // appendAll
        list.addAll(4, Arrays.asList(4, 5)); // insertAll
        
        for (int i = 0; i < list.size(); i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }
    
    @Test
    public void testRemoveInt() {
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        
        assertEquals(Integer.valueOf(0), list.remove(0));
        assertEquals(Integer.valueOf(4), list.remove(3));
        assertEquals(Integer.valueOf(2), list.remove(1));
        assertEquals(Integer.valueOf(1), list.remove(0));
        assertEquals(Integer.valueOf(3), list.remove(0));
    }
    
    @Test
    public void testBasicIteratorUsage() {
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
        
        Iterator<Integer> iterator = list.iterator();
        
        for (int i = 0; i < 1000; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(Integer.valueOf(i), iterator.next());
        }
        
        assertFalse(iterator.hasNext());
    }
    
    @Test
    public void bruteForceAddCollectionAtIndex() {
        long seed = System.currentTimeMillis();
        System.out.println("bruteForceAddCollectionAtIndex.seed = " + seed);
        Random random = new Random(seed);
        
        list.addAll(getIntegerList());
        
        java.util.LinkedList<Integer> referenceList = 
                new java.util.LinkedList<>(list);
        
        for (int op = 0; op < 100; op++) {
            int index = random.nextInt(list.size());
            Collection<Integer> coll = getIntegerList(random.nextInt(40));
            
            referenceList.addAll(index, coll);
            list.addAll(index, coll);
            
            if (!listsEqual(list, referenceList)) {
                fail("Lists not equal!");
            }
        }
    }
    
    @Test
    public void removeAtIndex() {
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        
        assertEquals(Integer.valueOf(2), list.remove(2));
        assertEquals(Integer.valueOf(0), list.remove(0));
        assertEquals(Integer.valueOf(4), list.remove(2));
    }
    
    @Test
    public void removeObject() {
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        
        assertFalse(list.remove(Integer.valueOf(10)));
        assertFalse(list.remove(null));
        
        list.add(3, null);
        
        assertTrue(list.remove(null));
        
        assertTrue(list.remove(Integer.valueOf(4)));
        assertTrue(list.remove(Integer.valueOf(0)));
        assertTrue(list.remove(Integer.valueOf(2)));
        assertFalse(list.remove(Integer.valueOf(2)));
    }
    
    @Test
    public void basicIteratorTraversal() {
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        
        Iterator<Integer> iter = list.iterator();
        
        for (int i = 0; i < list.size(); i++) {
            assertTrue(iter.hasNext());
            assertEquals(Integer.valueOf(i), iter.next());
        }
        
        iter = list.iterator();
        
        class MyConsumer implements Consumer<Integer> {

            int total;
            
            @Override
            public void accept(Integer t) {
                total += t;
            }
        }
        
        MyConsumer myConsumer = new MyConsumer();
        
        list.iterator().forEachRemaining(myConsumer);
        assertEquals(10, myConsumer.total);
    }
    
    @Test
    public void basicIteratorRemoval() {
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        Iterator<Integer> iter = list.iterator();
        
        iter.next();
        iter.next();
        iter.remove();
        
        assertEquals(4, list.size());
        
        iter = list.iterator();
        iter.next();
        iter.remove();
        
        assertEquals(3, list.size());
        
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(3), list.get(1));
        assertEquals(Integer.valueOf(4), list.get(2));
    }
    
    @Test
    public void enhancedIteratorTraversal() {
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        ListIterator<Integer> iter = list.listIterator();
        
        assertFalse(iter.hasPrevious());
        
        for (int i = 0; i < list.size(); i++) {
            assertTrue(iter.hasNext());
            assertEquals(Integer.valueOf(i), iter.next());
        }
        
        assertFalse(iter.hasNext());
        
        for (int i = 4; i >= 0; i--) {
            assertTrue(iter.hasPrevious());
            assertEquals(Integer.valueOf(i), iter.previous());
        }
        
        iter = list.listIterator(2);
        
        assertEquals(Integer.valueOf(2), iter.next());
        assertEquals(Integer.valueOf(2), iter.previous());
        
        iter = list.listIterator(3);
        
        assertEquals(Integer.valueOf(3), iter.next());
        assertEquals(Integer.valueOf(4), iter.next());
        
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
    }
    
    @Test
    public void enhancedIteratorAddition() {
        list.addAll(Arrays.asList(1, 2, 3));
        ListIterator<Integer> iter = list.listIterator();
        
        iter.add(0);
        
        while (iter.hasNext()) {
            iter.next();
        }
        
        iter.add(4);
        iter = list.listIterator();
        
        for (int i = 0; i < list.size(); i++) {
            assertEquals(Integer.valueOf(i), iter.next());
        }
        
        iter = list.listIterator(2);
        iter.add(10);
        
        assertEquals(Integer.valueOf(10), list.get(2));
    }
    
    @Test
    public void findFailingIterat() {
        list.addAll(getIntegerList(345_850));
        Iterator<Integer> iterator = list.iterator();
        int counter = 0;
        
        while (iterator.hasNext()) {
            iterator.next();
            
            // Remove every 2nd element:
            if (counter % 10 == 0) {
                iterator.remove();
            }
            
            counter++;
        }
    }
    
    @Test
    public void bruteForceIteratorRemove() throws Exception {
        list.addAll(getIntegerList(1000));
        
        int counter = 0;
        List<Integer> arrayList = new ArrayList<>(list);
        Iterator<Integer> iter = list.iterator();
        Iterator<Integer> arrayListIter = arrayList.iterator();
        int totalIterations = 0;
        
        while (iter.hasNext()) {
            iter.next();
            arrayListIter.next();
            
            if (counter % 10 == 0) {
                
                try {
                    iter.remove();
                } catch (IllegalStateException ex) {
                    throw new Exception(ex);
                }
                
                arrayListIter.remove();
                counter = 0;
            } else {
                counter++;
            }
            
            if (!listsEqual(list, arrayList)) {
                throw new IllegalStateException(
                        "totalIterations = " + totalIterations);
            }
            
            totalIterations++;
        }
    }
    
    @Test
    public void bruteForceRemoveObjectBeforeIteratorRemove() {
        LinkedList<String> ll = new com.github.coderodde.util.LinkedList<>();
        
        ll.add("a");
        ll.add("b");
        ll.add("c");
        ll.add("d");
    
        ll.remove("b");
        ll.remove("c");
        ll.remove("d");
        ll.remove("a");
    }
    
    @Test
    public void findFailingRemoveObject() {
        java.util.LinkedList<Integer> referenceList = 
                new java.util.LinkedList<>();
        
        list.addAll(getIntegerList(10));
        referenceList.addAll(list);
        
        Integer probe = list.get(1);
        
        list.remove(probe);
        referenceList.remove(probe);
        
        Iterator<Integer> iterator1 = list.iterator();
        Iterator<Integer> iterator2 = referenceList.iterator();
        
        Random random = new Random(100L);
        
        while (!list.isEmpty()) {
            if (!iterator1.hasNext()) {
                
                if (iterator2.hasNext()) {
                    throw new IllegalStateException();
                }
                
                iterator1 = list.iterator();
                iterator2 = referenceList.iterator();
                continue;
            }
            
            iterator1.next();
            iterator2.next();
            
            if (random.nextBoolean()) {
                iterator1.remove();
                iterator2.remove();
                assertTrue(listsEqual(list, referenceList));
            }
        }
        
        assertTrue(listsEqual(list, referenceList));
    }
    
    private static boolean listsEqual(
            com.github.coderodde.util.LinkedList<Integer> list1, 
            java.util.List<Integer> list2) {
        
        if (list1.size() != list2.size()) {
            return false;
        }
        
        Iterator<Integer> iter1 = list1.iterator();
        Iterator<Integer> iter2 = list2.iterator();
        
        while (iter1.hasNext() && iter2.hasNext()) {
            Integer int1 = iter1.next();
            Integer int2 = iter2.next();
            
            if (!int1.equals(int2)) {
                return false;
            }
        }
        
        if (iter1.hasNext() || iter2.hasNext()) {
            throw new IllegalStateException();
        }
        
        return true;
    }   
    
    private static List<Integer> getIntegerList() {
        return getIntegerList(100);
    }

    private static List<Integer> getIntegerList(int length) {
        List<Integer> list = new ArrayList<>(length);
        
        for (int i = 0; i < length; i++) {
            list.add(i);
        }
        
        return list;
    }
}
