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
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author  Rodion Efremov
 * @see     List
 * @see     ArrayList
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
    
    public LinkedList(Collection<? extends E> c) {
        addAll(c);
    }
    
    private void linkFirst(E e) {
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
        shiftFingersLeft(1);
        
        if (mustAddFinger())
            addFinger(newNode, 0);
    }
    
    private void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>();
        newNode.item = e;
        newNode.prev = l;
        
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
        return fingerStack.size() != getRecommendedFingerCount();
    }
    
    private boolean mustRemoveFinger() {
        // here, fingerStack.size() == getRecommendedFingerCount(), or, 
        // fingerStack.size() == getRecommendedFingerCount() + 1
        return fingerStack.size() != getRecommendedFingerCount();
    }
    
    private void addFinger(Node<E> node, int index) {
        final Finger<E> finger = new Finger<>(node, index);
        fingerStack.push(finger);
    }
    
    private void removeFinger() {
        fingerStack.pop();
    }
    
    private void shiftFingersRight(int startIndex, int steps) {
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
                finger.index -= steps;
        }
    }
    
    private void shiftFingersRight(int startIndex) {
        shiftFingersRight(startIndex, 1);
    }
    
    private void shiftFingersLeft(int startIndex) {
        shiftFingersLeft(startIndex, 1);
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
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>();
        
        newNode.prev = pred;
        newNode.item = e;
        newNode.next = succ;
        succ.prev = newNode;
        
        if (pred == null) 
            first = newNode;
        else
            pred.next = newNode;
        
        size++;
        modCount++;
        
        if (mustAddFinger()) 
            addFinger(newNode, index);
        
        //! or index + 1?
        shiftFingersRight(index);
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
    
    public boolean remove(Object o) {
        int index = 0;
        
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (x.item == null) {
                    unlink(x, index);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (o.equals(x.item)) {
                    unlink(x, index);
                    return true;
                }
            }
        }
        return false;
    }
    
    private void unlink(Node<E> node, int index) {
        
    }
    
    public boolean addAll(Collection<? extends E> c) {
        return false;
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
        addFingers(newFirst, c.size());
    }
    
    private void appendAll(Collection<? extends E> c) {
        
    }
    
    public boolean addAll(int index, Collection<? extends E> c) {
//        checkPositionIndex(index);
//        if (c.isEmpty()) 
//            return false;
//        
//        if (index == 0) 
//            prependall(c);
//        else if (index == size) 
//            appendAll(c);
//            addAllImpl(index, c);
//        shiftFingersRight(index, c.size());
//        Finger<E> finger = getClosestFinger(index);
//        int distance = finger.index - index;
//        
//        if (distance > 0) 
//            finger.rewindLeft(distance);
//        else if (distance < 0) 
//            finger.rewindRight(-distance);
//        
//        Node<E> pred;
//        Node<E> succ;
//        
//        if (index == 0) {
//            pred = null;
//            succ = first;
//        } else {
//            pred
//        }
//        
//        for (E e : c) {
//            Node<E> newNode = new Node<>();
//            newNode.item = e;
//            
//        }
        return true;
    }
    
    private void addFingers(Node<E> first, int collectionSize) {
        final int requestedFingers = getRecommendedFingerCount();
        final int newFingers = requestedFingers - fingerStack.size();
        final int distance = collectionSize / newFingers;
        final int startOffset = distance / 2;
        int index = startOffset;
        Node<E> node = first;
        
        for (int i = 0; i < startOffset; i++) 
            node = node.next;
        
        for (int i = 0; i < newFingers; i++) {
            Finger<E> finger = new Finger<>(node, index);
            fingerStack.push(finger);
            index += distance;
            
            for (int j = 0; j < distance; j++) 
                node = node.next;
        }
    }

    @Override
    public void addFirst(E e) {
        linkFirst(e);
    }

    @Override
    public void addLast(E e) {
        linkLast(e);
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
    public E removeFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E removeLast() {
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
    public E getFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E getLast() {
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
    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static class Node<E> {
        E item;
        Node<E> prev;
        Node<E> next;
    }
    
    private static class Finger<E> {
        Node<E> node;
        int index;
        
        Finger(Node<E> node, int index) {
            this.node = node;
            this.index = index;
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
        
        private void enlargeFingerArrayIfNeeded() {
            if (size == fingerArray.length) {
                final int nextCapacity = 3 * fingerArray.length / 2;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
    }
    
    private int getRecommendedFingerCount() {
        return (int) Math.ceil(Math.sqrt(size / 2.0));
    }
    
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    /**
     * Tells if the argument is the index of a valid position for an
     * iterator or an add operation.
     */
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
}
