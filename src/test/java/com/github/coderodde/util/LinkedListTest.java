package com.github.coderodde.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkedListTest {

    private final LinkedList<Integer> list = new LinkedList<>();
    
    @org.junit.jupiter.api.BeforeAll
    public static void setUpClass() throws Exception {
        
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownClass() throws Exception {
        
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() throws Exception {
        list.clear();
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testListIterator() {
        
    }

    @org.junit.jupiter.api.Test
    public void testSize() {
        
    }
    
    @org.junit.jupiter.api.Test
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
        
        assertEquals(Integer.valueOf(2), list.get(1));
    }
    
    @org.junit.jupiter.api.Test(expected = IndexOutOfBoundsException.class)
    public void testAddThrowsOnMinusOneIndex() {
        list.add(1);
        
    }
    
    @org.junit.jupiter.api.Test
    public void testAddThrowsOnTooLargeIndex() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testRemove_Object() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testAddAll_Collection() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testAddAll_int_Collection() {
        
    }

    @org.junit.jupiter.api.Test
    public void testAddFirst() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testAddLast() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testOfferFirst() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testOfferLast() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testRemoveFirst() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testRemoveLast() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPollFirst() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPollLast() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testGetFirst() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testGetLast() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPeekFirst() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPeekLast() {
        
    }

    @org.junit.jupiter.api.Test
    public void testRemoveFirstOccurrence() {
        
    }

    @org.junit.jupiter.api.Test
    public void testRemoveLastOccurrence() {
        
    }

    @org.junit.jupiter.api.Test
    public void testOffer() {
        
    }

    @org.junit.jupiter.api.Test
    public void testRemove_0args() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPoll() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testElement() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPeek() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPush() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testPop() {
        
    }
    
    @org.junit.jupiter.api.Test
    public void testDescendingIterator() {
        
    }
}
