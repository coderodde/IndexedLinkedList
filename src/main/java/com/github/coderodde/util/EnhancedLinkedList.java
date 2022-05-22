package com.github.coderodde.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.61 (Sep 26, 2021)
 * @since 1.6 (Sep 1, 2021)
 */
public class EnhancedLinkedList<E> 
        implements Deque<E>, List<E>, Cloneable, java.io.Serializable {
    
    class FingerList<E> {

        private static final int INITIAL_CAPACITY = 8;

        // The actual list storage array:
        Finger<E>[] fingerArray = new Finger[INITIAL_CAPACITY];

        // The number of fingers stored in the list. This field does not count
        // the end-of-list sentinel finger 'F' for which 'F.index = size'.
        private int size;
        
        @Override
        public String toString() {
            return "[FingerList, size = " + size + "]";
        }

        private FingerList() {
            fingerArray[0] = new Finger<>(null, 0);
        }

        Finger<E> get(int index) {
            return fingerArray[index];
        }

        int size() {
            return size;
        }

        private int getFingerIndexImpl(int elementIndex) {
            int count = size + 1; // + 1 for the end sentinel.
            int it;
            int idx = 0;

            while (count > 0) {
                it = idx;
                int step = count / 2;
                it += step;

                if (fingerArray[it].index < elementIndex) {
                    idx = ++it;
                    count -= step + 1;
                } else {
                    count = step;
                }
            }
            
            return idx;
        }
        
        int getFingerIndex(int elementIndex) {
            return normalize(getFingerIndexImpl(elementIndex), elementIndex);
        }
        
        int getNextFingerIndex(int elementIndex) {
            return getFingerIndexImpl(elementIndex);
        }
        
        void setFinger(int index, Finger<E> finger) {
            fingerArray[index] = finger;
        }
        
        void makeRoomAtIndex(int fingerIndex, int roomSize, int numberOfNodes) {
            shiftFingerIndicesToRight(fingerIndex, numberOfNodes);
            size += roomSize;
            enlargeFingerArrayIfNeeded(size + 1); // +1 for the end of list
                                                  // sentinel.
            System.arraycopy(fingerArray, 
                             fingerIndex, 
                             fingerArray, 
                             fingerIndex + roomSize,
                             size - roomSize - fingerIndex + 1);
        }
        
        private int normalize(int fingerIndex, int elementIndex) {
            if (fingerIndex == 0) {
                return 0;
            }
            
            if (fingerIndex == size) {
                return size - 1;
            }
            
            Finger finger1 = fingerArray[fingerIndex - 1];
            Finger finger2 = fingerArray[fingerIndex];
            
            int distance1 = Math.abs(elementIndex - finger1.index);
            int distance2 = Math.abs(elementIndex - finger2.index);
            return distance1 < distance2 ? fingerIndex - 1 : fingerIndex;
        }

        Node<E> node(int index) {
            Finger finger = fingerArray[getFingerIndex(index)];
            int steps = finger.index - index;

            if (steps > 0) {
                finger.rewindLeft(steps);
            } else {
                finger.rewindRight(-steps);
            }

            return finger.node;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Removes the last ´numberOfFingers´ fingers from this finger list. 
        ////////////////////////////////////////////////////////////////////////
        void removeTrailingFingers(int numberOfFingers) {
            Arrays.fill(fingerArray, size - numberOfFingers, size, null);
            fingerArray[size - numberOfFingers] = fingerArray[size];
            fingerArray[size] = null;
            size -= numberOfFingers;
            // TODO: Remove?
            fingerArray[size].index = EnhancedLinkedList.this.size;
            contractFingerArrayIfNeeded(size);
        }
        
        // Appends the input finger to the tail of the finger list:
        void appendFinger(com.github.coderodde.util.Finger<E> finger) {
            size++;
            enlargeFingerArrayIfNeeded(size + 1);
            fingerArray[size] = fingerArray[size - 1];
            fingerArray[size - 1] = finger;
            fingerArray[size].index = EnhancedLinkedList.this.size;
        }

        // Inserts the input finger into the finger list such that the entire
        // finger list is sorted by indices:
        void insertFingerAndShiftOnceToRight(Finger<E> finger) {
            enlargeFingerArrayIfNeeded(size + 2);
            int beforeFingerIndex = getFingerIndex(finger.index);
            System.arraycopy(
                    fingerArray, 
                    beforeFingerIndex, 
                    fingerArray, 
                    beforeFingerIndex + 1, 
                    size + 1 - beforeFingerIndex);
            
            // Shift fingerArray[beforeFingerIndex ... size] one position to the
            // right (towards larger index values:
            shiftFingerIndicesToRightOnce(beforeFingerIndex + 1);

            fingerArray[beforeFingerIndex] = finger;
            fingerArray[++size].index = EnhancedLinkedList.this.size;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Removes the finger range [startFingerIndex, endFingerIndex).
        ////////////////////////////////////////////////////////////////////////
        void removeRange(int prefixSize, 
                         int suffixSize,
                         int nodesToRemove) {
            int fingersToRemove = size - suffixSize - prefixSize;
            
            shiftFingerIndicesToLeft(size - suffixSize, nodesToRemove);
            
            System.arraycopy(fingerArray, 
                             size - suffixSize, 
                             fingerArray, 
                             prefixSize,
                             suffixSize + 1);
            
            size -= fingersToRemove;
            contractFingerArrayIfNeeded(size);
            
            Arrays.fill(fingerArray,
                        size + 1,
                        size + 1 + fingersToRemove,
                        null);
        }
        
        private void shiftFingerIndicesToLeft(int startIndex,      
                                              int shiftLength) {
            for (int i = startIndex; i <= size; ++i) {
                fingerArray[i].index -= shiftLength;
            }
        }
        
        /***********************************************************************
        For each finger with the index at least 'startIndex', add 'steps' to the 
        index. This method updates the index of the end-of-list sentinel too.
        ***********************************************************************/
        private void shiftFingerIndicesToRight(int startIndex,      
                                               int shiftLength) {
            for (int i = startIndex; i <= size; ++i) {
                fingerArray[i].index += shiftLength;
            }
        }
    
        /***********************************************************************
        For each finger with the index at least 'startIndex', incremnt the index
        by one. This method updates the index of the end-of-list sentinel too.
        ***********************************************************************/
        private void shiftFingerIndicesToRightOnce(int startIndex) {
            for (int i = startIndex; i <= size; ++i) {
                fingerArray[i].index++;
            }
        }
        
        private void moveFingersToPrefixOnEmptyPrefix(int fromIndex,
                                                      int numberOfFingers) {
            System.out.println("moveFingersToPrefixOnEmptyPrefix");
            Finger<E> firstFinger = fingerArray[0];
            int toMove = firstFinger.index - fromIndex + numberOfFingers;

            for (int i = 0; i < toMove; ++i) {
                firstFinger.node = firstFinger.node.prev;
            }

            firstFinger.index -= toMove;

            for (int i = 1; i < numberOfFingers; ++i) {
                Finger<E> previousFinger = fingerArray[i - 1];
                Finger<E> currentFinger = fingerArray[i];
                currentFinger.node = previousFinger.node.next;
                currentFinger.index = previousFinger.index + 1;
            }
        }
        
        private void moveFingersToSuffixOnEmptySuffix(int toIndex,
                                                      int numberOfFingers) {
            int toMove = toIndex 
                       - fingerArray[size - 1].index 
                       + numberOfFingers - 1;

            Finger<E> finger = fingerArray[size - 1];

            for (int i = 0; i < toMove; ++i) {
                finger.node = finger.node.next;
            }   

            finger.index += toMove;

            for (int i = 1; i < numberOfFingers; ++i) {
                Finger<E> predecessorFinger = fingerArray[size - i - 1];
                Finger<E> currentFinger = fingerArray[size - i];
                predecessorFinger.index = currentFinger.index - 1;
                predecessorFinger.node = currentFinger.node.prev;
            }
        }
        
        void moveFingersToPrefix(int fromIndex, 
                                 int numberOfFingers) {
            
            if (numberOfFingers == 0) {
                System.out.println("Prefix.0");
                return;
            }
            
            int fromFingerIndex = getFingerIndex(fromIndex);
            
            if (fromFingerIndex == 0) {
                System.out.println("Prefix.1");
                moveFingersToPrefixOnEmptyPrefix(
                        fromIndex, 
                        numberOfFingers);
                
                return;
            }
            
            int i;
            Finger<E> targetFinger = null;
            
            for (i = fromFingerIndex - 1; i >= 0; --i) {
                Finger<E> finger = fingerArray[i];
                
                if (finger.index + numberOfFingers - 1 + i < fromIndex) {
                    targetFinger = finger;
                    break;
                }
            }
            
            if (targetFinger == null) {
                System.out.println("Prefix.2");
                
                int toMove = fingerArray[0].index + numberOfFingers - fromIndex;
                Finger<E> finger = fingerArray[0];
                
                for (int j = 0; j < toMove; ++j) {
                    finger.node = finger.node.prev;
                }
                
                finger.index -= toMove;
            }
            
            for (int j = 1; j <= numberOfFingers; ++j) {
                Finger<E> predecessorFinger = fingerArray[j - 1];
                Finger<E> currentFinger = fingerArray[j];
                currentFinger.index = predecessorFinger.index + 1;
                currentFinger.node = predecessorFinger.node.next;
            }
        }
        
        void moveFingersToSuffix(int toIndex, 
                                 int numberOfFingers,
                                 int removalSize) {
            if (numberOfFingers == 0) {
                System.out.println("Suffix.0");
                return;
            }
            
            int toFingerIndex = getFingerIndexImpl(toIndex);
            
            if (toFingerIndex == fingerList.size) {
                System.out.println("Suffix.1");
                moveFingersToSuffixOnEmptySuffix(toIndex, numberOfFingers);
                return;
            }
            
            int i;
            Finger<E> targetFinger = null;
            
            for (i = toFingerIndex; i < size; ++i) {
                Finger<E> finger = fingerArray[i];
                
                if (finger.index - numberOfFingers + 1 >= toIndex) {
                    targetFinger = finger;
                    break;
                }
            }
            
            if (targetFinger == null) {
                System.out.println("Suffix.2");
                Finger<E> f = fingerArray[size - 1];
                int toMove = toIndex + numberOfFingers - 1 - f.index;
                
                for (int j = 0; j < toMove; ++j) {
                    f.node = f.node.next;
                }
                
                f.index += toMove;
                i = size - 1;
            }

            int stopIndex = numberOfFingers - (size - i);
            
            for (int j = i - 1, k = 0; k < stopIndex; ++k, --j) {
                Finger<E> predecessorFinger = fingerArray[j];
                Finger<E> currentFinger = fingerArray[j + 1];
                predecessorFinger.index = currentFinger.index - 1;
                predecessorFinger.node = currentFinger.node.prev;
            }
        }

        void removeFinger() {
            contractFingerArrayIfNeeded(--size);
            fingerArray[size] = fingerArray[size + 1];
            fingerArray[size + 1] = null;
            fingerArray[size].index = EnhancedLinkedList.this.size;
        }

        void clear() {
            Arrays.fill(fingerArray, 0, size, null);
            fingerArray[0] = fingerArray[size];
            fingerArray[0].index = 0;

            if (size != 0) {
                fingerArray[size] = null;
                size = 0;
            }
        }

        // Makes sure that the next finger fits in this finger stack:
        private void enlargeFingerArrayIfNeeded(int requestedSize) {
            // If the finger array is full, double the capacity:
            if (requestedSize > fingerArray.length) {
                int nextCapacity = 2 * fingerArray.length;
                
                while (nextCapacity < size + 1) {
                    nextCapacity *= 2;
                }
                
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }

        // We can save some space while keeping the finger array operations 
        // amortized O(1). The 'nextSize' defines the requested finger array 
        // size not counting the end-of-finger-list sentinel finger:
        private void contractFingerArrayIfNeeded(int nextSize) {
            if ((nextSize + 1) * 4 < fingerArray.length 
                    && fingerArray.length > 2 * INITIAL_CAPACITY) {
                int nextCapacity = fingerArray.length / 4;
                
                while (nextCapacity >= 2 * (nextSize + 1)
                        && nextCapacity > INITIAL_CAPACITY) {
                    nextCapacity /= 2;
                }
                
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
        
        private void adjustOnRemoveFirst() {
            int lastPrefixIndex = Integer.MAX_VALUE;
            
            for (int i = 0; i < size; ++i) {
                Finger<E> finger = fingerArray[i];
                
                if (finger.index != i) {
                    lastPrefixIndex = i;
                    break;
                } else {
                    finger.node = finger.node.next;
                }
            }
            
            for (int i = lastPrefixIndex; i <= size; ++i) {
                fingerArray[i].index--;
            }
        }
    }
    
    /**
     * The cached number of elements in this list.
     */
    private int size;
    private int modCount;
    private transient Node<E> first;
    private transient Node<E> last;
    
    // Without 'private' since it is accessed in unit tests.
    transient FingerList<E> fingerList = new FingerList<>();
    
    /**
     * Constructs an empty list.
     */
    public EnhancedLinkedList() {
        
    }
    
    /**
     * Constructs a new list and copies the data in {@code c} to it.
     * 
     * @param c the collection to copy. 
     */
    public EnhancedLinkedList(Collection<? extends E> c) {
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
     * {@inheritDoc }
     */
    public void addFirst(E e) {
        linkFirst(e);
    }
    
    /**
     * {@inheritDoc }
     */
    public void addLast(E e) {
        linkLast(e);
    }
    
    /**
     * Checks the data structure invariant. Throws 
     * {@link java.lang.IllegalStateException} on invalid invariant. The 
     * invariant is valid if:
     * <ol>
     * <li>All the fingers in the finger list are sorted by indices.</li>
     * <li>There is no duplicate indices.</li>
     * <li>The index of the leftmost finger is no less than zero.</li>
     * <li>There must be an end-of-list sentinel finger {@code F}, such that 
     * {@code F.index = } <i>size of linked list</i> and {@code F.node} is 
     * {@code null}.
     * </li>
     * <li>Each finger {@code F} points to the {@code i}th linked list node,
     * where {@code i = F.index}</li>
     * </ol>
     */
    public void checkInvarant() {
        for (int i = 0; i < fingerList.size() - 1; ++i) {
            Finger<E> left = fingerList.get(i);
            Finger<E> right = fingerList.get(i + 1);
            
            if (left.index >= right.index) {
                throw new IllegalStateException(
                        "FingerList failed: fingerList[" 
                                + i
                                + "].index = " 
                                + left.index 
                                + " >= " 
                                + right.index 
                                + " = fingerList[" 
                                + (i + 1) 
                                + "]");
            }
        }
        
        Finger<E> sentinelFinger = fingerList.get(fingerList.size());
        
        if (sentinelFinger.index != this.size || sentinelFinger.node != null) {
            throw new IllegalStateException(
                    "Invalid end-of-list sentinel: " + sentinelFinger);
        }
        
        Finger<E> finger = fingerList.get(0);
        Node<E> node = first;
        int fingerCount = 0;
        int tentativeSize = 0;
        
        while (node != null) {
            tentativeSize++;
            
            if (finger.node == node) {
                finger = fingerList.get(++fingerCount);
            }
            
            node = node.next;
        }
        
        if (size != tentativeSize) {
            throw new IllegalStateException(
                    "Number of nodes mismatch: size = " 
                            + size 
                            + ", tentativeSiz = " 
                            + tentativeSize);
        }
        
        if (fingerList.size() != fingerCount) {
            throw new IllegalStateException(
                    "Number of fingers mismatch: fingerList.size() = " 
                            + fingerList.size() 
                            + ", fingerIndex = " 
                            + fingerCount);
        }
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void clear() {
        fingerList.clear();
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
    
    @Override
    public Object clone() {
        EnhancedLinkedList<E> list = new EnhancedLinkedList<>(this);
        return list;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public E element() {
        return getFirst();
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        
        if (!(o instanceof List)) {
            return false;
        }
        
        int expectedModCount = modCount;
        
        List<?> otherList = (List<?>) o;
        boolean equal = equalsRange(otherList, 0, size);
        
        checkForComodification(expectedModCount);
        return equal;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public E getFirst() {
        if (first == null) {
            throw new NoSuchElementException(
                    "Getting the head element from an empty list.");
        }
        
        return first.item;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public E getLast() {
        if (last == null) {
            throw new NoSuchElementException(
                    "Getting the tail element from an empty list.");
        }
        
        return last.item;
    }
    
    public int hashCode() {
        int expectedModCount = modCount;
        int hash = hashCodeRange(0, size);
        checkForComodification(expectedModCount);
        return hash;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public int indexOf(Object obj) {
        return indexOfRange(obj, 0, size);
    }
    
    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Iterator<E> iterator() {
        return new BasicIterator();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int lastIndexOf(Object obj) {
        return lastIndexOfRange(obj, 0, size);
    }
    
    @Override
    public ListIterator<E> listIterator() {
        return new EnhancedIterator(0);
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return new EnhancedIterator(index);
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }
    
    /**
     * Moves all the fingers such that they are evenly distributed.
     */
    public void optimize() {
        if (size == 0) {
            return;
        }
        
        int nodesPerFinger = size / fingerList.size();
        int startOffset = nodesPerFinger / 2;
        int index = startOffset;
        Node<E> node = first;
        
        for (int i = 0; i < startOffset; ++i) {
            node = node.next;
        }
        
        for (int i = 0; i < fingerList.size() - 1; ++i) {
            Finger<E> finger = fingerList.get(i);
            finger.node = node;
            finger.index = index;
            
            for (int j = 0; j < nodesPerFinger; ++j) {
                node = node.next;
            }
            
            index += nodesPerFinger;
        }
        
        Finger<E> lastFinger = fingerList.get(fingerList.size() - 1);
        lastFinger.node = node;
        lastFinger.index = index;
        fingerList.get(fingerList.size()).index = size;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public E peek() {
        return first == null ? null : first.item;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E peekFirst() {
        return first == null ? null : first.item;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E peekLast() {
        return last == null ? null : last.item;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public E poll() {
        return first == null ? null : removeFirst();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E pollFirst() {
        return first == null ? null : removeFirst();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E pollLast() {
        return last == null ? null : removeLast();
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public E pop() {
        return removeFirst();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E remove() {
        if (size == 0) {
            throw new NoSuchElementException("remove() from empty LinkedList.");
        }
        
        return removeFirst();
    }

    /**
     * {@inheritDoc }
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
        
        int closestFingerIndex = fingerList.getFingerIndex(index);
        Finger<E> closestFinger = fingerList.get(closestFingerIndex);
        
        E returnValue;
        Node<E> nodeToRemove;
        
        if (closestFinger.index == index) {
            nodeToRemove = closestFinger.node;
            moveFingerOutOfRemovalLocation(closestFinger, 
                                           closestFingerIndex);    
        } else {
            // Keep the fingers at their original position.
            // Find the target node:
            int steps = closestFinger.index - index;
            
            nodeToRemove =
                    traverseLinkedListBackwards(
                            closestFinger,
                            steps);
            
            for (int i = closestFingerIndex + 1;
                    i <= fingerList.size(); 
                    i++) {
                fingerList.get(i).index--;
            }
            
            if (steps > 0) {
                fingerList.get(closestFingerIndex).index--;
            }
        }
        
        returnValue = nodeToRemove.item;
        unlink(nodeToRemove);
        decreaseSize();

        if (mustRemoveFinger()) {
            removeFinger();
        }

        return returnValue;
    }
    
    public boolean removeAll(Collection<?> c) {
        return batchRemove(c, true, 0, size);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException(
                    "removeFirst from an empty LinkedList");
        }
        
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
        
        fingerList.adjustOnRemoveFirst();
        return returnValue;
    }
    
    /**
     * {@inheritDoc }
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
     * {@inheritDoc }
     */
    @Override
    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("removeLast on empty LinkedList");
        }
        
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
     * {@inheritDoc }
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

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        replaceAllRange(operator, 0, size);
        modCount++;
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return batchRemove(c, false, 0, size);
    }
    
    /**
     * Sets the element at index {@code index} to {@code element} and returns
     * the old element.
     * 
     * @param index   the target index.
     * @param element the element to set.
     * @return the previous element at the given index.
     */
    @Override
    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> node = node(index);
        E oldElement = node.item;
        node.item = element;
        return oldElement;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public int size() {
        return size;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LinkedListSpliterator<>(this, first, size, 0, modCount);
    }
    
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new EnhancedSubList(this, fromIndex, toIndex);
    }
    
    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int index = 0;
        
        for (Node<E> node = first; node != null; node = node.next) {
            arr[index++] = node.item;
        }
        
        return arr;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        
        int index = 0;
        
        for (Node<E> node = first; node != null; node = node.next) {
            a[index++] = (T) node.item;
        }
        
        if (a.length > size) {
            a[size] = null;
        }
        
        return a;
    }
    
    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }

        if (toIndex > size){
            throw new IndexOutOfBoundsException(
                    "toIndex(" + toIndex + ") > size(" + size + ")");
        }

        if (fromIndex > toIndex)
            throw new IllegalArgumentException(
                    "fromIndex(" + fromIndex + ") > toIndex(" 
                            + toIndex + ")");
    }
    
    @java.io.Serial
    private static final long serialVersionUID = -1L;
    
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
    This class implements a basic iterator over this list.
    ***************************************************************************/
    public final class BasicIterator implements Iterator<E> {

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
    
    boolean batchRemove(Collection<?> c,
                        boolean complement,
                        int from,
                        int end) {
        Objects.requireNonNull(c);
        
        if (c.isEmpty()) {
            return false;
        }
        
        boolean modified = false;
        
        for (Node<E> node = node(from); from < end; from++) {
            if (c.contains(node.item) == complement) {
                modified = true;
                Node<E> nextNode = node.next;
                this.removeNode(node);
                node = nextNode;
            }
        }
        
        return modified;
    }
    
    private void checkForComodification(int expectedModCount) {
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
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
    Checks that the input index is a valid position index for add operation or
    iterator position. In other words, checks that {@code index} is in the set
    '{ 0, 1, ..., size}'.
    ***************************************************************************/
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index)) {
            throw new IndexOutOfBoundsException(getOutOfBoundsMessage(index));
        }
    }
    
    
    private void decreaseSize() {
        size--;
        modCount++;
    }   
    
    /***************************************************************************
    Implements the descending list iterator over this list.                    
    ***************************************************************************/
    private final class DescendingIterator implements Iterator<E> {

        private Node<E> lastReturned;
        private Node<E> nextToIterate = last;
        private int nextIndex = EnhancedLinkedList.this.size - 1;
        int expectedModCount = EnhancedLinkedList.this.modCount;
        
        @Override
        public boolean hasNext() {
            return nextIndex > -1;
        }
        
        @Override
        public E next() {
            checkForComodification();
            
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            lastReturned = nextToIterate;
            nextToIterate = nextToIterate.prev;
            nextIndex--;
            return lastReturned.item;
        }
        
        @Override
        public void remove() {
            checkForComodification();
            
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            
            removeObjectImpl(lastReturned, nextIndex + 1);
//            nextInde
            lastReturned = null;
            expectedModCount++;
        }
        
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            
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
    
    boolean equalsRange(List<?> other, int from, int to) {
        Iterator<?> otherIterator = other.iterator();
        
        for (Node<E> node = node(from); from < to; from++, node = node.next) {
            if (!otherIterator.hasNext() || 
                    !Objects.equals(node.item, otherIterator.next())) {
                return false;
            }
        }
        
        return true;
    }

    int hashCodeRange(int from, int to) {
        int hashCode = 1;
        
        Node<E> node = node(from);
        
        while (from++ < to) {
            hashCode =
                    31 * hashCode + 
                    (node.item == null ? 0 : node.item.hashCode());
            
            node = node.next;
        }
        
        return hashCode;
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
        return (int) Math.ceil(Math.sqrt(size));
    }
    
    /***************************************************************************
    Computes the recommended number of fingers for 'size' elements.
    ***************************************************************************/
    private static int getRecommendedNumberOfFingers(int size) {
        return (int) Math.ceil(Math.sqrt(size));
    }
    
    /***************************************************************************
    Increases the size of the list and its modification count.
    ***************************************************************************/
    private void increaseSize() {
        ++size;
        ++modCount;
    }
    
    int indexOfRange(Object o, int start, int end) {
        int index = start;
        
        if (o == null) {
            for (Node<E> node = node(start);
                    index < end; 
                    index++, node = node.next) {
                if (node.item == null) {
                    return index;
                }
            }
        } else {
            for (Node<E> node = node(start);
                    index < end;
                    index++, node = node.next) {
                if (o.equals(node.item)) {
                    return index;
                }
            }
        }
        
        return -1;
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
    
    ////////////////////////////////////////////////////////////////////////////
    // Returns the last appearance index of 'obj'.
    ////////////////////////////////////////////////////////////////////////////
    int lastIndexOfRange(Object o, int start, int end) {
        int index = end - 1;
        
        if (o == null) {
            for (Node<E> node = node(index);
                    index >= start; 
                    index--, node = node.prev) {
                if (node.item == null) {
                    return index;
                }
            }
        } else {
            for (Node<E> node = node(index);
                    index >= start;
                    index--, node = node.prev) {
                if (o.equals(node.item)) {
                    return index;
                }
            }
        }
        
        return -1;
    }
    
    /***************************************************************************
    Links the input element right before the node 'succ'.
    ***************************************************************************/
    private void linkBefore(E e, Node<E> succ, int index) {
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
            fingerList.insertFingerAndShiftOnceToRight(
                    new Finger<>(newNode, index));
        } else {
            int fingerIndex = fingerList.getFingerIndex(index);
            fingerList.shiftFingerIndicesToRightOnce(fingerIndex);
        }
    }
    
    /***************************************************************************
    Prepends the input element to the head of this list.
    ***************************************************************************/
    private void linkFirst(E e) {
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
            fingerList.insertFingerAndShiftOnceToRight(
                    new Finger<>(newNode, 0));
        } else {
            fingerList.shiftFingerIndicesToRightOnce(0);
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
            appendFinger(newNode, size - 1);
        } else {
            fingerList.get(fingerList.size()).index++;
        }
    }
    
    /***************************************************************************
    Inserts the input collection right before the node 'succ'.
    ***************************************************************************/
    private void insertAll(
            Collection<? extends E> c,
            Node<E> succ,
            int succIndex) {
        int fingerIndex = fingerList.getNextFingerIndex(succIndex);
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
        
        // Add fingers:
        addFingersAfterInsertAll(pred.next, 
                                 succIndex,
                                 sz);
    }
    
    /***************************************************************************
    Returns a finger that does not point to the element to remove. We need this
    in order to make sure that after removal, all the fingers point to valid
    nodes.
    ***************************************************************************/
    void moveFingerOutOfRemovalLocation(Finger<E> finger, int fingerIndex) {
        if (fingerList.size() == size()) {
            // Here, fingerList.size() is 1 or 2 and the size of the list is the
            // same:
            if (fingerList.size() == 1) {
                // The only finger will be removed in 'remove(int)'. Return:
                return;
            }
            
            // Once here, 'fingerList.size() == 2'!
            switch (fingerIndex) {
                case 0:
                    // Shift 2nd and the sentinal fingers one position to the
                    // left:
                    fingerList.setFinger(0, fingerList.get(1));
                    fingerList.get(0).index = 0;
                    fingerList.setFinger(1, fingerList.get(2));
                    fingerList.get(1).index = 1;
                    fingerList.setFinger(2, null);
                    fingerList.size = 1;
                    break;
                    
                case 1:
                    // Just remove the (last) finger:
                    fingerList.removeFinger();
                    fingerList.get(1).index = 1;
                    break;
            }
            
            return;
        }
        
        // Try push the fingers to the right:
        for (int f = fingerIndex; f < fingerList.size(); ++f) {
            Finger<E> fingerLeft  = fingerList.get(f);
            Finger<E> fingerRight = fingerList.get(f + 1);
            
            if (fingerLeft.index + 1 < fingerRight.index) {
                for (int i = f; i >= fingerIndex; --i) {
                    Finger<E> fngr = fingerList.get(i);
                    fngr.node = fngr.node.next;
                }
                
                for (int j = f + 1; j <= fingerList.size(); ++j) {
                    fingerList.get(j).index--;
                }
                
                return;
            }
        }
        
        // Could not push the fingers to the right. Push to the left. Since the
        // number of fingers here is smaller than the list size, there must be
        // a spot to move to some fingers:
        for (int f = fingerIndex; f > 0; --f) {
            Finger<E> fingerLeft  = fingerList.get(f - 1);
            Finger<E> fingerRight = fingerList.get(f);
            
            if (fingerLeft.index + 1 < fingerRight.index) {
                for (int i = fingerIndex; i > 0; --i) {
                    Finger<E> fngr = fingerList.get(i);
                    fngr.node = fngr.node.prev;
                    fngr.index--;
                }
                
                for (int i = fingerIndex + 1; i <= fingerList.size(); ++i) {
                    fingerList.get(i).index--;
                }
                
                return;
            }
        }
        
        // Once here, the only free spots are at the very beginning of the
        // finger list:
        for (int i = 0; i < fingerList.size(); ++i) {
            Finger<E> fngr = fingerList.get(i);
            fngr.index--;
            fngr.node = fngr.node.prev;
        }
        
        // The end-of-finger-list node has no Finger<E>.node defined. Take it 
        // outside of the above loop and decrement its index manually:cd 
        fingerList.get(fingerList.size()).index--;
    }
    
    /***************************************************************************
    Returns true only if this list requires more fingers.
    ***************************************************************************/
    private boolean mustAddFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() - 1
        return fingerList.size() != getRecommendedNumberOfFingers();
    }
    
    /***************************************************************************
    Returns true only if this list requires less fingers.
    ***************************************************************************/
    private boolean mustRemoveFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() + 1
        return fingerList.size() != getRecommendedNumberOfFingers();
    }
    
    private Node<E> node(int elementIndex) {
         return fingerList.node(elementIndex);
    }
    
    /**
     * Reconstitutes this {@code LinkedList} instance from a stream (that is, 
     * deserializes it).
     * 
     * @param s the object input stream.
     * 
     * @serialData first, the size of the list is read. Then all the node items
     *             are read and stored in the deserialization order, that is the
     *             same order as in serialization.
     * 
     * @throws java.io.IOException if I/O fails.
     * @throws ClassNotFoundException if the class is not found.
     */
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s) 
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        int size = s.readInt();
        this.size = size;
        this.fingerList = new FingerList<>();

        switch (size) {
            case 0:
                return;
                
            case 1:
                Node<E> newNode = new Node<>((E) s.readObject());
                first = last = newNode;
                fingerList.appendFinger(new Finger<>(newNode, 0));
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
                fingerList.appendFinger(new Finger<>(node, i));
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
     * @param s the object output stream.
     *
     * @serialData The size of the list (the number of elements it
     *             contains) is emitted (int), followed by all of its
     *             elements (each an Object) in the proper order.
     * 
     * @throws java.io.IOException if the I/O fails.
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
    
    private void removeFinger() {
        fingerList.removeFinger();
    }
    
    private void removeNode(Node<E> node) {
        
    }
    
    /***************************************************************************
    Implements the node removal. 
    ***************************************************************************/
    private void removeObjectImpl(Node<E> node, int index) {
        int closestFingerIndex = fingerList.getFingerIndex(index);
        Finger<E> closestFinger = fingerList.get(closestFingerIndex);
        
        if (closestFinger.index == index) {
            // Make sure no finger is pointing to 'node':
            moveFingerOutOfRemovalLocation(closestFinger, closestFingerIndex);
        } else {
            for (int i = closestFingerIndex + 1;
                    i <= fingerList.size();
                    i++) {
                fingerList.get(i).index--;
            }
            
            int steps = closestFinger.index - index;
            
            if (steps > 0) {
                fingerList.get(closestFingerIndex).index--;
            }
        }
        
        unlink(node);
        decreaseSize();
        
        if (mustRemoveFinger()) {
            removeFinger();
        }
    }
    
    private void removeFirstNode() {
        first = first.next;
        first.prev = null;
        fingerList.fingerArray[0].node = first;
        fingerList.fingerArray[1].node = null;
        fingerList.fingerArray[1].index = 1;
        fingerList.fingerArray[2] = null;
        fingerList.size--;
        decreaseSize();
    }
    
    private void removeSecondNode() {
        last = last.prev;
        last.next = null;
        fingerList.fingerArray[1] = fingerList.fingerArray[2];
        fingerList.fingerArray[2] = null;
        fingerList.fingerArray[1].index = 1;
        fingerList.size--;
        decreaseSize();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Removes a range from this list.
    ////////////////////////////////////////////////////////////////////////////
    protected void removeRange(int fromIndex, int toIndex) {
        int removalSize = toIndex - fromIndex;
        
        if (removalSize == 0) {
            return;
        }
        
        if (removalSize == size) {
            clear();
            return;
        }
        
        if (size == 2 && removalSize == 1) {
            switch (fromIndex) {
                case 0:
                    removeFirstNode();
                    break;
                    
                case 1:
                    removeSecondNode();
                    break;
            }
            
            return;
        }
        
        Node<E> firstNodeToRemove = node(fromIndex);
        
        int nextFingerCount = getRecommendedNumberOfFingers(size - removalSize);
        int prefixSize = fromIndex;
        int suffixSize = size - toIndex;
        int prefixFingersSize = fingerList.getFingerIndexImpl(fromIndex);
        int suffixFingersSize = fingerList.size - 
                                fingerList.getFingerIndexImpl(toIndex);
        
        int prefixFreeSpotCount = prefixSize - prefixFingersSize;
        int suffixFreeSpotCount = suffixSize - suffixFingersSize;

        if (prefixFreeSpotCount == 0) {
            if (suffixFreeSpotCount == 0) {
                removeRangeNoPrefixNoSuffix(firstNodeToRemove,
                                            fromIndex, 
                                            removalSize);
            } else {
                int numberOfFingersToMove = nextFingerCount - suffixFingersSize;
                
                // Once here, prefixFreeSpotCount = 0 and 
                // suffixFreeSpotCount > 0. In other words, we are moving to 
                // suffix.
                fingerList.moveFingersToSuffix(toIndex,                               
                                               numberOfFingersToMove,
                                               removalSize);
                
                fingerList.removeRange(0, suffixFingersSize, removalSize);
                removeRangeNodes(firstNodeToRemove, removalSize);
            }
        } else {
            if (suffixFreeSpotCount == 0) {
                int numberOfFingersToMove = nextFingerCount - prefixFingersSize;
                
                // Once here, suffixFreeSpotCount = 0 and 
                // prefixFreeSpotCount > 0. In other words, we are moving a to
                // prefix.
                System.out.println("--------------");
                System.out.println("numberOfFingersToMove: " + numberOfFingersToMove);
                System.out.println("nextFingerCount:       " + nextFingerCount);
                System.out.println("prefixFingersSize:     " + prefixFingersSize);
                System.out.println("prefixFreeSpotCount:   " + prefixFreeSpotCount);
                System.out.println("fromIndex:             " + fromIndex);
                System.out.println("toIndex:               " + toIndex);
                System.out.println("size:                  " + size);
                System.out.println("--------------");
                
                fingerList.moveFingersToPrefix(
                        fromIndex,
                        numberOfFingersToMove);
                
                fingerList.removeRange(numberOfFingersToMove, 0, removalSize);
                removeRangeNodes(firstNodeToRemove, removalSize);
            } else {
                int prefixSuffixFreeSpotCount = prefixFreeSpotCount 
                                              + suffixFreeSpotCount;

                float prefixLoadFactor = ((float)(prefixFreeSpotCount)) /
                                         ((float)(prefixSuffixFreeSpotCount));

                int numberOfFingersOnLeft = 
                        (int)(prefixLoadFactor * nextFingerCount);

                int numberOfFingersOnRight = 
                        nextFingerCount - numberOfFingersOnLeft;

                fingerList.moveFingersToPrefix(fromIndex, 
                                               numberOfFingersOnLeft);
                
                fingerList.moveFingersToSuffix(toIndex, 
                                               numberOfFingersOnRight, 
                                               removalSize);

                fingerList.removeRange(numberOfFingersOnLeft,
                                       numberOfFingersOnRight, 
                                       removalSize);
                
                removeRangeNodes(firstNodeToRemove, removalSize);
            }
        }
        
        modCount++;
        size -= removalSize;
    }
    
    void removeRangeNoPrefixNoSuffix(Node<E> node,
                                     int fromIndex, 
                                     int removalSize) {

        int nextListSize = EnhancedLinkedList.this.size - removalSize;
        int nextFingerListSize =
                getRecommendedNumberOfFingers(nextListSize);

        int fingersToRemove = fingerList.size() - nextFingerListSize;
        int firstFingerIndex = fingerList.getFingerIndexImpl(fromIndex);
        int fingerCount = 0;
        
        Finger<E> finger = fingerList.get(firstFingerIndex);
        Node<E> prefixLastNode = node.prev;
        Node<E> nextNode = node;

        for (int i = 0; i < removalSize - 1; ++i) {
            Finger<E> f = fingerList.get(firstFingerIndex + fingerCount);
            
            if (finger == f) {
                if (fingersToRemove != 0) {
                    fingersToRemove--;
                    fingerCount++;
                }
            }
            
            nextNode = node.next;
            node.next = null;
            node.prev = null;
            node.item = null;
            node = nextNode;
        }

        Node<E> suffixFirstNode = nextNode.next;
        nextNode.next = null;
        nextNode.prev = null;
        nextNode.item = null;

        if (prefixLastNode != null) {
            if (suffixFirstNode == null) {
                prefixLastNode.next = null;
                last = prefixLastNode;
            } else {
                prefixLastNode.next = suffixFirstNode;
                suffixFirstNode.prev = prefixLastNode;
            }
        } else {
            suffixFirstNode.prev = null;
            first = suffixFirstNode;
        }
        
        fingerList.removeRange(
                firstFingerIndex, 
                fingerList.size() - firstFingerIndex - fingerCount, 
                removalSize);
    }
    
    private void removeRangeNodes(Node<E> node, int numberOfNodesToRemove) {
        Node<E> prefixLastNode = node.prev;
        Node<E> nextNode = node;
        
        for (int i = 0; i < numberOfNodesToRemove - 1; ++i) {
            nextNode = node.next;
            node.next = null;
            node.prev = null;
            node.item = null;
            node = nextNode;
        }
        
        Node<E> suffixFirstNode = nextNode.next;
        nextNode.next = null;
        nextNode.prev = null;
        nextNode.item = null;
        
        if (prefixLastNode != null) {
            if (suffixFirstNode == null) {
                prefixLastNode.next = null;
                last = prefixLastNode;
            } else {
                prefixLastNode.next = suffixFirstNode;
                suffixFirstNode.prev = prefixLastNode;
            }
        } else {
            suffixFirstNode.prev = null;
            first = suffixFirstNode;
        }
    }
    
    private void replaceAllRange(UnaryOperator<E> operator, int i, int end) {
        Objects.requireNonNull(operator);
        int expectedModCount = modCount;
        Node<E> node = node(i);
        
        while (modCount == expectedModCount && i < end) {
            node.item = operator.apply(node.item);
            i++;
        }
        
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
    
    /***************************************************************************
    Adds fingers after appending a collection to this list.
    ***************************************************************************/
    private void addFingersAfterAppendAll(
            Node<E> first,
            int firstIndex,
            int collectionSize) {
        int numberOfNewFingers = 
                getRecommendedNumberOfFingers() - fingerList.size();

        if (numberOfNewFingers == 0) {
            fingerList.get(fingerList.size()).index += collectionSize;
            return;
        }

        int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        int nodesToSkip = distanceBetweenFingers / 2;
        int index = firstIndex + nodesToSkip;
        Node<E> node = first;

        for (int i = 0; i < nodesToSkip; i++) {
            node = node.next;
        }
        
        int fingerIndex = fingerList.size();
        
        fingerList.makeRoomAtIndex(fingerIndex, 
                                   numberOfNewFingers, 
                                   collectionSize);

        fingerList.setFinger(fingerIndex++, new Finger<>(node, index));

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;

            for  (int j = 0; j < distanceBetweenFingers; j++) {
                node = node.next;
            }

            fingerList.setFinger(fingerIndex++, new Finger<>(node, index));
        }
    }
    
    
    /***************************************************************************
    Adds fingers after inserting a collection in this list.
    ***************************************************************************/
    private void addFingersAfterInsertAll(Node<E> headNodeOfInsertedRange,
                                          int indexOfInsertedRangeHead,
                                          int collectionSize) {
        int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerList.size();

        if (numberOfNewFingers == 0) {
            int fingerIndex = 
                    fingerList.getNextFingerIndex(indexOfInsertedRangeHead);
            
            fingerList.shiftFingerIndicesToRight(fingerIndex, collectionSize);
            return;
        }

        int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        int startOffset = distanceBetweenFingers / 2;
        int index = indexOfInsertedRangeHead + startOffset;
        Node<E> node = headNodeOfInsertedRange;
        
        for (int i = 0; i < startOffset; i++) {
           node = node.next;
        }

        int startFingerIndex =
                fingerList.getNextFingerIndex(indexOfInsertedRangeHead);
        
        fingerList.makeRoomAtIndex(startFingerIndex, 
                                   numberOfNewFingers, 
                                   collectionSize);
        
        fingerList.setFinger(startFingerIndex, new Finger<>(node, index));

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;

            for (int j = 0; j < distanceBetweenFingers; j++) {
                node = node.next;
            }

            fingerList.setFinger(startFingerIndex + i, 
                                 new Finger<>(node, index));
        }
    }
    
    /***************************************************************************
    Adds fingers after prepending a collection to this list.
    ***************************************************************************/
    private void addFingersAfterPrependAll(Node<E> first, int collectionSize) {
        int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerList.size();

        if (numberOfNewFingers == 0) {
            fingerList.shiftFingerIndicesToRight(0, collectionSize);
            return;
        }
        
        fingerList.makeRoomAtIndex(0, numberOfNewFingers, collectionSize);

        int distance = collectionSize / numberOfNewFingers;
        int startIndex = distance / 2;
        int index = startIndex;
        Node<E> node = first;

        for (int i = 0; i < startIndex; i++) {
            node = node.next;
        }
        
        int fingerIndex = 0;
        
        fingerList.setFinger(fingerIndex++, new Finger<>(node, index));

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;

            for (int j = 0; j < distance; j++) {
                node = node.next;
            }

            fingerList.setFinger(fingerIndex++, new Finger<>(node, index)); 
        }
    }
    
    /***************************************************************************
    Adds fingers after setting a collection as a list.
    ***************************************************************************/
    private void addFingersAfterSetAll(int collectionSize) {
        int numberOfNewFingers = getRecommendedNumberOfFingers();

        if (numberOfNewFingers == 0) {
            return;
        }

        int distance = size / numberOfNewFingers;
        int startIndex = distance / 2;
        int index = startIndex;
        fingerList.makeRoomAtIndex(0,
                                   numberOfNewFingers, 
                                   collectionSize);
        
        Node<E> node = first;

        for (int i = 0; i < startIndex; i++) {
            node = node.next;
        }
        
        fingerList.setFinger(0, new Finger<>(node, startIndex));

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;

            for (int j = 0; j < distance; j++) {
                node = node.next;
            }

            fingerList.setFinger(i, new Finger<>(node, index));
        }
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

        // Now, add the missing fingers:
        addFingersAfterPrependAll(first, sz);
    }
    
    /***************************************************************************
    If steps &lt; 0, rewind to the right. Otherwise, rewind to the right.
    ***************************************************************************/
    private Node<E> traverseLinkedListBackwards(Finger<E> finger, int steps) {
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
    
    private void appendFinger(Node<E> node, int index) {
        Finger<E> finger = new Finger<>(node, index);
        fingerList.appendFinger(finger);
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
        size = c.size();
        modCount++;

        addFingersAfterSetAll(c.size());
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
    
    static final class LinkedListSpliterator<E> implements Spliterator<E> {
        
        static final long MINIMUM_BATCH_SIZE = 1 << 10; // 1024 items
        
        private final EnhancedLinkedList<E> list;
        private Node<E> node;
        private long lengthOfSpliterator;
        private long numberOfProcessedElements;
        private long offsetOfSpliterator;
        private final int expectedModCount;
        
        private LinkedListSpliterator(EnhancedLinkedList<E> list,
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
    
    private static class EnhancedSubList<E> extends AbstractList<E> {
        
        private final EnhancedLinkedList<E> root;
        private final EnhancedSubList<E> parent;
        private final int offset;
        private int size;
        
        public EnhancedSubList(EnhancedLinkedList<E> root, int fromIndex, int toIndex) {
            this.root = root;
            this.parent = null;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = root.modCount;
        }
        
        private EnhancedSubList(EnhancedSubList<E> parent, int fromIndex, int toIndex) {
            this.root = parent.root;
            this.parent = parent;
            this.offset = parent.offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = root.modCount;
        }
        
        public void add(int index, E element) {
            checkInsertionIndex(index);
            checkForComodification();
            root.add(offset + index, element);
            updateSizeAndModCount(1);
        }
        
        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }
        
        public boolean addAll(int index, Collection<? extends E> collection) {
            checkInsertionIndex(index);
            int collectionSize = collection.size();
            
            if (collectionSize == 0) {
                return false;
            }
            
            checkForComodification();
            root.addAll(offset + index, collection);
            updateSizeAndModCount(collectionSize);
            return true;
        }
        
        public void clear() {
            checkForComodification();
            root.removeRange(offset, offset + size);
            updateSizeAndModCount(-size);
        }
        
        public E get(int index) {
            Objects.checkIndex(index, size);
            checkForComodification();
            return root.get(offset + index);
        }
        
        public Iterator<E> iterator() {
            return listIterator();
        }
        
        public ListIterator<E> listIterator(int index) {
            checkForComodification();
            checkInsertionIndex(index);
            
            return new ListIterator<E>() {
                private final ListIterator<E> i = 
                        root.listIterator(offset + index);
                
                public boolean hasNext() {
                    return nextIndex() < size;
                }
                
                public E next() {
                    if (hasNext()) {
                        return i.next();
                    } 
                    
                    throw new NoSuchElementException();
                }
                
                public boolean hasPrevious() {
                    return previousIndex() >= 0;
                }
                
                public E previous() {
                    if (hasPrevious()) {
                        return i.previous();
                    }
                    
                    throw new NoSuchElementException();
                }
                
                public int nextIndex() {
                    return i.nextIndex() - offset;
                }
                
                public int previousIndex() {
                    return i.previousIndex() - offset;
                }
                
                public void remove() {
                    i.remove();
                    updateSizeAndModCount(-1);
                }
                
                public void set(E e) {
                    i.set(e);
                }
                
                public void add(E e) {
                    i.add(e);
                    updateSizeAndModCount(1);
                }
            };
        }
        
        public E remove(int index) {
            Objects.checkIndex(index, size);
            checkForComodification();
            E result = root.remove(offset + index);
            updateSizeAndModCount(-1);
            return result;
        }
        
        public void replaceAll(UnaryOperator<E> operator) {
            root.replaceAllRange(operator, offset, offset + size);
        }
        
        public E set(int index, E element) {
            Objects.checkIndex(index, size);
            checkForComodification();
            return root.set(offset + index, element);
        }
        
        public int size() {
            checkForComodification();
            return size;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void sort(Comparator<? super E> c) {
            int expectedModCount = modCount;
            Object[] arr = this.toArray();
            Arrays.sort((E[]) arr, 0, size, c);
            
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            
            modCount++;
        }
        
        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new EnhancedSubList<>(this, fromIndex, toIndex);
        }
        
        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            root.removeRange(offset + fromIndex, offset + toIndex);
            updateSizeAndModCount(fromIndex - toIndex);
        }
        
        private void checkForComodification() {
            if (root.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        private void checkInsertionIndex(int index) {
            if (index < 0) {
                throw new IndexOutOfBoundsException(index);
            }
            
            if (index > size) {
                throw new IndexOutOfBoundsException(
                        "index(" + index + ") > size(" + size + ")");
            }
        }
        
        private void updateSizeAndModCount(int sizeDelta) {
            EnhancedSubList<E> subList = this;
            
            do {
                subList.size += sizeDelta;
                subList.modCount = root.modCount;
                subList = subList.parent;
            } while (subList != null);
        }
    }
}
