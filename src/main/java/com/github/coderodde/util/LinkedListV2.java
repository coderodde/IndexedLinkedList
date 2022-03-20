package com.github.coderodde.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.61 (Sep 26, 2021)
 * @since 1.6 (Sep 1, 2021)
 */
public class LinkedListV2<E> extends LinkedList<E> {
    
    class FingerList<E> {

        private static final int INITIAL_CAPACITY = 8;

        // The actual list storage array:
        Finger<E>[] fingerArray = new Finger[INITIAL_CAPACITY];

        // The number of fingers stored in the list. This field does not count
        // the end-of-list sentinel finger 'F' for which 'F.index = size'.
        private int size;

        FingerList() {
            fingerArray[0] = new Finger<>(null, 0);
        }

        Finger<E> get(int index) {
            return fingerArray[index];
        }

        int size() {
            return size;
        }

        int getFingerIndex(int elementIndex) {
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
            
            return normalize(idx, elementIndex);
        }
        
        void setFinger(int index, Finger<E> finger) {
            fingerArray[index] = finger;
        }
        
        void makeRoomAtIndex(int fingerIndex, int roomSize) {
            size += roomSize;
            enlargeFingerArrayIfNeeded();
            System.arraycopy(fingerArray, 
                             fingerIndex, 
                             fingerArray, 
                             fingerIndex + roomSize,
                             roomSize);
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

        // Appends the input finger to the tail of the finger list:
        void appendFinger(com.github.coderodde.util.Finger<E> finger) {
            enlargeFingerArrayIfNeeded();
            fingerArray[size + 1] = fingerArray[size];
            fingerArray[size] = finger;
            fingerArray[size + 1].index = LinkedListV2.this.size;
            size++;
        }

        // Inserts the input finger into the finger list such that the entire
        // finger list is sorted by indices:
        void insertFingerAndShiftOnceToRight(
                com.github.coderodde.util.Finger<E> finger) {
            
            enlargeFingerArrayIfNeeded();
            int beforeFingerIndex = getFingerIndex(finger.index);
            System.arraycopy(
                    fingerArray, 
                    beforeFingerIndex, 
                    fingerArray, 
                    beforeFingerIndex + 1, 
                    size + 1 - beforeFingerIndex);
            
            // Shift fingerArray[beforeFingerIndex ... size] one position to the
            // right (towards larger index values:
            shiftFingersToRightOnce(beforeFingerIndex);

            fingerArray[beforeFingerIndex] = finger;
            fingerArray[size].index = LinkedListV2.this.size;
            size++;
        }
        
        private void shiftFingersToRightOnce(int startIndex) {
            for (int i = startIndex; i <= size; ++i) {
                fingerArray[i].index++;
            }
        }
        
        private void shiftFingersToRight(int startIndex, int shiftLength) {
            for (int i = startIndex; i <= size; ++i) {
                fingerArray[i].index += shiftLength;
            }
        }

        void removeFinger() {
            --size;
            contractFingerArrayIfNeeded();
            fingerArray[size] = fingerArray[size + 1];
            fingerArray[size + 1] = null;
            fingerArray[size].index = LinkedListV2.this.size;
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
        void enlargeFingerArrayIfNeeded() {
            // If the finger array is full, double the capacity:
            if (size + 1 == fingerArray.length) {
                int nextCapacity = 2 * fingerArray.length;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }

        // We can save some space while keeping the finger array operations 
        // amortized O(1):
        private void contractFingerArrayIfNeeded() {
            if (size * 4 <= fingerArray.length 
                    && fingerArray.length > 2 * INITIAL_CAPACITY) {
                int nextCapacity = fingerArray.length / 4;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
    }
     
    int size;
    transient Node<E> first;
    transient Node<E> last;
    final transient FingerList<E> fingerList = new FingerList<>();
    
    public LinkedListV2() {
        
    }
    
    public LinkedListV2(Collection<? extends E> c) {
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

    /***************************************************************************
    Constructs an IndexOutOfBoundsException detail message.
    ***************************************************************************/
    private String getOutOfBoundsMessage(int index) {
        return "Index: " + index + ", Size: " + size;
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
        }
    }
    
    private void increaseSize() {
        ++size;
        ++modCount;
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
    Returns true only if this list requires more fingers.
    ***************************************************************************/
    private boolean mustAddFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() - 1
        return fingerStack.size() != getRecommendedNumberOfFingers();
    }
    
    protected Node<E> node(int elementIndex) {
         return fingerList.node(elementIndex);
    }
    
    protected void appendFinger(Finger<E> finger) {
        fingerList.appendFinger(finger);
    }
    
    protected void insertFinger(Finger<E> finger) {
        //fingerList.insertFinger(finger);
    }
    
    protected void removeFinger() {
        fingerList.removeFinger();
    }
    
    
    /***************************************************************************
    Subtracts 'steps' positions from each index at least 'startingIndex'.
    ***************************************************************************/
    protected void shiftIndicesToLeft(int startingFingerIndex, int steps) {
        for (int i = startingFingerIndex, sz = fingerStack.size(); 
                i < sz;
                i++) {
            
            Finger<E> finger = fingerStack.get(i);
            int nextIndex = finger.index - steps;
            finger.updateIndex = nextIndex;
            fingerStack.fingerIndexSet.remove(finger.index);
        }
        
        for (int i = startingFingerIndex, sz = fingerStack.size();
                i < sz; 
                i++) {

            Finger<E> finger = fingerStack.get(i);
            finger.index = finger.updateIndex;
            fingerStack.fingerIndexSet.add(finger.index);
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

        appendFinger(node, index);

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;

            for  (int j = 0; j < distanceBetweenFingers; j++) {
                node = node.next;
            }

            appendFinger(node, index);
        }
    }
    
    
    /***************************************************************************
    Adds fingers after inserting a collection in this list.
    ***************************************************************************/
    private int addFingersAfterInsertAll(Node<E> headNodeOfInsertedRange,
                                         int indexOfInsertedRangeHead,
                                         int collectionSize) {
        int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerStack.size();

        if (numberOfNewFingers == 0) {
            return -1;
        }

        int distanceBetweenFingers = collectionSize / numberOfNewFingers;
        int startOffset = distanceBetweenFingers / 2;
        int index = indexOfInsertedRangeHead + startOffset;
        Node<E> node = headNodeOfInsertedRange;

        for (int i = 0; i < startOffset; i++) {
           node = node.next;
        }

        fingerList.insertFinger(new Finger<>(node, index));

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distanceBetweenFingers;

            for (int j = 0; j < distanceBetweenFingers; j++) {
                node = node.next;
            }

            fingerList.insertFinger(new Finger<>(node, index));
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

        fingerList.shiftFingersToRight(0, collectionSize);
        fingerList.makeRoomAtIndex(0, collectionSize);
        
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
    protected void addFingersAfterSetAll() {
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

        appendFinger(node, startIndex);

        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;

            for (int j = 0; j < distance; j++) {
                node = node.next;
            }

            appendFinger(node, index);
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

        // Prior to adding new (possible) fingers, we need to shift all the
        // current fingers 'c.size()' nodes to the larger index values:
        shiftIndicesToRight(0, sz);

        // Now, add the missing fingers:
        addFingersAfterPrependAll(first, sz);
    }
    
    protected void appendFinger(Node<E> node, int index) {
        Finger<E> finger = new Finger<>(node, index);
        fingerList.appendFinger(finger);
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
}
