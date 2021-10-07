package com.github.coderodde.util;

// This class implements the finger into the doubly-linked list.

class Finger<E> {
 
    Node<E> node;
    int index; // Index at which 'node' is located.
    int updateIndex;

    Finger(Node<E> node, int index) {
        this.node = node;
        this.index = index;
    }

    @Override
    public String toString() {
        return "[Finger; index = " + index + 
                ", item = " + ((node == null) ? "null" : node.item) + 
                "]";
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
