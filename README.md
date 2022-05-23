# IndexedLinkedList - an indexed, heuristic doubly-linked list in Java

This repository maintains the implementation of a linked list data structure that runs single-element operations in &Theta;(sqrt(n)) time.

The blog post explaining the (simple, high school level) math behind this data structure may be found behind [this link](http://coderodde.github.io/weblog/#eill).

Our `IndexedLinkedList` exhibits performance comparable to [Apache Commons Collections4 `org.apache.commons.collections4.list.TreeList.java`](https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/list/TreeList.java) (which runs all the single-element operations in log(N) time due to the AVL-tree algorithm), while (apart from having ceil(N/2) fingers, each consisting from a reference and an `int` value) having smaller memory footprint: for each node, our list maintains 3 references; each node in the `TreeList` consists of 3 references, 2 `int` values and 2 `boolean` values.

## Running time comparison

| Operation        | ArrayList      | java.util.LinkedList | coderodde LinkedList | TreeList           |
| ---------------- | -------------- | -------------------- | -------------------- | ------------------ |
| `add(int)`       | ***O(n)***     | ***O(n)***           | ***O(sqrt(n))***     | ***O(log n)***     |
| `addFirst`       | ***O(n)***     | ***O(1)***           | ***O(sqrt(n))***     | ***O(log n)***     |
| `addLast`        | ***O(1)***     | ***O(1)***           | ***O(1)***           | ***O(log n)***     |
| `get`            | ***O(1)***     | ***O(n)***           | ***O(sqrt(n))***     | ***O(log n)***     |
| `remove(int)`    | ***O(n)***     | ***O(n)***           | ***O(sqrt(n))***     | ***O(log n)***     |
| `removeFirst`    | ***O(n)***     | ***O(1)***           | ***O(sqrt(n))***     | ***O(log n)***     |
| `removeLast`     | ***O(1)***     | ***O(1)***           | ***O(1)***           | ***O(log n)***     |
| `remove(Object)` | ***O(n)***     | ***O(n)***           | ***O(n)***           | ***O(n)***         |
| `setAll`         | ***O(n)***     | ***O(n)***           | ***O(n)***           | ***O(n)***         |
| `prependAll`     | ***O(m + n)*** | ***O(m)***           | ***O(m + sqrt(n))*** | ***O(m log n)***   |
| `appendAll`      | ***O(m)***     | ***O(m)***           | ***O(m)***           | ***O(m + log n)*** |
| `insertAll`      | ***O(m + n)*** | ***O(m + n)***       | ***O(m + sqrt(n))*** | ***O(m log n)***   |

Above, ***n*** is the current size of a list, and ***m*** is the size of a newly added collection.

## Benchmark output

On a PC with a quad-core CPU with base speed 1,99 GHz and 256 kB L1 cache, 1 MB L2 cache and 8 MB L3 cache, the benchmark gives typically the following results:

```
<<< LinkedList seed = 1630547803891 >>>

=== WARMUP RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 85
java.util.LinkedList.addFirst in (ms): 9
java.util.ArrayList.addFirst in (ms): 437
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 28

com.github.coderodde.util.LinkedList.addLast in (ms): 1
java.util.LinkedList.addLast in (ms): 0
java.util.ArrayList.addLast in (ms): 0
org.apache.commons.collections4.list.TreeList.addLast in (ms): 40

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 98
java.util.LinkedList.add(int, E) in (ms): 1539
java.util.ArrayList.add(int, E) in (ms): 105
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 5

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 1
java.util.LinkedList.addAll(Collection) in (ms): 0
java.util.ArrayList.addAll(Collection) in (ms): 18
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 24

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 76
java.util.LinkedList.addAll(int, Collection) in (ms): 1427
java.util.ArrayList.addAll(int, Collection) in (ms): 80
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 23

com.github.coderodde.util.LinkedList.get(int) in (ms): 29
java.util.LinkedList.get(int) in (ms): 783
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 0

com.github.coderodde.util.LinkedList.removeFirst() in (ms): 9
java.util.LinkedList.removeFirst() in (ms): 1
java.util.ArrayList.removeFirst() in (ms): 90
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 1

com.github.coderodde.util.LinkedList.removeLast() in (ms): 4
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 1

com.github.coderodde.util.LinkedList.remove(int) in (ms): 1
java.util.LinkedList.remove(int) in (ms): 6
java.util.ArrayList.remove(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 1

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 1
java.util.LinkedList.remove(Object) in (ms): 2
java.util.ArrayList.remove(Object) in (ms): 47
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 22

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 35
java.util.LinkedList.iterator().add() in (ms): 7
java.util.ArrayList.iterator().add() in (ms): 619
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 94

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 36
java.util.LinkedList.iterator().remove() in (ms): 7
java.util.ArrayList.iterator().remove() in (ms): 733
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 38

com.github.coderodde.util.LinkedList.stream() in (ms): 34
java.util.LinkedList.stream() in (ms): 23
java.util.ArrayList.stream() in (ms): 8
org.apache.commons.collections4.list.TreeList.stream() in (ms): 3

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 18
java.util.LinkedList.stream().parallel() in (ms): 60
java.util.ArrayList.stream().parallel() in (ms): 11
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 27

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 428
java.util.LinkedList in (ms): 3864
java.util.ArrayList in (ms): 2149
org.apache.commons.collections4.list.TreeList in (ms): 307

=== BENCHMARK RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 46
java.util.LinkedList.addFirst in (ms): 2
java.util.ArrayList.addFirst in (ms): 453
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 26

com.github.coderodde.util.LinkedList.addLast in (ms): 3
java.util.LinkedList.addLast in (ms): 0
java.util.ArrayList.addLast in (ms): 0
org.apache.commons.collections4.list.TreeList.addLast in (ms): 24

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 80
java.util.LinkedList.add(int, E) in (ms): 1350
java.util.ArrayList.add(int, E) in (ms): 99
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 6

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 8
java.util.LinkedList.addAll(Collection) in (ms): 7
java.util.ArrayList.addAll(Collection) in (ms): 8
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 43

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 78
java.util.LinkedList.addAll(int, Collection) in (ms): 904
java.util.ArrayList.addAll(int, Collection) in (ms): 82
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 13

com.github.coderodde.util.LinkedList.get(int) in (ms): 34
java.util.LinkedList.get(int) in (ms): 624
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 0

com.github.coderodde.util.LinkedList.removeFirst() in (ms): 0
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 86
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 1

com.github.coderodde.util.LinkedList.removeLast() in (ms): 3
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 1
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 0

com.github.coderodde.util.LinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 0
java.util.ArrayList.remove(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 0

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 20
java.util.LinkedList.remove(Object) in (ms): 3
java.util.ArrayList.remove(Object) in (ms): 37
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 9

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 35
java.util.LinkedList.iterator().add() in (ms): 8
java.util.ArrayList.iterator().add() in (ms): 628
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 18

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 38
java.util.LinkedList.iterator().remove() in (ms): 2
java.util.ArrayList.iterator().remove() in (ms): 759
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 24

com.github.coderodde.util.LinkedList.stream() in (ms): 6
java.util.LinkedList.stream() in (ms): 0
java.util.ArrayList.stream() in (ms): 17
org.apache.commons.collections4.list.TreeList.stream() in (ms): 79

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 3
java.util.LinkedList.stream().parallel() in (ms): 15
java.util.ArrayList.stream().parallel() in (ms): 13
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 196

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 354
java.util.LinkedList in (ms): 2915
java.util.ArrayList in (ms): 2183
org.apache.commons.collections4.list.TreeList in (ms): 439
```
