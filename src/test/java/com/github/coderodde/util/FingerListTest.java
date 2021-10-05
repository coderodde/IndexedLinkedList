package com.github.coderodde.util;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class FingerListTest {

    private final FingerList<Integer> fl = new FingerList<>();
    
    @Before
    public void setUp() {
        fl.clear();
    }

    @Test
    public void appendFinger() {
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(0)), 0));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(1)), 1));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(3)), 3));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(6)), 6));
        
        Finger<Integer> finger = fl.get(fl.getFingerIndex(0));
        assertEquals(0, finger.index);
        assertEquals(Integer.valueOf(0), finger.node.item);
        
        finger = fl.get(fl.getFingerIndex(1));
        assertEquals(1, finger.index);
        assertEquals(Integer.valueOf(1), finger.node.item);
        
        finger = fl.get(fl.getFingerIndex(2));
        assertEquals(3, finger.index);
        assertEquals(Integer.valueOf(3), finger.node.item);
        
        finger = fl.get(fl.getFingerIndex(3));
        assertEquals(3, finger.index);
        assertEquals(Integer.valueOf(3), finger.node.item);
        
        finger = fl.get(fl.getFingerIndex(4));
        assertEquals(6, finger.index);
        assertEquals(Integer.valueOf(6), finger.node.item);
        
        finger = fl.get(fl.getFingerIndex(5));
        assertEquals(6, finger.index);
        assertEquals(Integer.valueOf(6), finger.node.item);
        
        finger = fl.get(fl.getFingerIndex(6));
        assertEquals(6, finger.index);
        assertEquals(Integer.valueOf(6), finger.node.item);
    }
}