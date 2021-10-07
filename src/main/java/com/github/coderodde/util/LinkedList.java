package com.github.coderodde.util;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
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
 * <p>This implementation maintains <code>ceil(sqrt(n/2)/10)</code> fingers, 
 * which allows faster operations in methods requesting an integer index.
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
 * @author  Rodion "coderodde" Efremov
 * @see     List
 * @see     ArrayList
 * @version 1.6 (Sep 1, 2021)
 * @since 1.6 (Sep 1, 2021)
 * @param <E> the type of elements held in this collection
 */
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{

    /**
     * Number of elements in this list.
     */
    private int size = 0;

    /**
     * The first node.
     */
    private transient Node<E> first;

    /**
     * The last node.
     */
    private transient Node<E> last;

    /**
     * The stack of fingers.
     */
    transient FingerStack<E> fingerStack = new FingerStack<>();

    /**
     * Constructs an empty list.
     */
    public LinkedList() {}

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param  c the collection whose elements are to be placed into this list.
     * @throws NullPointerException if the specified collection is null.
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
     * @param e element to be appended to this list.
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    @Override
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * The affected finger indices will be incremented by one. A finger 
     * {@code F} is <i>affected</i>, if {@code F.index >= index}.
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public void add(int index, E element) {
        checkPositionIndex(index);
        
        if (index == size) {
            linkLast(element);
        } else {
            linkBefore(element, node(index), index);
        }
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order they are returned by the specified collection's
     * iterator.  The behavior of this operation is undefined if the specified 
     * collection is modified while the operation is in progress. (Note that 
     * this will occur if the specified collection is this list, and it's
     * nonempty.)
     *
     * @param c collection containing elements to be added to this list.
     * @return {@code true} if this list changed as a result of the call.
     * @throws NullPointerException if the specified collection is null.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    /**
     * Inserts all of the elements in the specified collection into this list, 
     * starting at the specified position. For each finger {@code F} with 
     * {@code F.index >= index} will increment {@code F.index} by 1.
     *
     * @param index index at which to insert the first element from the
     *              specified collection.
     * @param c collection containing elements to be added to this list.
     * @return {@code true} if this list changed as a result of the call.
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);
        
        if (c.isEmpty()) {
            return false;
        }

        if (size == 0) {
            setAll(c);
        } else if (index == 0) {
            prependAll(c);
        } else if (index == size) {
            appendAll(c);
        } else {
            insertAll(c, node(index), index);
        }
        
        return true;
    }

    /**
     * Inserts the specified element at the beginning of this list.
     *
     * @param e the element to add.
     */
    @Override
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #add}.
     *
     * @param e the element to add.
     */
    @Override
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * Removes all of the elements from this list. The list will be empty after 
     * this call returns.
     */
    @Override
    public void clear() {
        fingerStack.clear();
        size = 0;

        // Help GC:
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
     * Returns {@code true} if this list contains the specified element. More 
     * formally, returns {@code true} if and only if this list contains at least 
     * one element {@code e} such that {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this list is to be tested.
     * @return {@code true} if this list contains the specified element.
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns a basic, descending iterator over this list.
     * 
     * @return the descending iterator.
     */
    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list.
     * @throws NoSuchElementException if this list is empty.
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
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }
        
        if (!(o instanceof List)) {
            return false;
        }
        
        List<?> otherList = (List<?>) o;

        if (size != otherList.size()) {
            return false;
        }

        Iterator<?> iterator1 = iterator();
        Iterator<?> iterator2 = otherList.iterator();

        while (iterator1.hasNext() && iterator2.hasNext()) {
            Object object1 = iterator1.next();
            Object object2 = iterator2.next();

            if (!Objects.equals(object1, object2)) {
                return false;
            }
        }

        boolean iterator1HasMore = iterator1.hasNext();
        boolean iterator2HasMore = iterator2.hasNext();

        if (iterator1HasMore || iterator2HasMore) {
            throw new IllegalStateException(
                    iterator1HasMore ?
                            "This list has more elements to offer" :
                            "Argument list has more elements to offer");
        }

        return true;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E getFirst() {
        Node<E> f = first;
        
        if (f == null) {
            throw new NoSuchElementException();
        }
        
        return f.item;
    }

    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E getLast() {
        Node<E> l = last;
        
        if (l == null) {
            throw new NoSuchElementException();
        }
        
        return l.item;
    }
    
    /**
     * Returns the smallest index of the input object, or -1, if the object does
     * not appear in this list.
     * 
     * @param o the object whose index to return.
     * @return the index of {@code o}, or -1, if none is present.
     */
    @Override
    public int indexOf(Object o) {
        int index = 0;
        
        for (Node<E> x = first; x != null; x = x.next, index++) {
            if (Objects.equals(o, x.item)) {
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
    @Override
    public int lastIndexOf(Object o) {
        int index = size;
        
        for (Node<E> x = last; x != null; x = x.prev) {
            index--;

            if (Objects.equals(o, x.item)) {
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
     * The list iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list iterator's own {@code remove} or {@code add} methods,
     * the list-iterator will throw a {@code ConcurrentModificationException}.  
     * Thus, in the face of concurrent modification, the iterator fails quickly 
     * and cleanly, rather than risking arbitrary, non-deterministic behavior at
     * an undetermined time in the future.
     *
     * @param index index of the first element to be returned from the
     *              list iterator (by a call to {@code next}).
     * @return a ListIterator of the elements in this list (in proper
     *         sequence), starting at the specified position in the list.
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see List#listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return new EnhancedIterator(index);
    }

    /**
     * Adds the specified element as the tail of this list.
     *
     * @param e the element to add.
     * @return {@code true} (as specified by {@link Queue#offer}).
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    /**
     * Inserts the specified element at the front of this list.
     *
     * @param e the element to insert.
     * @return {@code true} (as specified by {@link Deque#offerFirst}).
     */
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * Appends the specified element at the end of this list.
     *
     * @param e the element to append
     * @return {@code true} (as specified by {@link Deque#offerLast}).
     */
    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty.
     */
    @Override
    public E peek() {
        return first == null ? null : first.item;
    }

    /**
     * Retrieves, but does not remove, the first element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the first element of this list, or {@code null} if this list is 
     *         empty.
     */
    @Override
    public E peekFirst() {
        return first == null ? null : first.item;
    }

    /**
     * Retrieves, but does not remove, the last element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null} if this list is 
     *         empty.
     */
    @Override
    public E peekLast() {
        return last == null ? null : last.item;
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty
     */
    @Override
    public E poll() {
        return first == null ? null : removeFirst();
    }

    /**
     * Retrieves and removes the first element of this list, or returns 
     * {@code null} if this list is empty.
     *
     * @return the first element of this list, or {@code null} if this list is 
     *         empty.
     */
    @Override
    public E pollFirst() {
        return first == null ? null : removeFirst();
    }

    /**
     * Retrieves and removes the last element of this list, or returns 
     * {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null} if this list is 
     *         empty.
     */
    @Override
    public E pollLast() {
        return last == null ? null : removeLast();
    }

    /**
     * Pops an element from the stack represented by this list. In other words,
     * removes and returns the first element of this list.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the element at the front of this list (which is the top of the 
     *         stack represented by this list).
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E pop() {
        return removeFirst();
    }

    /**
     * Pushes an element onto the stack represented by this list. In other 
     * words, inserts the element at the front of this list.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @param e the element to push.
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E remove() {
        if (size == 0) {
            throw new NoSuchElementException("remove() from empty LinkedList.");
        }
        
        return removeFirst();
    }

    /**
     * Removes the first occurrence of the specified element from this list, if 
     * it is present.  If this list does not contain the element, it is 
     * unchanged.  More formally, removes the element with the lowest index 
     * {@code i} such that {@code Objects.equals(o, get(i))} (if such an element 
     * exists). Returns {@code true} if this list contained the specified 
     * element (or equivalently, if this list changed as a result of the call).
     *
     * @param o element to be removed from this list, if present.
     * @return {@code true} if this list contained the specified element.
     */
    @Override
    public boolean remove(Object o) {
        int index = 0;

        for (Node<E> x = first; x != null; x = x.next, index++) {
            if (Objects.equals(o, x.item)) {
                removeObjectImpl(x, index);
                return true;
            }
        }

        return false;
    }
    
    /**
     * Removes the element residing at the given index.
     * 
     * @param index the index of the element to remove.
     * @return the removed element. (The one that resided at the index 
     *         {@code index}.)
    */
    @Override
    public E remove(int index) {
        checkElementIndex(index);
        
        Finger<E> closestFinger = getClosestFinger(index);
        E returnValue;
        Node<E> nodeToRemove;
        
        if (closestFinger.index == index) {
            nodeToRemove = closestFinger.node;
            moveFingerOutOfRemovalLocation(closestFinger);    
        } else {
            // Keep the fingers at their original position.
            // Find the target node:
            nodeToRemove = rewind(closestFinger, closestFinger.index - index);
        }
        
        returnValue = nodeToRemove.item;
        unlink(nodeToRemove);
        decreaseSize();

        if (mustRemoveFinger()) {
            removeFinger();
        }

        shiftIndicesToLeftOnce(index + 1);
        return returnValue;
    }

    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException(
                    "removeFirst from an empty LinkedList");
        }
        
        checkElementIndex(0);
        
        E returnValue = first.item;
        decreaseSize();
        
        first = first.next;
        
        if (first == null) {
            last = null;
        } else {
            first.prev = null;
        }

        if (mustRemoveFinger()) {
            removeFinger();
        }
        
        shiftIndicesToLeftOnce(1);
        return returnValue;
    }

    /**
     * Removes the first occurrence of the specified element in this list (when 
     * traversing the list from head to tail). If the list does not contain the 
     * element, it is unchanged.
     *
     * @param o element to be removed from this list, if present.
     * @return {@code true} only if this list contained the specified element.
     */
    @Override
    public boolean removeFirstOccurrence(Object o) {
        int index = 0;
        
        for (Node<E> x = first; x != null; x = x.next, index++) {
            if (Objects.equals(o, x.item)) {
                removeObjectImpl(x, index);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("removeLast on empty LinkedList");
        }
        
        checkElementIndex(size - 1);
        
        E returnValue = last.item;
        decreaseSize();
        
        last = last.prev;
        
        if (last == null) {
            first = null;
        } else {
            last.next = null;
        }
        
        if (mustRemoveFinger()) {
            removeFinger();
        }
        
        return returnValue;
    }

    /**
     * Removes the last occurrence of the specified element in this list (when 
     * traversing the list from tail to head). If the list does not contain the
     * element, it is unchanged.
     *
     * @param o element to be removed from this list, if present.
     * @return {@code true} only if this list contained the specified element.
     */
    @Override
    public boolean removeLastOccurrence(Object o) {
        int index = size - 1;

        for (Node<E> x = last; x != null; x = x.prev, index--) {
            if (Objects.equals(o, x.item)) {
                removeObjectImpl(x, index);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    @Override
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
     * and implements {@code trySplit} to permit limited parallelism.
     *
     * @return a {@code Spliterator} over the elements in this list.
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LinkedListSpliterator<>(this, first, size, 0, modCount);
    }

    @java.io.Serial
    private static final long serialVersionUID = -8812077630522402934L;
    
    // Internal implementation methods begin:

    /***************************************************************************
    Adds a finger pointing to the input node at the input index.
    ***************************************************************************/
    private void addFinger(Node<E> node, int index) {
        fingerStack.push(new Finger<>(node, index));
    }
    
    /***************************************************************************
    Adds fingers after appending a collection to this list.
    ***************************************************************************/
    private void addFingersAfterAppendAll(
            Node<E> first,
            int firstIndex,
            int collectionSize) {
        int numberOfNewFingers = 
                getRecommendedNumberOfFingers() - fingerStack.size();

        if (numberOfNewFingers == 0) {
            return;
        }

        int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        int nodesToSkip = distanceBetweenFingers / 2;
        int index = firstIndex + nodesToSkip;
        Node<E> node = first;

        for (int i = 0; i < nodesToSkip; i++) {
            node = node.next;
        }

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
        int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerStack.size();

        if (numberOfNewFingers == 0) {
            return;
        }

        int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        int startOffset = distanceBetweenFingers / 2;
        int index = indexOfInsertedRangeHead + startOffset;
        Node<E> node = headNodeOfInsertedRange;

        for (int i = 0; i < startOffset; i++) {
           node = node.next;
        }

        addFinger(node, index);

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;

            for (int j = 0; j < distanceBetweenFingers; j++) {
                node = node.next;
            }

            addFinger(node, index);
        }
    }
    
    /***************************************************************************
    Adds fingers after prepending a collection to this list.
    ***************************************************************************/
    private void addFingersAfterPrependAll(Node<E> first, int collectionSize) {
        int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerStack.size();

        if (numberOfNewFingers == 0) {
            return;
        }

        int distance = collectionSize / numberOfNewFingers;
        int startIndex = distance / 2;
        int index = startIndex;
        Node<E> node = first;

        for (int i = 0; i < startIndex; i++) {
            node = node.next;
        }

        addFinger(node, index);

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;

            for (int j = 0; j < distance; j++) {
                node = node.next;
            }

            addFinger(node, index);
        }
    }

    /***************************************************************************
    Adds fingers after setting a collection as a list.
    ***************************************************************************/
    private void addFingersAfterSetAll() {
        int numberOfNewFingers = getRecommendedNumberOfFingers();

        if (numberOfNewFingers == 0) {
            return;
        }

        int distance = size / numberOfNewFingers;
        int startIndex = distance / 2;
        int index = startIndex;
        Node<E> node = first;

        for (int i = 0; i < startIndex; i++) {
            node = node.next;
        }

        addFinger(node, startIndex);

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;

            for (int j = 0; j < distance; j++) {
                node = node.next;
            }

            addFinger(node, index);
        }
    }

    /***************************************************************************
    Appends the input collection to the tail of this list.
    ***************************************************************************/
    private void appendAll(Collection<? extends E> c) {
        Node<E> prev = last;
        Node<E> oldLast = last;

        for (E item : c) {
            Node<E> newNode = new Node<>(item);
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
    '{ 0, 1, ..., size - 1 }'.
    ***************************************************************************/
    private void checkElementIndex(int index) {
        if (!isElementIndex(index)) {
            throw new IndexOutOfBoundsException(getOutOfBoundsMessage(index));
        }
    }

    /***************************************************************************
    Used previously for debugging. Ignore.
    ***************************************************************************/
    public void checkInvariant() {
        if (fingerStack.size() < 2) {
            return;
        }
        
        for (int i = 0, sz = fingerStack.size(); i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            
            assert finger.node.prev != null || finger.node.next != null :
                    "checkInvariant() failed: finger " + finger + " has " +
                    "null siblings.";
            
            assert fingerStack.fingerIndexSet.contains(finger.index) :
                    "checkInvariant() failed: Set does not contain " + finger;
            
            int onLeftNodes = countLeft(finger);
            int onRightNodes = countRight(finger);
            
            assert this.size == onLeftNodes + 1 + onRightNodes :
                    "checkInvariant() failed at finger (" + finger +
                    "), prefix/suffix error";
            
            Node<E> node = getNodeRaw(finger.index);

            assert finger.node == node : 
                    "checkInvariant() failed: finger/node mismatch: " + 
                    "(finger = " + finger + ", node = " + node + ")";
        }
    }
    
   /***************************************************************************
    Checks that the input index is a valid position index for add operation or
    iterator position. In other words, checks that {@code index} is in the set
    '{ 0, 1, ..., size}'.
    ***************************************************************************/
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index)) {
            throw new IndexOutOfBoundsException(getOutOfBoundsMessage(index));
        }
    }
    
    // Used for checkInvariant()
    private static <E> int countLeft(Finger<E> finger) {
        Node<E> node = finger.node.prev;
        int count = 0;
        
        while (node != null) {
            node = node.prev;
            count++;
        }
        
        return count;
    }
    
    // Used for checkInvariant()
    private static <E> int countRight(Finger<E> finger) {
        Node<E> node = finger.node.next;
        int count = 0;
        
        while (node != null) {
            node = node.next;
            count++;
        }
        
        return count;
    }
    
    private void decreaseSize() {
        size--;
        modCount++;
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

            if (distance == 0) {
                return finger;
            }

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

        for (int i = 0; i < index; i++) {
            node = node.next;
        }

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
        return (int) Math.ceil(Math.sqrt(size) / 10.0);
    }
    
    /***************************************************************************
    Computes the recommended number of fingers for 'size' elements.
    ***************************************************************************/
    private static int getRecommendedNumberOfFingers(int size) {
        return (int) Math.ceil(Math.sqrt(size) / 10.0);
    }
    
    private void increaseSize() {
        size++;
        modCount++;
    }
    
    /***************************************************************************
    Inserts the input collection right before the node 'succ'.
    ***************************************************************************/
    private void insertAll(
            Collection<? extends E> c,
            Node<E> succ,
            int succIndex) {

        Node<E> pred = succ.prev;
        Node<E> prev = pred;

        for (E item : c) {
            Node<E> newNode = new Node<>(item);
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

        Node<E> pred = succ.prev;
        Node<E> newNode = new Node<>(e);
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

        if (mustAddFinger()) {
            addFinger(newNode, index);
        }
    }

    /***************************************************************************
    Prepends the input element to the head of this list.
    ***************************************************************************/
    private void linkFirst(E e) {
        shiftIndicesToRightOnce(0);

        Node<E> f = first;
        Node<E> newNode = new Node<>(e);
        newNode.next = f;
        first = newNode;

        if (f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }

        increaseSize();

        if (mustAddFinger()) {
            addFinger(newNode, 0);
        }
    }

    /***************************************************************************
    Appends the input element to the tail of this list.
    ***************************************************************************/
    private void linkLast(E e) {
        Node<E> l = last;
        Node<E> newNode = new Node<>(e);
        newNode.prev = l;
        last = newNode;

        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        
        increaseSize();

        if (mustAddFinger()) {
            addFinger(newNode, size - 1);
        }
    }
    
    /***************************************************************************
    Makes sure that the input node is not being pointed to by a finger.
    ***************************************************************************/
    private void makeSureNoFingerPointsTo(Node<E> node, int index) {
        Finger<E> finger = getClosestFinger(index);
        
        if (finger.node == node) {
            moveFingerOutOfRemovalLocation(finger);
        }
    }

    /***************************************************************************
    Returns a finger that does not point to the element to remove. We need this
    in order to make sure that after removal, all the fingers point to valid
    nodes.
    ***************************************************************************/
    void moveFingerOutOfRemovalLocation(Finger<E> finger) {
        if (fingerStack.size == 1) {
            
            if (finger.index > 0) {
                fingerStack.fingerIndexSet.remove(finger.index);
                fingerStack.fingerIndexSet.add(--finger.index);
                finger.node = finger.node.prev;
            } else {
                finger.node = finger.node.next;
            }
            
            return;
        }
        
        fingerStack.fingerIndexSet.remove(finger.index);
        
        int leftProbeIndex = finger.index - 1;
        int rightProbeIndex = finger.index + 1;
        
        Node<E> leftProbeNode = finger.node.prev;
        Node<E> rightProbeNode = finger.node.next;
        
        while (true) {
            
            if (leftProbeIndex >= 0) {
                if (!fingerStack.fingerIndexSet.contains(leftProbeIndex)) {
                    finger.index = leftProbeIndex;
                    finger.node = leftProbeNode;
                    fingerStack.fingerIndexSet.add(finger.index);
                    return;
                }
                
                leftProbeIndex--;
                leftProbeNode = leftProbeNode.prev;
            }
            
            if (rightProbeIndex < size) {
                if (!fingerStack.fingerIndexSet.contains(rightProbeIndex)) {
                    finger.index = rightProbeIndex;
                    finger.node = rightProbeNode;
                    fingerStack.fingerIndexSet.add(finger.index);
                    return;
                }
                
                rightProbeIndex++;
                rightProbeNode = rightProbeNode.next;
            } else if (leftProbeIndex == -1) {
                throw new IllegalStateException(
                        "Throwing out of possible infinite loop.");
            }
        }
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
    ***************************************************************************/
    private boolean mustRemoveFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() + 1
        return fingerStack.size() != getRecommendedNumberOfFingers();
    }

    /***************************************************************************
    Returns the node at index 'index'. Moves the closest finger to the node.
    ***************************************************************************/
    protected Node<E> node(int index) {
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
        Node<E> oldFirst = first;
        first = new Node<>(iterator.next());

        Node<E> prevNode = first;

        for (int i = 1, sz = c.size(); i < sz; i++) {
            Node<E> newNode = new Node<>(iterator.next());
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
    
    /***************************************************************************
    Implements the node removal. 
    ***************************************************************************/
    private void removeObjectImpl(Node<E> node, int index) {
        // Make sure no finger is pointing to 'node':
        makeSureNoFingerPointsTo(node, index);
        unlink(node);
        decreaseSize();
        
        if (mustRemoveFinger()) {
            removeFinger();
        }
        
        shiftIndicesToLeftOnce(index + 1);
    }
    
    /***************************************************************************
    If steps &lt; 0, rewind to the left. Otherwise, rewind to the right.
    ***************************************************************************/
    private Node<E> rewind(Finger<E> finger, int steps) {
        Node<E> node = finger.node;
        
        if (steps > 0) {
            for (int i = 0; i < steps; i++) {
                node = node.prev;
            }
        } else {
            steps = -steps;
            
            for (int i = 0; i < steps; i++) {
                node = node.next;
            }
        }
        
        return node;
    }
    
    /***************************************************************************
    Sets the input collection as a list.
    ***************************************************************************/
    private void setAll(Collection<? extends E> c) {
        Iterator<? extends E> iterator = c.iterator();

        first = new Node<>(iterator.next());
        Node<E> prevNode = first;

        for (int i = 1, sz = c.size(); i < sz; i++) {
            Node<E> newNode = new Node<>(iterator.next());
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
        for (int i = 0, sz = fingerStack.size(); i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            
            if (finger.index >= startingIndex) {
                int nextIndex = finger.index - steps;
                finger.updateIndex = nextIndex;
                fingerStack.fingerIndexSet.remove(finger.index);
            }
        }
        
        for (int i = 0, sz = fingerStack.size(); i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            
            if (finger.index >= startingIndex) {
                finger.index = finger.updateIndex;
                fingerStack.fingerIndexSet.add(finger.index);
            }
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
            
            if (finger.index >= startIndex) {
                int nextIndex = finger.index + steps;
                finger.updateIndex = nextIndex;
                fingerStack.fingerIndexSet.remove(finger.index);
            }
        }
        
        for (int i = 0, sz = fingerStack.size(); i < sz; i++) {
            Finger<E> finger = fingerStack.get(i);
            
            if (finger.index >= startIndex) {
                finger.index = finger.updateIndex;
                fingerStack.fingerIndexSet.add(finger.index);
            }
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
    Unlinks the input node from the actual doubly-linked list.
    ***************************************************************************/
    private void unlink(Node<E> x) {
        Node<E> next = x.next;
        Node<E> prev = x.prev;

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
    
    /**
     * Reconstitutes this {@code LinkedList} instance from a stream (that is, 
     * deserializes it).
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
                Node<E> newNode = new Node<>((E) s.readObject());
                first = last = newNode;
                addFinger(newNode, 0);
                return;
        }
        
        Node<E> rightmostNode = new Node<>((E) s.readObject());
        first = rightmostNode;
        
        int numberOfRequestedFingers = getRecommendedNumberOfFingers(size);
        int distance = size / numberOfRequestedFingers;
        int startOffset = distance / 2;
        
        // Read in all elements in the proper order.
        for (int i = 1; i < size; i++) {
            Node<E> node = new Node<>((E) s.readObject());
            
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
     * Saves the state of this {@code LinkedList} instance to a stream (that is, 
     * serializes it).
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
        for (Node<E> x = first; x != null; x = x.next) {
            s.writeObject(x.item);
        }
    }
    
    /***************************************************************************
    Implements a simple, array-based stack for storing the node fingers for
    items of type 'E'.
    ***************************************************************************/
    static class FingerStack<E> {
        static final int INITIAL_CAPACITY = 8;

        // package private for unit testing
        final IntHashSet fingerIndexSet = new IntHashSet();
        Finger<E>[] fingerArray = new Finger[INITIAL_CAPACITY];
        int size = 0;

        void push(Finger<E> finger) {
            enlargeFingerArrayIfNeeded();
            fingerArray[size++] = finger;
            fingerIndexSet.add(finger.index);
        }

        void pop() {
            --size;
            contractFingerArrayIfNeeded();
            Finger<E> finger = fingerArray[size];
            fingerArray[size] = null;
            fingerIndexSet.remove(finger.index);
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
            fingerIndexSet.clear();
            fingerArray = new Finger[INITIAL_CAPACITY];
        }
        
        @Override
        public String toString() {
            return "size = " + size;
        }

        // Makes sure that the next finger fits in this finger stack:
        void enlargeFingerArrayIfNeeded() {
            if (size == fingerArray.length) {
                int nextCapacity = 2 * fingerArray.length;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
        
        // We can save some space while keeping the finger array operations 
        // amortized O(1):
        private void contractFingerArrayIfNeeded() {
            if (size * 4 <= fingerArray.length 
                    && fingerArray.length > INITIAL_CAPACITY) {
                int nextCapacity = fingerArray.length / 2;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
    }

    /***************************************************************************
    This class implements a basic iterator over this list.
    ***************************************************************************/
    final class BasicIterator implements Iterator<E> {

        private Node<E> lastReturned;
        private Node<E> next = first;
        private int nextIndex;
        int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public E next() {
            checkForComodification();
            
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        @Override
        public void remove() {
            checkForComodification();
            
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            
            int removalIndex = nextIndex - 1;
            removeObjectImpl(lastReturned, removalIndex);
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

        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    
    /***************************************************************************
    Implements the enhanced list iterator over this list.
    ***************************************************************************/
    final class EnhancedIterator implements ListIterator<E> {

        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        
        // Package-private for the sake of unit testing:
        int expectedModCount = modCount;
        
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
            
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
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
            
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            
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
            
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            
            Node<E> lastNext = lastReturned.next;
            int removalIndex = nextIndex - 1;
            removeObjectImpl(lastReturned, removalIndex);
            
            if (next == lastReturned) {
                next = lastNext;
            } else {
                nextIndex = removalIndex;
            }
            
            lastReturned = null;
            expectedModCount++;
        }

        @Override
        public void set(E e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            
            checkForComdification();
            lastReturned.item = e;
        }

        @Override
        public void add(E e) {
            checkForComdification();
            
            lastReturned = null;
            
            if (next == null) {
                linkLast(e);
            } else {
                linkBefore(e, next, nextIndex);
            }
            
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
        
        private void checkForComdification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
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
    
    static final class LinkedListSpliterator<E> implements Spliterator<E> {
        
        static final long MINIMUM_BATCH_SIZE = 1 << 10; // 1024 items
        
        private final LinkedList<E> list;
        private Node<E> node;
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
            if (action == null) {
                throw new NullPointerException();
            }
            
            if (numberOfProcessedElements == lengthOfSpliterator) {
                return false;
            }
            
            numberOfProcessedElements++;
            E item = node.item;
            action.accept(item);
            node = node.next;
            
            if (list.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
                
            return true;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            
            for (long i = numberOfProcessedElements; 
                 i < lengthOfSpliterator; 
                 i++) {
                E item = node.item;
                action.accept(item);
                node = node.next;
            }
            
            numberOfProcessedElements = lengthOfSpliterator;
            
            if (list.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
        
        @Override
        public Spliterator<E> trySplit() {
            long sizeLeft = estimateSize();
            
            if (sizeLeft == 0) {
                return null;
            }
                
            long thisSpliteratorNewLength = sizeLeft / 2L;
            
            if (thisSpliteratorNewLength < MINIMUM_BATCH_SIZE) {
                return null;
            }
            
            long newSpliteratorLength = sizeLeft - thisSpliteratorNewLength;
            long newSpliteratorOffset = this.offsetOfSpliterator;
            
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
            return switch (characteristics) {
                case Spliterator.ORDERED, 
                     Spliterator.SIZED, 
                     Spliterator.SUBSIZED -> true;
                    
                default -> false;
            };
        }
    }
}
