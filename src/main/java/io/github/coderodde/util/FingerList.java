/*
 * The MIT License
 *
 * Copyright 2025 Rodion Efremov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.coderodde.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * This inner class implements the finger list data structure for managing list
 * fingers.
 *
 * @param <E> the list node item type.
 * @version 1.7.2 (Jul 7, 2025)
 */
final class FingerList<E> {

    /**
     * The owner indexed linked list.
     */
    final IndexedLinkedList<E> list;

    /**
     * This is also the minimum capacity.
     */
    static final int INITIAL_CAPACITY = 8;
    
    /**
     * When the actual size of the finger list (end-sentinel included) is
     * smaller than {@code fingerArray.length / THRESHOLD_FACTOR}, the array is
     * contracted to {@code fingerArray.length / CONTRACTION_FACTOR} elements.
     */
    static final int THRESHOLD_FACTOR = 4;
    
    /**
     * The actual contraction factor. The capacity of the finger array will be 
     * divided by this constant.
     */
    static final int CONTRACTION_FACTOR = 2;

    /**
     * The actual list storage array.
     */
    Finger<E>[] fingerArray = new Finger[INITIAL_CAPACITY];
    
    /**
     * Constructs this finger list setting it to empty.
     * 
     * @param list the owner list. 
     */
    FingerList(IndexedLinkedList<E> list) {
        this.list = list;
        this.fingerArray[0] = new Finger<>(null, 0);
    }
    
    /**
     * Verifies that this finger list and {@code o} have the same size and 
     * content. Runs in worst-case linear time.
     * 
     * @param o the object to compare to.
     * @return {@code true} if and only if {@code o} is a {@code FingerList},
     *         has the same size as this finger list and the same content.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        
        if (o == this) {
            return true;
        }
        
        if (!o.getClass().equals(this.getClass())) {
            return false;
        }
        
        final FingerList<E> other = (FingerList<E>) o;
        
        if (size != other.size) {
            return false;
        }
        
        for (int i = 0; i < size; i++) {
            if (!Objects.equals(fingerArray[i], other.fingerArray[i])) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = 
                new StringBuilder()
                        .append("[FingerList (size = ")
                        .append(size + 1)
                        .append(") | ");
        
        boolean first = true;

        for (int i = 0; i != size + 1; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(fingerArray[i].toString());
        }

        return sb.append("]").toString();
    }

    /**
     * The number of fingers stored in the list. This field does not count the
     * end-of-list sentinel finger {@code F} for which {@code F.index = size}.
     */
    int size;
    
    /**
     * Adjusts the finger list after removing the first finger. runs in worst-
     * case \(\mathcal{O}(\sqrt{n})\) time.
     */
    void adjustOnRemoveFirst() {
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

        shiftFingerIndicesToLeftOnceAll(lastPrefixIndex);
    }
    
    /**
     * Appends the input finger to the tail of the finger list. Runs in 
     * amortized constant time.
     *
     * @param finger the finger to append.
     */
     void appendFinger(Finger<E> finger) {
        
        enlargeFingerArrayWithEmptyRange(size + 2 , 
                                         size, 
                                         1,
                                         1);
        fingerArray[size - 1] = finger;
        fingerArray[size].index = list.size;
    }
    
    /**
     * Pushes {@code numberOfFingersToMoveToPrefix} fingers to the prefix with
     * {@code numberOfFingersInPrefix} fingers.
     * 
     * @param fromIndex                     the index of the leftmost element to 
     *                                      remove.
     * @param numberOfFingersInPrefix       the number of fingers already in the
     *                                      prefix.
     * @param numberOfFingersToMoveToPrefix the number of fingers we need to
     *                                      move to the prefix.
     */
    void arrangePrefix(int fromIndex,
                       int numberOfFingersInPrefix,
                       int numberOfFingersToMoveToPrefix) {
        
        makeRoomAtPrefix(fromIndex,
                         numberOfFingersInPrefix, 
                         numberOfFingersToMoveToPrefix);
        
        pushCoveredFingersToPrefix(fromIndex, 
                                   numberOfFingersInPrefix,
                                   numberOfFingersToMoveToPrefix);
    }
    
    void arrangeSuffix(int toIndex,
                       int toFingerIndex,
                       int numberOfSuffixFingers,
                       int numberOfFingetsToPush) {
        
        makeRoomAtSuffix(toIndex,
                         toFingerIndex,
                         numberOfSuffixFingers, 
                         numberOfFingetsToPush);
        
        pushCoveredFingersToSuffix(toIndex,
                                   numberOfSuffixFingers, 
                                   numberOfFingetsToPush);
    }

    /**
     * Clears entirely this finger list. Only the end-of-finger-list finger
     * remains in the finger list. Not {@code private} since is used in the unit
     * tests.
     */
    void clear() {
        Arrays.fill(fingerArray, 
                    0, 
                    size,
                    null);
        
        fingerArray    = new Finger[INITIAL_CAPACITY];
        fingerArray[0] = new Finger<>(null, 0);
        size = 0;
    }

    /**
     * Contracts the finger array, if possible. The {@code nextSize} defines the
     * requested finger array size not counting the end-of-finger-list sentinel
     * finger.
     *
     * @param nextSize the requested size not counting the end-of-finger-list
     * sentinel finger.
     */
    void contractFingerArrayIfNeeded(int nextSize) {
        if (fingerArray.length == INITIAL_CAPACITY) {
            // Nothing to contract:
            return;
        }
        
        // Can we contract at least once?
        if (nextSize + 1 < fingerArray.length / THRESHOLD_FACTOR) {
            
            int nextCapacity = fingerArray.length / CONTRACTION_FACTOR;

            // Good, we can. But can we keep on splitting in half the 
            // capacity any further?
            while (nextCapacity >= (nextSize + 1) * CONTRACTION_FACTOR
                && nextCapacity > INITIAL_CAPACITY) {
                // Yes, we can do it as well.
                nextCapacity /= CONTRACTION_FACTOR;
            }
            
            fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
        }
    }
    
    
    void enlargeFingerArrayWithEmptyRange(int requestedCapacity,
                                          int fingerRangeStartIndex,
                                          int fingerRangeLength,
                                          int elementRangeLength) {
        
        if (requestedCapacity > fingerArray.length) {
            // Compute the next accommodating capacity:
            int nextCapacity = 2 * fingerArray.length;
            
            while (nextCapacity < requestedCapacity) {
                nextCapacity *= 2;
            }
            
            // Here, we have a next accommodating capacity!
            Finger<E>[] nextFingerArray = new Finger[nextCapacity];
            
            // Copy the finger array prefix:
            System.arraycopy(fingerArray, 
                             0,
                             nextFingerArray, 
                             0,
                             fingerRangeStartIndex);
            
            // Compute the number of fingers to shift to the right:
            int numberOfFingersToShift = size
                                       - fingerRangeStartIndex
                                       + 1;
            
            // Make room for the finger range:
            System.arraycopy(fingerArray, 
                             fingerRangeStartIndex,
                             nextFingerArray,
                             fingerRangeStartIndex + fingerRangeLength,
                             numberOfFingersToShift);
            
            // Deploy 'nextFingerArraqy':
            fingerArray = nextFingerArray;
            
            // Update the number of fingers in this finger list:
            size += fingerRangeLength;
            
            // Update the indices of the suffix finger list:
            shiftFingerIndicesToRight(fingerRangeStartIndex + fingerRangeLength,
                                      elementRangeLength);
        } else {
            // Shift the right part to the right:
            shiftFingerIndicesToRight(fingerRangeStartIndex, 
                                      elementRangeLength);
            
            int numberOfSuffixFingers = size
                                      + 1
                                      - fingerRangeStartIndex;

            // Make room for the finger range:
            System.arraycopy(fingerArray,
                             fingerRangeStartIndex,
                             fingerArray,
                             fingerRangeStartIndex + fingerRangeLength,
                             numberOfSuffixFingers);
            
            size += fingerRangeLength;
        }
    }
    
    /**
     * Returns {@code index}th finger.
     *
     * @param index the index of the target finger.
     * @return the {@code index}th finger.
     */
    Finger<E> getFinger(int index) {
        return fingerArray[index];
    }

    /**
     * Returns the index of the finger that is closest to the
     * {@code elementIndex}th list element.
     *
     * @param elementIndex the target element index.
     * @return the index of the finger that is closest to the
     * {@code elementIndex}th element.
     */
    int getClosestFingerIndex(int elementIndex) {
        return normalize(getFingerIndexImpl(elementIndex), 
                         elementIndex);
    }

    /**
     * Returns the finger index {@code i}, such that
     * {@code fingerArray[i].index} is no less than {@code elementIndex}, and
     * {@code fingerArray[i].index} is closest to {@code elementIndex}. This
     * algorithm is translated from
     * <a href="https://en.cppreference.com/w/cpp/algorithm/lower_bound">C++
     * <code>lower_bound</code> algorithm</a>.
     *
     * @param elementIndex the target element index.
     * @return the index of the finger {@code f}, for which
     * {@code elementIndex <= f.index} and {@code f} is the leftmost such
     * finger.
     */
    int getFingerIndexImpl(int elementIndex) {
        int count = size + 1; // + 1 for the end sentinel.
        int idx = 0;

        while (count > 0) {
            int it = idx;
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
    
    /**
     * Access the {@code index}th node without modifying the fingers unlike 
     * {@link #getNode(int)}. 
     * 
     * @param index the index of the desired node.
     * 
     * @return the {@code index}th node.
     */
    Node<E> getNodeNoFingersFix(int index) {
        Finger finger = fingerArray[getClosestFingerIndex(index)];
        int steps = index - finger.index;
        
        return IndexedLinkedList.rewindFinger(finger, 
                                              steps);
    }
    
    /**
     * Returns the {@code i}th node of this linked list. The closest finger is
     * updated to point to the returned node.
     *
     * @param elementIndex the element index.
     * @return the {@code index}th node in the linked list.
     */
    Node<E> getNode(int elementIndex) {
        if (size < 3) {
            // We need at least 3 fingers to do the actual trick:
            return list.getNodeSequentially(elementIndex);
        }

        int fingerIndex = getFingerIndexImpl(elementIndex);

        if (fingerIndex == 0) {
            // There is no required preceding finger:
            return getPrefixNode(elementIndex);
        }

        if (fingerIndex >= size - 1) {
            return getSuffixNode(elementIndex);
        }

        Finger a = fingerArray[fingerIndex - 1];
        Finger b = fingerArray[fingerIndex];
        Finger c = fingerArray[fingerIndex + 1];

        int diff = c.index - a.index;
        int step = diff / 2;
        int saveBIndex = b.index;
        int nextBIndex = a.index + step;

        b.index = nextBIndex;

        // Rewind the finger b node:
        if (saveBIndex < nextBIndex) {
            for (int i = 0; i != nextBIndex - saveBIndex; i++) {
                b.node = b.node.next;
            }
        } else {
            // Here, 'saveBIndex >= nextBIndex':
            for (int i = 0; i != saveBIndex - nextBIndex; i++) {
                b.node = b.node.prev;
            }
        }

        // Go fetch the correct node:
        if (elementIndex < nextBIndex) {
            // Here, the desired element is between a and b:
            int leftDistance = elementIndex - a.index;
            int rightDistance = b.index - elementIndex;

            if (leftDistance < rightDistance) {
                return scrollToRight(a.node,
                                     leftDistance);
            } else {
                return scrollToLeft(b.node,
                                    rightDistance);
            }
        } else {
            // Here, the desired element is between c and b:
            int leftDistance = elementIndex - b.index;
            int rightDistance = c.index - elementIndex;

            if (leftDistance < rightDistance) {
                return scrollToRight(b.node, 
                                     leftDistance);
            } else {
                return scrollToLeft(c.node,
                                    rightDistance);
            }
        }
    }
    
    /**
     * Normalizes the first finger and returns the {@code elementIndex}th node.
     *
     * @param elementIndex the index of the desired element.
     *
     * @return the node corresponding to the {@code elementIndex}th position.
     */
    private Node<E> getPrefixNode(int elementIndex) {
        Finger<E> a = fingerArray[0];
        Finger<E> b = fingerArray[1];
        Node<E> aNode = a.node;

        // Put a between b and the beginning of the list:
        int nextAIndex = b.index / 2;
        int saveAIndex = a.index;

        a.index = nextAIndex;

        if (saveAIndex < nextAIndex) {
            // Here, we need to rewind to the right:
            for (int i = saveAIndex; i < nextAIndex; i++) {
                aNode = aNode.next;
            }
        } else {
            // Once here, 'saveAIndex >= nextAIndex'.
            // We need to rewind to the left:
            for (int i = nextAIndex; i < saveAIndex; i++) {
                aNode = aNode.prev;
            }
        }

        a.node = aNode;

        // Go get the proper node:
        if (elementIndex < nextAIndex) {
            // Here, the desired element is between the head of the list and
            // the very first fi    nger:
            int leftDistance = elementIndex;
            int rightDistance = nextAIndex - elementIndex;

            if (leftDistance < rightDistance) {
                return scrollToRight(list.head,
                                     elementIndex);
            } else {
                return scrollToLeft(aNode, 
                                    rightDistance);
            }
        } else {
            return aNode;
        }
    }
    
    /**
     * Returns the {@code elementIndex}th node and normalizes the last finger.
     *
     * @param elementIndex the index of the desired element.
     *
     * @return the {@code elementIndex}th node.
     */
    private Node<E> getSuffixNode(int elementIndex) {
        Finger<E> a = fingerArray[size - 2];
        Finger<E> b = fingerArray[size - 1];
        Node<E> bNode = b.node;

        int saveBIndex = b.index;
        int nextBIndex = (a.index + list.size) / 2;

        b.index = nextBIndex;

        // Rewind the finger 'b' to between 'a' and tail:
        if (saveBIndex < nextBIndex) {
            int distance = nextBIndex - saveBIndex;

            for (int i = 0; i != distance; i++) {
                bNode = bNode.next;
            }
        } else {
            // Here, 'nextBIndex <= saveBIndex':
            int distance = saveBIndex - nextBIndex;

            for (int i = 0; i != distance; i++) {
                bNode = bNode.prev;
            }
        }

        b.node = bNode;

        // Go get the proper node:
        if (elementIndex < nextBIndex) {
            // Here, the desired element node is between 'a' and 'b':
            int leftDistance = elementIndex - a.index;
            int rightDistance = nextBIndex - elementIndex;

            if (leftDistance < rightDistance) {
                return scrollToRight(a.node,
                                     leftDistance);
            } else {
                return scrollToLeft(b.node,
                                    rightDistance);
            }
        } else {
            // Here, the desired element node is between 'b' and the tail 
            // node of the list:
            int leftDistance = elementIndex - nextBIndex;
            int rightDistance = list.size - elementIndex - 1;

            if (leftDistance < rightDistance) {
                // Once here, rewind the node reference from bNode to the
                // right:
                return scrollToRight(bNode, 
                                     leftDistance);
            } else {
                // Once here, rewind the node reference from tail to the 
                // left:
                return scrollToLeft(list.tail,
                                    rightDistance);
            }
        }
    }

    /**
     * Inserts the input finger into the finger list such that the entire finger
     * list is sorted by indices.
     *
     * @param finger the finger to insert.
     */
    void insertFingerAndShiftOnceToRight(Finger<E> finger) {
        int beforeFingerIndex = getFingerIndexImpl(finger.index);

        enlargeFingerArrayWithEmptyRange(size + 2,
                                         beforeFingerIndex, 
                                         1,
                                         1);
        
        fingerArray[beforeFingerIndex] = finger;
    }
    
    /**
     * Returns {@code true} if this finger list is empty.
     * 
     * @return {@code true} if this finger contains no fingers (except the
     *         end-of-finger-list sentinel).
     */
    boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Make sure we can insert {@code roomSize} fingers starting from
     * {@code fingerIndex}, shifting all the fingers starting from
     * {@code numberOfNodes} to the right.
     *
     * @param fingerIndex the finger index of the first finger in the shifted
     * finger slice.
     * @param roomSize the number of free spots requested.
     * @param numberOfNodes the shift amount of the moved fingers.
     */
    void makeRoomAtIndex(int fingerIndex,
                         int roomSize,
                         int numberOfNodes) {
        
        enlargeFingerArrayWithEmptyRange(size + 1 + roomSize, 
                                         fingerIndex, 
                                         roomSize,
                                         numberOfNodes);
    }
    
    void makeRoomAtPrefix(int fromIndex,
                          int numberOfFingersInPrefix,
                          int numberOfFingersToMoveToPrefix) {
       
        if (numberOfFingersInPrefix == 0) {
            // Here, no fingers in the prefix to move.
            return;
        }
        
        int targetFingerIndex = numberOfFingersInPrefix - 1;
        int freeFingerSpotsSoFar = fromIndex 
                                 - getFinger(targetFingerIndex).index
                                 - 1;
        
        if (freeFingerSpotsSoFar >= numberOfFingersToMoveToPrefix) {
            return;
        }
        
        for (; targetFingerIndex > 0; targetFingerIndex--) {
           Finger<E> finger1 = getFinger(targetFingerIndex - 1);
           Finger<E> finger2 = getFinger(targetFingerIndex);

           int distance = finger2.index
                        - finger1.index
                        - 1;

           freeFingerSpotsSoFar += distance;

           if (freeFingerSpotsSoFar >= numberOfFingersToMoveToPrefix) {
               break;
           }
        }
        
        if (freeFingerSpotsSoFar < numberOfFingersToMoveToPrefix) {
            // Once here, we need to move the leftmost prefix finger to the 
            // left.
            int index = fromIndex 
                      - numberOfFingersInPrefix 
                      - numberOfFingersToMoveToPrefix;
            
            Node<E> node = getNodeNoFingersFix(index);
            
            for (int i = 0; i < numberOfFingersInPrefix; i++) {
                Finger<E> finger = getFinger(i);
                finger.index = index++;
                finger.node = node;
                node = node.next;
            }
        } else {
            Finger<E> startFinger = getFinger(targetFingerIndex - 1);
            int index = startFinger.index;
            Node<E> node = startFinger.node;
            
            for (int i = targetFingerIndex; i < numberOfFingersInPrefix; i++) {
                Finger<E> finger = getFinger(i);
                node = node.next;
                finger.node = node;
                finger.index = ++index;
            }
        }
    }
    
    void makeRoomAtSuffix(int toIndex,
                          int toFingerIndex,
                          int numberOfFingersInSuffix,
                          int numberOfFingersToMoveToSuffix) {
        
        if (numberOfFingersInSuffix == 0) {
            // Here, no fingers in the suffix to move.
            return;
        }
        
        int targetFingerIndex = size - numberOfFingersInSuffix;
        int freeFingerSpotsSoFar = getFinger(targetFingerIndex).index 
                                 - toIndex;
        
        if (freeFingerSpotsSoFar >= numberOfFingersToMoveToSuffix) {
            return;
        }
        
        for (; targetFingerIndex < size - 1; targetFingerIndex++) {
            Finger<E> finger1 = getFinger(targetFingerIndex);
            Finger<E> finger2 = getFinger(targetFingerIndex + 1);

            int distance = finger2.index 
                         - finger1.index 
                         - 1;

            freeFingerSpotsSoFar += distance;

            if (freeFingerSpotsSoFar >= numberOfFingersToMoveToSuffix) {
                break;
            }
        }
        
        if (freeFingerSpotsSoFar < numberOfFingersToMoveToSuffix) {
            // Once here, we need to move the rightmost suffix finger to the 
            // right.
            int index = list.size
                      - numberOfFingersInSuffix;
            
            Node<E> node = getNodeNoFingersFix(index);
            
            for (int i = 0; i < numberOfFingersInSuffix; i++) {
                Finger<E> finger =
                        getFinger(size - numberOfFingersInSuffix + i);
                
                finger.index = index++;
                finger.node = node;
                node = node.next;
            }
        } else {
            Finger<E> startFinger = getFinger(targetFingerIndex + 1);
            int index = startFinger.index - 1;
            Node<E> node = startFinger.node.prev;
            
            // TODO: Debug, please!
            for (int i = targetFingerIndex; 
                    i >= toFingerIndex; 
                    i--) {
                Finger<E> finger = getFinger(i);
                finger.index = index--;
                finger.node = node;
                node = node.prev;
            }
        }
    }
    
    /**
     * Makes sure that the returned finger index {@code i} points to the closest
     * finger in the finger array.
     *
     * @param fingerIndex the finger index.
     * @param elementIndex the element index.
     *
     * @return the index of the finger that is closest to the
     * {@code elementIndex}th element.
     */
    private int normalize(int fingerIndex, int elementIndex) {
        if (fingerIndex == 0) {
            // Since we cannot point to '-1'th finger, return 0:
            return 0;
        }

        if (fingerIndex == size) {
            // Don't go outside of 'size - 1':
            return size - 1;
        }

        Finger finger1 = fingerArray[fingerIndex - 1];
        Finger finger2 = fingerArray[fingerIndex];

        int distance1 = elementIndex - finger1.index;
        int distance2 = finger2.index - elementIndex;

        // Return the closest finger index:
        return distance1 < distance2 ? fingerIndex - 1 : fingerIndex;
    }

    /**
     * Creates a finger for the input node {@code node} and inserts it at the
     * head of the finger array.
     *
     * @param node the target node.
     */
    void prependFingerForNode(Node<E> node) {
        Finger<E> finger = new Finger<>(node, 0);
        
        // 'size + 1': actual number of fingers + the end-of-finger-list 
        // sentinel:
        if (size + 1 == fingerArray.length) {
            // Once here, the 'fingerArray' is fully filled:
            Finger<E>[] newFingerArray = new Finger[2 * fingerArray.length];
            
            // Move the current finger list contents to the new finger array:
            System.arraycopy(fingerArray, 
                             0,
                             newFingerArray, 
                             1,
                             size + 1);
            
            fingerArray = newFingerArray;
            
            // Shift all the rest fingers' indices one step to the right towards
            // higher indices:
            shiftFingerIndicesToRightOnce(1);
            
            // Update the index of the new end-of-finger-list sentinel:
            ++getFinger(size() + 1).index;
        } else {
            // Shift the all fingers' indices one step to the right:
            shiftFingerIndicesToRightOnce(0);
            
            // Make room for the new finger:
            System.arraycopy(fingerArray,
                             0,
                             fingerArray,
                             1, 
                             size + 1);
            
        }
        
        fingerArray[0] = finger;
        size++;
    }
    
    /**
     * Pushes {@code numberOfFingersToPush} to the finger prefix.
     * 
     * @param fromIndex             the starting index of the range to delete.
     * @param numberOfPrefixFingers the number of fingers in the prefix.
     * @param numberOfFingersToPush the number of fingers to move to the prefix.
     */
    void pushCoveredFingersToPrefix(int fromIndex,
                                    int numberOfPrefixFingers,
                                    int numberOfFingersToPush) {
        if (numberOfPrefixFingers == 0) {
            int index = fromIndex - 1;
            Node<E> node = getNodeNoFingersFix(index);
            
            for (int i = numberOfFingersToPush - 1; i >= 0; i--) {
                Finger<E> finger = getFinger(i);
                finger.index = index--;
                finger.node = node;
                node = node.prev;
            }
        } else {
            Finger<E> rightmostPrefixFinger = 
                    getFinger(numberOfPrefixFingers - 1);
            
            int index = rightmostPrefixFinger.index + 1;
            Node<E> node = rightmostPrefixFinger.node.next;
            
            for (int i = numberOfPrefixFingers; 
                    i < numberOfPrefixFingers + numberOfFingersToPush;
                    i++) {
                
                Finger<E> finger = getFinger(i);
                finger.index = index++;
                finger.node = node;
                node = node.next;
            }
        }
    }
    
    void pushCoveredFingersToSuffix(int toIndex,
                                    int numberOfSuffixFingers,
                                    int numberOfFingersToPush) {
        if (numberOfSuffixFingers == 0) {
            int index = toIndex;
            Node<E> node = getNodeNoFingersFix(index);
            
            for (int i = 0; i < numberOfFingersToPush; i++) {
                Finger<E> finger = getFinger(size - numberOfFingersToPush + i);
                finger.index = index++;
                finger.node = node;
                node = node.next;
            }
        } else {
            Finger<E> leftmostSuffixFinger = 
                    getFinger(size - numberOfSuffixFingers);
            
            int index = leftmostSuffixFinger.index;
            Node<E> node = leftmostSuffixFinger.node;
            
            // TODO: Check this bound!
            for (int i = 0; i < numberOfFingersToPush; i++) {
                Finger<E> finger = 
                        getFinger(size - numberOfSuffixFingers - 1 - i);
                
                node = node.prev;
                finger.node = node;
                finger.index = --index;
            }
        }
    }

    /**
     * Removes the last finger residing right before the end-of-finger-list
     * sentinel finger.
     */
    void removeFinger() {
        contractFingerArrayIfNeeded(--size);
        fingerArray[size] = fingerArray[size + 1];
        fingerArray[size + 1] = null;
        fingerArray[size].index = list.size;
    }
    
    /**
     * This method is responsible for actual removal of the fingers. Run in 
     * worst-case \(\mathcal{O}(\sqrt{N})\) time.
     * 
     * @param fromFingerIndex         the index of the very first finger to 
     *                                remove.
     * @param numberOfFingersToRemove the number of fingers to remove.
     * @param removalRangeLength      the length of the element range belonging
     *                                to the range being removed.
     */
    void removeFingersOnDeleteRange(int fromFingerIndex,
                                    int numberOfFingersToRemove,
                                    int removalRangeLength) {
        
        if (numberOfFingersToRemove != 0) {
            // Push 'numberOfFingersToRemove' towards to the prefix:
            System.arraycopy(
                    fingerArray, 
                    fromFingerIndex 
                            + list.numberOfCoveringFingersToPrefix
                            + numberOfFingersToRemove,
                    fingerArray, 
                    fromFingerIndex + list.numberOfCoveringFingersToPrefix, 
                    size 
                            - fromFingerIndex
                            - numberOfFingersToRemove 
                            - list.numberOfCoveringFingersToPrefix 
                            + 1);
            
            // Set all unused finger array positions to 'null' in order to get
            // rid of junk:
            Arrays.fill(fingerArray,
                        size - numberOfFingersToRemove + 1,
                        size + 1,
                        null);

            // Update the number of fingers:
            this.size -= numberOfFingersToRemove;
        }
        
        // Update the finger indices on the right:
        shiftFingerIndicesToLeft(
                fromFingerIndex + list.numberOfCoveringFingersToPrefix,
                removalRangeLength);
        
        list.size -= removalRangeLength;
    }
    
    /**
     * Returns a node that is {@code steps} hops away from {@code node] to the 
     * left.
     * 
     * @param node  the starting node.
     * @param steps the number of hops to make.
     * 
     * @return the requested node.
     */
    static <E> Node<E> scrollToLeft(Node<E> node, int steps) {
        for (int i = 0; i != steps; ++i) {
            node = node.prev;
        }
        
        return node;
    }
    
    /**
     * Returns a node that is {@code steps} hops away from {@code node] to the 
     * right.
     * 
     * @param node  the starting node.
     * @param steps the number of hops to make.
     * 
     * @return the requested node.
     */
    static <E> Node<E> scrollToRight(Node<E> node, int steps) {
        for (int i = 0; i != steps; ++i) {
            node = node.next;
        }
        
        return node;
    }
        
    /**
     * Sets the finger {@code finger} to the finger array at index
     * {@code index}.
     *
     * @param index the index of the finger list component.
     * @param finger the target finger to set.
     */
    void setFinger(int index, Finger<E> finger) {
        fingerArray[index] = finger;
    }
    
    /**
     * Sets all the leftmost {@code indices.length} fingers to the specified 
     * indices.
     * 
     * @param indices the target indices.
     */
    void setFingerIndices(int... indices) {
        Arrays.sort(indices);
        int fingerIndex = 0;
        
        for (final int index : indices) {
            final Finger<E> finger = fingerArray[fingerIndex++];
            finger.index = index;
            finger.node = getNodeSequentially(index);
        }
    }
    
    /**
     * Accesses the {@code index}th node sequentially without using fingers and 
     * modifying the fingers.
     * 
     * @param index the index of the desired node.
     * 
     * @return {@code index} node. 
     */
    private Node<E> getNodeSequentially(final int index) {
        return list.getNodeSequentially(index);
    }

    /**
     * Moves all the fingers in range {@code [startFingerIndex, size]}
     * {@code shiftLength} positions to the left (towards smaller indices).
     *
     * @param startFingerIndex the index of the leftmost finger to shift.
     * @param shiftLength the length of the shift operation.
     */
    void shiftFingerIndicesToLeft(int startFingerIndex, int shiftLength) {
        for (int i = startFingerIndex; i <= size; ++i) {
            fingerArray[i].index -= shiftLength;
        }
    }

    /**
     * Moves all the fingers in range {@code [startFingerIndex, size]} one
     * position to the left (towards smaller indices).
     *
     * @param startFingerIndex the index of the leftmost finger to shift.
     */
    void shiftFingerIndicesToLeftOnceAll(int startFingerIndex) {
        for (int i = startFingerIndex; i <= size; ++i) {
            fingerArray[i].index--;
        }
    }

    /**
     * Moves all the fingers in range {@code [startFingerIndex, size]}
     * {@code shiftLength} positions to the right (towards larger indices).
     *
     * @param startIndex the index of the leftmost finger to shift.
     * @param shiftLength the length of the shift operation.
     */
    void shiftFingerIndicesToRight(int startIndex, int shiftLength) {
        for (int i = startIndex; i <= size; ++i) {
            fingerArray[i].index += shiftLength;
        }
    }

    /**
     * Moves all the fingers in range {@code [startFingerIndex, size]} one
     * position to the right (towards larger indices).
     *
     * @param startIndex the index of the leftmost finger to shift.
     */
    void shiftFingerIndicesToRightOnce(int startIndex) {
        shiftFingerIndicesToRight(startIndex, 1);
    }
    
    /**
     * Returns the number of fingers in this finger list not counting the
     * end-of-finger-list finger.
     *
     * @return the number of fingers in this finger list.
     */
    int size() {
        return size;
    }
}
