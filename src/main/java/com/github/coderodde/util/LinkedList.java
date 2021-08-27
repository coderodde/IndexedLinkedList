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
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Doubly-linked list implementation of the {@code List} and {@code Deque}
 * interfaces.  Implements all optional list operations, and permits all
 * elements (including {@code null}).
 *
 * <p>This implementation maintains <code>ceil(sqrt(n/2))</code> fingers, which
 * allows faster <code>add(int, E)</code> and <code>remove(int)</code>.
 * 
 * The running time table is as follows:
 * 
 * <p>All of the operations perform as could be expected for a doubly-linked
 * list.  Operations that index into the list will traverse the list from
 * the beginning or the end, whichever is closer to the specified index.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a linked list concurrently, and at least
 * one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more elements; merely setting the value of
 * an element is not a structural modification.)  This is typically
 * accomplished by synchronizing on some object that naturally
 * encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new LinkedList(...));</pre>
 *
 * <p>The iterators returned by this class's {@code iterator} and
 * {@code listIterator} methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in
 * any way except through the Iterator's own {@code remove} or
 * {@code add} methods, the iterator will throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than
 * risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:   <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @see     List
 * @see     ArrayList
 * @since 1.2
 * @param <E> the type of elements held in this collection
 */

public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{

    /**
     * Number of elements in the list.
     */
    private int size = 0;

    /**
     * Pointer to first node.
     */
    private transient Node<E> first;

    /**
     * Pointer to last node.
     */
    private transient Node<E> last;

    /**
     * Stack of fingers.
     */
    private transient FingerStack<E> fingerStack = new FingerStack<>();

    /**
     * Constructs an empty list.
     */
    public LinkedList() {

    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param  c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #addLast}.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
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

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress.  (Note that this will occur if the specified collection is
     * this list, and it's nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element
     *              from the specified collection
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
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
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clear() {
        fingerStack.clear();
        size = 0;

        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        for (Node<E> node = first; node != null;) {
            node.prev = null;
            node.item = null;
            Node<E> next = node.next;
            node.next = null;
            node = next;
        }

        first = last = null;
        modCount++;
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
     * @since 1.6
     */
    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */
    @Override
    public E element() {
        return getFirst();
    }

    /**
     * Returns {@code true} only if the input object is a {@link List}, has the
     * same size, and whose iterator returns the elements in the same order as
     * this list.
     *
     * @param o the query object.
     * @return {@code true} only if this list and the input list represent the
     * same element sequence.
     */
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
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
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
     * Returns the smallest index of the input object, or -1, if the object does
     * not appear in this list.
     * 
     * @param o the object whose index to return.
     * @return the index of {@code o}, or -1, if none is present.
     */
    public int indexOf(Object o) {
        int index = 0;
        
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (o.equals(x.item)) 
                    return index;
            }
        }
        
        return -1;
    }

    /**
     * Returns the basic iterator over this list supporting only traversal and
     * removal.
     *
     * @return the basic iterator.
     */
    @Override
    public Iterator<E> iterator() {
        return new BasicIterator();
    }

    /**
     * Returns the index of the last appearance of the input object {@code o}.
     * 
     * @param o the object to search for.
     * @return the largest index of {@code o}, or -1 if none is present.
     */
    public int lastIndexOf(Object o) {
        int index = size;
        
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null) 
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item)) 
                    return index;
            }
        }
        
        return -1;
    }
    
    /**
     * Returns a list-iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * Obeys the general contract of {@code List.listIterator(int)}.<p>
     *
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own {@code remove} or {@code add}
     * methods, the list-iterator will throw a
     * {@code ConcurrentModificationException}.  Thus, in the face of
     * concurrent modification, the iterator fails quickly and cleanly, rather
     * than risking arbitrary, non-deterministic behavior at an undetermined
     * time in the future.
     *
     * @param index index of the first element to be returned from the
     *              list-iterator (by a call to {@code next})
     * @return a ListIterator of the elements in this list (in proper
     *         sequence), starting at the specified position in the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see List#listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return new EnhancedIterator(index);
    }

    /**
     * Adds the specified element as the tail (last element) of this list.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Queue#offer})
     * @since 1.5
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    /**
     * Inserts the specified element at the front of this list.
     *
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerFirst})
     * @since 1.6
     */
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * Inserts the specified element at the end of this list.
     *
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerLast})
     * @since 1.6
     */
    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */
    @Override
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * Retrieves, but does not remove, the first element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the first element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */
    @Override
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * Retrieves, but does not remove, the last element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */
    @Override
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */
    @Override
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : removeFirst();
    }

    /**
     * Retrieves and removes the first element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the first element of this list, or {@code null} if
     *     this list is empty
     * @since 1.6
     */
    @Override
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : removeFirst();
    }

    /**
     * Retrieves and removes the last element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null} if
     *     this list is empty
     * @since 1.6
     */
    @Override
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast();
    }

    /**
     * Pops an element from the stack represented by this list.  In other
     * words, removes and returns the first element of this list.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the element at the front of this list (which is the top
     *         of the stack represented by this list)
     * @throws NoSuchElementException if this list is empty
     * @since 1.6
     */
    @Override
    public E pop() {
        return removeFirst();
    }

    /**
     * Pushes an element onto the stack represented by this list.  In other
     * words, inserts the element at the front of this list.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @param e the element to push
     * @since 1.6
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */
    @Override
    public E remove() {
        return removeFirst();
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * {@code i} such that
     * {@code Objects.equals(o, get(i))}
     * (if such an element exists).  Returns {@code true} if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */
    public boolean remove(Object o) {
        int index = 0;

        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (x.item == null) {
                    remove(index);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next, index++) {
                if (o.equals(x.item)) {
                    remove(index);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Removes the element residing at the given index.
     * The procedure:
     * 1. Find the node N to remove
     * 2. If N is fingered by F, move F left/right
     * 3. unlink(N)
    */
    public E remove(int index) {
        checkElementIndex(index);
        
        Finger<E> finger = getClosestFingerAndRewind(index);
        Node<E> nodeToRemove = finger.node;
        E returnValue = nodeToRemove.item;
        
        moveFingerOutOfRemovalLocation(finger);
        unlink(nodeToRemove);
        decreaseSize();
        
        if (mustRemoveFinger()) {
            removeFinger();
            fixFingersAfterRemoval(index);
        }
        
        shiftIndicesToLeftOnce(index + 1);
        return returnValue;
    }

    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public E removeFirst() {
        checkElementIndex(0);
        
        Finger<E> finger = getClosestFingerAndRewind(0);
        Node<E> nodeToRemove = finger.node;
        E returnValue = nodeToRemove.item;
        
        moveFingerOutOfRemovalLocation(finger);
        unlinkFirst();
        decreaseSize();
        
        if (mustRemoveFinger()) {
            removeFinger();
            fixFingersAfterRemoval(size);
        }
        
        shiftIndicesToLeftOnce(0);
        return returnValue;
    }

    /**
     * Removes the first occurrence of the specified element in this
     * list (when traversing the list from head to tail).  If the list
     * does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */
    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
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
        return unlinkLast();
    }

    /**
     * Removes the last occurrence of the specified element in this
     * list (when traversing the list from head to tail).  If the list
     * does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */
    @Override
    public boolean removeLastOccurrence(Object o) {
        int index = size - 1;

        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev, index--) {
                if (x.item == null) {
                    unlink(x);

                    if (mustRemoveFinger())
                        removeFinger();

                    shiftIndicesToLeftOnce(index + 1);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev, index--) {
                if (o.equals(x.item)) {
                    unlink(x);

                    if (mustRemoveFinger())
                        removeFinger();

                    shiftIndicesToLeftOnce(index + 1);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }
    
    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#ORDERED}.  Overriding implementations should document
     * the reporting of additional characteristic values.
     *
     * @implNote
     * The {@code Spliterator} additionally reports {@link Spliterator#SUBSIZED}
     * and implements {@code trySplit} to permit limited parallelism..
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LinkedListSpliterator<E>(this, first, size, 0, modCount);
    }

    @java.io.Serial
    private static final long serialVersionUID = -8812077630522402934L;
    
    // Internal implementation methods begin:

    /***************************************************************************
    Adds a finger pointing to the input node at the input index.
    ***************************************************************************/
    private void addFinger(Node<E> node, int index) {
        final Finger<E> finger = new Finger<>(node, index);
        fingerStack.push(finger);
    }
    
    /***************************************************************************
    Adds fingers after appending a collection to this list.
    ***************************************************************************/
    private void addFingersAfterAppendAll(
            Node<E> first,
            int firstIndex,
            int collectionSize) {
        final int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerStack.size();

        if (numberOfNewFingers == 0)
            return;

        final int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        final int nodesToSkip = distanceBetweenFingers / 2;
        int index = firstIndex + nodesToSkip;
        Node<E> node = first;

        for (int i = 0; i < nodesToSkip; i++)
            node = node.next;

        addFinger(node, index);

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;

            for  (int j = 0; j < distanceBetweenFingers; j++) {
                node = node.next;
            }

            addFinger(node, index);
        }
    }
    
    /***************************************************************************
    Adds fingers after inserting a collection in this list.
    ***************************************************************************/
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
    
    /***************************************************************************
    Adds fingers after prepending a collection to this list.
    ***************************************************************************/
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

    /***************************************************************************
    Adds fingers after setting a collection as a list.
    ***************************************************************************/
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

    /***************************************************************************
    Appends the input collection to the tail of this list.
    ***************************************************************************/
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

    /***************************************************************************
    Checks the element index. In the case of non-empty list, valid indices are
    {@code 0, 1, ..., size - 1}.
    ***************************************************************************/
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(getOutOfBoundsMessage(index));
    }

    /***************************************************************************
    Used previously for debugging. Ignore.
    ***************************************************************************/
    public void checkInvariant() {
        for (int i = 0, sz = fingerStack.size(); i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            Node<E> node = getNodeRaw(finger.index);

            if (finger.node != node)
                throw new AssertionError(
                        "checkInvariant() failed at finger index (" +
                                finger.index + "), expected node = " +
                                finger.node + ", actual node = " + node);
        }
    }
 
   /***************************************************************************
    Checks that the input index is a valid position index for add operation or
    iterator position.
    ***************************************************************************/
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(getOutOfBoundsMessage(index));
    }
    
    /***************************************************************************
    Returns the closest finger to the node with index 'index'.
    ***************************************************************************/
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
    
    /***************************************************************************
    Used previously for debugging. Ignore.
    ***************************************************************************/
    private Node<E> getNodeRaw(int index) {
        Node<E> node = first;

        for (int i = 0; i < index; i++)
            node = node.next;

        return node;
    }

    /***************************************************************************
    Constructs an IndexOutOfBoundsException detail message.
    ***************************************************************************/
    private String getOutOfBoundsMessage(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    /***************************************************************************
    Computes the recommended number of fingers.
    ***************************************************************************/
    private int getRecommendedNumberOfFingers() {
        return (int) Math.ceil(Math.sqrt(size / 2.0));
    }
    
    /***************************************************************************
    Computes the recommended number of fingers for {@code size} elements.
    ***************************************************************************/
    private static int getRecommendedNumberOfFingers(int size) {
        return (int) Math.ceil(Math.sqrt(size / 2.0));
    }
    
    /***************************************************************************
    Inserts the input collection right before the node 'succ'.
    ***************************************************************************/
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

    /***************************************************************************
    Tells if the argument is the index of an existing element.
    ***************************************************************************/
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /***************************************************************************
    Tells if the argument is the index of a valid position for an iterator or an
    add operation.
    ***************************************************************************/
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    /***************************************************************************
    Links the input element right before the node 'succ'.
    ***************************************************************************/
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

    /***************************************************************************
    Prepends the input element to the head of this list.
    ***************************************************************************/
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

    /***************************************************************************
    Appends the input element to the tail of this list.
    ***************************************************************************/
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

    /***************************************************************************
    Loads the removal operation related data.
    ***************************************************************************/
    private Finger<E> getClosestFingerAndRewind(int index) {
        Finger<E> finger = getClosestFinger(index);

        if (index < finger.index) {
            final int distance = finger.index - index;
            finger.index -= distance;
            
            for (int i = 0; i < distance; i++)
                finger.node = finger.node.prev;
        } else {
            final int distance = index - finger.index;
            finger.index += distance;
            
            for (int i = 0; i < distance; i++)
                finger.node = finger.node.next;
        }

        return finger;
    }

    /***************************************************************************
    Returns a finger that does not point to the element to remove. We need this
    in order to make sure that after removal, all the fingers point to valid
    nodes.
    ***************************************************************************/
    private void moveFingerOutOfRemovalLocation(Finger<E> finger) {
        if (size() < 2) 
            return;

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
    
    /***************************************************************************
    Returns true only if this list requires more fingers.
    ***************************************************************************/
    private boolean mustAddFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() - 1
        return fingerStack.size() != getRecommendedNumberOfFingers();
    }
    
    /***************************************************************************
    Returns true only if this list requires less fingers.
    /***************************************************************************
    ***************************************************************************/
    private boolean mustRemoveFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() + 1
        return fingerStack.size() != getRecommendedNumberOfFingers();
    }

    /***************************************************************************
    Returns the node at index 'index'. Moves the closest finger to the node.
    ***************************************************************************/
    private Node<E> node(int index) {
        Finger<E> finger = getClosestFinger(index);
        int distance = finger.index - index;

        if (distance > 0)
            finger.rewindLeft(distance);
        else
            finger.rewindRight(-distance);

        return finger.node;
    }
    
    /***************************************************************************
    Prepends the input collection to the head of this list.
    ***************************************************************************/
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
        shiftIndicesToRight(0, sz);

        // Now, add the missing fingers:
        addFingersAfterPrependAll(first, sz);
    }

    /***************************************************************************
    Removes a finger from the finger stack.
    ***************************************************************************/
    private void removeFinger() {
        fingerStack.pop();
    }
    
//    /***************************************************************************
//    Removes the node from this list. Modifies the fingers as needed.
//    ***************************************************************************/
//    private E removeNodeFromList(Node<E> node, int index) {
//        loadRemoveData(index);
//
//        if (removedDataFinger.index == index) 
//            moveFingerOutOfRemovalLocation(removedDataFinger);
//
//        return unlink(node, index);
//    }
    
    /***************************************************************************
    Sets the input collection as a list.
    ***************************************************************************/
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

    /***************************************************************************
    Subtracts 'steps' positions from each index at least 'startingIndex'.
    ***************************************************************************/
    private void shiftIndicesToLeft(int startingIndex, int steps) {
        for (int i = 0, sz = fingerStack.size; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index >= startingIndex)
                finger.index -= steps; // substract from index
        }
    }

    /***************************************************************************
    Shifts all the indices at least 'startingIndex' one position towards smaller
    index values.
    ***************************************************************************/
    private void shiftIndicesToLeftOnce(int startingIndex) {
        shiftIndicesToLeft(startingIndex, 1);
    }
    
    /***************************************************************************
    For each finger with the index at least 'startIndex', add 'steps' to the
    index.
    ***************************************************************************/
    private void shiftIndicesToRight(int startIndex, int steps) {
        for (int sz = fingerStack.size(), i = 0; i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            if (finger.index >= startIndex)
                finger.index += steps;
        }
    }

    /***************************************************************************
    Shifts all the indices at least 'startingIndex' one position towards larger
    index values.
    ***************************************************************************/
    private void shiftIndicesToRightOnce(int startingIndex) {
        shiftIndicesToRight(startingIndex, 1);
    }

    /***************************************************************************
    Unlinks the input node and adjusts the fingers.
    ***************************************************************************/
    private void unlink(Node<E> x) {
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
    }
    
    private void decreaseSize() {
        size--;
        modCount++;
    }
    
    private void fixFingersAfterRemoval(int index) {
        for (int i = 0, sz = fingerStack.size(); i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            
            if (finger.index == index) {
                if (finger.index > 0) {
                    finger.index--;
                    finger.node = finger.node.prev;
                } else {
                    finger.index++;
                    finger.node = finger.node.next;
                }
            }
        }
    }

    /***************************************************************************
    Unlinks the head node from this list.
    ***************************************************************************/
    private void unlinkFirst() {
        final Node<E> next = first.next;
        first.item = null;
        first.next = null; // help GC
        first = next;

        if (next == null)
            last = null;
        else
            next.prev = null;
    }

    /***************************************************************************
    Unlinks the tail node from this list.
    ***************************************************************************/
    private E unlinkLast() {
        final E element = last.item;
        final Node<E> prev = last.prev;
        last.item = null;
        last.prev = null; // help GC
        last = prev;

        if (prev == null)
            first = null;
        else
            prev.next = null;

        size--;
        modCount++;

        if (mustRemoveFinger()) {
            removeFinger();
            fixFingersAfterRemoval(size);
        }

        return element;
    }
    
    // Used in the removal operations:
    private transient Finger<E> removedDataFinger;
    
    /**
     * Reconstitutes this {@code LinkedList} instance from a stream
     * (that is, deserializes it).
     */
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s) 
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        int size = s.readInt();
        this.size = size;
        this.fingerStack = new FingerStack<>();

        switch (size) {
            case 0:
                return;
                
            case 1:
                Node<E> newNode = new Node<>();
                newNode.item = (E) s.readObject();
                first = last = newNode;
                addFinger(newNode, 0);
                return;
        }
        
        Node<E> rightmostNode = new Node<>();
        rightmostNode.item = (E) s.readObject();
        first = rightmostNode;
        
        int numberOfRequestedFingers = getRecommendedNumberOfFingers(size);
        final int distance = size / numberOfRequestedFingers;
        int startOffset = distance / 2;
        
        // Read in all elements in the proper order.
        for (int i = 1; i < size; i++) {
            E item = (E) s.readObject();
            Node<E> node = new Node<>();
            node.item = item;
            
            if ((i - startOffset) % distance == 0) {
                addFinger(node, i);
            }
            
            rightmostNode.next = node;
            node.prev = rightmostNode;
            rightmostNode = node;
        }
        
        last = rightmostNode;
    }
    
    /**
     * Saves the state of this {@code LinkedList} instance to a stream
     * (that is, serializes it).
     *
     * @serialData The size of the list (the number of elements it
     *             contains) is emitted (int), followed by all of its
     *             elements (each an Object) in the proper order.
     */
    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }
    
    /***************************************************************************
    Implements the doubly-linked list node.
    ***************************************************************************/
    private static class Node<E> {
        E item;
        Node<E> prev;
        Node<E> next;

        @Override
        public String toString() {
            return "[Node; item = " + item + "]";
        }
    }

    /***************************************************************************
    Implements the list node finger.
    ***************************************************************************/
    private static final class Finger<E> {
        Node<E> node;
        int index; // Index at which 'node' is located.

        Finger(Node<E> node, int index) {
            this.node = node;
            this.index = index;
        }

        @Override
        public String toString() {
            return "[Finger; index = " + index + ", item = " + node.item + "]";
        }

        // Moves this finger 'steps' position to the left
        void rewindLeft(int steps) {
            for (int i = 0; i < steps; i++) {
                node = node.prev;
            }

            index -= steps;
        }

        // Moves this finger 'steps' position to the right
        void rewindRight(int steps) {
            for (int i = 0; i < steps; i++) {
                node = node.next;
            }

            index += steps;
        }
    }

    /***************************************************************************
    Implements a simple, array-based stack for storing the node fingers.
    
    @param <E> the list element type
    ***************************************************************************/
    private static final class FingerStack<E> {
        private static final int INITIAL_CAPACITY = 8;

        private Finger<E>[] fingerArray;
        private int size = 0;

        FingerStack() {
            this.fingerArray = new Finger[INITIAL_CAPACITY];
        }

        void push(Finger<E> finger) {
            enlargeFingerArrayIfNeeded();
            fingerArray[size++] = finger;
        }

        void pop() {
            Finger<E> finger = fingerArray[--size];
            fingerArray[size] = null;
            finger.node = null;
        }

        int size() {
            return size;
        }

        Finger<E> get(int index) {
            return fingerArray[index];
        }

        // Clears this finger stack:
        void clear() {
            for (int i = 0; i < size; i++) {
                fingerArray[i].node = null; // help GC
                fingerArray[i] = null;
            }

            size = 0;
        }

        // Makes sure that the next finger fits in this finger stack:
        private void enlargeFingerArrayIfNeeded() {
            if (size == fingerArray.length) {
                final int nextCapacity = 3 * fingerArray.length / 2;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
    }

    /***************************************************************************
    This class implements a basic iterator over this list.

    @param E the element type.
    ***************************************************************************/
    private final class BasicIterator implements Iterator<E> {

        private Node<E> lastReturned;
        private Node<E> next = first;
        private int nextIndex;
        private int expectedModCount = modCount;

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
            checkForComodification();
            if (lastReturned == null) 
                throw new IllegalStateException();
            
            Node<E> lastNext = lastReturned.next;
            int removalIndex = nextIndex - 1;
            LinkedList.this.remove(removalIndex);
            
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            
            lastReturned = null;
            expectedModCount++;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }

        private final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
    
    /***************************************************************************
    Implements the list iterator over this list.
    ***************************************************************************/
    private final class EnhancedIterator implements ListIterator<E> {

        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        private int expectedModCount = modCount;
        
        EnhancedIterator(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }
        
        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }
        
        @Override
        public E next() {
            checkForComdification();
            if (!hasNext()) 
                throw new NoSuchElementException();
            
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public E previous() {
            checkForComdification();
            if (!hasPrevious())
                throw new NoSuchElementException();
            
            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForComdification();
            if (lastReturned == null)
                throw new IllegalStateException();
            
            Node<E> lastNext = lastReturned.next;
            int removalIndex = nextIndex - 1;
            LinkedList.this.remove(removalIndex);
            
            if (next == lastReturned)
                next = lastNext;
            else 
                nextIndex = removalIndex;
            
            lastReturned = null;
            expectedModCount++;
        }

        @Override
        public void set(E e) {
            if (lastReturned == null) 
                throw new IllegalStateException();
            checkForComdification();
            lastReturned.item = e;
        }

        @Override
        public void add(E e) {
            checkForComdification();
            lastReturned = null;
            if (next == null) 
                linkLast(e);
            else
                linkBefore(e, next, nextIndex);
            nextIndex++;
            expectedModCount++;
        }
        
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComdification();
        }
        
        private final void checkForComdification() {
            if (modCount != expectedModCount) 
                throw new ConcurrentModificationException();
        }
    }
    
    private final class DescendingIterator implements Iterator<E> {

        private final ListIterator<E> iterator = new EnhancedIterator(size());
        
        @Override
        public boolean hasNext() {
            return iterator.hasPrevious();
        }

        @Override
        public E next() {
            return iterator.previous();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
    
    private static final class LinkedListSpliterator<E> 
            implements Spliterator<E> {
        
        private static final long MINIMUM_BATCH_SIZE = 1 << 10; // 1024 items
        
        private final LinkedList<E> list;
        private LinkedList.Node<E> node;
        private long lengthOfSpliterator;
        private long numberOfProcessedElements;
        private long offsetOfSpliterator;
        private final int expectedModCount;
        
        private LinkedListSpliterator(LinkedList<E> list,
                                      Node<E> node,
                                      long lengthOfSpliterator,
                                      long offsetOfSpliterator,
                                      int expectedModCount) {
            this.list = list;
            this.node = node;
            this.lengthOfSpliterator = lengthOfSpliterator;
            this.offsetOfSpliterator = offsetOfSpliterator;
            this.expectedModCount = expectedModCount;
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            if (numberOfProcessedElements == lengthOfSpliterator)
                return false;
            numberOfProcessedElements++;
            E item = node.item;
            action.accept(item);
            node = node.next;
            if (list.modCount != expectedModCount) 
                throw new ConcurrentModificationException();
            return true;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            for (long i = numberOfProcessedElements; 
                 i < lengthOfSpliterator; 
                 i++) {
                E item = node.item;
                action.accept(item);
                node = node.next;
            }
            numberOfProcessedElements = lengthOfSpliterator;
            if (list.modCount != expectedModCount) 
                throw new ConcurrentModificationException();
        }
        
        @Override
        public Spliterator<E> trySplit() {
            final long sizeLeft = estimateSize();
            if (sizeLeft == 0) 
                return null;
            
            final long thisSpliteratorNewLength = sizeLeft / 2L;
            
            if (thisSpliteratorNewLength < MINIMUM_BATCH_SIZE)
                return null;
            
            final long newSpliteratorLength = 
                    sizeLeft - thisSpliteratorNewLength;
            
            final long newSpliteratorOffset = this.offsetOfSpliterator;
            
            this.offsetOfSpliterator += newSpliteratorLength;
            this.lengthOfSpliterator -= newSpliteratorLength;
            Node<E> newSpliteratorNode = this.node;
            this.node = list.node((int) this.offsetOfSpliterator);
            
            return new LinkedListSpliterator<>(
                    list,
                    newSpliteratorNode,
                    newSpliteratorLength, // length
                    newSpliteratorOffset, // offset
                    expectedModCount);
        }

        @Override
        public long estimateSize() {
            return (long)(lengthOfSpliterator - numberOfProcessedElements);
        }

        @Override
        public long getExactSizeIfKnown() {
            return estimateSize();
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | 
                   Spliterator.SUBSIZED |
                   Spliterator.SIZED;
        }
        
        @Override
        public boolean hasCharacteristics(int characteristics) {
            switch (characteristics) {
                case Spliterator.ORDERED:
                case Spliterator.SIZED:
                case Spliterator.SUBSIZED:
                    return true;
                    
                default:
                    return false;
            }
        }
    }
}
