package com.github.coderodde.util;

import java.util.Random;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class IntHashSetTest {

    private final IntHashSet set = new IntHashSet();

    @Before
    public void beforeTest() {
        set.clear();
    }
    
    @Test
    public void removeFromCollisionChainBug() {
        bar("removeFromCollisionChainBug");
        
        set.add(0b00001); // 1
        set.add(0b01001); // 9
        set.add(0b10001); // 17
        
        set.remove(1); // remove from tail
        
        set.add(0b00001); // 1
        set.add(0b01001); // 9
        set.add(0b10001); // 17
        
        set.remove(1); // remove from head
        
        set.add(0b00001); // 1
        set.add(0b01001); // 9
        set.add(0b10001); // 17
    
        set.remove(17); // remove from middle
        
        bar("removeFromCollisionChainBug done!");
    }
    
    
    @Test
    public void removeBug() {
        bar("removeBug");
        
        for (int i = 0; i < 9; i++) 
            set.add(i);
        
        for (int i = 0; i < 9; i++) 
            set.remove(i);
        
        bar("removeBug done!");
    }
    
    @Test
    public void removeFirstMiddleLast() {
        bar("removeFirstMiddleLast");
        
        // All three ints will end up in the same collision chain:
        set.add(1);  // 0b00001
        set.add(9);  // 0b01001
        set.add(17); // 0b10001
        
        assertTrue(set.contains(1));
        assertTrue(set.contains(9));
        assertTrue(set.contains(17));
        
        set.remove(1);
        
        assertFalse(set.contains(1));
        assertTrue(set.contains(9));
        assertTrue(set.contains(17));
        
        set.remove(17);
        
        assertFalse(set.contains(1));
        assertTrue(set.contains(9));
        assertFalse(set.contains(17));
        
        set.remove(9);
        
        assertFalse(set.contains(1));
        assertFalse(set.contains(9));
        assertFalse(set.contains(17));
        
        bar("removeFirstMiddleLast done!");
    }

    @Test
    public void add() {
        bar("add");
        
        for (int i = 0; i < 500; i++) {
            set.add(i);
        }

        for (int i = 0; i < 500; i++) {
            assertTrue(set.contains(i));
        }

        for (int i = 500; i < 1_000; i++) {
            assertFalse(set.contains(i));
        }

        for (int i = 450; i < 550; i++) {
            set.remove(i);
        }

        for (int i = 450; i < 1_000; i++) {
            assertFalse(set.contains(i));
        }

        for (int i = 0; i < 450; i++) {
            assertTrue(set.contains(i));
        }
        
        bar("add done!");
    }

    @Test
    public void contains() {
        bar("contains");
        
        set.add(10);
        set.add(20);
        set.add(30);

        for (int i = 1; i < 40; i++) {
            if (i % 10 == 0) {
                assertTrue(set.contains(i));
            } else {
                assertFalse(set.contains(i));
            }
        }
        
        bar("contains done!");
    }

    @Test
    public void remove() {
        bar("remove");
        
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);
        set.add(5);

        set.remove(2);
        set.remove(4);

        set.add(2);

        assertFalse(set.contains(4));

        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertTrue(set.contains(5));
        
        bar("remove done!");
    }

    @Test
    public void clear() {
        bar("clear");
        
        for (int i = 0; i < 100; i++) {
            set.add(i);
        }

        for (int i = 0; i < 100; i++) {
            assertTrue(set.contains(i));
        }

        set.clear();

        for (int i = 0; i < 100; i++) {
            assertFalse(set.contains(i));
        }
        
        bar("clear done!");
    }

    @Test 
    public void bruteForceAdd() {
        bar("bruteForceAdd");
        
        Random random = new Random(13L);

        int[] data = new int[10_000];

        for (int i = 0; i < data.length; i++) {
            int datum = random.nextInt(5_000);
            data[i] = datum;
            set.add(datum);
        }

        for (int i = 0; i < data.length; i++) {
            assertTrue(set.contains(data[i]));
        }
        
        bar("bruteForceAdd done!");
    }

    @Test
    public void bruteForceRemove() {
        bar("bruteForceRemove");
        
        Random random = new Random(100L);

        int[] data = new int[10_000];

        for (int i = 0; i < data.length; i++) {
            int datum = random.nextInt(5_000);
            data[i] = datum;
            set.add(datum);
        }

        shuffle(data, random);

        for (int i = 0; i < data.length; i++) {
            int datum = data[i];

            if (set.contains(datum)) {
                set.remove(datum);
                if (set.contains(datum)) 
                    System.out.println("found i = " + i);
            } 

            assertFalse(set.contains(datum));
        }
        
        bar("bruteForceRemove done!");
    }

    private static void shuffle(int[] data, Random random) {
        for (int i = data.length - 1; i > 0; --i) {
            final int j = random.nextInt(i + 1);
            swap(data, i, j);
        }
    }

    private static void swap(int[] data, int i, int j) {
        int tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
    }
    
    private static void bar(String text) {
        System.out.println("--- " + text);
    }
}
