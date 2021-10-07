package com.github.coderodde.util;

import java.util.Arrays;

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
        private Finger<E>[] fingerArray = new Finger[INITIAL_CAPACITY];

        // The number of fingers stored in the list. This field does not count the
        // end-of-list sentinel finger 'F' for which 'F.index = size'.
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
        
        private int normalize(int fingerIndex, int elementIndex) {
            if (fingerIndex == 0) {
                return 0;
            }
            
            if (fingerIndex == size) {
                return size - 1;
            }
            
            Finger finger1 = fingerArray[fingerIndex];
            Finger finger2 = fingerArray[fingerIndex + 1];
            
            int distance1 = Math.abs(elementIndex - finger1.index);
            int distance2 = Math.abs(elementIndex - finger2.index);
            return distance2 < distance1 ? fingerIndex + 1 : fingerIndex;
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
            fingerArray[size + 1].index = fingerArray[size].index + 1;
            size++;
        }

        // Inserts the input finger into the finger list such that the entire
        // finger list is sorted by indices:
        void insertFinger(com.github.coderodde.util.Finger<E> finger) {
            enlargeFingerArrayIfNeeded();
            int beforeFingerIndex = getFingerIndex(finger.index);
            System.arraycopy(
                    fingerArray, 
                    beforeFingerIndex, 
                    fingerArray, 
                    beforeFingerIndex + 1, 
                    size + 1 - beforeFingerIndex);

            fingerArray[beforeFingerIndex] = finger;
            size++;
        }

        void removeFinger() {
            --size;
            contractFingerArrayIfNeeded();
            fingerArray[size] = fingerArray[size + 1];
            fingerArray[size + 1] = null;
        }

        void clear() {
            Arrays.fill(fingerArray, 0, size, null);
            fingerArray[0] = fingerArray[size];
            fingerArray[0].index = 0;

            if (size != 0) {
                fingerArray[size] = null;
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
                    && fingerArray.length > INITIAL_CAPACITY) {
                int nextCapacity = fingerArray.length / 2;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
            }
        }
    }
     
    final FingerList<E> fingerList = new FingerList<>();
}
