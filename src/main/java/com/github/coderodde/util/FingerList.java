/*
 * The MIT License
 *
 * Copyright 2024 rodio.
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
package com.github.coderodde.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * This inner class implements the finger list data structure for managing list
 * fingers.
 *
 * @param <E> the list node item type.
 */
final class FingerList<E> {

    /**
     * The owner indexed linked list.
     */
    private final IndexedLinkedList<E> list;

    /**
     * This is also the minimum capacity.
     */
    private static final int INITIAL_CAPACITY = 8;

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
        StringBuilder sb = new StringBuilder().append("[");
        boolean first = true;

        for (int i = 0; i != size; i++) {
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
     * Adjusts the finger list after removing the first finger.
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
     * Appends the input finger to the tail of the finger list.
     *
     * @param finger the finger to append.
     */
    void appendFinger(Finger<E> finger) {
        size++;
        enlargeFingerArrayIfNeeded(size + 1);
        fingerArray[size] = fingerArray[size - 1];
        fingerArray[size - 1] = finger;
        fingerArray[size].index = list.size;
    }

    /**
     * Clears entirely this finger list. Only the end-of-finger-list finger
     * remains in the finger list. Not {@code private} since is used in the unit
     * tests.
     */
    void clear() {
        Arrays.fill(fingerArray, 0, size, null);
        fingerArray = new Finger[INITIAL_CAPACITY];
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
        // Can we contract at least once?
        if ((nextSize + 1) * 4 < fingerArray.length
                && fingerArray.length > 2 * INITIAL_CAPACITY) {

            int nextCapacity = fingerArray.length / 4;

            // Good, we can. But can we keep on splitting in half the 
            // capacity any further?
            while (nextCapacity >= 2 * (nextSize + 1)
                    && nextCapacity > INITIAL_CAPACITY) {
                // Yes, we can do it as well.
                nextCapacity /= 2;
            }

            fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
        }
    }

    /**
     * Enlarges the finger array so that it can accommodate
     * {@code requestedSize} fingers.
     *
     * @param requestedSize the requested size, including the end-of-finger-list
     * sentinel finger.
     */
    private void enlargeFingerArrayIfNeeded(int requestedSize) {
        int nextCapacity = fingerArray.length;

        while (requestedSize > nextCapacity) {
            nextCapacity *= 2;
        }

        if (nextCapacity != fingerArray.length) {
            fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
        }
    }
    
    /**
     * Returns {@code index}th finger.
     *
     * @param index the index of the target finger.
     * @return the {@code index}th finger.
     */
    Finger<E> get(int index) {
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
        return normalize(getFingerIndexImpl(elementIndex), elementIndex);
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
        int steps = finger.index - index;
        Node<E> node = finger.node;
        
        if (steps > 0) {
            for (int i = 0; i < steps; i++) {
                node = node.prev;
            }
        } else {
            for (int i = 0; i < -steps; i++) {
                node = node.next;
            }
        }

        return node;
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
                Node<E> node = a.node;

                for (int i = 0; i != leftDistance; i++) {
                    node = node.next;
                }

                return node;
            } else {
                Node<E> node = b.node;
                // TODO: Replace saveBIndex - elementIndex with rightDistance?
                for (int i = 0; i != rightDistance; i++) {
                    node = node.prev;
                }

                return node;
            }
        } else {
            // Here, the desired element is between c and b:
            int leftDistance = elementIndex - b.index;
            int rightDistance = c.index - elementIndex;

            if (leftDistance < rightDistance) {
                Node<E> node = b.node;

                for (int i = 0; i != leftDistance; i++) {
                    node = node.next;
                }

                return node;
            } else {
                Node<E> node = c.node;

                for (int i = 0; i != rightDistance; i++) {
                    node = node.prev;
                }

                return node;
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
            // the very first figner:
            int leftDistance = elementIndex;
            int rightDistance = nextAIndex - elementIndex;

            if (leftDistance < rightDistance) {
                Node<E> node = (Node<E>) list.head;

                for (int i = 0; i != elementIndex; i++) {
                    node = node.next;
                }

                return node;
            } else {
                Node<E> node = aNode;

                for (int i = 0; i != rightDistance; i++) {
                    node = node.prev;
                }

                return node;
            }
        } else {
            // Here, 'elementIndex >= nextAIndex':
            int leftDistance = elementIndex - nextAIndex;
            int rightDistance = b.index - elementIndex;

            if (leftDistance < rightDistance) {
                // Once here, rewind the node reference from aNode to the 
                // right:
                Node<E> node = aNode;

                for (int i = 0; i != leftDistance; i++) {
                    node = node.next;
                }

                return node;
            } else {
                // Once here, rewind the node refrence from b to the left:
                Node<E> node = b.node;

                for (int i = 0; i != rightDistance; i++) {
                    node = node.prev;
                }

                return node;
            }
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
                Node<E> node = a.node;

                for (int i = 0; i != leftDistance; i++) {
                    node = node.next;
                }

                return node;
            } else {
                Node<E> node = b.node;

                for (int i = 0; i != rightDistance; i++) {
                    node = node.prev;
                }

                return node;
            }
        } else {
            // Here, the desired element node is between 'b' and the tail 
            // node of the list:
            int leftDistance = elementIndex - nextBIndex;
            int rightDistance = list.size - elementIndex - 1;

            if (leftDistance < rightDistance) {
                // Once here, rewind the node reference from bNode to the
                // right:
                Node<E> node = bNode;

                for (int i = 0; i != leftDistance; i++) {
                    node = node.next;
                }

                return node;
            } else {
                // Once here, rewind the node reference from tail to the 
                // left:
                Node<E> node = list.tail;

                for (int i = 0; i != rightDistance; i++) {
                    node = node.prev;
                }

                return node;
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
        enlargeFingerArrayIfNeeded(size + 2);
        int beforeFingerIndex = getFingerIndexImpl(finger.index);
        System.arraycopy(
                fingerArray,
                beforeFingerIndex,
                fingerArray,
                beforeFingerIndex + 1,
                size + 1 - beforeFingerIndex);

        ++size;

        // Shift fingerArray[beforeFingerIndex + 1 ... size] one position to 
        // the right (towards larger index values):
        shiftFingerIndicesToRightOnce(beforeFingerIndex + 1);
        fingerArray[beforeFingerIndex] = finger;
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
        enlargeFingerArrayIfNeeded(size + 2);
        shiftFingerIndicesToRightOnce(0);
        System.arraycopy(fingerArray, 0, fingerArray, 1, size + 1);
        fingerArray[0] = finger;
        size++;
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
