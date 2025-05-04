/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.coderodde.util;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class FingerListTest {

    private final IndexedLinkedList<Integer> list = new IndexedLinkedList<>();
    private final FingerList<Integer> fl = list.fingerList;
    
    @Before
    public void setUp() {
        fl.clear();
    }
    
    @Test
    public void fingerToString() {
        Finger<Integer> f = new Finger<>(new Node<>(13), 2);
        assertEquals("[Finger; index = 2, item = 13]", f.toString());
        f = new Finger<>(new Node<>(null), 3);
        assertEquals("[Finger; index = 3, item = null]", f.toString());
    }
    
//    @Test
    public void fingerListString() {
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(0)), 0));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(2)), 1));
        assertEquals("[FingerList, size = 2]", fl.toString());
    }

    @Test
    public void appendGetFinger() {
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(0)), 0));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(1)), 1));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(3)), 3));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(6)), 6));
        fl.fingerArray[4].index = 8;
        fl.fingerArray[4].node = new Node<>(Integer.valueOf(1000));
        
        Finger<Integer> finger = fl.getFinger(fl.getClosestFingerIndex(0));
        assertEquals(0, finger.index);
        assertEquals(Integer.valueOf(0), finger.node.item);
        
        finger = fl.getFinger(fl.getClosestFingerIndex(1));
        assertEquals(1, finger.index);
        assertEquals(Integer.valueOf(1), finger.node.item);
        
        finger = fl.getFinger(fl.getClosestFingerIndex(2));
        assertEquals(3, finger.index);
        assertEquals(Integer.valueOf(3), finger.node.item);
        
        finger = fl.getFinger(fl.getClosestFingerIndex(3));
        assertEquals(3, finger.index);
        assertEquals(Integer.valueOf(3), finger.node.item);
        
        finger = fl.getFinger(fl.getClosestFingerIndex(4));
        assertEquals(3, finger.index);
        assertEquals(Integer.valueOf(3), finger.node.item);
        
        finger = fl.getFinger(fl.getClosestFingerIndex(5));
        assertEquals(6, finger.index);
        assertEquals(Integer.valueOf(6), finger.node.item);
        
        finger = fl.getFinger(fl.getClosestFingerIndex(6));
        assertEquals(6, finger.index);
        assertEquals(Integer.valueOf(6), finger.node.item);
    }
    
//    @Test
    public void insertFingerAtFront() {
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(0)), 0));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(1)), 1));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(3)), 3));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(6)), 6));
        
        Finger<Integer> insertionFinger = new Finger<>(new Node<>(null), 0);
        
//        fl.insertFinger(insertionFinger);
        
        Finger<Integer> finger = fl.getFinger(fl.getClosestFingerIndex(0));
        assertEquals(insertionFinger.index, finger.index);
        
        assertEquals(5, fl.size());
    }
    
//    @Test
    public void insertFingerAtTail() {
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(2)), 2));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(4)), 4));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(5)), 5));
        
        // Add end of finger list sentinel:
        fl.fingerArray[3] = 
                new Finger<>(new Node<Integer>(Integer.valueOf(100)), 10);
        
        Finger<Integer> insertionFinger = new Finger<>(new Node<>(null), 6);
        
//        fl.insertFinger(insertionFinger);

        Finger<Integer> finger = fl.getFinger(fl.getClosestFingerIndex(6));
        assertEquals(insertionFinger.index, finger.index);
        
        assertEquals(4, fl.size());
    }
    
//    @Test
    public void insertFingerInBetween1() {
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(2)), 2));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(4)), 4));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(5)), 5));
        
        Finger<Integer> insertionFinger = new Finger<>(new Node<>(null), 4);
        
//        fl.insertFinger(insertionFinger);
        
        assertEquals(insertionFinger, fl.getFinger(1));
    }
    
//    @Test
    public void insertFingerInBetween2() {
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(2)), 2));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(4)), 4));
        fl.appendFinger(new Finger<>(new Node<>(Integer.valueOf(5)), 5));
        
        Finger<Integer> insertionFinger = new Finger<>(new Node<>(null), 3);
        
//        fl.insertFinger(insertionFinger);
        
        assertEquals(insertionFinger, fl.getFinger(1));
    }
    
//    @Test
    public void makeRoomAtPrefix1Old() {
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        
        list.fingerList.setFingerIndices(6, 7, 8, 9);
        
        list.fingerList.makeRoomAtPrefix(5, 0, 3);
        
        Finger<Integer> finger0 = list.fingerList.fingerArray[0];
        Finger<Integer> finger1 = list.fingerList.fingerArray[1];
        Finger<Integer> finger2 = list.fingerList.fingerArray[2];
        
        assertEquals(2, finger0.index);
        assertEquals(3, finger1.index);
        assertEquals(4, finger2.index);
        
        System.out.println("makeRoomAtPrefix1Oldl passed!");
    }
    
    @Test
    public void makeRoomAtPrefix1() {
        
        loadList(10);
        
        list.fingerList.setFingerIndices(1, 3, 4, 6);
        list.fingerList.makeRoomAtPrefix(4, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(0, 1, 4, 6);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtPrefix1 passed!");
    }
    
    @Test
    public void makeRoomAtPrefix2() {
        
        loadList(10);
        
        list.fingerList.setFingerIndices(1, 3, 5, 8);
        list.fingerList.makeRoomAtPrefix(4, 2, 1);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(1, 2, 5, 8);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtPrefix2 passed!");
    }
    
    @Test
    public void makeRoomAtPrefix3() {
        
        loadList(10);
        
        list.fingerList.setFingerIndices(1, 4, 6, 9);
        list.fingerList.makeRoomAtPrefix(5, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(1, 2, 6, 9);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtPrefix3 passed!");
    }
    
    @Test
    public void makeRoomAtPrefix4() {
        
        loadList(10);
        
        list.fingerList.setFingerIndices(0, 1, 7, 8);
        list.fingerList.makeRoomAtPrefix(4, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(0, 1, 7, 8);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtPrefix4 passed!");
    }
    
    @Test
    public void makeRoomAtPrefix5() {
        
        loadList(10);
        
        list.fingerList.setFingerIndices(0, 2, 6, 8);
        list.fingerList.makeRoomAtPrefix(5, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(0, 2, 6, 8);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtPrefix5 passed!");
    }
    
    @Test
    public void makeRoomAtSuffix1() {
        loadList(10);
        
        list.fingerList.setFingerIndices(3, 4, 8, 9);
        list.fingerList.makeRoomAtSuffix(6, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(3, 4, 8, 9);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtSuffix1 passed!");
    }
    
    @Test
    public void makeRoomAtSuffix2() {
        loadList(10);
        
        list.fingerList.setFingerIndices(3, 4, 7, 9);
        list.fingerList.makeRoomAtSuffix(6, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(3, 4, 8, 9);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtSuffix2 passed!");
    }
    
    @Test
    public void makeRoomAtSuffix3() {
        loadList(10);
        
        list.fingerList.setFingerIndices(3, 4, 7, 8);
        list.fingerList.makeRoomAtSuffix(6, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(3, 4, 8, 9);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtSuffix3 passed!");
    }
    
    @Test
    public void makeRoomAtSuffix4() {
        loadList(10);
        
        list.fingerList.setFingerIndices(3, 4, 7, 8);
        list.fingerList.makeRoomAtSuffix(6, 0, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(3, 4, 7, 8);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("makeRoomAtSuffix4 passed!");
    }
    
    @Test
    public void arrangePrefix1() {
        loadList(10);
        
        list.fingerList.setFingerIndices(1, 3, 5, 8);
        list.fingerList.arrangePrefix(4, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(0, 1, 2, 3);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("arrangePrefix1 passed!");
    }
    
    @Test
    public void arrangePrefix2() {
        loadList(10);
        
        list.fingerList.setFingerIndices(5, 6, 8, 9);
        list.fingerList.arrangePrefix(5, 0, 4);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(1, 2, 3, 4);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("arrangePrefix2 passed!");
    }
    
    @Test
    public void pushCoveredFingersToSuffix1() {
        
        loadList(10);
        
        list.fingerList.setFingerIndices(1, 3, 4, 5);
        list.fingerList.pushCoveredFingersToSuffix(6, 0, 3);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(1, 6, 7, 8);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("pushCoveredFingersToSuffix1 passed!");
    }
    
    @Test
    public void pushCoveredFingersToSuffix2() {
        
        loadList(10);
        
        list.fingerList.setFingerIndices(1, 3, 4, 5);
        list.fingerList.pushCoveredFingersToSuffix(4, 2, 2);
        
        IndexedLinkedList<Integer> expectedList = new IndexedLinkedList<>(list);
        expectedList.fingerList.setFingerIndices(1, 3, 4, 5);
        
        assertTrue(list.strongEquals(expectedList));
        
        System.out.println("pushCoveredFingersToSuffix2 passed!");
    }
    
    private void loadList(int size) {
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
    }
}