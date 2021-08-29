package com.github.coderodde.util;

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
    public void add() {
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
    }
    
    @Test
    public void contains() {
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
    }
    
    @Test
    public void remove() {
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
    }
    
    @Test
    public void clear() {
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
    }
}
