# LinkedList - an indexed, heuristic doubly-linked list in Java

This repository maintains the implementation of a linked list data structure that runs single-element operations in &Theta;(sqrt(n)) time.

The blog post explaining the (simple, high school level) math behind this data structure may be found behind [this link](http://coderodde.github.io/weblog/#eill).

Our `LinkedList` exhibits performance comparable to [Apache Commons Collections4 `org.apache.commons.collections4.list.TreeList.java`](https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/list/TreeList.java) (which runs all the single-element operations in log(N) time due to the AVL-tree algorithm), while (apart from having ceil(N/2) fingers, each consisting from a reference and an `int` value) having smaller memory footprint: for each node, our list maintains 3 references; each node in the `TreeList` consists of 3 references, 2 `int` values and 2 `boolean` values.

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
<<< LinkedList seed = 1630488442461 >>>

=== WARMUP RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 100
java.util.LinkedList.addFirst in (ms): 18
java.util.ArrayList.addFirst in (ms): 412
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 31

com.github.coderodde.util.LinkedList.addLast in (ms): 8
java.util.LinkedList.addLast in (ms): 5
java.util.ArrayList.addLast in (ms): 4
org.apache.commons.collections4.list.TreeList.addLast in (ms): 41

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 110
java.util.LinkedList.add(int, E) in (ms): 1427
java.util.ArrayList.add(int, E) in (ms): 97
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 11

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 7
java.util.LinkedList.addAll(Collection) in (ms): 9
java.util.ArrayList.addAll(Collection) in (ms): 4
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 22

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 75
java.util.LinkedList.addAll(int, Collection) in (ms): 1367
java.util.ArrayList.addAll(int, Collection) in (ms): 74
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 14

com.github.coderodde.util.LinkedList.get(int) in (ms): 31
java.util.LinkedList.get(int) in (ms): 788
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 2

com.github.coderodde.util.LinkedList.removeFirst() in (ms): 223
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 88
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 1

com.github.coderodde.util.LinkedList.removeLast() in (ms): 54
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 2

com.github.coderodde.util.LinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 4
java.util.ArrayList.remove(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 0

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 15
java.util.LinkedList.remove(Object) in (ms): 8
java.util.ArrayList.remove(Object) in (ms): 41
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 23

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 38
java.util.LinkedList.iterator().add() in (ms): 8
java.util.ArrayList.iterator().add() in (ms): 607
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 93

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 36
java.util.LinkedList.iterator().remove() in (ms): 9
java.util.ArrayList.iterator().remove() in (ms): 741
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 27

com.github.coderodde.util.LinkedList.stream() in (ms): 34
java.util.LinkedList.stream() in (ms): 11
java.util.ArrayList.stream() in (ms): 67
org.apache.commons.collections4.list.TreeList.stream() in (ms): 16

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 32
java.util.LinkedList.stream().parallel() in (ms): 67
java.util.ArrayList.stream().parallel() in (ms): 14
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 40

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 763
java.util.LinkedList in (ms): 3721
java.util.ArrayList in (ms): 2150
org.apache.commons.collections4.list.TreeList in (ms): 323

=== BENCHMARK RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 41
java.util.LinkedList.addFirst in (ms): 0
java.util.ArrayList.addFirst in (ms): 482
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 16

com.github.coderodde.util.LinkedList.addLast in (ms): 0
java.util.LinkedList.addLast in (ms): 16
java.util.ArrayList.addLast in (ms): 3
org.apache.commons.collections4.list.TreeList.addLast in (ms): 17

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 80
java.util.LinkedList.add(int, E) in (ms): 1317
java.util.ArrayList.add(int, E) in (ms): 97
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 6

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 7
java.util.LinkedList.addAll(Collection) in (ms): 6
java.util.ArrayList.addAll(Collection) in (ms): 7
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 8

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 58
java.util.LinkedList.addAll(int, Collection) in (ms): 1277
java.util.ArrayList.addAll(int, Collection) in (ms): 67
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 13

com.github.coderodde.util.LinkedList.get(int) in (ms): 27
java.util.LinkedList.get(int) in (ms): 737
java.util.ArrayList.get(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 0

com.github.coderodde.util.LinkedList.removeFirst() in (ms): 90
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 67
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 0

com.github.coderodde.util.LinkedList.removeLast() in (ms): 52
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 0

com.github.coderodde.util.LinkedList.remove(int) in (ms): 1
java.util.LinkedList.remove(int) in (ms): 4
java.util.ArrayList.remove(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 0

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 6
java.util.LinkedList.remove(Object) in (ms): 3
java.util.ArrayList.remove(Object) in (ms): 38
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 10

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 52
java.util.LinkedList.iterator().add() in (ms): 17
java.util.ArrayList.iterator().add() in (ms): 618
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 17

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 32
java.util.LinkedList.iterator().remove() in (ms): 4
java.util.ArrayList.iterator().remove() in (ms): 767
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 24

com.github.coderodde.util.LinkedList.stream() in (ms): 5
java.util.LinkedList.stream() in (ms): 5
java.util.ArrayList.stream() in (ms): 0
org.apache.commons.collections4.list.TreeList.stream() in (ms): 0

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 82
java.util.LinkedList.stream().parallel() in (ms): 6
java.util.ArrayList.stream().parallel() in (ms): 4
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 156

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 533
java.util.LinkedList in (ms): 3392
java.util.ArrayList in (ms): 2152
org.apache.commons.collections4.list.TreeList in (ms): 267
```
