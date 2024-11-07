package com.github.coderodde.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * <p>
 * This class implements the indexed, heuristic doubly-linked list data 
 * structure that runs all the single-element operations in expected
 * \(\mathcal{O}(\sqrt{n})\) time. Under the hood, the actual elements are
 * stored in a doubly-linked list. However, we also maintain a list of so-called 
 * <i>"fingers"</i> stored in a random access array. Each finger {@code F} 
 * contains two data fields:
 * <ul>
 *  <li>{@code F.node} - the actual element node,</li>
 *  <li>{@code F.index} - the appearance index of {@code F.node} in the actual 
 *       list.</li>
 * </ul>
 * 
 * <p>
 * 
 * For the list of size \(n\), we maintain 
 * \(\bigg \lceil \sqrt{n} \bigg \rceil + 1\) fingers. The rightmost finger in 
 * the finger list is a <i>special end-of-list sentinel</i>. It always has 
 * {@code F.node = null} and {@code F.index = } \(n\). The fingers are sorted by 
 * their indices. That arrangement allows simpler and faster code in the method 
 * that accesses a finger via element index; see 
 * {@link FingerList#getFingerIndexImpl(int)}. Since number of fingers is 
 * \(\sqrt{n}\), and assuming that the fingers are evenly distributed, each 
 * finger "covers" \(n / \sqrt{n} = \sqrt{n}\) elements. In order to access an 
 * element in the actual list, we first consult the finger list for the index 
 * {@code i} of the finger {@code fingerArray[i]} that is closest to the index 
 * of the target element. This runs in 
 * 
 * \[ 
 * \mathcal{O}(\log \sqrt{n}) = \mathcal{O}(\log n^{1/2}) = \mathcal{O}(\frac{1}{2} \log n) = \mathcal{O}(\log n).
 * \]
 * 
 * The rest is to <i>"rewind"</i> the closest finger to point to the target 
 * element (which requires \(\mathcal{O}(\sqrt{n})\) on evenly distributed 
 * finger lis). 
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.618033988 (Sep 30, 2023)
 * @since 1.6 (Sep 1, 2021)
 * @param <E> the element type.
 */
public class IndexedLinkedList<E> implements Deque<E>, 
                                             List<E>, 
                                             Cloneable, 
                                             java.io.Serializable {
    
    /**
     * This inner class implements the finger list data structure for managing
     * list fingers.
     * 
     * @param <E> the list node item type.
     */
    public final class FingerList<E> {

        /**
         * This is also the minimum capacity.
         */
        private static final int INITIAL_CAPACITY = 8;

        /** 
         * The actual list storage array.
         */
        Finger<E>[] fingerArray = new Finger[INITIAL_CAPACITY];
         
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
         * The number of fingers stored in the list. This field does not count
         * the end-of-list sentinel finger {@code F} for which 
         * {@code F.index = size}.
         */
        int size;
        
        /**
         * Constructs the empty finger list consisting only of end-of-list 
         * sentinel finger.
         */
        private FingerList() {
            fingerArray[0] = new Finger<>(null, 0);
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
            fingerArray[size].index = IndexedLinkedList.this.size;
        }

        /**
         * Clears entirely this finger list. Only the end-of-finger-list finger
         * remains in the finger list. Not {@code private} since is used in the
         * unit tests.
         */
        void clear() {
            Arrays.fill(fingerArray, 0, size, null);
            fingerArray = new Finger[INITIAL_CAPACITY];
            fingerArray[0] = new Finger<>(null, 0);
            size = 0;
        }
        
        /**
         * Returns {@code index}th finger.
         * 
         * @param index the index of the target finger.
         * @return the {@code index}th finger.
         */
        public Finger<E> get(int index) {
            return fingerArray[index];
        }
        
        /**
         * Returns the index of the finger that is closest to the 
         * {@code elementIndex}th list element.
         * 
         * @param elementIndex the target element index.
         * @return the index of the finger that is closest to the 
         *         {@code elementIndex}th element.
         */
        int getClosestFingerIndex(int elementIndex) {
            return normalize(getFingerIndexImpl(elementIndex), elementIndex);
        }

        /**
         * Returns the number of fingers in this finger list not counting the
         * end-of-finger-list finger.
         * 
         * @return the number of fingers in this finger list.
         */
        public int size() {
            return size;
        }
        
        /**
         * Adjusts the finger list after removing the first finger.
         */
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
            
            shiftFingerIndicesToLeftOnceAll(lastPrefixIndex);
        }
        
        /**
         * Contracts the finger array, if possible. The {@code nextSize} defines
         * the requested finger array size not counting the end-of-finger-list 
         * sentinel finger.
         * 
         * @param nextSize the requested size not counting the 
         *                 end-of-finger-list sentinel finger.
         */
        private void contractFingerArrayIfNeeded(int nextSize) {
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
         * @param requestedSize the requested size, including the 
         *                      end-of-finger-list sentinel finger.
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
         * Returns the finger index {@code i}, such that
         * {@code fingerArray[i].index} is no less than {@code elementIndex}, 
         * and {@code fingerArray[i].index} is closest to {@code elementIndex}. 
         * This algorithm is translated from
         * <a href="https://en.cppreference.com/w/cpp/algorithm/lower_bound">C++ <code>lower_bound</code> algorithm</a>.
         * 
         * @param elementIndex the target element index.
         * @return the index of the finger {@code f}, for which 
         *         {@code elementIndex <= f.index} and {@code f} is the leftmost 
         *         such finger. 
         */
        private int getFingerIndexImpl(int elementIndex) {
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
         * Inserts the input finger into the finger list such that the entire
         * finger list is sorted by indices.
         * 
         * @param finger the finger to insert.
         */
        private void insertFingerAndShiftOnceToRight(Finger<E> finger) {
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
         * @param fingerIndex   the finger index of the first finger in the 
         *                      shifted finger slice.
         * @param roomSize      the number of free spots requested.
         * @param numberOfNodes the shift amount of the moved fingers.
         */ 
        private void makeRoomAtIndex(int fingerIndex, 
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
         * Accesses the {@code elementIndex}th element sequentially. This method
         * is supposed to be used on very small indexed lists.
         * 
         * @param elementIndex the element index.
         * 
         * @return {@code elementIndex} the index of the element.
         */
        private Node<E> getNodeSequentially(int elementIndex) {
            return (Node<E>) IndexedLinkedList
                    .this
                    .getNodeSequentially(elementIndex);
        }
        
        /**
         * Normalizes the first finger and returns the {@code elementIndex}th
         * node.
         * 
         * @param elementIndex the index of the desired element.
         * 
         * @return the node corresponding to the {@code elementIndex}th 
         *         position. 
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
                int leftDistance  = elementIndex;
                int rightDistance = nextAIndex - elementIndex;
                
                if (leftDistance < rightDistance) {
                    Node<E> node = (Node<E>) IndexedLinkedList.this.head;
                    
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
                int leftDistance  = elementIndex - nextAIndex;
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
         * Returns the {@code elementIndex}th node and normalizes the last 
         * finger.
         * 
         * @param elementIndex the index of the desired element.
         * 
         * @return the {@code elementIndex}th node. 
         */
        private Node<E> getSuffixNode(int elementIndex) {
            Finger<E> a = fingerArray[fingerList.size - 2];
            Finger<E> b = fingerArray[fingerList.size - 1];
            Node<E> bNode = b.node;
            
            int saveBIndex = b.index;
            int nextBIndex = // TODO: Simplify.
                    a.index + (IndexedLinkedList.this.size - a.index) / 2;
            
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
                int leftDistance  = elementIndex - a.index;
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
                int leftDistance  = elementIndex - nextBIndex;
                int rightDistance = IndexedLinkedList.this.size - elementIndex;
                
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
                    Node<E> node = (Node<E>) IndexedLinkedList.this.tail;
                    
                    for (int i = 0; i != rightDistance; i++) {
                        node = node.prev;
                    }
                    
                    return node;
                }
            }
        }
        
        /**
         * Returns the {@code i}th node of this linked list. The closest finger 
         * is updated to point to the returned node.
         * 
         * @param elementIndex the element index.
         * @return the {@code index}th node in the linked list.
         */
        Node<E> getNode(int elementIndex) { 
            if (fingerList.size < 3) {
                // We need at least 3 fingers to do the actual trick:
                return getNodeSequentially(elementIndex);
            }
            
            int fingerIndex = getFingerIndexImpl(elementIndex);
            
            if (fingerIndex == 0) {
                // There is no required preceding finger:
                return getPrefixNode(elementIndex);
            } 
            
            if (fingerIndex >= fingerList.size - 1) {
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
                int leftDistance  = elementIndex - a.index;
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
                int leftDistance  = elementIndex - b.index;
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
         * Makes sure that the returned finger index {@code i} points to the 
         * closest finger in the finger array.
         * 
         * @param fingerIndex  the finger index.
         * @param elementIndex the element index.
         * 
         * @return the index of the finger that is closest to the 
         *         {@code elementIndex}th element.
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
         * Creates a finger for the input node {@code node} and inserts it at 
         * the head of the finger array.
         * 
         * @param node the target node.
         */
        private void prependFingerForNode(Node<E> node) {
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
        private void removeFinger() {
            contractFingerArrayIfNeeded(--size);
            fingerArray[size] = fingerArray[size + 1];
            fingerArray[size + 1] = null;
            fingerArray[size].index = IndexedLinkedList.this.size;
        }
        
        /**
         * Removes the finger range {@code [prefixSize, size - suffixSize]}
         * from the finger list.
         * 
         * @param prefixSize    the length of the prefix that remains in this
         *                      finger list.
         * @param suffixSize    the length of the suffix that remains in this
         *                      finger list.
         * @param nodesToRemove the number of nodes that are to be removed.
         */
        private void removeFingerRange(int prefixSize, 
                                       int suffixSize,
                                       int nodesToRemove) {
            
            int fingersToRemove = size - prefixSize - suffixSize;
            
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
                        Math.min(fingerArray.length, 
                                 size + 1 + fingersToRemove),
                        null);
        }
        
        /**
         * Sets the finger {@code finger} to the finger array at index 
         * {@code index}.
         * 
         * @param index  the index of the finger list component.
         * @param finger the target finger to set.
         */
        private void setFinger(int index, Finger<E> finger) {
            fingerArray[index] = finger;
        }

        /**
         * Moves all the fingers in range {@code [startFingerIndex, size]} 
         * {@code shiftLength} positions to the left (towards smaller indices).
         * 
         * @param startFingerIndex the index of the leftmost finger to shift.
         * @param shiftLength      the length of the shift operation.
         */
        private void shiftFingerIndicesToLeft(int startFingerIndex,      
                                              int shiftLength) {
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
        private void shiftFingerIndicesToLeftOnceAll(int startFingerIndex) {
            for (int i = startFingerIndex; i <= size; ++i) {
                fingerArray[i].index--;
            }
        }
        
        /**
         * Moves all the fingers in range {@code [startFingerIndex, size]} 
         * {@code shiftLength} positions to the right (towards larger indices).
         * 
         * @param startIndex  the index of the leftmost finger to shift.
         * @param shiftLength the length of the shift operation.
         */ 
        private void shiftFingerIndicesToRight(int startIndex,      
                                               int shiftLength) {
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
        private void shiftFingerIndicesToRightOnce(int startIndex) {
            shiftFingerIndicesToRight(startIndex, 1);
        }
    }
    
    /**
     * This static inner class implements the actual linked list node.
     * 
     * @param <E> the type of the satellite data.
     */
    static final class Node<E> {
    
        /**
         * The actual satellite datum.
         */
        E item;
        
        /**
         * The previous node or {@code null} if this {@link Node} is the head of
         * the list.
         */
        Node<E> prev;
        
        /**
         * The next node or {@code null} if this {@link Node} is the tail of the
         * list.
         */
        Node<E> next;

        /**
         * Constructs a new {@link Node} object.
         * 
         * @param item the satellite datum of the newly created {@link Node}.
         */
        Node(E item) {
            this.item = item;
        }

        /**
         * Returns the textual representation of this {@link Node}.
         * 
         * @return the textual representation.
         */
        @Override
        public String toString() {
            return "[Node; item = " + item + "]";
        }
    }
    
    /**
     * This static inner class implements the actual finger.
     * 
     * @param <E> the type of the list's satellite data.
     */
    public static final class Finger<E> {
 
        /**
         * The pointed to {@link Node}.
         */
        Node<E> node;
        
        /**
         * The index at which the {@code node} appears in the list.
         */
        int index;

        /**
         * Constructs a new {@link Finger}.
         * 
         * @param node  the pointed node.
         * @param index the index of {@code node} in the actual list.
         */
        Finger(Node<E> node, int index) {
            this.node = node;
            this.index = index;
        }
        
        /**
         * Copy constructs this finger.
         * 
         * @param finger the finger whose state to copy.
         */
        Finger(Finger<E> finger) {
            this.node = finger.node;
            this.index = finger.index;
        }
        
        /**
         * Returns the index of this finger. Used for research.
         * 
         * @return the index of this finger.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns the textual representation of this finger.
         * 
         * @return the textual representation.
         */
        @Override
        public String toString() {
            return "[Finger; index = " + index + 
                    ", item = " + ((node == null) ? "null" : node.item) + 
                    "]";
        }
    }
    
    /**
     * The serial version UID.
     */
    @java.io.Serial
    private static final long serialVersionUID = 54170828611556733L;
    
    /**
     * The cached number of elements in this list.
     */
    int size;
    
    /**
     * The modification counter. Used to detect state changes during concurrent
     * modifications.
     */
    transient int modCount;
    
    /**
     * The head node of the list.
     */
    transient Node<E> head;
    
    /**
     * The tail node of the list.
     */
    transient Node<E> tail;
    
    /**
     * The actual finger list. Without {@code private} keyword since it is 
     * accessed in unit tests.
     */
    transient FingerList<E> fingerList;
    
    /**
     * Constructs an empty list.
     */
    public IndexedLinkedList() {
        this.fingerList = new FingerList<>();
    }
    
    /**
     * Constructs a new list and copies the data in {@code c} to it. Runs in
     * \(\mathcal{O}(m + \sqrt{m})\) time, where \(m = |c|\).
     * 
     * @param c the collection to copy. 
     */
    public IndexedLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }
    
    /**
     * Appends the specified element to the end of this list. Runs in amortized 
     * constant time.
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
     * {@code F} is <i>affected</i>, if {@code F.index >= index}. Runs in
     * \(\mathcal{O}(\sqrt{\mathrm{size}})\) time.
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws IndexOutOfBoundsException if index is outside of the valid range.
     */
    @Override
    public void add(int index, E element) {
        checkPositionIndex(index);
        
        if (index == size) { // Check push-back first as it is used more often.
            linkLast(element);
        } else if (index == 0) {
            linkFirst(element);
        } else {
            linkBefore(element, index, node(index));
        }
    }
    
    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order they are returned by the specified collection's
     * iterator.  The behavior of this operation is undefined if the specified 
     * collection is modified while the operation is in progress. (Note that 
     * this will occur if the specified collection is this list, and it's
     * nonempty.) Runs in \(\mathcal{O}(m + \sqrt{m + n} - \sqrt{n})\), where
     * \(m = |c|\) and \(n\) is the size of this list.
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
     * {@code F.index >= index} will increment {@code F.index} by 1. Runs in 
     * \(\Theta(m + \sqrt{m + n} - \sqrt{n}) + \mathcal{O}(\sqrt{n})\), where 
     * \(m = |c|\) and \(n\) is the size of this list.
     *
     * @param index index at which to insert the first element from the
     *              specified collection.
     * @param c collection containing elements to be added to this list.
     * @return {@code true} if this list changed as a result of the call.
     * @throws IndexOutOfBoundsException if the index is outside of the valid
     *                                   range.
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
        } else if (index == size) {
            appendAll(c);
        } else if (index == 0) {
            prependAll(c);
        } else {
            insertAll(c, node(index), index);
        }
        
        return true;
    }
    
    /**
     * Adds the element {@code e} before the head of this list. Runs in 
     * \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @param e the element to add.
     */
    @Override
    public void addFirst(E e) {
        linkFirst(e);
    }
    
    /**
     * Adds the element {@code e} after the tail of this list. Runs in constant
     * time.
     * 
     * @param e the element to add.
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
     * where {@code i = F.index}.</li>
     * </ol>
     * Runs always in linear time.
     */
    public void checkInvarant() {
        if (fingerList.get(0).index < 0) {
            throw new IllegalStateException(
                    "First finger index is negative: "
                            +  fingerList.get(0).index);
        }
        
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
        
        if (sentinelFinger.index != this.size) {
            throw new IllegalStateException(
                    "sentinelFinger.index != this.size. (" 
                            + sentinelFinger.index 
                            + " != " 
                            + this.size 
                            + ")");
        }
        
        if (sentinelFinger.node != null) {
            throw new IllegalStateException(
                    "sentinelFigner.node != null: " + sentinelFinger);
        }
        
        Finger<E> finger = fingerList.get(0);
        Node<E> node = head;
        int fingerCount = 0;
        int tentativeSize = 0;
        
        while (node != null) {
            tentativeSize++;
            
            if (finger.node == node) {
                finger = fingerList.get(++fingerCount);
                
                if (finger == null) {
                    throw new IllegalStateException("figner == null");
                }
            }
            
            node = node.next;
        }
        
        if (size != tentativeSize) {
            throw new IllegalStateException(
                    "Number of nodes mismatch: size = " 
                            + size 
                            + ", tentativeSize = " 
                            + tentativeSize);
        }
        
        if (fingerList.size() != fingerCount) {
            throw new IllegalStateException(
                    "Number of fingers mismatch: fingerList.size() = " 
                            + fingerList.size() 
                            + ", fingerCount = " 
                            + fingerCount);
        }
    }
    
    /**
     * Completely clears this list.
     */
    @Override
    public void clear() {
        fingerList.clear();
        size = 0;
        
        // Help GC:
        for (Node<E> node = head; node != null;) {
            node.prev = null;
            node.item = null;
            Node<E> next = node.next;
            node.next = null;
            node = next;
        }

        head = tail = null;
        modCount++;
    }
    
    /**
     * Returns a clone list with same content as this list.
     * 
     * @return the clone list.
     */
    @Override
    public Object clone() {
        return new IndexedLinkedList<>(this);
    }
    
    /**
     * Returns {@code true} only if {@code o} is present in this list. Runs in
     * worst-case linear time.
     * 
     * @param o the query object.
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    
    /**
     * Returns {@code true} only if this list contains all the elements 
     * mentioned in the {@code c}. Runs in Runs in \(\mathcal{O}(mn)\) time, 
     * where \(m = |c|\).
     * 
     * @param c the query object collection.
     * @return {@code true} only if  this list contains all the elements in
     *         {@code c}, or {@code false} otherwise.
     */
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
     * Returns a deep copy of this list that mimics both the linked list and the
     * finger list. (Used for unit testing. Normal usage of the class should not
     * rely on this method.)
     * 
     * @return a deep copy of this list.
     */
    public IndexedLinkedList<E> deepCopy() {
        IndexedLinkedList<E> other = new IndexedLinkedList<>(this);
        int fingerIndex = 0;
        
        for (int i = 0; i <= this.fingerList.size; i++) {
            Finger<E> fingerCopy = new Finger<>(this.fingerList.fingerArray[i]);
            fingerCopy.node = this.getNodeSequentially(fingerCopy.index);
            other.fingerList.fingerArray[fingerIndex++] = fingerCopy;
        }
        
        return other;
    }
    
    /**
     * Packs all the fingers to beginning of this list. Used for research.
     */
    public void deoptimize() {
        Node<E> node = head;
        
        for (int i = 0; i < fingerList.size(); i++) {
            fingerList.get(i).index = i;
            fingerList.get(i).node = node;
            node = node.next;
        }
    }
    
    /**
     * Returns the descending iterator.
     * 
     * @return the descending iterator pointing to the tail of this list.
     */
    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }
    
    /**
     * Returns the first element of this list. Runs in constant time.
     * 
     * @return the first element of this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E element() {
        return getFirst();
    }
    
    /**
     * Returns {@code true} only if {@code o} is an instance of 
     * {@link java.util.List} and has the sane contents as this list. Runs in
     * worst-case linear time.
     * 
     * @return {@code true} only if {@code o} is a list with the same contents
     *         as this list.
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
     * Returns {@code index}th element. Runs in the worst-case 
     * \(\mathcal{O}(\sqrt{n})\) time, but may run in \(\mathcal{O}(\sqrt{n})\)
     * if the entropy of this list is high.
     * 
     * @return {@code index}th element.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         {@code 0, 1, ..., size - 1}, or if this list is empty.
     */
    @Override
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
    
    /**
     * Computes and returns the entropy of this list, which is defined as
     * \[
     * H = 1 - \frac{1}{N} \sum_{i = 0}^{n - 1} \Bigg|f_{i + 1} - f_i - \sqrt{N}\Bigg|. 
     * \]
     * The larger the entropy, the faster the single-element operations should
     * run.
     * 
     * @return the entropy of this list.
     */
    public double getEntropy() {
        double sum = 0.0;
        
        for (int i = 0; i < fingerList.size(); i++) {
            double value = fingerList.get(i + 1).index 
                         - fingerList.get(i).index 
                         - fingerList.size();
            
            value = Math.abs(value);
            sum += value;
        }
        
        sum /= size;
        return Math.max(0.0, 1.0 - sum);
    }
            
    /**
     * TODO: Remove.
     * Returns the finger list.
     * 
     * @return the finger list.
     */
    public FingerList<E> getFingerList() {
        return fingerList;
    }
    
    /**
     * Returns the first element of this list. Runs in constant time.
     * 
     * @return the first element of this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E getFirst() {
        if (head == null) {
            throw new NoSuchElementException(
                    "Getting the head element from an empty list.");
        }
        
        return head.item;
    }
    
    /**
     * Returns the last element of this list. Runs in constant time.
     * 
     * @return the last element of this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E getLast() {
        if (tail == null) {
            throw new NoSuchElementException(
                    "Getting the tail element from an empty list.");
        }
        
        return tail.item;
    }
    
    /**
     * Returns the hash code of this list. Runs in linear time.
     * 
     * @return the hash code of this list.
     */
    @Override
    public int hashCode() {
        int expectedModCount = modCount;
        int hash = hashCodeRange(0, size);
        checkForComodification(expectedModCount);
        return hash;
    }
    
    /**
     * Returns the index of the leftmost {@code obj}, or {@code -1} if 
     * {@code obj} does not appear in this list. Runs in worst-case linear time.
     * 
     * @param obj the object to search.
     * 
     * @return the index of the leftmost {@code obj}, or {@code -1} if 
     *         {@code obj} does not appear in this list.
     * 
     * @see IndexedLinkedList#lastIndexOf(java.lang.Object) 
     */
    @Override
    public int indexOf(Object obj) {
        return indexOfRange(obj, 0, size);
    }
    
    /**
     * Returns {@code true} only if this list is empty.
     * 
     * @return {@code true} only if this list is empty.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the iterator over this list.
     * 
     * @return the iterator over this list.
     */
    @Override
    public Iterator<E> iterator() {
        return new BasicIterator();
    }

    /**
     * Returns the index of the rightmost {@code obj}, or {@code -1} if 
     * {@code obj} does not appear in this list. Runs in worst-case linear time.
     * 
     * @param obj the object to search.
     * 
     * @return the index of the rightmost {@code obj}, or {@code -1} if
     *         {@code obj} does not appear in this list.
     * 
     * @see IndexedLinkedList#lastIndexOf(java.lang.Object) 
     */
    @Override
    public int lastIndexOf(Object obj) {
        return lastIndexOfRange(obj, 0, size);
    }
    
    /**
     * Returns the list iterator pointing to the head element of this list.
     * 
     * @return the list iterator.
     * @see java.util.ListIterator
     */
    @Override
    public ListIterator<E> listIterator() {
        return new EnhancedIterator(0);
    }
    
    /**
     * Returns the list iterator pointing between {@code list[index - 1]} and
     * {@code list[index]}.
     * 
     * @param index the gap index. The value of zero will point before the head 
     *              element.
     * 
     * @return the list iterator pointing to the {@code index}th gap.
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return new EnhancedIterator(index);
    }
    
    /**
     * Adds {@code e} after the tail element of this list. Runs in constant 
     * time.
     * 
     * @param e the element to add.
     * @return always {@code true}.
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    /**
     * Adds {@code e} before the head element of this list. Runs in 
     * \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @param e the element to add.
     * @return always {@code true}.
     */
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * Adds {@code e} after the tail element of this list. Runs in constant 
     * time.
     * 
     * @param e the element to add.
     * @return always {@code true}.
     */
    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }
    
    /**
     * Moves all the fingers such that they are evenly distributed. Runs in 
     * linear time.
     */
    public void optimize() {
        distributeAllFingers();
    }
    
    /**
     * Takes a look at the first element in this list.
     * 
     * @return the head element or {@code null} if this list is empty.
     */
    @Override
    public E peek() {
        return head == null ? null : head.item;
    }

    /**
     * Takes a look at the first element in this list.
     * 
     * @return the head element or {@code null} if this list is empty.
     */
    @Override
    public E peekFirst() {
        return head == null ? null : head.item;
    }

    /**
     * Takes a look at the last element in this list.
     * 
     * @return the tail element or {@code null} if this list is empty.
     */
    @Override
    public E peekLast() {
        return tail == null ? null : tail.item;
    }
    
    /**
     * If this list is empty, does nothing else but return {@code null}. 
     * Otherwise, removes the first element and returns it. Runs in 
     * \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @return the first element (which was removed due to the call to this 
     *         method), or {@code null} if the list is empty.
     */
    @Override
    public E poll() {
        return head == null ? null : removeFirst();
    }

    /**
     * If this list is empty, does nothing else but return {@code null}. 
     * Otherwise, removes the first element and returns it. Runs in 
     * \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @return the first element (which was removed due to the call to this 
     *         method), or {@code null} if the list is empty.
     */
    @Override
    public E pollFirst() {
        return head == null ? null : removeFirst();
    }

    /**
     * If this list is empty, does nothing else but return {@code null}. 
     * Otherwise, removes the last element and returns it. Runs in constant 
     * time.
     * 
     * @return the last element (which was removed due to the call to this 
     *         method), or {@code null} if the list is empty.
     */
    @Override
    public E pollLast() {
        return tail == null ? null : removeLast();
    }
    
    /**
     * Removes the first element and returns it.
     * Runs in \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @return the first element.
     * @throws NoSuchElementException if the list is empty.
     */
    @Override
    public E pop() {
        return removeFirst();
    }

    /**
     * Adds {@code e} before the head of this list. Runs in 
     * \(\mathcal{O}(\sqrt{n})\) time.
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }
    
    /**
     * Randomizes the fingers. Used primarily for research. Uses the current 
     * milliseconds timestamp as the seed.
     */
    public void randomizeFingers() {
        randomizeFingers(System.currentTimeMillis());
    }

    /**
     * Randomizes the fingers. Used primarily for research.
     * 
     * @param seed the random seed.
     */
    public void randomizeFingers(long seed) {
        randomizeFingers(new Random(seed));
        
    }
    
    /**
     * Randomizes the fingers. Used primarily for research.
     * 
     * @param random the random number generator object.
     */
    public void randomizeFingers(Random random) {
        final Set<Integer> indexFilter = new HashSet<>();
        
        while (indexFilter.size() < fingerList.size) {
            indexFilter.add(random.nextInt(size));
        }
        
        Integer[] newFingerIndexArray = new Integer[fingerList.size];
        newFingerIndexArray = indexFilter.toArray(newFingerIndexArray);
        Arrays.sort(newFingerIndexArray);
        
        for (int i = 0; i < fingerList.size; i++) {
            Finger finger = fingerList.fingerArray[i];
            finger.index = newFingerIndexArray[i];
            finger.node = getNodeSequentially(finger.index);
        }
    }
    
    /**
     * Removes and returns the first element. Runs in 
     * \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @return the head element of this list.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E remove() {
        return removeFirst();
    }

    /**
     * Removes the leftmost occurrence of {@code o} in this list. Runs in worst-
     * case \(\mathcal{O}(n + \sqrt{n})\) time. \(\mathcal{O}(n)\) for iterating 
     * the list and \(\mathcal{O}(\sqrt{n})\) time for fixing the fingers.
     * 
     * @param o the object to remove.
     * 
     * @return {@code true} only if {@code o} was located in this list and, 
     *         thus, removed.
     */
    @Override
    public boolean remove(Object o) {
        int index = 0;

        for (Node<E> x = head; x != null; x = x.next, index++) {
            if (Objects.equals(o, x.item)) {
                removeObjectImpl(x, index);
                return true;
            }
        }

        return false;
    }
    
    /**
     * Removes the element residing at the given index. Runs in worst-case
     * \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @param index the index of the element to remove.
     * @return the removed element. (The one that resided at the index 
     *         {@code index}.)
    */
    @Override
    public E remove(int index) {
        checkElementIndex(index);
        
        int closestFingerIndex = fingerList.getClosestFingerIndex(index);
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
            
            fingerList.shiftFingerIndicesToLeftOnceAll(closestFingerIndex + 1);
            
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
    
    /**
     * Removes from this list all the elements mentioned in {@code c}. Runs in
     * \(\mathcal{O}(n\sqrt{n} + fn)\) time, where \(\mathcal{O}(f)\) is the 
     * time of checking for element inclusion in {@code c}.
     * 
     * @param c the collection holding all the elements to remove.
     * @return {@code true} only if at least one element in {@code c} was 
     *         located and removed from this list.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return batchRemove(c, true, 0, size);
    }

    /**
     * Removes the first element from this list. Runs in 
     * \(\mathcal{O}(\sqrt{n})\) time.
     * 
     * @return the first element.
     */
    @Override
    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException(
                    "removeFirst from an empty IndexedLinkedList");
        }
        
        return removeFirstImpl();
    }
    
    /**
     * Removes the leftmost occurrence of {@code o}. Runs in worst-case 
     * \(\mathcal{O}(n)\) time.
     * 
     * @return {@code true} only if {@code o} was present in the list and was 
     *         successfully removed.
     */
    @Override
    public boolean removeFirstOccurrence(Object o) {
        int index = 0;
        
        for (Node<E> x = head; x != null; x = x.next, index++) {
            if (Objects.equals(o, x.item)) {
                removeObjectImpl(x, index);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Removes from this list all the elements that satisfy the given input
     * predicate. Runs in \(\mathcal{O}(n\sqrt{n})\) time.
     * 
     * @param filter the filtering predicate.
     * @return {@code true} only if at least one element was removed.
     */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return removeIf(filter, 0, size);
    }
    
    /**
     * Removes and returns the last element of this list. Runs in constant time.
     * 
     * @return the removed head element.
     * @throws NoSuchElementException if this list is empty.
     */
    @Override
    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException(
                    "removeLast on empty IndexedLinkedList");
        }
        
        return removeLastImpl();
    }

    /**
     * Removes the rightmost occurrence of {@code o}. Runs in 
     * \(\mathcal{O}(n)\) time.
     * 
     * @param o the object to remove.
     * @return {@code true} only if an element was actually removed.
     */
    @Override
    public boolean removeLastOccurrence(Object o) {
        int index = size - 1;

        for (Node<E> x = tail; x != null; x = x.prev, index--) {
            if (Objects.equals(o, x.item)) {
                removeObjectImpl(x, index);
                return true;
            }
        }

        return false;
    }

    /**
     * Replaces all the elements in this list by applying the given input 
     * operator to each of the elements. Runs in linear time.
     * 
     * @param operator the operator mapping one element to another. 
     */
    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        replaceAllRange(operator, 0, size);
        modCount++;
    }
    
    /**
     * Remove all the elements that <strong>do not</strong> appear in 
     * {@code c}. Runs in worst-case \(\mathcal{O}(nf + n\sqrt{n})\) time, where
     * the inclusion check in {@code c} is run in \(\mathcal{O}(f)\) time.
     * 
     * @param c the collection of elements to retain.
     * @return {@code true} only if at least one element was removed.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return batchRemove(c, false, 0, size);
    }
    
    /**
     * Sets the element at index {@code index} to {@code element} and returns
     * the old element. Runs in worst-case \(\mathcal{O}(\sqrt{n})\) time.
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
     * Returns the number of elements in this list.
     * 
     * @return the size of this list.
     */
    @Override
    public int size() {
        return size;
    }
    
    /**
     * Sorts stably this list into non-descending order. Runs in 
     * \(\mathcal{O}(n \log n)\).
     * 
     * @param c the element comparator.
     */
    @Override
    public void sort(Comparator<? super E> c) {
        if (size == 0) {
            return;
        }
        
        Object[] array = toArray();
        Arrays.sort((E[]) array, c);
        
        Node<E> node = head;
        
        // Rearrange the items over the linked list nodes:
        for (int i = 0; i < array.length; ++i, node = node.next) {
            E item = (E) array[i];
            node.item = item;
        }
        
        distributeAllFingers();
        modCount++;
    }
    
    /**
     * Returns the spliterator over this list.
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LinkedListSpliterator<>(this, head, size, 0, modCount);
    }
    
    /**
     * Returns a sublist view 
     * {@code list[fromIndex, fromIndex + 1, ..., toIndex - 1]}.
     * 
     * @param fromIndex the smallest index, inclusive.
     * @param toIndex the largest index, exclusive.
     * @return the sublist view.
     */
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new EnhancedSubList(this, fromIndex, toIndex);
    }
    
    /**
     * Returns the {@link Object} array containing all the elements in this 
     * list, in the same order as they appear in the list.
     * 
     * @return the list contents in an {@link Object} array.
     */
    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int index = 0;
        
        for (Node<E> node = head; node != null; node = node.next) {
            arr[index++] = node.item;
        }
        
        return arr;
    }
    
    /**
     * Generates the array containing all the elements in this list.
     * 
     * @param <T>       the array component type.
     * @param generator the generator function.
     * @return the list contents in an array with component type of {@code T}.
     */
    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return toArray(generator.apply(size));
    }
    
    /**
     * If {@code a} is sufficiently large, returns the same array holding all
     * the contents of this list. Also, if {@code a} is larger than the input
     * array, sets {@code a[s] = null}, where {@code s} is the size of this
     * list. However, if {@code a} is smaller than this list, allocates a new
     * array of the same length, populates it with the list contents and returns
     * it.
     * 
     * @param <T> the element type.
     * @param a the input array.
     * @return an array holding the contents of this list.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        
        int index = 0;
        
        for (Node<E> node = head; node != null; node = node.next) {
            a[index++] = (T) node.item;
        }
        
        if (a.length > size) {
            a[size] = null;
        }
        
        return a;
    }
    
    /**
     * Returns the string representation of this list, listing all the elements.
     * 
     * @return the string representation of this list.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        
        boolean firstIteration = true;
        
        for (E element : this) {
            if (firstIteration) {
                firstIteration = false;
            } else {
                stringBuilder.append(", ");
            }
            
            stringBuilder.append(element);
        }
        
        return stringBuilder.append("]").toString();
    }

    /**
     * Returns the number of fingers in the finger list. Does not count the 
     * end-of-finger-list sentinel finger. Used in unit tests.
     * 
     * @return the size of the finger list.
     */
    int getFingerListSize() {
        return fingerList.size();
    }
    
    /**
     * Computes the recommended number of fingers for {@code size} elements. 
     * Equals \(\Bigg\lceil \sqrt{N} \Bigg\rceil\), where \(N = \) {@code size}.
     * 
     * @param size the size for which we want to compute the recommended number
     *             of fingers.
     * 
     * @return the recommended number of fingers.
     */
    private static int getRecommendedNumberOfFingers(int size) {
        return (int) Math.ceil(Math.sqrt(size));
    }
    
    /**
     * Checks the indices for a list of size {@code size}.
     * 
     * @param fromIndex the starting, inclusive index.
     * @param toIndex   the ending, exclusive index.
     * @param size      the size of the target list.
     */
    static void subListRangeCheck(int fromIndex, 
                                  int toIndex, 
                                  int size) {
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
    
    /**
     * Adds the fingers for the range just appended.
     * 
     * @param first          the first node of the added collection.
     * @param firstIndex     the index of {@code first}.
     * @param collectionSize the size of the added collection.
     */
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
        
        int fingerIndex = fingerList.size();

        fingerList.makeRoomAtIndex(fingerIndex, 
                                   numberOfNewFingers, 
                                   collectionSize);

        int distance = collectionSize / numberOfNewFingers;
        int nodesToSkip = distance / 2;
        int index = firstIndex + nodesToSkip;
        
        spreadFingers(first, 
                      numberOfNewFingers, 
                      index, 
                      distance,
                      fingerIndex);
    }
    
    /**
     * Adds fingers after inserting a collection in this list.
     * 
     * @param headNodeOfInsertedRange  the head node of the inserted range.
     * @param indexOfInsertedRangeHead the index of 
     *                                 {@code headNodeOfInsertedRange}.
     * @param collectionSize           the size of the inserted collection.
     */
    private void addFingersAfterInsertAll(Node<E> headNodeOfInsertedRange,
                                          int indexOfInsertedRangeHead,
                                          int collectionSize) {
        int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerList.size();

        if (numberOfNewFingers == 0) {
            int fingerIndex = 
                    fingerList.getFingerIndexImpl(indexOfInsertedRangeHead);
            
            fingerList.shiftFingerIndicesToRight(fingerIndex, collectionSize);
            return;
        }

        int startFingerIndex =
                fingerList.getFingerIndexImpl(indexOfInsertedRangeHead);
        
        fingerList.makeRoomAtIndex(startFingerIndex, 
                                   numberOfNewFingers, 
                                   collectionSize);
        
        int distance = collectionSize / numberOfNewFingers;
        int startOffset = distance / 2;
        int index = indexOfInsertedRangeHead + startOffset;
        
        spreadFingers(headNodeOfInsertedRange,
                      numberOfNewFingers,
                      index,
                      distance,
                      startFingerIndex);
    }
    
    /**
     * Adds fingers after prepending a collection to this list.
     * 
     * @param collectionSize the size of the prepended collection.
     */
    private void addFingersAfterPrependAll(int collectionSize) {
        int numberOfNewFingers =
                getRecommendedNumberOfFingers() - fingerList.size();

        if (numberOfNewFingers == 0) {
            fingerList.shiftFingerIndicesToRight(0, collectionSize);
            return;
        }
        
        fingerList.makeRoomAtIndex(0, numberOfNewFingers, collectionSize);

        int distance = collectionSize / numberOfNewFingers;
        int startIndex = distance / 2;
        
        spreadFingers(head, 
                      numberOfNewFingers,
                      startIndex, 
                      distance, 
                      0);
    }
    
    /**
     * Adds fingers after setting a collection as a list.
     * 
     * @param collectionSize the size of the collection being set.
     */
    private void addFingersAfterSetAll(int collectionSize) {
        int numberOfNewFingers = getRecommendedNumberOfFingers();
        
        fingerList.makeRoomAtIndex(0,
                                   numberOfNewFingers,
                                   collectionSize);
        
        int distance = size / numberOfNewFingers;
        int startIndex = distance / 2;
        
        spreadFingers(head,
                      numberOfNewFingers,
                      startIndex, 
                      distance,
                      0);
    }
    
    /**
     * Appends the input collection to the tail of this list.
     * 
     * @param c the collection to append.
     */
    private void appendAll(Collection<? extends E> c) {
        Node<E> prev = tail;
        Node<E> oldLast = tail;

        for (E item : c) {
            Node<E> newNode = new Node<>(item);
            newNode.prev = prev;
            prev.next = newNode;
            prev = newNode;
        }

        tail = prev;
        int sz = c.size();
        size += sz;
        modCount++;
        addFingersAfterAppendAll(oldLast.next, size - sz, sz);
    }
    
    /**
     * Adds the finger to the tail of the finger list and before the 
     * end-of-finger-list sentinel.
     * 
     * @param node  the node of the newly created finger.
     * @param index the index of {@code node}.
     */
    private void appendFinger(Node<E> node, int index) {
        Finger<E> finger = new Finger<>(node, index);
        fingerList.appendFinger(finger);
    }
    
    /**
     * This class implements a basic iterator over this list.
     */
    public final class BasicIterator implements Iterator<E> {
        
        /**
         * Caches the most recently returned node.
         */
        private Node<E> lastReturned;
        
        /**
         * Caches the next node to iterate over.
         */
        private Node<E> next = head;
        
        /**
         * The index of the next node to iterate over.
         */
        private int nextIndex;
        
        /**
         * Caches the expected modification count. We use this value in order to
         * detect the concurrent modifications as early as possible.
         */
        int expectedModCount = IndexedLinkedList.this.modCount;

        /**
         * Constructs the basic iterator pointing to the first element.
         */
        BasicIterator() {
            
        }
        
        /**
         * Returns {@code true} if and only if this iterator has more elements 
         * to offer.
         * 
         * @return {@code true} if and only if iteration may continue.
         */
        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        /**
         * Attempts to return the next element in the iteration order.
         * 
         * @return the next element.
         * @throws ConcurrentModificationException if the list was modified 
         *                                         outside the iterator API. 
         */
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

        /**
         * Removes the most recently iterated element from the list.
         * 
         * @throws IllegalStateException if there is no most recent element. 
         *                               This can happen if there was no 
         *                               {@link DescendingIterator#next()} 
         *                               on this iterator, or the previous 
         *                               operation was
         *                               {@link DescendingIterator#remove()}.
         */
        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            
            checkForComodification();
            
            int removalIndex = nextIndex - 1;
            removeObjectImpl(lastReturned, removalIndex);
            nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }

        /**
         * Runs {@code action} on every remaining elements.
         * 
         * @param action the action to perform on each remaining element.
         * @throws ConcurrentModificationException if the list was modified 
         *                                         outside the iterator API.
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                next = next.next;
                nextIndex++;
            }
            
            checkForComodification();
        }

        /**
         * Makes sure that the list was not modified outside of the iterator API
         * while iterating.
         */
        void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    
    /**
     * Implements the batch remove. If {@code complement} is {@code true}, this 
     * operation removes all the elements appearing in {@code c}. Otherwise, it 
     * will retain all the elements present in {@code c}.
     * 
     * @param c          the target collection to operate on.
     * @param complement the operation choice flag.
     * @param from       the starting, inclusive index of the range to consider.
     * @param end        the ending, exclusive index of the range to consider.
     * 
     * @return {@code true} if and only if the list was modified.
     */
    boolean batchRemove(Collection<?> c,
                        boolean complement,
                        int from,  
                        int end) {
        Objects.requireNonNull(c);
        
        if (c.isEmpty()) {
            return false;
        }
        
        boolean modified = false;
        
        int numberOfNodesToIterate = end - from;
        int i = 0;
        int nodeIndex = from;
        
        for (Node<E> node = node(from); i < numberOfNodesToIterate; ++i) {
            Node<E> nextNode = node.next;
            
            if (c.contains(node.item) == complement) {
                modified = true;
                removeObjectImpl(node, nodeIndex);
            } else {
                nodeIndex++;
            }
            
            node = nextNode;
        }
        
        return modified;
    }
    
    /**
     * Checks the element index. In the case of non-empty list, valid indices 
     * are {@code { 0, 1, ..., size - 1 }}.
     * 
     * @param index the index to validate.
     * @throws IndexOutOfBoundsException if the {@code index} is not a valid 
     *                                   list index.
     */
    private void checkElementIndex(int index) {
        if (!isElementIndex(index)) {
            throw new IndexOutOfBoundsException(getOutOfBoundsMessage(index));
        }
    }
    
    /**
     * Checks that the input {@code expectedModCount} equals the list's 
     * internal, cached modification count.
     * 
     * @param expectedModCount the modification count to check.
     * @throws ConcurrentModificationException if the cached and the input 
     *                                         modification counts differ.
     */
    void checkForComodification(int expectedModCount) {
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
    
    /**
     * Checks that the input index is a valid position index for 
     * {@link #add(int, java.lang.Object)} operation or iterator position. In 
     * other words, checks that {@code index} is in the set 
     * {@code {0, 1, ..., size}}.
     * 
     * @param index the index to validate.  
     */
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index)) {
            throw new IndexOutOfBoundsException(getOutOfBoundsMessage(index));
        }
    }
    
    /**
     * Decreases the size counter and increments the modification count.
     */
    private void decreaseSize() {
        size--;
        modCount++;
    }   
    
    /**
     * Distributes the fingers over the element list
     * {@code [fromIndex, toIndex)}.
     * 
     * @param fromIndex the leftmost element index in the range over which to 
     *                  distribute the fingers.
     * @param toIndex   the one past the rightmost element index in the range 
     *                  over which to disribute the fingers.
     */
    void distributeFingers(int fromIndex, int toIndex) {
        int rangeLength = toIndex - fromIndex;
        
        if (rangeLength == 0) {
            return;
        }
        
        int fingerPrefixLength = fingerList.getFingerIndexImpl(fromIndex);
        int fingerSuffixLength = fingerList.size() 
                               - fingerList.getFingerIndexImpl(toIndex);
        
        int numberOfRangeFingers = fingerList.size()
                                 - fingerPrefixLength 
                                 - fingerSuffixLength;
        
        if (numberOfRangeFingers == 0) {
            return;
        }
        
        int numberOfElementsPerFinger = rangeLength / numberOfRangeFingers;
        int startOffset = numberOfElementsPerFinger / 2;
        int index = fromIndex + startOffset;
        
        Node<E> node = node(fromIndex);
        node = scrollNodeToRight(node, startOffset);
        
        for (int i = 0; i < numberOfRangeFingers - 1; ++i) {
            Finger<E> finger = fingerList.get(i);
            finger.node = node;
            finger.index = index;
            
            for (int j = 0; j < numberOfElementsPerFinger; ++j) {
                node = node.next;
            }
            
            index += numberOfElementsPerFinger;
        }
        
        // Since we cannot advance node to the right, we need to deal with the
        // last (non-sentinel) finger manually:
        Finger<E> lastFinger = fingerList.get(fingerList.size() - 1);
        lastFinger.node = node;
        lastFinger.index = index;
    }
    
    /**
     * Distributes evenly all the fingers over this list.
     */
    private void distributeAllFingers() {
        distributeFingers(0, size);
    }
    
    /**
     * Implements the descending list iterator over this list.
     */
    private final class DescendingIterator implements Iterator<E> {

        /**
         * The most recently returned element.
         */
        private Node<E> lastReturned;
        
        /**
         * The next node to iterate.
         */
        private Node<E> nextToIterate = tail;
        
        /**
         * The index of the node next to iterate.
         */
        private int nextIndex = IndexedLinkedList.this.size - 1;
        
        /**
         * The cached expected modification count. Used to detect the situations
         * where the list is modified outside iterator API during iteration.
         */
        int expectedModCount = IndexedLinkedList.this.modCount;
        
        /**
         * Constructs this descending iterator.
         */
        public DescendingIterator() {
            
        }
        
        /**
         * Returns {@code true} if and only if this iterator has more elements
         * to offer.
         * 
         * @return {@code true} if and only if there is more to iterate. 
         */
        @Override
        public boolean hasNext() {
            return nextIndex > -1;
        }
        
        /**
         * Returns the next element in the iteration order.
         * 
         * @return the next element.
         * @throws ConcurrentModificationException if the underlying list was
         *                                         modified outside the iterator
         *                                         API.
         */
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
        
        /**
         * Removes the most recently returned element from the list.
         * 
         * @throws IllegalStateException if there is no most recent element. 
         *                               This can happen if there was no 
         *                               {@link DescendingIterator#next()} 
         *                               on this iterator, or the previous 
         *                               operation was
         *                               {@link DescendingIterator#remove()}.
         */
        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            
            checkForComodification();
            
            removeObjectImpl(lastReturned, nextIndex + 1);
            lastReturned = null;
            expectedModCount++;
        }
        
        /**
         * Iterates over all remaining elements in the descendinig iteration 
         * order.
         * 
         * @param action the action to call for each iterated element. 
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            
            while (modCount == expectedModCount && hasNext()) {
                action.accept(nextToIterate.item);
                nextToIterate = nextToIterate.prev;
                nextIndex--;
            }
            
            checkForComodification();
        }
        
        /**
         * Makes sure that the expected modification count equals the iterator's
         * modification count, and throws an exception if that is not the case.
         * 
         * @throws ConcurrentModificationException if the modification counts do
         *                                         not match.
         */
        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    
    /**
     * Implements the enhanced list iterator over this list.
     */
    final class EnhancedIterator implements ListIterator<E> {

        /**
         * The most recently iterated node.
         */
        private Node<E> lastReturned;
        
        /**
         * The next node to iterate.
         */
        private Node<E> next;
        
        /**
         * The index of {@code next} in the list.
         */
        private int nextIndex;
        
        /**
         * The expected modification count. Package-private for the sake of unit 
         * testing.
         */
        int expectedModCount = modCount;
        
        /**
         * Constructs a new enhanced list iterator starting from the node with
         * index {@code index}.
         * 
         * @param index the starting node index.
         */
        EnhancedIterator(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }
        
        /**
         * Returns {@code true} if and only if there is more to iterate.
         * 
         * @return {@code true} if and only if there is more to iterate.
         */
        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }
        
        /**
         * Returns the next element in the forward iteration order.
         * 
         * @return the next element in forward direction.
         * @throws ConcurrentModificationException if the list was modified 
         *                                         outside the iterator API.
         * @throws NoSuchElementException if there is no elements to iterate.
         */
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

        /**
         * Returns {@code true} if and only if there is a previous element in
         * the forward iteration order.
         * 
         * @return {@code true} if and only if there is a previous element. 
         */
        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        /**
         * Returns the previous element in the forward iteration order.
         * 
         * @return the previous element in forward direction.
         * @throws ConcurrentModificationException if the list was modified 
         *                                         outside the iterator API.
         * @throws NoSuchElementException if there is no elements to iterate.
         */
        @Override
        public E previous() {
            checkForComodification();
            
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            
            lastReturned = next = (next == null) ? tail : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        /**
         * Returns the index of the element next to iterate.
         * 
         * @return the index of the next element.
         */
        @Override
        public int nextIndex() {
            return nextIndex;
        }

        /**
         * Returns the index of the element previously iterated.
         * 
         * @return the index of the previous element.
         */
        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        /**
         * Removes the most recently iterated element from the list.
         * 
         * @throws ConcurrentModificationException if the underlying list was
         *                                         was modified outside the
         *                                         iterator API.
         * @throws IllegalStateException if there is no previously iterated 
         *                               element. This can happen if 
         *                               {@link ListIterator#previous()} or
         *                               {@link ListIterator#next()} was not 
         *                               called yet, or the previous operation 
         *                               was 
         *                               {@link ListIterator#add(java.lang.Object)}
         *                               or {@link ListIterator#remove()}.
         *                        
         */
        @Override
        public void remove() {
            checkForComodification();
            
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

        /**
         * Sets the element currently pointed by this iterator.
         * 
         * @param e the element to set.
         * @throws IllegalStateException if there is no current element.
         * @throws ConcurrentModificationException if the underlying list was
         *                                         modified outside the iterator
         *                                         API.
         */
        @Override
        public void set(E e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            
            checkForComodification();
            lastReturned.item = e;
        }

        /**
         * Adds the element {@code e} right after the previously iterated 
         * element.
         * 
         * @param e the element to add. 
         */
        @Override
        public void add(E e) {
            checkForComodification();
            
            lastReturned = null;
            
            if (next == null) {
                linkLast(e);
            } else if (next.prev == null) {
                linkFirst(e);
            } else {
                linkBefore(e, nextIndex, next);
            }
            
            nextIndex++;
            expectedModCount++;
        }
        
        /**
         * Iterates over the remaining elements in the forward iteration order.
         * 
         * @param action the action to apply to each remaining element. 
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                next = next.next;
                nextIndex++;
            }
            
            checkForComodification();
        }
        
        /**
         * Checks that the expected modification count matches the modification
         * count of the underlying list.
         */
        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    
    /**
     * Checks that the list {@code other} matches {@code this[from ... to - 1]}.
     * 
     * @param other the target list to compare to.
     * @param from  the starting, inclusive index of the range in this list.
     * @param to    the ending, exclusive index of the range in this list.
     * 
     * @return {@code true} if and only if {@code other} equals 
     *         {@code this[from ... to - 1]}.
     */
    private boolean equalsRange(List<?> other, int from, int to) {
        int rangeLength = to - from;
        
        if (rangeLength != other.size()) {
            return false;
        }
        
        Iterator<?> otherIterator = other.iterator();
        
        for (Node<E> node = node(from); from < to; from++, node = node.next) {
            if (!otherIterator.hasNext() || 
                    !Objects.equals(node.item, otherIterator.next())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * 
     * @param index the target index.
     * @return the detail message.
     */
    private String getOutOfBoundsMessage(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    /**
     * Computes the recommended number of fingers.
     * 
     * @return the recommended number of fingers. Equals 
     * \(\Bigg\lceil\sqrt{N}\Bigg\rceil\), where \(N\) is {@code size}.
     */
    private int getRecommendedNumberOfFingers() {
        return (int) Math.ceil(Math.sqrt(size));
    }
    
    /**
     * Accesses the {@code index}th node sequentially without relying on 
     * fingers. Used in {@link #randomizeFingers()}.
     * 
     * @param index the index of the desired node.
     * 
     * @return the {@code index}th node.
     */
    private Node<E> getNodeSequentially(int index) {
        Node<E> node = head;
        
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        
        return node;
    }
    
    /**
     * Computes the hash code for the range {@code this[from, to - 1]} and 
     * returns it.
     * 
     * @param from the starting, inclusive index.
     * @param to   the ending, exclusive index.
     * @return the hash value of the range.
     */
    private int hashCodeRange(int from, int to) {
        int hashCode = 1;
        
        Node<E> node = node(from);
        
        while (from++ < to) {
            // Same arithmetics as in ArrayList.
            hashCode =
                    31 * hashCode + 
                    (node.item == null ? 0 : node.item.hashCode());
            
            node = node.next;
        }
        
        return hashCode;
    }
    
    /**
     * Increases the size of the list and its modification count.
     */
    private void increaseSize() {
        ++size;
        ++modCount;
    }
    
    /**
     * Increases the size of this list by {@code m} elements and modifies the
     * modification count.
     * 
     * @param m the size add-on. 
     */
    private void increaseSize(final int m) {
        size += m;
        ++modCount;
    }
    
    /**
     * Returns the index of the leftmost occurrence of the object {@code o} in
     * the range {@code this[start ... end - 1]}.   
     * 
     * @param o     the object to search. May be {@code null}.
     * @param start the starting, inclusive index of the range to search.
     * @param end   the ending, exclusive index of the range to search.
     * @return the leftmost occurrence index.
     */
    private int indexOfRange(Object o, int start, int end) {
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
    
    /**
     * Inserts the input collection right before the node {@code succ}.
     * 
     * @param c         the collection to insert.
     * @param succ      the node that is right before the end of the inserted 
     *                  collection.
     * @param succIndex the appearance index of {@code succ} in the list.
     */
    private void insertAll(Collection<? extends E> c,
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
        
        // Add fingers:
        addFingersAfterInsertAll(pred.next, 
                                 succIndex,
                                 sz);
    }

    /**
     * Tells if the argument is the index of an existing element. The index is
     * valid if it is in the set \(\{0, 1, ..., \) {@code size}\( - 1\}\).
     * 
     * @param index the index to validate.
     * @return {@code true} if and only if the index is valid.
     */
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /**
     * Tells if the argument is the index of a valid position for an iterator or 
     * an add operation. The index is valid if it is in set
     * \(\{ 0, 1, ..., \) {@code size}\(\}\).
     * 
     * @param index the index to validate.
     * @return {@code true} if and only if the index is valid.
     */
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
    
    /**
     * Returns the last appearance index of {@code obj} or {@code -1} if the 
     * {@code o} is not in this list.
     *
     * @param o     the object to search for.
     * @param start the starting, inclusive index of the range to search.
     * @param end   the ending, exclusive index of the range to search.
     * @return the index of the rightmost appearance of {@code o} or {@code -1}
     *         if there is no such.
     */
    private int lastIndexOfRange(Object o, int start, int end) {
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
    
    /**
     * Links the input element right before the node {@code succ}.
     * 
     * @param e     the element to link.
     * @param succ  the node before which to link the {@code e}'s node.
     * @param index the index of {@code e}.
     */
    private void linkBefore(E e, int index, Node<E> succ) {
        Node<E> pred = succ.prev;
        Node<E> newNode = new Node<>(e);
        
        newNode.next = succ;
        newNode.prev = pred;
        succ.prev = newNode;
        pred.next = newNode;

        increaseSize();

        if (mustAddFinger()) {
            fingerList.insertFingerAndShiftOnceToRight(
                    new Finger<>(newNode, index));
        } else {
            int fingerIndex = fingerList.getFingerIndexImpl(index);
            fingerList.shiftFingerIndicesToRightOnce(fingerIndex);
        }
    }
    
    /**
     * Prepends the input element to the head of this list.
     * 
     * @param e the element to prepend.
     */
    private void linkFirst(E e) {
        Node<E> oldFirst = head;
        Node<E> newNode = new Node<>(e);
        newNode.item = e;
        newNode.next = oldFirst;
        head = newNode;

        if (oldFirst == null) {
            tail = newNode;
        } else {
            oldFirst.prev = newNode;
        }

        increaseSize();

        if (mustAddFinger()) {
            fingerList.prependFingerForNode(newNode);
        } else {
            fingerList.shiftFingerIndicesToRightOnce(0);
        }
    }
    
    /**
     * Appends the input element to the tail of this list.
     * 
     * @param e the element to append.
     */
    private void linkLast(E e) {
        Node<E> oldTail = tail;
        Node<E> newNode = new Node<>(e);
        newNode.prev = oldTail;
        tail = newNode;
        
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        
        increaseSize();
        
        if (mustAddFinger()) {
            appendFinger(newNode, size - 1);
        } else {
            fingerList.get(fingerList.size()).index++;
        }
    }
    
    /**
     * Moves the {@code finger} out of the element with index 
     * {@code finger.index}.
     * 
     * @param finger      the finger to move. 
     * @param fingerIndex the index of {@code finger}.
     */
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
        if (tryPushFingersToRight(fingerIndex)) {
            return;
        }
        
        // Could not push the fingers to the right. Try push to the left:
        if (tryPushFingersToLeft(fingerIndex)) {
            return;
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
    
    /**
     * Returns {@code true} if and only if this list requires more fingers.
     * 
     * @return {@code true} if and only if this list requires more fingers.
     */
    private boolean mustAddFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() - 1
        return fingerList.size() != getRecommendedNumberOfFingers();
    }
    
    /**
     * Returns {@code true} if and only if this list requires less fingers.
     * 
     * @return {@code true} if and only if this list requires less fingers.
     */
    private boolean mustRemoveFinger() {
        // Here, fingerStack.size() == getRecommendedFingerCount(), or,
        // fingerStack.size() == getRecommendedFingerCount() + 1
        return fingerList.size() != getRecommendedNumberOfFingers();
    }
    
    /**
     * Returns the node at index {@code elementIndex}.
     * 
     * @param elementIndex the index of the target element.
     * @return the node containing the target element.
     */
    private Node<E> node(int elementIndex) {
         return fingerList.getNode(elementIndex);
    }
    
    /**
     * Prepends the input collection to the head of this list.
     * 
     * @param c the collection to prepend.
     */
    private void prependAll(Collection<? extends E> c) {
        Iterator<? extends E> iterator = c.iterator();
        Node<E> oldFirst = head;
        head = new Node<>(iterator.next());

        Node<E> prevNode = head;

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
        addFingersAfterPrependAll(sz);
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
                head = tail = newNode;
                fingerList.appendFinger(new Finger<>(newNode, 0));
                return;
        }
        
        Node<E> rightmostNode = new Node<>((E) s.readObject());
        head = rightmostNode;
        
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
        
        tail = rightmostNode;
    }
    
    /**
     * Removes the last non-sentinel finger from the finger list. 
     */
    private void removeFinger() {
        fingerList.removeFinger();
    }
    
    /**
     * Implements the actual removal of the first/head element.
     * 
     * @return the removed element.
     */
    private E removeFirstImpl() {
        E returnValue = head.item;
        decreaseSize();
        
        head = head.next;
        
        if (head == null) {
            tail = null;
        } else {
            head.prev = null;
        }
        
        fingerList.adjustOnRemoveFirst();
        
        if (mustRemoveFinger()) {
            removeFinger();
        }

        fingerList.get(fingerList.size()).index = size;
        return returnValue;
    }
    
    /**
     * Removes all the items that satisfy the given predicate.
     * 
     * @param filter the filter object.
     * @param fromIndex the starting, inclusive index of the range to crawl.
     * @param toIndex   the ending, exclusive index of the range to crawl.
     * 
     * @return {@code true} if and only if this list was modified.
     */
    private boolean removeIf(Predicate<? super E> filter,
                             int fromIndex, 
                             int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        
        boolean modified = false;
        int numberOfNodesToIterate = toIndex - fromIndex;
        int i = 0;
        int nodeIndex = fromIndex;
        
        for (Node<E> node = node(fromIndex); i < numberOfNodesToIterate; ++i) {
            Node<E> nextNode = node.next;
            
            if (filter.test(node.item)) {
                modified = true;
                removeObjectImpl(node, nodeIndex);
            } else {
                nodeIndex++;
            }
            
            node = nextNode;
        }
        
        return modified;
    }
    
    /**
     * Implements the actual removal of the last/tail element.
     * 
     * @return the removed element.
     */
    private E removeLastImpl() {
        E returnValue = tail.item;
        decreaseSize();
        
        tail = tail.prev;
        
        if (tail == null) {
            head = null;
        } else {
            tail.next = null;
        }
        
        if (mustRemoveFinger()) {
            removeFinger();
        }
        
        return returnValue;
    }
    
    /**
     * Implements the node removal. 
     * 
     * @param node  the node to remove.
     * @param index the index of {@code node}.
     */
    private void removeObjectImpl(Node<E> node, int index) {
        int closestFingerIndex = fingerList.getClosestFingerIndex(index);
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
    
    /**
     * The main range removal method. Unlinks the range
     * {@code [fromIndex, ..., toIndex]} and fixes the finger list.
     * 
     * @param fromIndex the starting, inclusive index of the range to remove.
     * @param toIndex the ending, exclusive index of the range to remove.
     */
    private void removeRangeImpl(final int fromIndex, final int toIndex) {
        final int removeRangeLength = toIndex - fromIndex;
        final int currentNumberOfFingers = fingerList.size();
        final int nextNumberOfFingers = 
                getRecommendedNumberOfFingers(size - removeRangeLength);
        
        final Node<E> nodeStart  = fingerList.getNode(fromIndex);
        final Node<E> nodeEnd    = fingerList.getNode(toIndex);
        final Node<E> prefixNode = nodeStart.prev; 
        final Node<E> suffixNode = nodeEnd;
        
        int numberOfCoveredFingers     = 0;
        int indexOfFirstCoveringFinger = -1;
        int nodeIndex = fromIndex;
        int fingerIndex = fingerList.getFingerIndexImpl(fromIndex);
        
        Finger<E> finger = fingerList.get(fingerIndex);
        
        for (Node<E> node = nodeStart; node != nodeEnd;) {
            
            if (finger.index == nodeIndex) {
                numberOfCoveredFingers++;
                
                if (indexOfFirstCoveringFinger == -1) {
                    indexOfFirstCoveringFinger = fingerIndex;
                }
                
                finger = fingerList.get(++fingerIndex);
            }
            
            final Node<E> nextNode = node.next;
            node.next = null;
            node.prev = null;
            node.item = null;
            node = nextNode;
            nodeIndex++;
        }
        
        // Do the actual node unlinking:
        if (prefixNode == null) {
            head = suffixNode;
            suffixNode.prev = null;
        } else if (suffixNode == null) {
            tail = prefixNode;
            prefixNode.next = null;
        } else {
            prefixNode.next = suffixNode;
            suffixNode.prev = prefixNode;
        }
        
        size -= removeRangeLength;
        
        // Remove fingers starting from the tail:
        if (numberOfCoveredFingers == 0) {
            final int fingersToRemove = currentNumberOfFingers 
                                      - nextNumberOfFingers;
            
            // Once here, there is no fingers pointing to the removed range:
            for (int i = 0; i < fingersToRemove; i++) {
                fingerList.removeFinger();
            }
            
            throw new UnsupportedOperationException("numberOfcoveredFingers == 0");
        } 
        
        for (int i = 0;
                 i < currentNumberOfFingers - indexOfFirstCoveringFinger; 
                 i++) {
            
            fingerList.removeFinger();
        }
        
        final int fingersToAppend = nextNumberOfFingers 
                                  - indexOfFirstCoveringFinger;
        
        Finger<E> previousFinger = new Finger<>(prefixNode == null ? head : prefixNode, fromIndex - 1);
        
        for (int i = 0; i < fingersToAppend; i++) {
            final Finger<E> currentFinger = 
                    new Finger<>(
                            previousFinger.node.next, 
                            previousFinger.index + 1);
            
            fingerList.appendFinger(currentFinger);
            previousFinger = currentFinger;
        }
    }
    
    /**
     * Removes the list range {@code [fromIndex, ..., toIndex - 1]}.
     * 
     * @param fromIndex the staring, inclusive range index.
     * @param toIndex   the ending, exclusive range index.
     */
    void removeRange(int fromIndex, int toIndex) {
        int removalSize = toIndex - fromIndex;
        
        if (removalSize == 0) {
            return;
        }
        
        if (removalSize == 1) {
            // TODO: modCount update?
            remove(fromIndex);
            return;
        }
        
        if (removalSize == size) {
            clear();
            return;
        }
        
        if (toIndex == this.size) {
            removeRangeSuffixImpl(fromIndex);
            size -= (size - fromIndex);
            return;
        }
        
        removeRangeImpl(fromIndex, toIndex);
    }
    
    /**
     * Unlinks the nodes in the range {@code [fromIndex, ..., size - 1]}.
     * 
     * @param fromIndex the tarting inclusive index of the range to remove.
     */
    private void removeRangeSuffixImpl(final int fromIndex) {
        int numberOfElementsToRemove = size - fromIndex;
        int saveListSize = size;
        
        for (int i = 0; i < numberOfElementsToRemove; i++) {
            removeLast();
        }
        
        fingerList.fingerArray[fingerList.size()].index = 
                saveListSize - numberOfElementsToRemove;
    }
    
    /**
     * Replaces all the elements from range {@code [i, end - 1]}.
     * 
     * @param operator the replacement operator.
     * @param i the starting, inclusive index of the range to replace.
     * @param end the ending, exclusive index of the range to replace.
     */
    private void replaceAllRange(UnaryOperator<E> operator, int i, int end) {
        Objects.requireNonNull(operator); 
        int expectedModCount = modCount;
        Node<E> node = node(i);
        
        while (modCount == expectedModCount && i < end) {
            node.item = operator.apply(node.item);
            node = node.next;
            i++;
        }
        
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
    
    /**
     * Scrolls the input node {@code scrolls} positions towards the tail of the
     * linked list and returns the reached node.
     * 
     * @param startNode the node from which to start the scrolling.
     * @param scrolls   the number of positions to scroll.
     * @return          the reached node.
     */
    private Node<E> scrollNodeToRight(Node<E> startNode, int scrolls) {
        for (int i = 0; i < scrolls; i++) {
            startNode = startNode.next;
        }
        
        return startNode;
    }
    
    /**
     * Sets the input collection as a list.
     * 
     * @param c the collection to set.
     */
    private void setAll(Collection<? extends E> c) {
        Iterator<? extends E> iterator = c.iterator();

        head = new Node<>(iterator.next());
        Node<E> prevNode = head;

        for (int i = 1, sz = c.size(); i < sz; i++) {
            Node<E> newNode = new Node<>(iterator.next());
            prevNode.next = newNode;
            newNode.prev = prevNode;
            prevNode = newNode;
        }

        tail = prevNode;
        size = c.size();
        modCount++;

        addFingersAfterSetAll(c.size());
    }
    
    /**
     * Spreads the fingers over the range starting from {@code node}.
     * 
     * @param node               the starting node.
     * @param numberOfNewFingers the total number of fingers to spread.
     * @param index              the starting index.
     * @param distance           the distance between two consecutive fingers.
     * @param fingerIndex        the starting finger index.
     */
    private void spreadFingers(Node<E> node, 
                               int numberOfNewFingers,
                               int index,
                               int distance,
                               int fingerIndex) {
        
        node = scrollNodeToRight(node, distance / 2);
        fingerList.setFinger(fingerIndex++, new Finger<>(node, index));
        
        for (int i = 1; i < numberOfNewFingers; i++) {
            index += distance;
            node = scrollNodeToRight(node, distance);
            fingerList.setFinger(fingerIndex++, new Finger<>(node, index));
        }
    }
    
    /**
     * If steps > 0, rewind to the left. Otherwise, rewinds to the right.
     * 
     * @param finger the finger to traverse.
     * @param steps  the number of steps to traverse the {@code finger}.
     * @return the reached node.
     */
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
    
    /**
     * Attempts to move the finger with index {@code fingerIndex} to the left.
     * 
     * @param fingerIndex the index of the finger to move to the left.
     * 
     * @return {@code true} if a free spot is found, {@code false} otherwise. 
     */
    private boolean tryPushFingersToLeft(int fingerIndex) {
        for (int j = fingerIndex; j > 0; --j) {
            Finger<E> fingerLeft  = fingerList.get(j - 1);
            Finger<E> fingerRight = fingerList.get(j);
            
            if (fingerLeft.index + 1 < fingerRight.index) {
                for (int i = fingerIndex; i > 0; --i) {
                    Finger<E> fngr = fingerList.get(i);
                    fngr.node = fngr.node.prev;
                    fngr.index--;
                }
                
                fingerList.shiftFingerIndicesToLeftOnceAll(fingerIndex + 1);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Attempts to move the finger with index {@code fingerIndex} to the right.
     * 
     * @param fingerIndex the index of the finger to move to the right.
     * 
     * @return {@code true} if a free spot is found, {@code false} otherwise.
     */
    private boolean tryPushFingersToRight(int fingerIndex) {
        for (int j = fingerIndex; j < fingerList.size(); ++j) {
            Finger<E> fingerLeft  = fingerList.get(j);
            Finger<E> fingerRight = fingerList.get(j + 1);

            if (fingerLeft.index + 1 < fingerRight.index) {
                for (int i = j; i >= fingerIndex; --i) {
                    Finger<E> fngr = fingerList.get(i);
                    fngr.node = fngr.node.next;
                }

                fingerList.shiftFingerIndicesToLeftOnceAll(j + 1);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Unlinks the input node from the actual doubly-linked list.
     * 
     * @param x the node to unlink from the underlying linked list.
     */
    private void unlink(Node<E> x) {
        Node<E> next = x.next;
        Node<E> prev = x.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
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
        for (Node<E> x = head; x != null; x = x.next) {
            s.writeObject(x.item);
        }
    }
    
    /**
     * This static inner class implements the spliterator over this list.
     * 
     * @param <E> the node datum type. 
     */
    static final class LinkedListSpliterator<E> implements Spliterator<E> {
        
        /**
         * The minimum batch size.
         */
        static final long MINIMUM_BATCH_SIZE = 1024L;
        
        /**
         * The target list.
         */
        private final IndexedLinkedList<E> list;
        
        /**
         * The current node.
         */
        private Node<E> node;
        
        /**
         * The length of this spliterator.
         */
        private long lengthOfSpliterator;
        
        /**
         * The number of processed elements so far.
         */
        private long numberOfProcessedElements;
        
        /**
         * The offset of this spliterator.
         */
        private long offsetOfSpliterator;
        
        /**
         * The expected modification count. We rely on this in order to catch 
         * the modifications from outside the spliterator API.
         */
        private final int expectedModCount;
        
        /**
         * Constructs a new spliterator.
         * 
         * @param list                the target list to split.
         * @param node                the initial node of this spliterator.
         * @param lengthOfSpliterator the length of this spliterator.
         * @param offsetOfSpliterator the offset of this spliterator.
         * @param expectedModCount    the expected modification count.
         */
        private LinkedListSpliterator(IndexedLinkedList<E> list,
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

        /**
         * Makes an attempt to advance in this spliterator.
         * 
         * @param action the action which to apply to the advanced element.
         * @return {@code true} if and only if there were an element to advance.
         */
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

        /**
         * Applies {@code action} to all remaining elements in this spliterator.
         * 
         * @param action the action to apply to each remaining element.
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            
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
        
        /**
         * Attempts to split this spliterator. Upon success, it returns the rest
         * of this spliterator, and a child spliterator working on the another
         * half of the range.
         * 
         * @return another spliterator, or {@code null} if splitting was not 
         *         possible.
         */
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
            
            return new LinkedListSpliterator<>(list,
                                               newSpliteratorNode,
                                               newSpliteratorLength, // length
                                               newSpliteratorOffset, // offset
                                               expectedModCount);
        }

        /**
         * Returns the estimated size left. This method, however, returns the 
         * exact size estimate.
         * 
         * @return the number of elements in this spliterator not yet advanced
         *         over.
         */
        @Override
        public long estimateSize() {
            return (long)(lengthOfSpliterator - numberOfProcessedElements);
        }

        /**
         * Just like {@link #estimateSize()}, returns the exact number of 
         * elements not yet advanced over via this spliterator.
         * 
         * @return the number of elements in this spliterator not yet advanced
         *         over.
         */
        @Override
        public long getExactSizeIfKnown() {
            return estimateSize();
        }

        /**
         * Returns characteristics masks.
         * 
         * @return characteristics masks.
         */
        @Override
        public int characteristics() {
            return Spliterator.ORDERED | 
                   Spliterator.SUBSIZED |
                   Spliterator.SIZED;
        }
        
        /**
         * Queries for particular characteristics.
         * 
         * @param characteristics the characteristic flag.
         * @return {@code true} if and only if this spliterator supports the 
         *         {@code characteristics}.
         */
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
    
    /**
     * This inner class implements a sublist view over the compassing list. This
     * class is not {@code static} since it needs to work with the underlying 
     * list.
     */
    class EnhancedSubList implements List<E>, Cloneable {
        
        /**
         * The root list.
         */
        private final IndexedLinkedList<E> root;
        
        /**
         * The parent view. This view cannot be wider than its parent view.
         */
        private final EnhancedSubList parent;
        
        /**
         * The offset with regard to the parent view or the root list.
         */
        private final int offset;
        
        /**
         * The length of this view.
         */
        private int size;
        
        /**
         * The modification count.
         */
        private int modCount;
        
        /**
         * Constructs a new sublist view over a {@code IndexedLinkedList}.
         * 
         * @param root      the root list.
         * @param fromIndex the starting, inclusive index of this view.
         * @param toIndex   the ending, exclusive index of this view.
         */
        public EnhancedSubList(IndexedLinkedList<E> root, 
                               int fromIndex, 
                               int toIndex) {
            
            this.root = root;
            this.parent = null;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = root.modCount;
        }
        
        /**
         * Constructs a new sublist view over a parent view.
         * 
         * @param parent    the parent view.
         * @param fromIndex the starting, inclusive index of the new subview.
         *                  Starts from the beginning of {@code parent}.
         * @param toIndex   the ending, exclusive index of the new subview.
         *                  Starts from the beginning of {@code parent}.
         */
        private EnhancedSubList(EnhancedSubList parent, 
                                int fromIndex, 
                                int toIndex) {
            
            this.root = parent.root;
            this.parent = parent;
            this.offset = parent.offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = root.modCount;
        }

        /**
         * Appends {@code e} to the end of this view.
         * 
         * @param e the element to add.
         * @return always {@code true}.
         */
        @Override
        public boolean add(E e) {
            checkInsertionIndex(size); 
            checkForComodification();
            root.add(offset + size, e);
            updateSizeAndModCount(1);
            return true;
        }
        
        /**
         * Inserts the element {@code element} before view element at index
         * {@code index}.
         * 
         * @param index   the insertion index.
         * @param element the element to add.
         */
        @Override
        public void add(int index, E element) {
            checkInsertionIndex(index);
            checkForComodification();
            root.add(offset + index, element);
            updateSizeAndModCount(1);
        }
        
        /**
         * Appends the entire collection {@code c} to the end of this view. The
         * elements from {@code c} are appended in the iteration order of
         * {@code c}.
         * 
         * @param c the collection to append.
         * @return {@code true} if and only if this view changed due to the 
         *         call.
         */
        @Override
        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }
        
        /**
         * Inserts the collection {@code collection} before the {@code index}th 
         * element of this view. The elements are inserted in the iteration 
         * order of {@code collection}.
         * 
         * @param index      the index of the element before which to insert.
         * @param collection the collection to insert.
         * @return {@code true} if and only if this view changed.
         */
        @Override
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
        
        /**
         * Cleares the entire view. This will delegate down the parent chain to
         * the actual root list, effectively removing a subrange from the root
         * list.
         */
        @Override
        public void clear() {
            checkForComodification();
            root.removeRange(offset, offset + size);
            updateSizeAndModCount(-size);
        }
        
        /**
         * Creates a {@link IndexedLinkedList} and loads the contents of this 
         * view to it in iterative order.
         * 
         * @return the clone object of this subview.
         */
        @Override
        public Object clone() {
            List<E> list = new IndexedLinkedList<>();
            
            for (E element : this) {
                list.add(element);
            }
            
            return list;
        }

        /**
         * Return {@code true} if and only if the given object appears in this
         * view.
         * 
         * @param o the object to locate.
         * @return {@code true} if and only if {@code o} is in this view.
         */
        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        /**
         * Checks that all the elements in {@code c} are in this view.
         * 
         * @param c the collection to test for inclusion.
         * @return {@code true} if and only if all the elements in {@code c}
         *         appear in this view.
         */
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
         * Checks that the given object is a {@link java.util.List} and has the
         * same content as this view.
         * 
         * @param o the object to check.
         * @return {@code true} if and only if the input object is a list with
         *         the same contents as this view.
         */
        @Override
        public boolean equals(Object o) {
            return root.equalsRange((List<?>) o, offset, offset + size);
        }
        
        /**
         * Applies the input action to each element in this view.
         * 
         * @param action the action to apply.
         */
        @Override
        public void forEach(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            int expectedModCount = modCount;
            int iterated = 0;
            
            for (Node<E> node = node(offset); 
                    modCount == expectedModCount && iterated < size; 
                    node = node.next, ++iterated) {
                
                action.accept(node.item);
            }
            
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
        
        /**
         * Returns the {@code index}th element of this view.
         * 
         * @param index the index of the target element.
         * @return the {@code index}th element.
         */
        @Override
        public E get(int index) {
            Objects.checkIndex(index, size);
            checkForComodification();
            return root.get(offset + index);
        }
        
        /**
         * Returns the hash code of this view.
         * 
         * @return the hash code of this view. 
         */
        @Override
        public int hashCode() {
            return root.hashCodeRange(offset, offset + size);
        }

        /**
         * Returns the index of the leftmost appearance of {@code o}, or 
         * {@code -1} if there is no such.
         * 
         * @param o the target object.
         * @return the index of the input object, or {@code -1} if there is no
         *         match.
         */
        @Override
        public int indexOf(Object o) {
            int index = root.indexOfRange(o, offset, offset + size);
            checkForComodification();
            return index >= 0 ? index - offset : -1;
        }
        
        /**
         * Returns {@code true} if and only if this view is empty.
         * 
         * @return {@code true} if and only if this view is empty.
         */
        @Override
        public boolean isEmpty() {
            return size == 0;
        }
        
        /**
         * Returns an iterator over this view.
         * 
         * @return an iterator.
         */
        @Override
        public Iterator<E> iterator() {
            return listIterator();
        }

        /**
         * Returns the index of the rightmost occurrence of {@code o}, or 
         * {@code -1} if there is no such.
         * 
         * @param o the targe object.
         * @return the index of {@code o} in this view.
         */
        @Override
        public int lastIndexOf(Object o) {
            int index = root.lastIndexOfRange(o, offset, offset + size);
            checkForComodification();
            return index >= 0 ? index - offset : -1;
        }

        /**
         * Returns a list iterator over this view.
         * 
         * @return a list iterator. 
         */
        @Override
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }
        
        /**
         * Returns a list iterator over this view starting from the specified
         * position.
         * 
         * @param index the starting position.
         * @return a list iterator.
         */
        @Override
        public ListIterator<E> listIterator(int index) {
            checkForComodification();
            checkInsertionIndex(index);
            
            return new ListIterator<E>() {
                private final ListIterator<E> i = 
                        root.listIterator(offset + index);
                
                @Override
                public boolean hasNext() {
                    return nextIndex() < size;
                }
                
                @Override
                public E next() {
                    if (hasNext()) {
                        return i.next();
                    } 
                    
                    throw new NoSuchElementException();
                }
                
                @Override
                public boolean hasPrevious() {
                    return previousIndex() >= 0;
                }
                
                @Override
                public E previous() {
                    if (hasPrevious()) {
                        return i.previous();
                    }
                    
                    throw new NoSuchElementException();
                }
                
                @Override
                public int nextIndex() {
                    return i.nextIndex() - offset;
                }
                
                @Override
                public int previousIndex() {
                    return i.previousIndex() - offset;
                }
                
                @Override
                public void remove() {
                    i.remove();
                    updateSizeAndModCount(-1);
                }
                
                @Override
                public void set(E e) {
                    i.set(e);
                }
                
                @Override
                public void add(E e) {
                    i.add(e);
                    updateSizeAndModCount(1);
                }
            };
        }
        
        /**
         * Removes the leftmost occurrence of {@code o} from this view.
         * 
         * @param o the object to remove. May be {@code null}.
         * @return {@code true} if and only if {@code o} was removed.
         */
        @Override
        public boolean remove(Object o) {
            ListIterator<E> iterator = listIterator();
            
            if (o == null) {
                while (iterator.hasNext()) {
                    if (iterator.next() == null) {
                        iterator.remove();
                        return true;
                    }
                }
            } else {
                while (iterator.hasNext()) {
                    if (o.equals(iterator.next())) {
                        iterator.remove();
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        /**
         * Removes the {@code index}th element from this view.
         * 
         * @param index the index of the element to remove.
         * @return the removed element.
         */
        @Override
        public E remove(int index) {
            Objects.checkIndex(index, size);
            checkForComodification();
            E result = root.remove(offset + index);
            updateSizeAndModCount(-1);
            return result;
        }

        /**
         * Removes all the elements appearing in {@code c}.
         * 
         * @param c the collection of elements to remove from this view.
         * @return {@code true} if and only if any element in {@code c} was 
         *         removed.
         */
        @Override
        public boolean removeAll(Collection<?> c) {
            return batchRemove(c, true);
        }

        /**
         * Removes all the elements in this view that are filtered by 
         * {@code filter}.
         * 
         * @param filter the filter to apply.
         * @return {@code true} if and only if any element was removed.
         */
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            checkForComodification();
            int oldSize = root.size;
            boolean modified = root.removeIf(filter, offset, offset + size);
            
            if (modified) {
                updateSizeAndModCount(root.size - oldSize);
            }
            
            return modified;
        }
        
        /**
         * Replaces all the elements in this view with another values dictated
         * by {@code operator}.
         * 
         * @param operator the replacement operator to apply.
         */
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            root.replaceAllRange(operator, offset, offset + size);
        }

        /**
         * Remove all the elements from this view that are <b><i>not</i></b>
         * present in {@code c}.
         * 
         * @param c the collection to retain.
         * @return {@code true} if and only if this view changed.
         */
        @Override
        public boolean retainAll(Collection<?> c) {
            return batchRemove(c, false);
        }
        
        /**
         * Sets the {@code index}th element of this view to {@code element}.
         * 
         * @param index   the index of the target element.
         * @param element the element to set instead of the target element.
         * @return the old value of the {@code index}th element of this view.
         */
        @Override
        public E set(int index, E element) {
            Objects.checkIndex(index, size);
            checkForComodification();
            return root.set(offset + index, element);
        }
        
        /**
         * Returns the size of this view.
         * 
         * @return the size of this view.
         */
        @Override
        public int size() {
            checkForComodification();
            return size;
        }
        
        /**
         * Sorts this view.
         * 
         * @param c the comparator object.
         */
        @Override
        @SuppressWarnings("unchecked")
        public void sort(Comparator<? super E> c) {
            if (size == 0) {
                return;
            }
            
            int expectedModCount = modCount;
            Object[] array = toArray();
            Node<E> node = node(offset);

            Arrays.sort((E[]) array, c);

            // Rearrange the items over the linked list nodes:
            for (int i = 0; i < array.length; ++i, node = node.next) {
                E item = (E) array[i];
                node.item = item;
            }
            
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            
            distributeFingers(offset, offset + size);
            modCount++;
        }
        
        /**
         * Returns the spliterator over this view.
         * 
         * @return the spliterator over this view.
         */
        @Override
        public Spliterator<E> spliterator() {
            return new LinkedListSpliterator(root,
                                             node(offset),
                                             size,
                                             offset,
                                             modCount);
        }
        
        /**
         * Returns the subview of this view. The subview in question is
         * {@code this[fromIndex ... toIndex - 1]}.
         * 
         * @param fromIndex the starting, inclusive index of the new subview.
         * @param toIndex   the ending, exclusive index of the new subview.
         * @return the subview {@code this[fromIndex ... toIndex - 1}.
         */
        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new EnhancedSubList(this, fromIndex, toIndex);
        }

        /**
         * Returns the array of elements of this view.
         * 
         * @return the element array in the same order they appear in the 
         *         underlying linked list.
         */
        @Override
        public Object[] toArray() {
            checkForComodification();
            int expectedModCount = root.modCount;
            Object[] array = new Object[this.size];
            Node<E> node = node(offset);
            
            for (int i = 0; 
                    i < array.length && expectedModCount == root.modCount;
                    i++, node = node.next) {
                array[i] = node.item;
            }
            
            if (expectedModCount != root.modCount) {
                throw new ConcurrentModificationException();
            }
            
            return array;
        }

        /**
         * Returns the array of elements in this view.
         * 
         * @param <T>       the element data type.
         * @param generator the generator function. 
         * @return the element array in the same order they appear in the 
         *         underlying linked list.
         */
        @Override
        public <T> T[] toArray(IntFunction<T[]> generator) {
            return toArray(generator.apply(size));
        }

        /**
         * Returns the array of elements of this view. If the input array is too
         * small, an array of size {@code size} is created and filled with the
         * view contents. Otherwise, the elements in this view are copied to 
         * {@code a}. Also, if {@code a.length > size}, we set {@code a[size]} 
         * to {@code null} in order to signal the end-of-view.
         * 
         * @param <T> the element data type.
         * @param a the input array.
         * @return an array of all view elements.
         */
        @Override
        public <T> T[] toArray(T[] a) {
            checkForComodification();
            
            int expectedModCount = root.modCount;
            
            if (a.length < size) {
                a = (T[]) Array.newInstance(
                        a.getClass().getComponentType(),
                        size);
            }
            
            int index = 0;
            
            for (Node<E> node = node(offset);
                    expectedModCount == root.modCount && index < size; 
                    ++index, node = node.next) {
                
                a[index] = (T) node.item;
            }
            
            if (a.length > size) {
                a[size] = null;
            }
            
            return a;
        }
        
        /**
         * Returns the textual representation of this view.
         * 
         * @return the textual representation.
         */
        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");

            boolean firstIteration = true;

            for (E element : this) {
                if (firstIteration) {
                    firstIteration = false;
                } else {
                    stringBuilder.append(", ");
                }

                stringBuilder.append(element);
            }

            return stringBuilder.append("]").toString();
        }
    
        /**
         * Operates in batch over all the elements in {@code c}.
         * 
         * @param c          the target collection to operate on.
         * @param complement the mode flag. If {@code true}, all elements in 
         *                   {@code c} will be removed from this view, and if
         *                   {@code false}, all elements <b><i>not</i></b> in
         *                   {@code c}, will be removed from this view.
         * @return {@code true} if and only if this view was modified.
         */
        private boolean batchRemove(Collection<?> c, boolean complement) {
            checkForComodification();
            int oldSize = root.size;
            boolean modified = root.batchRemove(c,
                                                complement, 
                                                offset, 
                                                offset + size);
            
            if (modified) {
                updateSizeAndModCount(root.size - oldSize);
            }
            
            return modified;
        }
        
        /**
         * Makes sure that the root list and this view are in sync.
         * 
         * @throws ConcurrentModificationException if there is modification 
         *                                         mismatch.
         */
        private void checkForComodification() {
            if (root.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        /**
         * Checks that the input finger {@code index} is in the set
         * \(\{ 0, 1, \ldots, N\}\), where \(N = \){@code size}.
         * 
         * @param index the insertion index to validate. 
         */
        private void checkInsertionIndex(int index) {
            if (index < 0) {
                throw new IndexOutOfBoundsException("Negative index: " + index);
            }
            
            if (index > this.size) {
                throw new IndexOutOfBoundsException(
                        "index(" + index + ") > size(" + size + ")");
            }
        }
        
        /**
         * Updates the sizes and modification counters up the subview chain.
         * 
         * @param sizeDelta the value to add to the chain. If this view grew in
         *                  size, {@code sizeDelta} is positive. Otherwise, if
         *                  the view shrinked in size, {@code sizeDelta} will be
         *                  negative.
         */
        private void updateSizeAndModCount(int sizeDelta) {
            EnhancedSubList subList = this;
            
            do {
                subList.size += sizeDelta;
                subList.modCount = root.modCount;
                subList = subList.parent;
            } while (subList != null);
        }
    }
}
