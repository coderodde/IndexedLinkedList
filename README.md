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
<<< Benchmark seed = 1653369830960 >>>

=== WARMUP RUN ===
com.github.coderodde.util.IndexedLinkedList.addFirst in (ms): 34
java.util.LinkedList.addFirst in (ms): 18
java.util.ArrayList.addFirst in (ms): 424
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 40

com.github.coderodde.util.IndexedLinkedList.addLast in (ms): 8
java.util.LinkedList.addLast in (ms): 24
java.util.ArrayList.addLast in (ms): 4
org.apache.commons.collections4.list.TreeList.addLast in (ms): 25

com.github.coderodde.util.IndexedLinkedList.add(int, E) in (ms): 16
java.util.LinkedList.add(int, E) in (ms): 1388
java.util.ArrayList.add(int, E) in (ms): 101
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 11

com.github.coderodde.util.IndexedLinkedList.addAll(Collection) in (ms): 23
java.util.LinkedList.addAll(Collection) in (ms): 20
java.util.ArrayList.addAll(Collection) in (ms): 4
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 28

com.github.coderodde.util.IndexedLinkedList.addAll(int, Collection) in (ms): 19
java.util.LinkedList.addAll(int, Collection) in (ms): 1158
java.util.ArrayList.addAll(int, Collection) in (ms): 78
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 14

com.github.coderodde.util.IndexedLinkedList.get(int) in (ms): 4
java.util.LinkedList.get(int) in (ms): 660
java.util.ArrayList.get(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 2

com.github.coderodde.util.IndexedLinkedList.removeFirst() in (ms): 12
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 93
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 1

com.github.coderodde.util.IndexedLinkedList.removeLast() in (ms): 10
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 2

com.github.coderodde.util.IndexedLinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 5
java.util.ArrayList.remove(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 1

com.github.coderodde.util.IndexedLinkedList.remove(Object) in (ms): 21
java.util.LinkedList.remove(Object) in (ms): 9
java.util.ArrayList.remove(Object) in (ms): 41
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 28

com.github.coderodde.util.IndexedLinkedList.iterator().add() in (ms): 15
java.util.LinkedList.iterator().add() in (ms): 10
java.util.ArrayList.iterator().add() in (ms): 615
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 22

com.github.coderodde.util.IndexedLinkedList.iterator().remove() in (ms): 20
java.util.LinkedList.iterator().remove() in (ms): 0
java.util.ArrayList.iterator().remove() in (ms): 761
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 19

com.github.coderodde.util.IndexedLinkedList.stream() in (ms): 35
java.util.LinkedList.stream() in (ms): 17
java.util.ArrayList.stream() in (ms): 61
org.apache.commons.collections4.list.TreeList.stream() in (ms): 17

com.github.coderodde.util.IndexedLinkedList.stream().parallel() in (ms): 39
java.util.LinkedList.stream().parallel() in (ms): 30
java.util.ArrayList.stream().parallel() in (ms): 10
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 86

com.github.coderodde.util.IndexedLinkedList.subList(...).clear() in (ms): 0
java.util.LinkedList.subList(...).clear() in (ms): 15
java.util.ArrayList.subList(...).clear() in (ms): 20
org.apache.commons.collections4.list.TreeList.subList(...).clear() in (ms): 145

--- Total time elapsed ---
com.github.coderodde.util.IndexedLinkedList in (ms): 256
java.util.LinkedList in (ms): 3354
java.util.ArrayList in (ms): 2214
org.apache.commons.collections4.list.TreeList in (ms): 441

=== BENCHMARK RUN ===
com.github.coderodde.util.IndexedLinkedList.addFirst in (ms): 20
java.util.LinkedList.addFirst in (ms): 5
java.util.ArrayList.addFirst in (ms): 457
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 13

com.github.coderodde.util.IndexedLinkedList.addLast in (ms): 6
java.util.LinkedList.addLast in (ms): 3
java.util.ArrayList.addLast in (ms): 2
org.apache.commons.collections4.list.TreeList.addLast in (ms): 16

com.github.coderodde.util.IndexedLinkedList.add(int, E) in (ms): 9
java.util.LinkedList.add(int, E) in (ms): 1430
java.util.ArrayList.add(int, E) in (ms): 96
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 6

com.github.coderodde.util.IndexedLinkedList.addAll(Collection) in (ms): 5
java.util.LinkedList.addAll(Collection) in (ms): 1
java.util.ArrayList.addAll(Collection) in (ms): 0
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 0

com.github.coderodde.util.IndexedLinkedList.addAll(int, Collection) in (ms): 10
java.util.LinkedList.addAll(int, Collection) in (ms): 1415
java.util.ArrayList.addAll(int, Collection) in (ms): 69
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 11

com.github.coderodde.util.IndexedLinkedList.get(int) in (ms): 5
java.util.LinkedList.get(int) in (ms): 795
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 0

com.github.coderodde.util.IndexedLinkedList.removeFirst() in (ms): 9
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 72
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 0

com.github.coderodde.util.IndexedLinkedList.removeLast() in (ms): 9
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 0

com.github.coderodde.util.IndexedLinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 6
java.util.ArrayList.remove(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 0

com.github.coderodde.util.IndexedLinkedList.remove(Object) in (ms): 7
java.util.LinkedList.remove(Object) in (ms): 3
java.util.ArrayList.remove(Object) in (ms): 38
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 10

com.github.coderodde.util.IndexedLinkedList.iterator().add() in (ms): 17
java.util.LinkedList.iterator().add() in (ms): 6
java.util.ArrayList.iterator().add() in (ms): 625
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 16

com.github.coderodde.util.IndexedLinkedList.iterator().remove() in (ms): 11
java.util.LinkedList.iterator().remove() in (ms): 1
java.util.ArrayList.iterator().remove() in (ms): 761
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 24

com.github.coderodde.util.IndexedLinkedList.stream() in (ms): 5
java.util.LinkedList.stream() in (ms): 7
java.util.ArrayList.stream() in (ms): 5
org.apache.commons.collections4.list.TreeList.stream() in (ms): 13

com.github.coderodde.util.IndexedLinkedList.stream().parallel() in (ms): 2
java.util.LinkedList.stream().parallel() in (ms): 22
java.util.ArrayList.stream().parallel() in (ms): 3
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 16

com.github.coderodde.util.IndexedLinkedList.subList(...).clear() in (ms): 3
java.util.LinkedList.subList(...).clear() in (ms): 21
java.util.ArrayList.subList(...).clear() in (ms): 1
org.apache.commons.collections4.list.TreeList.subList(...).clear() in (ms): 135

--- Total time elapsed ---
com.github.coderodde.util.IndexedLinkedList in (ms): 118
java.util.LinkedList in (ms): 3715
java.util.ArrayList in (ms): 2130
org.apache.commons.collections4.list.TreeList in (ms): 260
```
