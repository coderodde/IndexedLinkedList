package com.github.coderodde.util;

class Node<E> {
    
    E item;
    Node<E> prev;
    Node<E> next;
    
    Node(E item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "[Node; item = " + item + "]";
    }
}
