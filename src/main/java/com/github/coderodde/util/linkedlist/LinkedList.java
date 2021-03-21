package com.github.coderodde.util.linkedlist;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/**
 *
 * @author Rodion Efremov
 * @see    List
 * @see    ArrayList
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
    transient FingerStack<E> fingerStack;
    
    public LinkedList() {
        this.fingerStack = new FingerStack<>();
    }
    
    public LinkedList(Collection<? extends E> c) {
        this();
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
        
        for (int sz = fingerStack.size(), i = 0; i < sz; i++)
            fingerStack.get(i).index++;
        
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
            addFinger(l, size - 1);
    }
    
    private boolean mustAddFinger() {
        return fingerStack.size() < getRecommendedFingerCount();
    }
    
    private void addFinger(Node<E> node, int index) {
        final Finger<E> finger = new Finger<>(node, index);
        fingerStack.push(finger);
    }
    
    private void shiftFingersRight(int startIndex) {
        for (int sz = fingerStack.size(), i = 0; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index > startIndex) 
                finger.index++;
        }
    }
    
    private void shiftFingersLeft(int startIndex) {
        for (int sz = fingerStack.size(), i = 0; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index > startIndex)
                finger.index--;
        }
    }
    
    private Node<E> search(int index) {
        int bestDistance = Integer.MAX_VALUE;
        Finger<E> bestFinger = null;
        
        for (int sz = fingerStack.size(), i = 0; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            int distance = finger.index - index;
            if (distance == 0) 
                return finger.node;
            
            if (Math.abs(bestDistance) > Math.abs(distance)) {
                bestDistance = distance;
                bestFinger = finger;
            }
        }
        
        if (bestDistance > 0)
            for (int i = bestFinger.index; i < index; i++) 
                bestFinger.node = bestFinger.node.next;
        else 
            for (int i = bestFinger.index; i > index; i--) 
                bestFinger.node = bestFinger.node.prev;
        
        bestFinger.index += bestDistance;
        return bestFinger.node;
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
}
