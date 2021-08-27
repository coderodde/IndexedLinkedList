package com.github.coderodde.util;

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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        System.out.println("- bruteForceAddCollectionAtIndex.seed = " + seed);
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
        assertEquals(Integer.valueOf(3), list.remove(1));
        assertEquals(Integer.valueOf(1), list.remove(0));
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
                    System.out.println(totalIterations);
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

    @Test
    public void iteratorAdd() {
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
    }

    @Test
    public void bruteForceIteratorTest() {
        list.addAll(getIntegerList(100));
        List<Integer> referenceList = new java.util.LinkedList<>(list);

        ListIterator<Integer> iterator1 = list.listIterator(2);
        ListIterator<Integer> iterator2 = referenceList.listIterator(2);

        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        System.out.println("- bruteForceIteratorTest: seed = " + seed);

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
    }

    @Test
    public void indexOf() {
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
    }

    @Test
    public void spliteratorForEachRemaining() {
        list.addAll(getIntegerList(10_000));
        Spliterator<Integer> split = list.spliterator();
        MyIntegerConsumer consumer = new MyIntegerConsumer();

        split.forEachRemaining(consumer);

        for (int i = 0; i < 10_000; i++) {
            assertEquals(Integer.valueOf(i), consumer.ints.get(i));
        }
    }

    @Test
    public void spliteratorForEachRemainingTwoSpliterators() {
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
    }

    @Test
    public void spliteratorForEachRemainingWithAdvance() {
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
    }

    @Test
    public void spliterator() {
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
    }

    @Test
    public void bruteforceSpliterator() {
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
    
    @Test
    public void bugCheckInvariantAfterRemoval() {
        for (int i = 0; i < 4; i++) {
            list.add(i);
        }
        
        list.remove(Integer.valueOf(3));
        list.remove(1);
//        list.add(0);
//        list.add(1);
//        list.add(2);
//        list.removeLast();
//        assertEquals(list.size(), 2);
//        assertEquals(Integer.valueOf(0), list.get(0));
//        assertEquals(Integer.valueOf(1), list.get(1));
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
