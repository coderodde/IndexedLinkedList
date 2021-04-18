package com.github.coderodde.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        
        for (int i = 0; i < 8; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }
}
