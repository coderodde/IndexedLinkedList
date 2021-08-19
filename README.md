# LinkedList - an indexed, heuristic doubly-linked list in Java

This repository maintains the implementation of a linked list data structure that runs single-element operations in &Theta;(sqrt(n)) time.

The blog post explaining the (simple, high school level) math behind this data structure may be found behind [this link](http://coderodde.github.io/weblog/#eill).

Our `LinkedList` exhibits performance comparable to [Apache Commons Collections4 `org.apache.commons.collections4.list.TreeList.java`](https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/list/TreeList.java) (which runs all the single-element operations in log(N) time due to the AVL-tree algorithm), while (apart from having ceil(N/2) fingers, each consisting from a reference and an `int` value) having smaller memory footprint: for each node, our list maintains 3 references; each node in the `TreeList` consists of 3 references, 2 `int` values and 2 `boolean` values.

## Running time comparison

| Operation        | ArrayList | java.util.LinkedList | coderodde LinkedList |
| ---------------- | --------- | -------------------- | -------------------- |
| `add(int)`       | O(n)      | O(n)                 | O(sqrt(n))           |
| `addFirst`       | O(n)      | O(1)                 | O(sqrt(n))           |
| `addLast`        | O(1)      | O(1)                 | O(1)                 |
| `get`            | O(1)      | O(n)                 | O(sqrt(n))           |
| `remove(int)`    | O(n)      | O(n)                 | O(sqrt(n))           |
| `removeFirst`    | O(n)      | O(1)                 | O(sqrt(n))           |
| `removeLast`     | O(1)      | O(1)                 | O(1)                 |
| `remove(Object)` | O(n)      | O(n)                 | O(n)                 |
| `setAll`         | O(m)      | O(m)                 | O(m)                 |
| `prependAll`     | O(m + n)  | O(m)                 | O(m + sqrt(n))       |
| `appendAll`      | O(m)      | O(m)                 | O(m)                 |
| `insertAll`      | O(m + n)  | O(m + n)             | O(m + sqrt(n))       |

Above, `n` is the current size of a list, and `m` is the size of a newly added collection.

## Benchmark output

On a PC with a quad-core CPU with base speed 1,99 GHz and 256 kB L1 cache, 1 MB L2 cache and 8 MB L3 cache, the benchmark gives typically the following results:

```
<<< LinkedList seed = 1629264992750 >>>

=== WARMUP RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 57
java.util.LinkedList.addFirst in (ms): 5
java.util.ArrayList.addFirst in (ms): 493
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 34

com.github.coderodde.util.LinkedList.addLast in (ms): 8
java.util.LinkedList.addLast in (ms): 27
java.util.ArrayList.addLast in (ms): 3
org.apache.commons.collections4.list.TreeList.addLast in (ms): 40

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 40
java.util.LinkedList.add(int, E) in (ms): 1821
java.util.ArrayList.add(int, E) in (ms): 99
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 13

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 26
java.util.LinkedList.addAll(Collection) in (ms): 22
java.util.ArrayList.addAll(Collection) in (ms): 5
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 23

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 37
java.util.LinkedList.addAll(int, Collection) in (ms): 1431
java.util.ArrayList.addAll(int, Collection) in (ms): 74
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 15

com.github.coderodde.util.LinkedList.remove(int) in (ms): 35
java.util.LinkedList.remove(int) in (ms): 4138
java.util.ArrayList.remove(int) in (ms): 181
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 11

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 12
java.util.LinkedList.remove(Object) in (ms): 10
java.util.ArrayList.remove(Object) in (ms): 39
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 22

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 18
java.util.LinkedList.iterator().add() in (ms): 13
java.util.ArrayList.iterator().add() in (ms): 596
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 19

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 69
java.util.LinkedList.iterator().remove() in (ms): 8
java.util.ArrayList.iterator().remove() in (ms): 738
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 28

com.github.coderodde.util.LinkedList.stream() in (ms): 17
java.util.LinkedList.stream() in (ms): 16
java.util.ArrayList.stream() in (ms): 7
org.apache.commons.collections4.list.TreeList.stream() in (ms): 68

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 41
java.util.LinkedList.stream().parallel() in (ms): 29
java.util.ArrayList.stream().parallel() in (ms): 88
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 123

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 360
java.util.LinkedList in (ms): 7520
java.util.ArrayList in (ms): 2323
org.apache.commons.collections4.list.TreeList in (ms): 396

=== BENCHMARK RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 37
java.util.LinkedList.addFirst in (ms): 4
java.util.ArrayList.addFirst in (ms): 415
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 15

com.github.coderodde.util.LinkedList.addLast in (ms): 7
java.util.LinkedList.addLast in (ms): 4
java.util.ArrayList.addLast in (ms): 3
org.apache.commons.collections4.list.TreeList.addLast in (ms): 25

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 18
java.util.LinkedList.add(int, E) in (ms): 1410
java.util.ArrayList.add(int, E) in (ms): 95
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 6

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 9
java.util.LinkedList.addAll(Collection) in (ms): 7
java.util.ArrayList.addAll(Collection) in (ms): 5
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 2

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 18
java.util.LinkedList.addAll(int, Collection) in (ms): 1376
java.util.ArrayList.addAll(int, Collection) in (ms): 65
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 16

com.github.coderodde.util.LinkedList.remove(int) in (ms): 22
java.util.LinkedList.remove(int) in (ms): 4055
java.util.ArrayList.remove(int) in (ms): 171
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 8

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 8
java.util.LinkedList.remove(Object) in (ms): 4
java.util.ArrayList.remove(Object) in (ms): 36
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 1

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 32
java.util.LinkedList.iterator().add() in (ms): 7
java.util.ArrayList.iterator().add() in (ms): 595
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 73

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 58
java.util.LinkedList.iterator().remove() in (ms): 4
java.util.ArrayList.iterator().remove() in (ms): 726
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 24

com.github.coderodde.util.LinkedList.stream() in (ms): 4
java.util.LinkedList.stream() in (ms): 4
java.util.ArrayList.stream() in (ms): 5
org.apache.commons.collections4.list.TreeList.stream() in (ms): 7

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 3
java.util.LinkedList.stream().parallel() in (ms): 22
java.util.ArrayList.stream().parallel() in (ms): 4
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 29

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 216
java.util.LinkedList in (ms): 6897
java.util.ArrayList in (ms): 2120
org.apache.commons.collections4.list.TreeList in (ms): 206
```
