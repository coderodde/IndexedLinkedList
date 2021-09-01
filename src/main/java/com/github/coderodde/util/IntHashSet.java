package com.github.coderodde.util;

/**
 * This class implements a simple hash set for non-negative {@code int} values.
 * It is used in the {@link com.github.coderodde.util.LinkedList} in order to 
 * keep track of nodes that are being pointed to by fingers.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 29, 2021)
 * @since 1.6 (Aug 29, 2021)
 */
public class IntHashSet {

    private static final int INITIAL_CAPACITY = 8;
    private static final float MAXIMUM_LOAD_FACTOR = 0.75f;

    private static final class Node {
        Node next;
        int integer;

        Node(int integer, Node next) {
            this.integer = integer;
            this.next = next;
        }

        @Override
        public String toString() {
            return "Chain node, integer = " + integer;
        }
    }

    private Node[] table = new Node[INITIAL_CAPACITY];
    private int size = 0;
    private int mask = INITIAL_CAPACITY - 1;
    
    @Override
    public String toString() {
        return "size = " + size;
    }

    public void add(int integer) {
        if (contains(integer)) {
            return;
        }

        size++;

        if (shouldExpand())
            expand();

        final int targetCollisionChainIndex = integer & mask;
        final Node newNode = 
                new Node(
                        integer, 
                        table[targetCollisionChainIndex]);

        newNode.next = table[targetCollisionChainIndex];
        table[targetCollisionChainIndex] = newNode;
    }

    public boolean contains(int integer) {
        final int collisionChainIndex = integer & mask;
        Node node = table[collisionChainIndex];

        while (node != null) {
            if (node.integer == integer) {
                return true;
            }

            node = node.next;
        }

        return false;
    }

    public void remove(int integer) {
        if (!contains(integer)) {
            return;
        }

        size--;

        if (shouldContract()) 
            contract();

        final int targetCollisionChainIndex = integer & mask;

        Node current = 
                table[targetCollisionChainIndex];

        Node previous = null;

        while (current != null) {
            Node next = current.next;

            if (current.integer == integer) {
                if (previous == null) {
                    table[targetCollisionChainIndex] = next;
                } else {
                    previous.next = next;
                }

                return;
            }

            previous = current;
            current = next;
        }
    }

    public void clear() {
         size = 0;
         table = new Node[INITIAL_CAPACITY];
         mask = table.length - 1;
    }

    // Keep add(int) an amortized O(1)
    private boolean shouldExpand() {
        return size > table.length * MAXIMUM_LOAD_FACTOR;
    }

    // Keep remove(int) an amortized O(1)
    private boolean shouldContract() {
        if (table.length == INITIAL_CAPACITY) {
            // Do not ocntract below INITIAL_CAPACITY:
            return false;
        }
        
        return size < table.length / 4;
    }

    private void expand() {
        Node[] newTable = 
                new Node[table.length * 2];

        rehash(newTable);
        table = newTable;
        mask = table.length - 1;
    }

    private void contract() {
        Node[] newTable = 
                new Node[table.length / 4];

        rehash(newTable);
        table = newTable;
        mask = table.length - 1;
    }

    private void rehash(Node[] newTable) {
        for (Node node : table) {
            while (node != null) {
                final Node next = node.next;
                final int rehashedIndex = getHashValue(node.integer, newTable);

                node.next = newTable[rehashedIndex];
                newTable[rehashedIndex] = node;
                node = next;
            }
        }
    }

    private static int getHashValue(
            int integer, 
            Node[] newTable) {
        return integer & (newTable.length - 1);
    }
}
