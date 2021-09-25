package com.github.coderodde.util;

import com.github.coderodde.util.LinkedList.Finger;
import com.github.coderodde.util.LinkedList.FingerStack;
import com.github.coderodde.util.LinkedList.Node;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNull;
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
    public void constructAdd() {
        List<String> l = new LinkedList<>(Arrays.asList("a", "b", "c"));
        
        assertEquals(3, l.size());
        
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }

    @Test
    public void contains() {
        assertFalse(list.contains(Integer.valueOf(1)));
        assertFalse(list.contains(Integer.valueOf(2)));
        assertFalse(list.contains(Integer.valueOf(3)));
        
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        
        list.addAll(Arrays.asList(1, 2, 3));
        
        assertEquals(3, list.size());
        assertFalse(list.isEmpty());
        
        assertTrue(list.contains(Integer.valueOf(1)));
        assertTrue(list.contains(Integer.valueOf(2)));
        assertTrue(list.contains(Integer.valueOf(3)));
    }
    
    @Test
    public void descendingIterator() {
        list.addAll(Arrays.asList(1, 2, 3));
        Iterator<Integer> iterator = list.descendingIterator();
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(3), iterator.next());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(2), iterator.next());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(1), iterator.next());
        
        assertFalse(iterator.hasNext());
    }
    
    @Test
    public void descendingIteratorRemove() {
        list.addAll(Arrays.asList(1, 2, 3));
        Iterator<Integer> iterator = list.descendingIterator();
        
        iterator.next();
        iterator.remove();
        
        assertEquals(2, list.size());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(2), iterator.next());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(1), iterator.next());
        
        assertFalse(iterator.hasNext());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void elementThrowsOnEmptyList() {
        list.element();
    }

    @Test
    public void element() {
        list.add(1);
        list.add(2);
        
        assertEquals(Integer.valueOf(1), list.element());
        
        list.remove();
        
        assertEquals(Integer.valueOf(2), list.element());
    }
    
    @Test
    public void listEquals() {
        list.addAll(Arrays.asList(1, 2, 3, 4));
        List<Integer> otherList = Arrays.asList(1, 2, 3, 4);
        
        assertTrue(list.equals(otherList));
        
        list.remove(Integer.valueOf(3));
        
        assertFalse(list.equals(otherList));
        
        assertFalse(list.equals(null));
        assertTrue(list.equals(list));
        
        Set<Integer> set = new HashSet<>(list);
        
        assertFalse(list.equals(set));
        
        list.clear();
        list.addAll(Arrays.asList(0, 1, 2, 3));
        otherList = Arrays.asList(0, 1, 4, 3);
        
        assertFalse(list.equals(otherList));
    }
    
    class DummyList extends ArrayList<Integer> {
        private final class DummyIterator implements Iterator<Integer> {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                return Integer.valueOf(0);
            }
        }
        
        public Iterator<Integer> iterator()  {
            return new DummyIterator();
        }
        
        public int size() {
            return 2;
        }
    }
    
    @Test(expected = IllegalStateException.class) 
    public void listEqualsThrowsOnBadIterator() {
        DummyList dummyList = new DummyList();
        list.addAll(Arrays.asList(0, 0));
        list.equals(dummyList);
    }
    
    @Test
    public void offer() {
        assertTrue(list.equals(Arrays.asList()));
        
        list.offer(1);
        
        assertTrue(list.equals(Arrays.asList(1)));
        
        list.offer(2);
        
        assertTrue(list.equals(Arrays.asList(1, 2)));
    }
    
    @Test
    public void offerFirst() {
        assertTrue(list.equals(Arrays.asList()));
        
        list.offerFirst(1);
        
        assertTrue(list.equals(Arrays.asList(1)));
        
        list.offerFirst(2);
        
        assertTrue(list.equals(Arrays.asList(2, 1)));
    }
    
    @Test
    public void offerLast() {
        assertTrue(list.equals(Arrays.asList()));
        
        list.offerLast(1);
        
        assertTrue(list.equals(Arrays.asList(1)));
        
        list.offerLast(2);
        
        assertTrue(list.equals(Arrays.asList(1, 2)));
    }
    
    @Test
    public void peek() {
        assertNull(list.peek());
        
        list.addLast(0);
        
        assertEquals(Integer.valueOf(0), list.peek());
        
        list.addLast(1);
        
        assertEquals(Integer.valueOf(0), list.peek());
    
        list.addFirst(Integer.valueOf(-1));

        assertEquals(Integer.valueOf(-1), list.peek());
    }
    
    @Test
    public void peekFirst() {
        assertNull(list.peek());
        
        list.addLast(0);
        
        assertEquals(Integer.valueOf(0), list.peekFirst());
        
        list.addFirst(1);
        
        assertEquals(Integer.valueOf(1), list.peekFirst());
    
        list.addFirst(Integer.valueOf(-1));

        assertEquals(Integer.valueOf(-1), list.peekFirst());
    }
    
    @Test
    public void peekLast() {
        assertNull(list.peek());
        
        list.addLast(0);
        
        assertEquals(Integer.valueOf(0), list.peekLast());
        
        list.addLast(1);
        
        assertEquals(Integer.valueOf(1), list.peekLast());
    
        list.addLast(2);

        assertEquals(Integer.valueOf(2), list.peekLast());
    }
    
    @Test
    public void poll() {
        assertNull(list.poll());
        
        list.addAll(Arrays.asList(1, 2, 3));
        
        assertEquals(Integer.valueOf(1), list.poll());
        assertEquals(Integer.valueOf(2), list.poll());
        assertEquals(Integer.valueOf(3), list.poll());
    }
    
    @Test
    public void pollFirst() {
        assertNull(list.pollFirst());
        
        list.addAll(Arrays.asList(1, 2, 3));
        
        assertEquals(Integer.valueOf(1), list.pollFirst());
        assertEquals(Integer.valueOf(2), list.pollFirst());
        assertEquals(Integer.valueOf(3), list.pollFirst());
    }
    
    @Test
    public void pollLast() {
        assertNull(list.pollLast());
        
        list.addAll(Arrays.asList(1, 2, 3));
        
        assertEquals(Integer.valueOf(3), list.pollLast());
        assertEquals(Integer.valueOf(2), list.pollLast());
        assertEquals(Integer.valueOf(1), list.pollLast());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void removeFirstThrowsOnEmptyList() {
        list.removeFirst();
    }
    
    @Test
    public void pop() {
        list.addAll(Arrays.asList(1, 2, 3));
        
        assertEquals(Integer.valueOf(1), list.pop());
        assertEquals(Integer.valueOf(2), list.pop());
        assertEquals(Integer.valueOf(3), list.pop());
    }
    
    @Test
    public void push() {
        list.push(1);
        list.push(2);
        list.push(3);
        
        assertTrue(list.equals(Arrays.asList(3, 2, 1)));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void removeThrowsOnEmptyList() {
        list.remove();
    }
    
    class BadList extends com.github.coderodde.util.LinkedList<Integer> {
        
        class BadListIterator implements Iterator<Integer> {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                return Integer.valueOf(3);
            }
        }
        
        @Override
        public Iterator<Integer> iterator() {
            return new BadListIterator();
        };
        
        public int size() {
            return 2;
        }
    }
    
    @Test(expected = IllegalStateException.class) 
    public void badThisIterator() {
        List<Integer> arrayList = Arrays.asList(3, 3);
        BadList badList = new BadList();
        badList.addAll(Arrays.asList(3, 3));
        badList.equals(arrayList);
    }
    
    @Test
    public void removeFirstOccurrenceOfNull() {
        list.addAll(Arrays.asList(1, 2, null, 4, null, 6));
        
        assertTrue(list.removeFirstOccurrence(null));
        
        // Remove the last null value:
        list.set(3, 10);
        
        assertFalse(list.removeFirstOccurrence(null));
    }
    
    @Test
    public void removeLastOccurrenceOfNull() {
        list.addAll(Arrays.asList(1, 2, null, 4, null, 6));
        
        assertTrue(list.removeLastOccurrence(null));
        
        // Remove the last null value:
        list.set(2, 10);
        
        assertFalse(list.removeLastOccurrence(null));
    }
    
    @Test
    public void appendAll() {
        list.addAll(Arrays.asList(0, 1, 2));
        
        List<Integer> arrayList = new ArrayList<>();
        
        for (int i = 3; i < 20_000; i++) {
            arrayList.add(i);
        }
        
        list.addAll(arrayList);
        
        for (int i = 0; i < 20_000; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }
    
    @Test
    public void prependAll() {
        List<Integer> l = new ArrayList<>();
        
        for (int i = 0; i < 10_000; i++) {
            l.add(i);
        }
        
        list.addAll(l);
        
        l = new ArrayList<>();
        
        for (int i = 10_000; i < 20_000; i++) {
            l.add(i);
        }
        
        list.addAll(0, l);
        
        int index = 0;
        
        for (int i = 10_000; i < 20_000; i++) {
            assertEquals(Integer.valueOf(i), list.get(index++));
        }
        
        for (int i = 0; i < 10_000; i++) {
            assertEquals(Integer.valueOf(i), list.get(index++));
        }
    }
    
    @Test
    public void insertAll() {
        for (int i = 0; i < 20_000; i++) {
            list.add(i);
        }
        
        List<Integer> arrayList = new ArrayList<>(10_000);
        
        for (int i = 20_000; i < 30_000; i++) {
            arrayList.add(i);
        }
        
        list.addAll(10_000, arrayList);
        
        int index = 0;
        
        for (int i = 0; i < 10_000; i++) {
            assertEquals(Integer.valueOf(i), list.get(index++));
        }
        
        for (int i = 20_000; i < 30_000; i++) {
            assertEquals(Integer.valueOf(i), list.get(index++));
        }
        
        for (int i = 10_000; i < 20_000; i++) {
            assertEquals(Integer.valueOf(i), list.get(index++));    
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkPositionIndexThrowsOnNegativeIndex() {
        list.add(-1, Integer.valueOf(0));
    }
    
    @Test(expected = IndexOutOfBoundsException.class) 
    public void checkPositionIndxThrowsOnTooLargeIndex() {
        list.add(Integer.valueOf(0));
        
        list.add(2, Integer.valueOf(1));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void removeLastThrowsOnEmptyList() {
        list.removeLast();
    }
    
    @Test(expected = NoSuchElementException.class)
    public void getFirstThrowsOnEmptyList() {
        list.getFirst();
    }
    
    @Test
    public void getFirst() {
        list.addAll(Arrays.asList(10, 20));
        assertEquals(Integer.valueOf(10), list.getFirst());
        
        list.removeFirst();
        
        assertEquals(Integer.valueOf(20), list.getFirst());
    }
    
    @Test(expected = IllegalStateException.class)
    public void moveFingerOutOfRemovalLocation() {
        list.addAll(Arrays.asList(1, 2, 3));
        
        list.fingerStack.fingerArray[0] = new Finger<>(list.node(0), 0);
        list.fingerStack.fingerArray[1] = new Finger<>(list.node(1), 1);
        list.fingerStack.fingerArray[2] = new Finger<>(list.node(2), 2);
        
        list.fingerStack.fingerIndexSet.add(0);
        list.fingerStack.fingerIndexSet.add(1);
        list.fingerStack.fingerIndexSet.add(2);
        
        list.fingerStack.size = 3;
        list.moveFingerOutOfRemovalLocation(list.fingerStack.fingerArray[1]);
    }
    
    @Test
    public void nodeToString() {
        Node<String> node = new Node<>();
        node.item = "hello";
        assertEquals("[Node; item = hello]", node.toString());
    }
    
    @Test
    public void fingerToString() {
        Node<String> node = new Node<>();
        node.item = "World";
        Finger<String> finger = new Finger<>(node, 1);
        assertEquals("[Finger; index = 1, item = World]", finger.toString());
    }
    
    @Test
    public void fingerStackToString() {
        FingerStack<String> fingerStack = new FingerStack<>();
        
        Node<String> node1 = new Node<>();
        Node<String> node2 = new Node<>();
        Node<String> node3 = new Node<>();
        
        Finger<String> finger1 = new Finger<>(node1, 1);
        Finger<String> finger2 = new Finger<>(node2, 2);
        Finger<String> finger3 = new Finger<>(node3, 2);
        
        fingerStack.push(finger1);
        fingerStack.push(finger2);
        
        assertEquals("size = 2", fingerStack.toString());
        
        fingerStack.push(finger3);
        
        assertEquals("size = 3", fingerStack.toString());
    }
    
    @Test
    public void contractFingerStack() {
        FingerStack<Integer> fingerStack = new FingerStack<>();
        
        for (int i = 0; i < 100; i++) {
            Node<Integer> node = new Node<>();
            node.item = i;
            
            Finger<Integer> finger = new Finger<>(node, i);
            fingerStack.push(finger);
        }
        
        while (fingerStack.size() > 0) {
            fingerStack.pop();
        }
        
        assertEquals(FingerStack.INITIAL_CAPACITY, 
                     fingerStack.fingerArray.length);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void getLastThrowsOnEmptyList() {
        list.getLast();
    }
    
    @Test
    public void getLast() {
        list.addAll(Arrays.asList(10, 20));
        assertEquals(Integer.valueOf(20), list.getLast());
        
        list.removeLast();
        
        assertEquals(Integer.valueOf(10), list.getLast());
    }
    
    @Test
    public void indexOfNull() {
        list.addAll(Arrays.asList(1, 2, null, 3, null, 4));
        
        assertEquals(2, list.indexOf(null));
        
        list.set(2, 5);
        list.set(4, 10);
        
        assertEquals(-1, list.indexOf(null));
    }
    
    @Test
    public void lastIndexOfNull() {
        list.addAll(Arrays.asList(1, 2, null, 3, null, 4));
        
        assertEquals(4, list.lastIndexOf(null));
        
        list.set(2, 5);
        list.set(4, 10);
        
        assertEquals(-1, list.lastIndexOf(null));
    }
    
    @Test // checked!
    public void add() {
        bar("add");
        
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
        System.out.println(getBar("add done!"));
    }

    @Test // checked!
    public void addFirst() {
        bar("addFirst");
        
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
        
        bar("addFirst done!");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void throwsOnAccessingEmptyList() {
        list.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class) 
    public void throwsOnNegativeIndexInEmptyList() {
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class) 
    public void throwsOnNegativeIndexInNonEmptyList() {
        list.addFirst(10);
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class) 
    public void throwsOnTooLargeIndex() {
        list.addFirst(10);
        list.addLast(20);
        list.get(2);
    }

    @Test // checked!
    public void addIndexAndElement() {
        bar("addIndexedAndElement");
        
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
        
        bar("addIndexedAndElement done!");
    }

    @Test // checked!
    public void addCollectionOneElementToEmptyList() {
        bar("addCollectionOneElementToEmptyList");
        
        List<Integer> c = new ArrayList<>();
        c.add(100);

        list.addAll(c);

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(100), list.get(0));
        
        bar("addCollectionOneElementToEmptyList done!");
    }

    @Test // checked!
    public void addCollectionThreeElementsToEmptyList() {
        bar("addCollectionThreeElementsToEmptyList");
        
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());

        List<Integer> c = Arrays.asList(1, 2, 3);

        list.addAll(c);
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(Integer.valueOf(i + 1), list.get(i));
        }
        
        bar("addCollectionThreeElementsToEmptyList done!");
    }

    @Test // checked!
    public void addCollectionAtIndex() {
        bar("addCollectionAtIndex");
        
        list.addAll(0, Arrays.asList(2, 3)); // setAll
        list.checkInvariant();
        list.addAll(0, Arrays.asList(0, 1)); // prependAll
        list.checkInvariant();
        list.addAll(4, Arrays.asList(6, 7)); // appendAll
        list.checkInvariant();
        list.addAll(4, Arrays.asList(4, 5)); // insertAll
        list.checkInvariant();

        for (int i = 0; i < list.size(); i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
        
        bar("addCollectionAtIndex done!");
    }

    @Test // shadowed
    public void removeInt() {
        bar("removeInt");
        
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));

        // [0, 1, 2, 3, 4]
        assertEquals(Integer.valueOf(0), list.remove(0));
        // [1, 2, 3, 4]
        assertEquals(Integer.valueOf(4), list.remove(3));
        // [1, 2, 3]
        assertEquals(Integer.valueOf(2), list.remove(1));
        // [1, 3]
        assertEquals(Integer.valueOf(1), list.remove(0));
        // [3]
        assertEquals(Integer.valueOf(3), list.remove(0));
        // []
        bar("removeInt done!");
    }

    @Test // shadowed
    public void basicIteratorUsage() {
        bar("basicIteratorUsage");
        
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        Iterator<Integer> iterator = list.iterator();

        for (int i = 0; i < 1000; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(Integer.valueOf(i), iterator.next());
        }

        assertFalse(iterator.hasNext());
        
        bar("basicIteratorUsage done!");
    }
    
    @Test
    public void removeFirstLast() {
        list.addAll(getIntegerList(5));
        
        List<Integer> referenceList = new ArrayList<>(list);
        
        list.removeFirst();
        referenceList.remove(0);
        
        assertTrue(listsEqual(list, referenceList));
        
        list.removeFirst();
        referenceList.remove(0);
        
        assertTrue(listsEqual(list, referenceList));
        
        list.removeLast();
        referenceList.remove(referenceList.size() - 1);
        
        assertTrue(listsEqual(list, referenceList));
        
        list.removeLast();
        referenceList.remove(referenceList.size() - 1);
        
        assertTrue(listsEqual(list, referenceList));
    }
    
    @Test
    public void removeFirstLastOccurrence() {
        com.github.coderodde.util.LinkedList<Integer> l =
                new LinkedList<>();
        
        list.addAll(Arrays.asList(1, 2, 3, 1, 2, 3));
        l.addAll(list);
        
        list.removeFirstOccurrence(2);
        l.removeFirstOccurrence(2);
        
        assertTrue(listsEqual(list, l));
        
        list.removeLastOccurrence(3);
        l.removeLastOccurrence(3);
        
        assertTrue(listsEqual(list, l));
    }

    @Test // checked!
    public void bruteForceAddCollectionAtIndex() {
        long seed = System.currentTimeMillis();
        
        bar("bruteForceAddCollectionAtIndex: seed = " + seed);
        
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
        
        bar("bruteForceAddCollectionAtIndex done!");
    }

    @Test // checked!
    public void removeAtIndex() {
        bar("removeAtIndex");
        
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        list.checkInvariant();
        
        // [0, 1, 2, 3, 4]
        assertEquals(Integer.valueOf(2), list.remove(2));
        list.checkInvariant();
        // [0, 1, 3, 4]
        assertEquals(Integer.valueOf(0), list.remove(0));
        list.checkInvariant();
        // [1, 3, 4]
        assertEquals(Integer.valueOf(4), list.remove(2));
        list.checkInvariant();
        // [1, 3]
        assertEquals(Integer.valueOf(3), list.remove(1));
        list.checkInvariant();
        // [1]
        assertEquals(Integer.valueOf(1), list.remove(0));
        list.checkInvariant();
        // []
        
        bar("removeAtIndex done!");
    }

    @Test // checked
    public void removeObject() {
        bar("removeObject");
        
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));

        assertFalse(list.remove(Integer.valueOf(10)));
        assertFalse(list.remove(null));

        list.add(3, null);

        assertTrue(list.remove(null));

        assertTrue(list.remove(Integer.valueOf(4)));
        assertTrue(list.remove(Integer.valueOf(0)));
        assertTrue(list.remove(Integer.valueOf(2)));
        assertFalse(list.remove(Integer.valueOf(2)));
        
        bar("removeObject done!");
    }

    @Test // checked
    public void basicIteratorTraversal() {
        bar("basicIteratorTraversal");
        
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
        
        bar("basicIteratorTraversal done!");
    }

    @Test // checked
    public void basicIteratorRemoval() {
        bar("basicIteratorRemoval");
        
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
        
        bar("basicIteratorRemoval done!");
    }

    @Test // checked
    public void enhancedIteratorTraversal() {
        bar("enhancedIteratorTraversal");
        
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
        
        bar("enhancedIteratorTraversal done!");
    }
    
    // Used to find a failing removal sequence:
    @Test // shadowed
    public void removeAtFindFailing() {
        long seed = System.currentTimeMillis();
        bar("removeAtFindFailing: seed = " + seed);
        
        Random random = new Random(seed);
        int yeah = 0;
        while (true) {
//            System.out.println("yeah = " + yeah);
            yeah++;
            
            list.clear();
            list.addAll(getIntegerList(45));
            
            List<Integer> indices = new ArrayList<>();
            
            if (yeah == 100) {
                return;
            }
            
            while (!list.isEmpty()) {
                int index = random.nextInt(list.size());
                indices.add(index);
                
                try {
                    list.checkInvariant();
                    list.remove(index);
                    list.checkInvariant();
                } catch (AssertionError ae) {
                    System.out.println(indices);
                    bar("removeAtFindFailing done!");
                    return;
                }
            }
        }
    }
    
    @Test
    public void bugTinyRemoveInt() {
        bar("bugTinyRemoveInt");
        
        list.addAll(getIntegerList(5));
        
        list.checkInvariant();
        list.remove(4);
        
        list.checkInvariant();
        list.remove(0);
        
        list.checkInvariant();
        list.remove(2);
        
        list.checkInvariant();
        list.remove(0);
        
        list.checkInvariant();
        list.remove(0);
        
        list.checkInvariant();
        
        bar("bugTinyRemoveInt done!");        
    }
    
    @Test // shadowed
    public void removeAtIndex1() {
        bar("removeAtIndex1");
        
        list.addAll(getIntegerList(10));
        int[] indices = { 9, 3, 3, 3, 1, 0 };
        
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
//            System.out.println("ye = " + index);
            list.checkInvariant();
            list.remove(index);
            list.checkInvariant();
        }
        
        bar("removeAtIndex1 done!");
    }

    @Test
    public void enhancedIteratorAddition() {
        bar("enhancedIteratorAddition");
        
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
        
        bar("enhancedIteratorAddition done!");
    }

    @Test
    public void findFailingIterator() {
        bar("findFailingIterator");
        
        list.addAll(getIntegerList(3850));
        Iterator<Integer> iterator = list.iterator();
        int counter = 0;

        while (iterator.hasNext()) {
//            System.out.println("size = " + list.size() + " counter = " + counter);
            assertEquals(Integer.valueOf(counter), iterator.next());
            
            // Remove every 10th element:
            if (counter % 10 == 0) {
                iterator.remove();
            }

            counter++;
        }
        
        System.out.println(getBar("findFailingIterator done!"));
    }

    @Test
    public void bruteForceIteratorRemove() throws Exception {
        System.out.println(getBar("bruteForceIteratorRemove"));
        
        list.addAll(getIntegerList(1000));
 
        int counter = 1;
        List<Integer> arrayList = new ArrayList<>(list);
        Iterator<Integer> iter = list.iterator();
        Iterator<Integer> arrayListIter = arrayList.iterator();
        int totalIterations = 0;

        while (iter.hasNext()) {
//            System.out.println("total iters: " + totalIterations);
            
            iter.next();
            arrayListIter.next();
            list.checkInvariant();
            
            
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
        
        System.out.println(getBar("bruteForceIteratorRemove done!"));
    }

    @Test
    public void findFailingRemoveObject() {
        System.out.println(getBar("findFailingRemoveObject"));
        
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

        System.out.println(getBar("findFailingRemoveObject done!"));
    }

    @Test
    public void iteratorAdd() {
        bar("iteratorAdd");
        
        list.addAll(getIntegerList(4));

        ListIterator<Integer> iterator = list.listIterator(1);

        assertEquals(1, iterator.nextIndex());
        assertEquals(0, iterator.previousIndex());

        iterator.next();

        assertEquals(2, iterator.nextIndex());
        assertEquals(1, iterator.previousIndex());

        iterator.add(Integer.valueOf(100));

        assertEquals(Integer.valueOf(0), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        assertEquals(Integer.valueOf(100), list.get(2));
        assertEquals(Integer.valueOf(2), list.get(3));
        assertEquals(Integer.valueOf(3), list.get(4));
        
        bar("iteratorAdd done!");
    }

    @Test
    public void bruteForceIteratorTest() {
        bar("bruteForceIteratorTest");
        
        list.addAll(getIntegerList(100));
        List<Integer> referenceList = new java.util.LinkedList<>(list);

        ListIterator<Integer> iterator1 = list.listIterator(2);
        ListIterator<Integer> iterator2 = referenceList.listIterator(2);

        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        bar("bruteForceIteratorTest: seed = " + seed);

        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) {
                fail("Iterator mismatch on hasNext().");
            }

            iterator1.next();
            iterator2.next();

            int choice = random.nextInt(10);

            if (choice < 2) {
                Integer integer = Integer.valueOf(random.nextInt(100));
                iterator1.add(integer);
                iterator2.add(integer);
                assertTrue(listsEqual(list, referenceList));
            } else if (choice == 2) {
                iterator1.remove();
                iterator2.remove();
                assertTrue(listsEqual(list, referenceList));
            } else if (choice < 6) {
                if (iterator1.hasPrevious()) {
                    iterator1.previous();
                }

                if (iterator2.hasPrevious()) {
                    iterator2.previous();
                }
            } else {
                if (iterator1.hasNext()) {
                    iterator1.next();
                }

                if (iterator2.hasNext()) {
                    iterator2.next();
                }
            }
        }

        if (iterator2.hasNext()) {
            fail("Java List iterator has more to offer.");
        }
        
        bar("bruteForceIteratorTest done!");
    }

    @Test
    public void indexOf() {
        bar("indexOf");
        
        list.add(1);
        list.add(2);
        list.add(3);

        list.add(3);
        list.add(2);
        list.add(1);

        assertEquals(0, list.indexOf(1));
        assertEquals(1, list.indexOf(2));
        assertEquals(2, list.indexOf(3));

        assertEquals(3, list.lastIndexOf(3));
        assertEquals(4, list.lastIndexOf(2));
        assertEquals(5, list.lastIndexOf(1));
        
        bar("indexOf done!");
    }

    class MyIntegerConsumer implements Consumer<Integer> {

        List<Integer> ints = new ArrayList<>();

        @Override
        public void accept(Integer t) {
            ints.add(t);
        }
    }

    @Test
    @SuppressWarnings("empty-statement")
    public void basicSpliteratorUsage() {
        bar("basicSpliteratorUsage");
        
        list.addAll(getIntegerList(10_000));

        Spliterator<Integer> spliterator1 = list.spliterator();
        Spliterator<Integer> spliterator2 = spliterator1.trySplit();

        //// spliterator 2 : spliterator 1

        assertEquals(5000, spliterator1.getExactSizeIfKnown());
        assertEquals(5000, spliterator2.getExactSizeIfKnown());


        assertTrue(spliterator2.tryAdvance(
                i -> assertEquals(list.get(0), Integer.valueOf(0))));

        assertTrue(spliterator2.tryAdvance(
                i -> assertEquals(list.get(1), Integer.valueOf(1))));

        assertTrue(spliterator2.tryAdvance(
                i -> assertEquals(list.get(2), Integer.valueOf(2))));



        assertTrue(spliterator1.tryAdvance(
                i -> assertEquals(list.get(5000), Integer.valueOf(5000))));

        assertTrue(spliterator1.tryAdvance(
                i -> assertEquals(list.get(5001), Integer.valueOf(5001))));

        assertTrue(spliterator1.tryAdvance(
                i -> assertEquals(list.get(5002), Integer.valueOf(5002))));

        //// spliterator 3 : spliterator 2 : splitereator 1

        Spliterator<Integer> spliterator3 = spliterator2.trySplit();

        assertEquals(4997, spliterator1.getExactSizeIfKnown());

        assertTrue(spliterator3.tryAdvance(
                i -> assertEquals(list.get(3), Integer.valueOf(3))));

        assertTrue(spliterator3.tryAdvance(
                i -> assertEquals(list.get(4), Integer.valueOf(4))));

        assertTrue(spliterator3.tryAdvance(
                i -> assertEquals(list.get(5), Integer.valueOf(5))));

        //// 

        MyIntegerConsumer consumer = new MyIntegerConsumer();

        while (spliterator1.tryAdvance(consumer));

        for (int i = 0; i < consumer.ints.size(); i++) {
            Integer actualInteger = consumer.ints.get(i);
            Integer expectedInteger = 5003 + i;
            assertEquals(expectedInteger, actualInteger);
        }
        
        bar("basicSpliteratorUsage done!");
    }

    @Test // checked
    public void spliteratorForEachRemaining() {
        bar("spliteratorForEachRemaining");
        
        list.addAll(getIntegerList(10_000));
        Spliterator<Integer> split = list.spliterator();
        MyIntegerConsumer consumer = new MyIntegerConsumer();

        split.forEachRemaining(consumer);

        for (int i = 0; i < 10_000; i++) {
            assertEquals(Integer.valueOf(i), consumer.ints.get(i));
        }
        
        bar("spliteratorForEachRemaining done!");
    }

    @Test // checked
    public void spliteratorForEachRemainingTwoSpliterators() {
        bar("spliteratorForEachRemainingTwoSpliterators");
        
        list.addAll(getIntegerList(10_000));
        Spliterator<Integer> splitRight = list.spliterator();
        Spliterator<Integer> splitLeft = splitRight.trySplit();

        MyIntegerConsumer consumerRight = new MyIntegerConsumer();
        MyIntegerConsumer consumerLeft = new MyIntegerConsumer();

        splitRight.forEachRemaining(consumerRight);
        splitLeft.forEachRemaining(consumerLeft);

        for (int i = 0; i < 5_000; i++) {
            assertEquals(Integer.valueOf(i), consumerLeft.ints.get(i));
        }

        for (int i = 5_000; i < 10_000; i++) {
            assertEquals(Integer.valueOf(i), consumerRight.ints.get(i - 5_000));
        }
        
        bar("spliteratorForEachRemainingTwoSpliterators done!");
    }

    @Test // checked
    public void spliteratorForEachRemainingWithAdvance() {
        bar("spliteratorForEachRemainingWithAdvance");
        
        list.addAll(getIntegerList(10_000));
        Spliterator<Integer> rightSpliterator = list.spliterator();

        assertTrue(
                rightSpliterator.tryAdvance(
                        i -> assertEquals(Integer.valueOf(0), i)));

        Spliterator<Integer> leftSpliterator = rightSpliterator.trySplit();

        assertEquals(4_999, rightSpliterator.getExactSizeIfKnown());
        assertEquals(5_000, leftSpliterator.getExactSizeIfKnown());

        // Check two leftmost elements of the left spliterator:
        assertTrue(leftSpliterator.tryAdvance(
                i -> assertEquals(Integer.valueOf(1), i)));

        assertTrue(leftSpliterator.tryAdvance(
                i -> assertEquals(Integer.valueOf(2), i)));

        // Check two leftmost elements of the right splliterator:
        assertTrue(rightSpliterator.tryAdvance(
                i -> assertEquals(Integer.valueOf(5_000), i)));

        assertTrue(rightSpliterator.tryAdvance(
                i -> assertEquals(Integer.valueOf(5_001), i)));

        bar("spliteratorForEachRemainingWithAdvance done!");
    }

    @Test // checked
    public void spliterator() {
        bar("spliterator");
        
        list.addAll(getIntegerList(6_000));
        Spliterator split = list.spliterator();

        assertEquals(6_000L, split.getExactSizeIfKnown());
        assertEquals(6_000L, split.estimateSize());

        assertTrue(split.tryAdvance((i) -> assertEquals(list.get((int) i), i)));
        assertTrue(split.tryAdvance((i) -> assertEquals(list.get((int) i), i)));

        assertEquals(5998, split.getExactSizeIfKnown());

        // 5998 elements left / 2 = 2999 per spliterator:
        Spliterator leftSpliterator = split.trySplit();

        assertNotNull(leftSpliterator);
        assertEquals(2999, split.getExactSizeIfKnown());
        assertEquals(2999, leftSpliterator.getExactSizeIfKnown());

        //// leftSpliterator = [1, 2999]

        for (int i = 2; i < 3000; i++) {
            Integer integer = list.get(i);
            assertTrue(
                    leftSpliterator.tryAdvance(
                            (j) -> assertEquals(integer, j)));
        }

        //// split = [3001, 5999]

        assertTrue(split.tryAdvance(i -> assertEquals(2999, i)));
        assertTrue(split.tryAdvance(i -> assertEquals(3000, i)));
        assertTrue(split.tryAdvance(i -> assertEquals(3001, i)));

        while (split.getExactSizeIfKnown() > 0) {
            split.tryAdvance(i -> {});
        }

        assertFalse(split.tryAdvance(i -> {}));
        
        bar("spliterator done!");
    }

    @Test // checked
    public void bruteforceSpliterator() {
        bar("bruteforceSpliterator");
        
        list.addAll(getIntegerList(1_000_000));
        Collections.<Integer>shuffle(list);

        List<Integer> newList = 
               list.parallelStream()
                   .map(i -> 2 * i)
                   .collect(Collectors.toList());

        assertEquals(newList.size(), list.size());

        for (int i = 0; i < list.size(); i++) {
            Integer integer1 = 2 * list.get(i);
            Integer integer2 = newList.get(i);
            assertEquals(integer1, integer2);
        }
        
        bar("bruteforceSpliterator done!");
    }

    private static final String SERIALIZATION_FILE_NAME = "LinkedList.ser";

    @Test
    public void serialization() {
        list.add(10);
        list.add(13);
        list.add(12);

        try {
            File file = new File(SERIALIZATION_FILE_NAME);

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(list);
            oos.flush();
            oos.close();

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            com.github.coderodde.util.LinkedList<Integer> ll =    
                    (com.github.coderodde.util.LinkedList<Integer>)
                    ois.readObject();

            ois.close();
            boolean equal = listsEqual(list, ll);
            assertTrue(equal);

            if (!file.delete()) {
                file.deleteOnExit();
            }

        } catch (IOException | ClassNotFoundException ex) {
            fail(ex.getMessage());
        }   
    }

    @Test
    public void bruteforceSerialization() {
        for (int i = 0; i < 20; i++) {
            list.addAll(getIntegerList(i));

            try {
                File file = new File(SERIALIZATION_FILE_NAME);

                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeObject(list);
                oos.flush();
                oos.close();

                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);

                com.github.coderodde.util.LinkedList<Integer> ll =    
                        (com.github.coderodde.util.LinkedList<Integer>)
                        ois.readObject();

                ois.close();
                boolean equal = listsEqual(list, ll);
                assertTrue(equal);

                if (!file.delete()) {
                    file.deleteOnExit();
                }

            } catch (IOException | ClassNotFoundException ex) {
                fail(ex.getMessage());
            }   

            list.clear();
        }
    }
    
    @Test // checked
    public void bugCheckInvariantAfterRemoval() {
        bar("bugCheckInvariantAfterRemoval");
        
        for (int i = 0; i < 4; i++) {
            list.add(i);
        }
        
        list.remove(Integer.valueOf(3));
        list.remove(1);
        assertEquals(list.size(), 2);
        assertEquals(Integer.valueOf(0), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        
        bar("bugCheckInvariantAfterRemoval done!");
    }
    
    @Test
    public void bruteForceRemoveAt1() {
        long seed = System.currentTimeMillis();
        bar("bruteForceRemoveAt1: seed = " + seed);
        
        Random random = new Random(seed);
        
        list.addAll(getIntegerList(1000));
        List<Integer> referenceList = new ArrayList<>(list);
        
        Integer probe = Integer.valueOf(3);
        
        list.remove(probe);
        referenceList.remove(probe);
        
        int iters = 0;
        
        while (!list.isEmpty()) {
//            System.out.println("iters = " + iters);
            iters++;
            
            int index = random.nextInt(list.size());
            list.remove(index);
            referenceList.remove(index);
            
            listsEqual(list, referenceList);
        } 
        
        bar("bruteForceRemoveA1t done!");
    }
    
//    @Test
    public void bruteForceRemoveAt2() {
        long seed = 1630487847317L; // System.currentTimeMillis();
        
        bar("bruteForceRemoveAt2: seed = " + seed);

        Random random = new Random(seed);
        
        while (true) {
            list.addAll(getIntegerList(10));
            List<Integer> indices = new ArrayList<>(list.size());
            
            while (!list.isEmpty()) {
                int index = random.nextInt(list.size());
                indices.add(index);
                System.out.println(indices);
                
                try {
                    list.remove(index);
                } catch (AssertionError ae) {
                    System.out.println(
                            "Message: " + ae.getMessage() + ", indices: " + 
                                    indices.toString());
                    return;
                }
            }
            
            indices.clear();
        }
        
//        bar("bruteForceRemoveAt2 done!");
    }
    
    @Test
    public void bugRemoveAt2() {
        bar("bugRemoveAt2");
        
        list.addAll(getIntegerList(10));
        final int[] indices = { 7, 7, 4, 1, 2, 1, 3, 1, 1, 0 };
        
        for (int i = 0; i < indices.length; i++) {
            final int index = indices[i];
//            System.out.println("i = " + i + ", index = " + index);
            
            list.checkInvariant();
            list.remove(index);
            list.checkInvariant();
//            System.out.println("index = " + index + ", i = " + i);
        }
        
        bar("bugRemoveAt2 done!");
    }
    
    @Test
    public void bugRemoveAt() {
        bar("bugRemoveAt");
        
        list.addAll(getIntegerList(10));
        
        list.checkInvariant();
        assertEquals(Integer.valueOf(5), list.remove(5));
        
        list.checkInvariant();
        assertEquals(Integer.valueOf(3), list.remove(3));
        
        list.checkInvariant();
        assertEquals(Integer.valueOf(2), list.remove(2));
        
        list.checkInvariant();
        assertEquals(Integer.valueOf(1), list.remove(1));
        
        list.checkInvariant();
        // list = [0, 4, 5, 7, 8, 8]
        assertEquals(Integer.valueOf(8), list.remove(4));
        
        list.checkInvariant();
        
        bar("bugRemoveAt done!");
    }
    
    // Should not throw anything:
    //@Test // checked!
    public void bugRemoveFirst() {
        list.addAll(getIntegerList(5));
        
        assertEquals(5, list.size());
        
        for (int i = 0; i < 2; i++) {
            list.removeFirst();
        }
        
        long seed = System.currentTimeMillis();
        bar("bugRemoveFirst: seed = " + seed);
        
        Random random = new Random(seed);
        List<Integer> referenceList = new ArrayList<>(list);
        
        while (!list.isEmpty()) {
            int index = random.nextInt(list.size());
            list.remove(index);
            referenceList.remove(index);
            assertTrue(listsEqual(list, referenceList));
        }
        
        bar("bugRemoveFirst done!");
    }
    
    // Should not throw anything:
    @Test
    public void bugRemoveLast() {
        bar("bugRemoveLast");
        
        list.addAll(getIntegerList(10));
        
        assertEquals(10, list.size());
        
        for (int i = 0; i < 5; i++) {
            list.removeLast();
        }
        
        long seed = System.currentTimeMillis();
        bar("bugRemoveLast: seed = " + seed);
        
        Random random = new Random(seed);
        List<Integer> referenceList = new ArrayList<>(list);
        
        while (!list.isEmpty()) {
            int index = random.nextInt(list.size());
            list.remove(index);
            referenceList.remove(index);
            assertTrue(listsEqual(list, referenceList));
        }
        
        bar("bugRemoveLast done!");
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
    
    private static String getBar(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("--- LinkedList.");
        stringBuilder.append(text);
        return stringBuilder.toString();
    }
    
    private static void bar(String text) {
        System.out.println(getBar(text));
    }
}
