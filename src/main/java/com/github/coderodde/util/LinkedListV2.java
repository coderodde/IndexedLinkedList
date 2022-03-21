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
            shiftFingersToRight(fingerIndex, numberOfNodes);
            size += roomSize;
            enlargeFingerArrayIfNeeded();
            
            System.arraycopy(fingerArray, 
                             fingerIndex, 
                             fingerArray, 
                             fingerIndex + roomSize,
                             size - fingerIndex);
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
        void insertFingerAndShiftOnceToRight(Finger<E> finger) {
            
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
        
        /***********************************************************************
        For each finger with the index at least 'startIndex', decrease the index
        by one. This method updates the index of the end-of-list sentinel too.
        ***********************************************************************/
        private void shiftFingersToLeft(int startIndex) {
            for (int i = startIndex; i <= size; ++i) {
                fingerArray[i].index++;
            }
        }
        
        /***********************************************************************
        For each finger with the index at least 'startIndex', add 'steps' to the 
        index. This method updates the index of the end-of-list sentinel too.
        ***********************************************************************/
        private void shiftFingersToRight(int startIndex, int shiftLength) {
            for (int i = startIndex; i <= size; ++i) {
                fingerArray[i].index += shiftLength;
            }
        }
    
        /***********************************************************************
        For each finger with the index at least 'startIndex', incremnt the index
        by one. This method updates the index of the end-of-list sentinel too.
        ***********************************************************************/
        private void shiftFingersToRightOnce(int startIndex) {
            shiftFingersToRight(startIndex, 1);
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
        private void enlargeFingerArrayIfNeeded() {
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
     * {@inheritDoc } 
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
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
    Computes the recommended number of fingers.
    ***************************************************************************/
    private int getRecommendedNumberOfFingers() {
        return (int) Math.ceil(Math.sqrt(size));
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
    Returns true only if this list requires more fingers.
    ***************************************************************************/
    private boolean mustAddFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() - 1
        return fingerList.size() != getRecommendedNumberOfFingers();
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
    protected void shiftIndicesToLeftOnce(int startFingerIndex) {
        for (int sz = fingerList.size(), i = startFingerIndex; i <= sz; ++i) {
            fingerList.get(i).index--;
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
        
        fingerList.makeRoomAtIndex(fingerList.size(), 
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
            
            fingerList.shiftFingersToRight(fingerIndex, collectionSize);
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
            fingerList.shiftFingersToRight(0, collectionSize);
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
    
    protected void appendFinger(Node<E> node, int index) {
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
}
