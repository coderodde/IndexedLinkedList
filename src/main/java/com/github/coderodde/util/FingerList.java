package com.github.coderodde.util;

// This class inplements the sorted (by node indices) list of fingers.
import java.util.Arrays;

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

    int getFingerIndex(int index) {
        int count = size + 1; // + 1 for the end sentinel.
        int it;
        int idx = 0;

        while (count > 0) {
            it = idx;
            int step = count / 2;
            it += step;

            if (fingerArray[it].index < index) {
                idx = ++it;
                count -= step + 1;
            } else {
                count = step;
            }
        }

        return idx;
    }

    Node<E> node(int index) {
        Finger<E> finger = fingerArray[getFingerIndex(index)];
        int steps = finger.index - index;

        if (steps > 0) {
            finger.rewindLeft(steps);
        } else {
            finger.rewindRight(-steps);
        }

        return finger.node;
    }

    // Appends the input finger to the tail of the finger list:
    void appendFinger(Finger<E> finger) {
        enlargeFingerArrayIfNeeded();
        fingerArray[size + 1] = fingerArray[size];
        fingerArray[size] = finger;
        fingerArray[size + 1].index = fingerArray[size].index + 1;
        size++;
    }

    // Inserts the input finger into the finger list such that the entire
    // finger list is sorted by indices:
    void insertFinger(Finger<E> finger) {
        enlargeFingerArrayIfNeeded();
        int beforeFingerIndex = getFingerIndex(finger.index);
        System.arraycopy(
                fingerArray, 
                beforeFingerIndex, 
                fingerArray, 
                beforeFingerIndex + 1, size + 1 - beforeFingerIndex);
        
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
        
