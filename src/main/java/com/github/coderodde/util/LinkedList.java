/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.github.coderodde.util;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 *
 * @author  Rodion Efremov
 * @see     List
 * @see     ArrayList
 * @see     java.util.LinkedList
 * @since 17
 * @param <E> the type of elements held in this collection
 */

public class LinkedList<E> 
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    transient int size = 0;
    
    /**
     * Pointer to first node.
     */
    transient Node<E> first;
    
    /**
     * Pointer to last node.
     */
    transient Node<E> last;
    
    /**
     * Stack of fingers.
     */
    transient FingerStack<E> fingerStack = new FingerStack<>();
    
    public LinkedList() {
        
    }
    
    public LinkedList(Collection<? extends E> c) {
        addAll(c);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) 
            return false;
        
        if (o == this) 
            return true;
        
        if (!o.getClass().equals(o.getClass())) 
            return false;
        
        List<?> otherList = (List<?>) o;
        
        if (size != otherList.size())
            return false;
        
        Iterator<?> iterator1 = iterator();
        Iterator<?> iterator2 = otherList.iterator();
        
        while (iterator1.hasNext() && iterator2.hasNext()) {
            Object object1 = iterator1.next();
            Object object2 = iterator2.next();
            
            if (!java.util.Objects.equals(object1, object2)) 
                return false;
        }
        
        boolean iterator1HasMore = iterator1.hasNext();
        boolean iterator2HasMore = iterator2.hasNext();
        
        if (iterator1HasMore || iterator2HasMore)
            throw new IllegalStateException(
                    iterator1HasMore ? 
                            "This list has more elements to offer" : 
                            "Argument list has more elements to offer");
        
        return true;
    }
    
    /**
     * Inserts the specified element at the beginning of this list.
     *
     * @param e the element to add
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #add}.
     *
     * @param e the element to add
     */
    public void addLast(E e) {
        linkLast(e);
    }
    
    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list
     * @throws NoSuchElementException if this list is empty
     */
    public E getFirst() {
        final Node<E> f = first;
        if (f == null) 
            throw new NoSuchElementException();
        
        return f.item;
    }
    
    /**
     * Returns {@code true} if this list contains the specified element.
     * More formally, returns {@code true} if and only if this list contains
     * at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    
    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list
     * @throws NoSuchElementException if this list is empty
     */
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }
    
    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }
    
    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }
    
    private void linkFirst(E e) {
        shiftIndicesToRightOnce(0);
        
        final Node<E> f = first;
        final Node<E> newNode = new Node<>();
        newNode.item = e;
        newNode.next = f;
        first = newNode;
        
        if (f == null) 
            last = newNode;
        else
            f.prev = newNode;
        
        size++;
        modCount++;
        
        if (mustAddFinger())
            addFinger(newNode, 0);
    }
    
    private void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>();
        newNode.item = e;
        newNode.prev = l;
        last = newNode;
        
        if (l == null) 
            first = newNode;
        else
            l.next = newNode;
        
        size++;
        modCount++;
        
        if (mustAddFinger()) 
            addFinger(newNode, size - 1);
    }
    
    private boolean mustAddFinger() {
        // here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() - 1
        return fingerStack.size() != getRecommendedNumberOfFingers();
    }
    
    private boolean mustAddFinger(int size) {
        return fingerStack.size() != getRecommendedFingerCount(size);
    }
    
    private boolean mustRemoveFinger() {
        // here, fingerStack.size() == getRecommendedFingerCount(), or, 
        // fingerStack.size() == getRecommendedFingerCount() + 1
        return fingerStack.size() != getRecommendedNumberOfFingers();
    }
    
    private void addFinger(Node<E> node, int index) {
        final Finger<E> finger = new Finger<>(node, index);
        fingerStack.push(finger);
    }
    
    public void clear() {
        fingerStack.clear();
        size = 0;
        
        for (Node<E> node = first; node != null;) {
            node.prev = null;
            node.item = null;
            Node<E> next = node.next;
            node.next = null;
            node = next;
        }
    }
    
    private void removeFinger() {
        fingerStack.pop();
    }
    
    private void shiftFingersToRight(int startIndex, int steps) {
        for (int sz = fingerStack.size(), i = 0; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index >= startIndex)
                finger.index += steps;
        }
    }
    
    private void shiftFingersLeft(int startIndex, int steps) {
        for (int sz = fingerStack.size(), i = 0; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index >= startIndex)
                finger.rewindLeft(steps);
        }
    }
    
    
    private Finger<E> getClosestFinger(int index) {
        int bestDistance = Integer.MAX_VALUE;
        Finger<E> bestFinger = null;
        
        for (int sz = fingerStack.size(), i = 0; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            int distance = Math.abs(finger.index - index);
            
            if (distance == 0) 
                return finger;
            
            if (bestDistance > distance) {
                bestDistance = distance;
                bestFinger = finger;
            }
        }
        
        return bestFinger;
    }
    
    private Node<E> node(int index) {
        Finger<E> finger = getClosestFinger(index);
        int distance = finger.index - index;
        
        if (distance > 0) 
            finger.rewindLeft(distance);
        else 
            finger.rewindRight(-distance);
        
        return finger.node;
    }
    
    private void linkBefore(E e, Node<E> succ, int index) {
        shiftIndicesToRightOnce(index);
        
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>();
        newNode.item = e;
        newNode.next = succ;
        succ.prev = newNode;
        
        if (pred == null) {
            first = newNode;
        } else {
            pred.next = newNode;
            newNode.prev = pred;
        }
        
        size++;
        modCount++;
        
        if (mustAddFinger()) 
            addFinger(newNode, index);
    }
    
    private E unlinkFirst(Node<E> f) {
        shiftIndicesToLeftOnce(1);
        
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null; // help GC
        first = next;
        
        if (next == null) 
            last = null;
        else
            next.prev = null;
        
        size--;
        modCount++;
        
        if (mustRemoveFinger()) 
            fingerStack.pop();
        
        return element;
    }
    
    private E unlinkLast(Node<E> l) {
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null; // help GC
        last = prev;
        
        if (prev == null) 
            first = null;
        else 
            prev.next = null;
        
        size--;
        modCount++;
        
        if (mustRemoveFinger()) 
            fingerStack.pop();
        
        return element;
    }

    private E unlink(Node<E> x, int index) {
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;
        
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }
        
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
        
        x.item = null;
        size--;
        modCount++;
        
        if (mustRemoveFinger())
            removeFinger();
        
        shiftIndicesToLeftOnce(index + 1);
        return element;
    }
    
    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        return size;
    }
    
    public boolean add(E e) {
        linkLast(e);
        return true;
    }
    
    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index), index);
    }
    
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
    
    public boolean remove(Object o) {
        int index = 0;
        
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (x.item == null) {
                    shiftIndicesToLeftOnce(index + 1);
                    unlink(x, index);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (o.equals(x.item)) {
                    shiftIndicesToLeftOnce(index + 1);
                    unlink(x, index);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private final class RemoveData<E> {
        Node<E> node;
        Finger<E> finger;
    }
    
    private final RemoveData<E> removeData = new RemoveData<>();
    
    /*
    
    1. Find the node N to remove
    2. If N is fingered by F, move F left/right
    3. unlink(N)
    
    */
    public E remove(int index) {
        checkElementIndex(index);
        
        // Loads the removeData!
        findNodeToRemove(index);
        
        // Make sure that no finger is on our way pointing to the node to remove
        if (removeData.finger.index == index) 
            moveFingerOutOfRemovalLocation(removeData.finger);
        
        // Once here, the list is not empty and has at least one finger!
        return unlink(removeData.node, index);
    }
    
    private void findNodeToRemove(int index) {
        Finger<E> finger = getClosestFinger(index);
        Node<E> node = finger.node;
        
        if (index < finger.index) {
            int distance = finger.index - index;
            
            for (int i = 0; i < distance; i++) 
                node = node.prev;
        } else {
            int distance = index - finger.index;
            
            for (int i = 0; i < distance; i++) 
                node = node.next;
        }
        
        removeData.finger = finger;
        removeData.node = node;
    }
    
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
    
    private void prependall(Collection<? extends E> c) {
        Iterator<? extends E> iterator = c.iterator();
        
        // once here, c is not empty, grab the first element of c
        final Node<E> newFirst = new Node<>();
        newFirst.item = iterator.next();
        Node<E> newLast = newFirst;
        
        // build a sublist of added elemenets
        while (iterator.hasNext()) {
            final Node<E> newNode = new Node<>();
            newNode.item = iterator.next();
            newNode.prev = newLast;
            newLast.next = newNode;
            newLast = newNode;
        }
        
        if (first == null) {
            first = newFirst;
            last = newLast;
        } else {
            newLast.next = first;
            first.prev = newLast;
        }
        
        size += c.size();
        addFingers(newFirst, 0, c.size());
    }
    
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);
        if (c.isEmpty()) 
            return false;
        
        if (size == 0) 
            setAll(c);
        else if (index == 0) 
            prependAll(c);
        else if (index == size) 
            appendAll(c);
        else 
            insertAll(c, node(index), index);
        
        return true;
    }
    
    private void setAll(Collection<? extends E> c) {
        Iterator<? extends E> iterator = c.iterator();
        
        first = new Node<>();
        first.item = iterator.next();
        
        Node<E> prevNode = first;
        
        for (int i = 1, sz = c.size(); i < sz; i++) {
            Node<E> newNode = new Node<>();
            newNode.item = iterator.next();
            prevNode.next = newNode;
            newNode.prev = prevNode;
            prevNode = newNode;
        }
        
        last = prevNode;
        int sz = c.size();
        modCount++;
        size += sz;
        
        addFingersAfterSetAll();
    }
    
    private void prependAll(Collection<? extends E> c) {
        Iterator<? extends E> iterator = c.iterator();
        final Node<E> oldFirst = first;
        first = new Node<>();
        first.item = iterator.next();
        
        Node<E> prevNode = first;
        
        for (int i = 1, sz = c.size(); i < sz; i++) {
            Node<E> newNode = new Node<>();
            newNode.item = iterator.next();
            newNode.prev = prevNode;
            prevNode.next = newNode;
            prevNode = newNode;
        }
        
        prevNode.next = oldFirst;
        oldFirst.prev = prevNode;
        
        int sz = c.size();
        modCount++;
        size += sz;
        
        // Prior to adding new (possible) fingers, we need to shift all the 
        // current fingers 'c.size()' nodes to the larger index values:
        shiftFingersToRight(0, sz);
        
        // Now, add the missing fingers:
        addFingersAfterPrependAll(first, sz);
    }
    
    private void appendAll(Collection<? extends E> c) {
        Node<E> prev = last;
        final Node<E> oldLast = last;
        
        for (E item : c) {
            Node<E> newNode = new Node<>();
            newNode.item = item;
            newNode.prev = prev;
            prev.next = newNode;
            prev = newNode;
        }
        
        last = prev;
        int sz = c.size();
        size += sz;
        modCount++;
        addFingersAfterAppendAll(oldLast.next, size - sz, sz);
    }
    
    private void insertAll(
            Collection<? extends E> c, 
            Node<E> succ, 
            int succIndex) {
        
        final Node<E> pred = succ.prev;
        Node<E> prev = pred;
        
        for (E item : c) {
            final Node<E> newNode = new Node<>();
            newNode.item = item;
            newNode.prev = prev;
            prev.next = newNode;
            prev = newNode;
        }
        
        prev.next = succ;
        succ.prev = prev;
        
        int sz = c.size();
        modCount++;
        size += sz;
        
        // Shift all the fingers positions past the 'succ' on the right 'sz' 
        // positions to the right:
        shiftIndicesToRight(succIndex, sz);
        //                                   0 1 |10 11 12| 3 4 5 6 7 8 9
        // Add fingers:
        addFingersAfterInsertAll(pred.next, succIndex, sz);
    }
    
    private void addFingersAfterSetAll() {
        final int numberOfNewFingers = getRecommendedNumberOfFingers();
        
        if (numberOfNewFingers == 0) 
            return;
        
        final int distance = size / numberOfNewFingers;
        final int startIndex = distance / 2;
        int index = startIndex;
        Node<E> node = first;
        
        for (int i = 0; i < startIndex; i++)
            node = node.next;
        
        addFinger(node, startIndex);
        
        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;
            
            for (int j = 0; j < distance; j++)
                node = node.next;
            
            addFinger(node, index);
        }
    }
    
    /**
     * Adds fingers after prepending a collection to this list.
     * 
     * @param first 
     * @param collectionSize 
     */
    private void addFingersAfterPrependAll(Node<E> first, int collectionSize) {
        final int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerStack.size();
        
        if (numberOfNewFingers == 0) 
            return;
        
        final int distance = collectionSize / numberOfNewFingers;
        final int startIndex = distance / 2;
        int index = startIndex;
        Node<E> node = first;
        
        for (int i = 0; i < startIndex; i++) 
            node = node.next;
        
        addFinger(node, index);
        
        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;
            
            for (int j = 0; j < distance; j++) 
                node = node.next;
            
            addFinger(node, index);
        }
    }
    
    private void addFingersAfterInsertAll(Node<E> headNodeOfInsertedRange,
                                          int indexOfInsertedRangeHead,
                                          int collectionSize) {
        final int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerStack.size();
        
        if (numberOfNewFingers == 0) 
            return;
        
        final int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        final int startOffset = distanceBetweenFingers / 2;
        
        int index = indexOfInsertedRangeHead + startOffset;
        Node<E> node = headNodeOfInsertedRange;
        
        for (int i = 0; i < startOffset; i++) 
           node = node.next;
        
        addFinger(node, index);
        
        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;
            
            for (int j = 0; j < distanceBetweenFingers; j++) 
                node = node.next;
            
            addFinger(node, index);
        }
    }
    
    private void addFingersAfterAppendAll(
            Node<E> first, 
            int firstIndex, 
            int collectionSize) {
        final int numberOfNewFingers = 
                getRecommendedNumberOfFingers() - fingerStack.size();
        
        if (numberOfNewFingers == 0) 
            return;
        
        final int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        final int startOffset = firstIndex + distanceBetweenFingers / 2;
        int index = firstIndex + startOffset;
        Node<E> node = first;
        
        for (int i = 0; i < startOffset; i++) {
            if (node.next == null) 
                break;
            
            node = node.next;
        }
        
        addFinger(node, index);
        
        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;
            
            for  (int j = 0; j < distanceBetweenFingers; j++) {
                if (node.next == null) 
                    break;
                
                node = node.next;
            }
            
            addFinger(node, index);
        }
    }
    
    // first - the leftmost node of the inserted range,
    // firstIndex - the index of the leftmost node,
    // collectionSize - the size of the inserted range.
    private void addFingers(Node<E> first, int firstIndex, int collectionSize) {
        shiftIndicesToRight(firstIndex, collectionSize);
        final int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerStack.size();
        
        if (numberOfNewFingers == 0) 
            return;
        
        final int distance = collectionSize / numberOfNewFingers;
        final int startOffset = firstIndex + distance / 2;
        int index = firstIndex + startOffset;
        Node<E> node = first;
        
        for (int i = 0; i < startOffset; i++) {
            if (node.next == null) 
                break;
        
            node = node.next;
        }
        
        addFinger(node, index);
        
        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;
            
            for (int j = 0; j < distance; j++) 
                if (node.next != null)
                    node = node.next;
            
            addFinger(node, index);
        }
        
        for (int i = 0, sz = fingerStack.size(); i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            
            if (finger.index >= this.size) {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peekFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peekLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E poll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E element() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peek() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void push(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Iterator<E> iterator() {
        return new BasicIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static class Node<E> {
        E item;
        Node<E> prev;
        Node<E> next;
        
        @Override
        public String toString() {
            return "[Node; item = " + item + "]";
        }
    }
    
    private static class Finger<E> {
        Node<E> node;
        int index;
        
        Finger(Node<E> node, int index) {
            this.node = node;
            this.index = index;
        }
        
        @Override
        public String toString() {
            return "[Finger; index = " + index + ", item = " + node.item + "]";
        }
        
        void rewindLeft(int steps) {
            for (int i = 0; i < steps; i++) {
                node = node.prev;
            }
            
            index -= steps;
        }
        
        void rewindRight(int steps) {
            for (int i = 0; i < steps; i++) {
                node = node.next;
            }
            
            index += steps;
        }
    }
    
    /**
     * Implements a simple, array-based stack for storing the node fingers.
     * 
     * @param <E> the list element type
     */
    private static class FingerStack<E> {
        private static final int INITIAL_CAPACITY = 8;
        
        transient Finger<E>[] fingerArray;
        transient int size = 0;
        
        FingerStack() {
            this.fingerArray = new Finger[INITIAL_CAPACITY];
        }
        
        void push(Finger<E> finger) {
            enlargeFingerArrayIfNeeded();
            fingerArray[size++] = finger;
        }
        
        void pop() {
            fingerArray[--size] = null;
        }
        
        int size() {
            return size;
        }
        
        Finger<E> get(int index) {
            return fingerArray[index];
        }
        
        private void rewind(int startingIndex, int steps) {
            for (int i = 0; i < size; i++) {
                Finger<E> finger = fingerArray[i];
                if (finger.index >= startingIndex) 
                    finger.index += steps;
            }
        }
        
        void rewindLeft(int startingIndex) {
            rewind(startingIndex, -1);
        }
        
        void rewindRight(int startingIndex) {
            rewind(startingIndex, 1);
        }
        
        void rewindLeft(int startingIndex, int steps) {
            rewind(startingIndex, -steps);
        }
        
        void rewindRight(int startingIndex, int steps) {
            rewind(startingIndex, steps);
        }
        
        void clear() {
            for (int i = 0; i < size; i++) {
                fingerArray[i].node = null; // help GC
                fingerArray[i] = null;
            }
            
            size = 0;
        }
        
        private void enlargeFingerArrayIfNeeded() {
            if (size == fingerArray.length) {
                final int nextCapacity = 3 * fingerArray.length / 2;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
    }
    
    private final class BasicIterator implements Iterator<E> {

        private Node<E> lastReturned;
        private Node<E> next = first;
        private int nextIndex;
        private final int expectedModCount = modCount;
        
        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public E next() {
            checkForComodification();
            if (!hasNext()) 
                throw new NoSuchElementException();
            
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        @Override
        public void remove() {
            
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            while (hasNext()) 
                action.accept(next());
        }
        
        private final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
    
    /***************************************************************************
    * Returns a finger that does not point to the element to remove. We need   *
    * this in order to make sure that after removal, all the fingers point to  *
    * valid nodes.                                                             *
    ***************************************************************************/
    private void moveFingerOutOfRemovalLocation(Finger<E> finger) {
        if (size == 1) {
            fingerStack.pop();
            return;
        }
        
        if (finger.node.prev != null) {
            // Move the finger one position to the left:
            finger.node = finger.node.prev;
            finger.index--;
            return;
        }
        
        if (finger.node.next != null) {
            // Move the finger one position to the right:
            finger.node = finger.node.next;
            finger.index++;
            return;
        }
        
        throw new IllegalStateException("Removing from an empty list.");
    }
    
    private int getRecommendedNumberOfFingers() {
        return (int) Math.ceil(Math.sqrt(size / 2.0));
    }
    
    private static int getRecommendedFingerCount(int size) {
        return (int) Math.ceil(Math.sqrt(size / 2.0));
    }
    
    /***************************************************************************
    * Checks that the input index is a valid position index for add operation  *
    * or iterator position.                                                    *
    ***************************************************************************/
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    /***************************************************************************
    * Tells if the argument is the index of a valid position for an iterator   *
    * or an add operation.                                                     *
    ***************************************************************************/
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
    
    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }
    
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    /***************************************************************************
    * Tells if the argument is the index of an existing element.               *
    ***************************************************************************/
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }
  
    /***************************************************************************
     * Subtracts 'steps' positions from each index at least 'startingIndex'.   *
     **************************************************************************/
    private void shiftIndicesToLeft(int startingIndex, int steps) {
        for (int i = 0, sz = fingerStack.size; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index >= startingIndex) 
                finger.index -= steps; // substract from index
        }
    }
    
    /***************************************************************************
     * Adds 'steps' positions to each index at least 'startingIndex'.          *
     **************************************************************************/
    private void shiftIndicesToRight(int startingIndex, int steps) {
        for (int i = 0, sz = fingerStack.size; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index >= startingIndex) 
                finger.index += steps; // add to index
        }
    }
    
    /***************************************************************************
    * Shifts all the indices at least 'startingIndex' one position towards     *
    * smaller index values.                                                    *
    ***************************************************************************/
    private void shiftIndicesToLeftOnce(int startingIndex) {
        shiftIndicesToLeft(startingIndex, 1);
    }
    
    /***************************************************************************
    * Shifts all the indices at least 'startingIndex' one position towards     *
    * larger index values.                                                     *
    ***************************************************************************/
    private void shiftIndicesToRightOnce(int startingIndex) {
        shiftFingersToRight(startingIndex, 1);
    }
}
