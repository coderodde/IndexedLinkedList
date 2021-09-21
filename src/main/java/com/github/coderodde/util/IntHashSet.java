package com.github.coderodde.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
        int targetCollisionChainIndex = integer & mask;
        Node node = table[targetCollisionChainIndex];
        
        while (node != null) {
            if (node.integer == integer) {
                return;
            }
            
            node = node.next;
        }
        
        size++;
        
        if (size > table.length) {
            Node[] newTable = new Node[2 * table.length];
            mask = newTable.length - 1;
            
            for (Node currentNode : table) {
                while (currentNode != null) {
                    Node nextNode = currentNode.next;
                    
                    int newTableHash = currentNode.integer & mask;
                    currentNode.next = newTable[newTableHash];
                    newTable[newTableHash] = currentNode;
                    
                    currentNode = nextNode;
                }
            }
            
            table = newTable;
            targetCollisionChainIndex = integer & mask;
        }
        
        Node newNode = new Node(integer, table[targetCollisionChainIndex]);
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
        int targetCollisionChainIndex = integer & mask;
        Node node = table[targetCollisionChainIndex];
        Node prev = null;
        
        while (node != null) {
            if (node.integer == integer) {
                break;
            }
            
            prev = node;
            node = node.next;
        }
        
        if (node == null) 
            return;

        size--;
        
        if (size * 4 <= table.length && table.length != INITIAL_CAPACITY) {
            Node[] newTable = new Node[table.length / 2];
            mask = newTable.length - 1;
            
            for (Node currentNode : table) {
                while (currentNode != null) {
                    if (currentNode == node) {
                        // Omit the node with the target integer:
                        currentNode = currentNode.next;
                        continue;
                    }
                    
                    Node nextNode = currentNode.next;
                    
                    int newTableHash = currentNode.integer & mask;
                    currentNode.next = newTable[newTableHash];
                    newTable[newTableHash] = currentNode;
                    
                    currentNode = nextNode;
                }
            }
            
            table = newTable;
        } else  if (prev == null) {
            table[targetCollisionChainIndex] = 
                    table[targetCollisionChainIndex].next;
        } else {
            prev.next = prev.next.next;
        }
    }

    public void clear() {
         size = 0;
         table = new Node[INITIAL_CAPACITY];
         mask = table.length - 1;
    }
    
    private static final int DATA_LENGTH = 5_000_000;
    

    
    public static void main(String[] args) {
        Random random = new Random(10L);
        
        int[] addData      = getAddData(random);
        int[] containsData = getContainsData(random);
        int[] removeData   = getRemoveData(random);
        
        for (int iter = 0; iter < 5; iter++) {
            System.out.println(">>> Iteration: " + (iter + 1) + "/5");
            
            IntHashSet myset = new IntHashSet();
            Set<Integer> set = new HashSet<>();

            long start = System.currentTimeMillis();
            for (int i : addData) {
                myset.add(i);
            }
            long end = System.currentTimeMillis();

            System.out.println("    IntHashSet.add in " + (end - start));

            start = System.currentTimeMillis();
            for (int i : addData) {
                set.add(i);
            }
            end = System.currentTimeMillis();

            System.out.println("    HashSet.add in " + (end - start) + "\n");

            start = System.currentTimeMillis();
            for (int i : containsData) {
                myset.contains(i);
            }
            end = System.currentTimeMillis();

            System.out.println("    IntHashSet.contains in " + (end - start));

            start = System.currentTimeMillis();
            for (int i : containsData) {
                set.contains(i);
            }
            end = System.currentTimeMillis();

            System.out.println("    HashSet.contains in " + (end - start) + 
                    "\n");

            start = System.currentTimeMillis();
            for (int i : removeData) {
                myset.remove(i);
            }
            end = System.currentTimeMillis();

            System.out.println("    IntHashSet.remove in " + (end - start));

            start = System.currentTimeMillis();
            for (int i : removeData) {
                set.remove(i);
            }
            end = System.currentTimeMillis();

            System.out.println("    HashSet.remove in " + (end - start) + "\n");
        }
    }
        
    private static int[] getAddData(Random random) {
        return getData(DATA_LENGTH, 3 * DATA_LENGTH / 2, random);
    }
        
    private static int[] getContainsData(Random random) {
        return getData(DATA_LENGTH, 3 * DATA_LENGTH / 2, random);
    }
        
    private static int[] getRemoveData(Random random) {
        return getData(DATA_LENGTH, 3 * DATA_LENGTH / 2, random);
    }
    
    private static int[] getData(int length, int maxValue, Random random) {
        int[] data = new int[length];
        
        for (int i = 0; i < length; i++) {
            data[i] = random.nextInt(maxValue + 1);
        }
        
        return data;
    }
}
