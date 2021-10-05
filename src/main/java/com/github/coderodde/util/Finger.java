package com.github.coderodde.util;

// This class implements the actual doubly-linked list node data type.
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
        return "[Finger; index = " + index + ", item = " + node.item + "]";
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
