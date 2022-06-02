[![Build Status](https://app.travis-ci.com/coderodde/IndexedLinkedList.svg?branch=main)](https://app.travis-ci.com/coderodde/IndexedLinkedList) 
[![codecov](https://codecov.io/gh/coderodde/IndexedLinkedList/branch/main/graph/badge.svg)](https://codecov.io/gh/coderodde/IndexedLinkedList) 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.coderodde/IndexedLinkedList/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.github.coderodde/IndexedLinkedList)
[![javadoc](https://javadoc.io/badge2/io.github.coderodde/IndexedLinkedList/javadoc.svg)](https://javadoc.io/doc/io.github.coderodde/IndexedLinkedList)

# IndexedLinkedList - an indexed, heuristic doubly-linked list in Java

This repository maintains the implementation of a linked list data structure that runs single-element operations in &Theta;(sqrt(n)) time under mild assumptions.

The blog post explaining the (simple, high school level) math behind this data structure may be found behind [this link](http://coderodde.github.io/weblog/#meill).

Our `IndexedLinkedList` exhibits performance faster than [Apache Commons Collections4 `org.apache.commons.collections4.list.TreeList.java`](https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/list/TreeList.java) (which runs all the single-element operations in O(log(N)) time due to the AVL-tree algorithm), while (apart from having ceil(sqrt(N)) fingers, each consisting from a reference and an `int` value) having smaller memory footprint: for each node, our list maintains 3 references; each node in the `TreeList` consists of 3 references, 2 `int` values and 2 `boolean` values.

## Running time comparison

| Operation        | ArrayList      | java.util.LinkedList | IndexedLinkedList         | TreeList           |
| ---------------- | -------------- | -------------------- | ------------------------- | ------------------ |
| `add(int)`       | ***O(n)***     | ***O(n)***           | ***O(sqrt(n))***          | ***O(log n)***     |
| `addFirst`       | ***O(n)***     | ***O(1)***           | ***O(sqrt(n))***          | ***O(log n)***     |
| `addLast`        | ***O(1)***     | ***O(1)***           | ***O(1)***                | ***O(log n)***     |
| `get`            | ***O(1)***     | ***O(n)***           | ***O(sqrt(n))***          | ***O(log n)***     |
| `remove(int)`    | ***O(n)***     | ***O(n)***           | ***O(sqrt(n))***          | ***O(log n)***     |
| `removeFirst`    | ***O(n)***     | ***O(1)***           | ***O(sqrt(n))***          | ***O(log n)***     |
| `removeLast`     | ***O(1)***     | ***O(1)***           | ***O(1)***                | ***O(log n)***     |
| `remove(Object)` | ***O(n)***     | ***O(n)***           | ***O(n)***                | ***O(n)***         |
| `setAll`         | ***O(n)***     | ***O(n)***           | ***O(n)***                | ***O(n)***         |
| `prependAll`     | ***O(m + n)*** | ***O(m)***           | ***O(m + sqrt(n))***      | ***O(m log n)***   |
| `appendAll`      | ***O(m)***     | ***O(m)***           | ***O(m)***                | ***O(m + log n)*** |
| `insertAll`      | ***O(m + n)*** | ***O(m + n)***       | ***O(m + sqrt(n))***      | ***O(m log n)***   |
| `removeAll`      | ***O(m + n)*** | ***O(nm)***          | ***O(nf + n * sqrt(n))*** | ***O(nmf)***       |

Above, ***n*** is the current size of a list, ***m*** is the size of a newly added collection, and ***f*** is the cost of consulting whether an element is contained in a filter collectoin.

## Benchmark output

On a PC with a quad-core CPU with base speed 1,99 GHz and 256 kB L1 cache, 1 MB L2 cache and 8 MB L3 cache, the benchmark gives typically the following results:

```
<<< Benchmark seed = 1654082923714 >>>

<<< Flags >>>
runSubListClear: true
runRemoveAll   : true
runSort        : true

=== WARMUP RUN ===
com.github.coderodde.util.IndexedLinkedList.addFirst in (ms): 32
java.util.LinkedList.addFirst in (ms): 1
java.util.ArrayList.addFirst in (ms): 475
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 37

com.github.coderodde.util.IndexedLinkedList.addLast in (ms): 11
java.util.LinkedList.addLast in (ms): 32
java.util.ArrayList.addLast in (ms): 4
org.apache.commons.collections4.list.TreeList.addLast in (ms): 37

com.github.coderodde.util.IndexedLinkedList.add(int, E) in (ms): 17
java.util.LinkedList.add(int, E) in (ms): 1696
java.util.ArrayList.add(int, E) in (ms): 113
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 16

com.github.coderodde.util.IndexedLinkedList.addAll(Collection) in (ms): 9
java.util.LinkedList.addAll(Collection) in (ms): 27
java.util.ArrayList.addAll(Collection) in (ms): 6
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 29

com.github.coderodde.util.IndexedLinkedList.addAll(int, Collection) in (ms): 25
java.util.LinkedList.addAll(int, Collection) in (ms): 1438
java.util.ArrayList.addAll(int, Collection) in (ms): 90
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 15

com.github.coderodde.util.IndexedLinkedList.get(int) in (ms): 5
java.util.LinkedList.get(int) in (ms): 796
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 2

com.github.coderodde.util.IndexedLinkedList.removeFirst() in (ms): 20
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 104
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 2

com.github.coderodde.util.IndexedLinkedList.removeLast() in (ms): 0
java.util.LinkedList.removeLast() in (ms): 1
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 1

com.github.coderodde.util.IndexedLinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 0
java.util.ArrayList.remove(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 0

com.github.coderodde.util.IndexedLinkedList.remove(Object) in (ms): 26
java.util.LinkedList.remove(Object) in (ms): 12
java.util.ArrayList.remove(Object) in (ms): 54
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 37

com.github.coderodde.util.IndexedLinkedList.iterator().add() in (ms): 20
java.util.LinkedList.iterator().add() in (ms): 18
java.util.ArrayList.iterator().add() in (ms): 882
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 47

com.github.coderodde.util.IndexedLinkedList.iterator().remove() in (ms): 17
java.util.LinkedList.iterator().remove() in (ms): 23
java.util.ArrayList.iterator().remove() in (ms): 1032
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 36

com.github.coderodde.util.IndexedLinkedList.stream() in (ms): 20
java.util.LinkedList.stream() in (ms): 11
java.util.ArrayList.stream() in (ms): 17
org.apache.commons.collections4.list.TreeList.stream() in (ms): 111

com.github.coderodde.util.IndexedLinkedList.stream().parallel() in (ms): 68
java.util.LinkedList.stream().parallel() in (ms): 114
java.util.ArrayList.stream().parallel() in (ms): 22
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 155

com.github.coderodde.util.IndexedLinkedList.removeAll() in (ms): 1
java.util.LinkedList.removeAll() in (ms): 104
java.util.ArrayList.removeAll() in (ms): 1
org.apache.commons.collections4.list.TreeList.removeAll() in (ms): 281

com.github.coderodde.util.IndexedLinkedList.subList(...).clear() in (ms): 10
java.util.LinkedList.subList(...).clear() in (ms): 49
java.util.ArrayList.subList(...).clear() in (ms): 6
org.apache.commons.collections4.list.TreeList.subList(...).clear() in (ms): 234

IndexedLinkedList.subList().sort() in (ms): 385
LinkedList.subList().sort() in (ms): 285
ArrayList.subList().sort() in (ms): 262
TreeList.subList().sort() in (ms): 326

--- Total time elapsed ---
com.github.coderodde.util.IndexedLinkedList in (ms): 666
java.util.LinkedList in (ms): 4607
java.util.ArrayList in (ms): 3068
org.apache.commons.collections4.list.TreeList in (ms): 1366

=== BENCHMARK RUN ===
com.github.coderodde.util.IndexedLinkedList.addFirst in (ms): 25
java.util.LinkedList.addFirst in (ms): 4
java.util.ArrayList.addFirst in (ms): 526
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 17

com.github.coderodde.util.IndexedLinkedList.addLast in (ms): 10
java.util.LinkedList.addLast in (ms): 2
java.util.ArrayList.addLast in (ms): 4
org.apache.commons.collections4.list.TreeList.addLast in (ms): 18

com.github.coderodde.util.IndexedLinkedList.add(int, E) in (ms): 13
java.util.LinkedList.add(int, E) in (ms): 1581
java.util.ArrayList.add(int, E) in (ms): 110
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 6

com.github.coderodde.util.IndexedLinkedList.addAll(Collection) in (ms): 8
java.util.LinkedList.addAll(Collection) in (ms): 6
java.util.ArrayList.addAll(Collection) in (ms): 5
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 127

com.github.coderodde.util.IndexedLinkedList.addAll(int, Collection) in (ms): 12
java.util.LinkedList.addAll(int, Collection) in (ms): 1169
java.util.ArrayList.addAll(int, Collection) in (ms): 79
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 14

com.github.coderodde.util.IndexedLinkedList.get(int) in (ms): 5
java.util.LinkedList.get(int) in (ms): 675
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 0

com.github.coderodde.util.IndexedLinkedList.removeFirst() in (ms): 2
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 87
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 0

com.github.coderodde.util.IndexedLinkedList.removeLast() in (ms): 0
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 1

com.github.coderodde.util.IndexedLinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 5
java.util.ArrayList.remove(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 0

com.github.coderodde.util.IndexedLinkedList.remove(Object) in (ms): 7
java.util.LinkedList.remove(Object) in (ms): 3
java.util.ArrayList.remove(Object) in (ms): 46
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 10

com.github.coderodde.util.IndexedLinkedList.iterator().add() in (ms): 21
java.util.LinkedList.iterator().add() in (ms): 13
java.util.ArrayList.iterator().add() in (ms): 829
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 23

com.github.coderodde.util.IndexedLinkedList.iterator().remove() in (ms): 27
java.util.LinkedList.iterator().remove() in (ms): 4
java.util.ArrayList.iterator().remove() in (ms): 998
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 32

com.github.coderodde.util.IndexedLinkedList.stream() in (ms): 5
java.util.LinkedList.stream() in (ms): 5
java.util.ArrayList.stream() in (ms): 4
org.apache.commons.collections4.list.TreeList.stream() in (ms): 12

com.github.coderodde.util.IndexedLinkedList.stream().parallel() in (ms): 4
java.util.LinkedList.stream().parallel() in (ms): 37
java.util.ArrayList.stream().parallel() in (ms): 5
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 181

com.github.coderodde.util.IndexedLinkedList.removeAll() in (ms): 0
java.util.LinkedList.removeAll() in (ms): 98
java.util.ArrayList.removeAll() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeAll() in (ms): 244

com.github.coderodde.util.IndexedLinkedList.subList(...).clear() in (ms): 4
java.util.LinkedList.subList(...).clear() in (ms): 37
java.util.ArrayList.subList(...).clear() in (ms): 2
org.apache.commons.collections4.list.TreeList.subList(...).clear() in (ms): 252

IndexedLinkedList.subList().sort() in (ms): 303
LinkedList.subList().sort() in (ms): 329
ArrayList.subList().sort() in (ms): 283
TreeList.subList().sort() in (ms): 299

--- Total time elapsed ---
com.github.coderodde.util.IndexedLinkedList in (ms): 446
java.util.LinkedList in (ms): 3968
java.util.ArrayList in (ms): 2979
org.apache.commons.collections4.list.TreeList in (ms): 1236
```
