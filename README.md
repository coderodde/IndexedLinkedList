[![Build Status](https://app.travis-ci.com/coderodde/IndexedLinkedList.svg?branch=main)](https://app.travis-ci.com/coderodde/IndexedLinkedList) 
[![codecov](https://codecov.io/gh/coderodde/IndexedLinkedList/branch/main/graph/badge.svg)](https://codecov.io/gh/coderodde/IndexedLinkedList) 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.coderodde/IndexedLinkedList.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.coderodde%22%20AND%20a:%22IndexedLinkedList%22)
[![javadoc](https://javadoc.io/badge2/io.github.coderodde/IndexedLinkedList/javadoc.svg)](https://javadoc.io/doc/io.github.coderodde/IndexedLinkedList)

# IndexedLinkedList - an indexed, heuristic doubly-linked list in Java

This repository maintains the implementation of a linked list data structure that runs single-element operations in &Theta;(sqrt(n)) time under mild assumptions.

The blog post explaining the (simple, high school level) math behind this data structure may be found behind [this link](http://coderodde.github.io/weblog/#meill).

Our `IndexedLinkedList` exhibits performance faster than [Apache Commons Collections4 `org.apache.commons.collections4.list.TreeList.java`](https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/list/TreeList.java) (which runs all the single-element operations in O(log(N)) time due to the AVL-tree algorithm), while (apart from having ceil(sqrt(N)) fingers, each consisting from a reference and an `int` value) having smaller memory footprint: for each node, our list maintains 3 references; each node in the `TreeList` consists of 3 references, 2 `int` values and 2 `boolean` values.

## Running time comparison

| Operation        | ArrayList      | LinkedList     | IndexedLinkedList         | TreeList                 |
| ---------------- | -------------- | -------------- | ------------------------- | ------------------------ |
| `add(int)`       | ***O(n)***     | ***O(n)***     | ***O(sqrt(n))***          | ***O(log n)***           |
| `addFirst`       | ***O(n)***     | ***O(1)***     | ***O(sqrt(n))***          | ***O(log n)***           |
| `addLast`        | ***O(1)***     | ***O(1)***     | ***O(1)***                | ***O(log n)***           |
| `get`            | ***O(1)***     | ***O(n)***     | ***O(sqrt(n))***          | ***O(log n)***           |
| `remove(int)`    | ***O(n)***     | ***O(n)***     | ***O(sqrt(n))***          | ***O(log n)***           |
| `removeFirst`    | ***O(n)***     | ***O(1)***     | ***O(sqrt(n))***          | ***O(log n)***           |
| `removeLast`     | ***O(1)***     | ***O(1)***     | ***O(1)***                | ***O(log n)***           |
| `remove(Object)` | ***O(n)***     | ***O(n)***     | ***O(n)***                | ***O(n)***               |
| `setAll`         | ***O(n)***     | ***O(n)***     | ***O(n)***                | ***O(n)***               |
| `prependAll`     | ***O(m + n)*** | ***O(m)***     | ***O(m + sqrt(n))***      | ***O(m log n)***         |
| `appendAll`      | ***O(m)***     | ***O(m)***     | ***O(m)***                | ***O(m + log n)***       |
| `insertAll`      | ***O(m + n)*** | ***O(m + n)*** | ***O(m + sqrt(n))***      | ***O(m log n)***         |
| `removeAll`      | ***O(nf)***    | ***O(nf)***    | ***O(nf + n * sqrt(n))*** | ***O(nf + n * log(n))*** |      |

Above, ***n*** is the current size of a list, ***m*** is the size of a newly added collection, and ***f*** is the cost of consulting whether an element is contained in a filter collectoin.

## Benchmark output
On a PC with a quad-core CPU with base speed 1,99 GHz and 256 kB L1 cache, 1 MB L2 cache and 8 MB L3 cache, the benchmark gives typically the following results:

### Benchmark with JMH

#### Small size data

| Operation                               | ms      |
|-----------------------------------------|---------|
|                                         |         |
|           `profileArrayListAddAtIndex`  |   0,084 |
|          `profileLinkedListAddAtIndex`  |   0,186 |
|           `profileRoddeListAddAtIndex`  |   0,114 |
|            `profileTreeListAddAtIndex`  |   0,153 |
|                                         |         |
|        `profileArrayListAddCollection`  |   0,133 |
|       `profileLinkedListAddCollection`  |   0,118 |
|        `profileRoddeListAddCollection`  |   0,131 |
|         `profileTreeListAddCollection`  |   0,408 |
|                                         |         |
|  `profileArrayListAddCollectionAtIndex` |   0,348 |
| `profileLinkedListAddCollectionAtIndex` |   2,898 |
|  `profileRoddeListAddCollectionAtIndex` |   0,315 |
|   `profileTreeListAddCollectionAtIndex` |   1,883 |
|                                                   |
|              `profileArrayListAddFirst` |   0,074 |
|             `profileLinkedListAddFirst` |   0,007 |
|              `profileRoddeListAddFirst` |   0,043 |
|               `profileTreeListAddFirst` |   0,221 |
|                                         |         |
|               `profileArrayListAddLast` |   0,005 |
|              `profileLinkedListAddLast` |   0,006 |
|               `profileRoddeListAddLast` |   0,010 |
|                `profileTreeListAddLast` |   0,105 |
|                                         |         |
|                   `profileArrayListGet` |   0,015 |
|                  `profileLinkedListGet` |   3,840 |
|                   `profileRoddeListGet` |   0,141 |
|                    `profileTreeListGet` |   0,155 |
|                                         |         |
|             `profileArrayListRemoveAll` |   0,164 |
|            `profileLinkedListRemoveAll` |  19,716 |
|             `profileRoddeListRemoveAll` |   0,362 |
|              `profileTreeListRemoveAll` |  57,339 |
|                                         |         |
|        `profileArrayListRemoveAtIndex`  |   2,144 |
|       `profileLinkedListRemoveAtIndex`  |  24,915 |
|         `profileRoddeListRemoveAtIndex` |   4,892 |
|         `profileTreeListRemoveAtIndex`  |   2,901 |
|                                         |         |
|          `profileArrayListRemoveFirst`  |   3,747 |
|         `profileLinkedListRemoveFirst`  |   0,191 |
|           `profileRoddeListRemoveFirst` |   1,105 |
|           `profileTreeListRemoveFirst`  |   0,988 |
|                                         |         |
|           `profileArrayListRemoveLast`  |   0,031 |
|          `profileLinkedListRemoveLast`  |   0,137 |
|            `profileRoddeListRemoveLast` |   0,186 |
|            `profileTreeListRemoveLast`  |   1,381 |
|                                         |         |
|         `profileArrayListRemoveObject`  |   4,617 |
|        `profileLinkedListRemoveObject`  |  11,606 |
|          `profileRoddeListRemoveObject` |  21,167 |
|          `profileTreeListRemoveObject`  |  33,067 |
|                                         |         |
|          `profileArrayListRemoveRange`  |   0,050 |
|         `profileLinkedListRemoveRange`  |   0,517 |
|           `profileRoddeListRemoveRange` |   0,533 |
|           `profileTreeListRemoveRange`  |   6,120 |
|                                         |         |
|            `profileArrayListSortRange`  |   1,389 |
|           `profileLinkedListSortRange`  |   1,507 |
|             `profileRoddeListSortRange` |   1,482 |
|             `profileTreeListSortRange`  |   1,635 |
|                                         |         |
|                    Total of `ArrayList` | 12,800  |
|                   Total of `LinkedList` | 65,644  |
|                    Total of `RoddeList` | 30,480  |
|                     Total of `TreeList` | 106,356 |

#### Medium size data

| Operation                               | ms      |
|-----------------------------------------|---------|
|            `profileArrayListAddAtIndex` |   0,327 |
|           `profileLinkedListAddAtIndex` |   2,599 |
|            `profileRoddeListAddAtIndex` |   0,432 |
|             `profileTreeListAddAtIndex` |   0,555 |
|                                         |         |
|         `profileArrayListAddCollection` |   0,386 |
|        `profileLinkedListAddCollection` |   0,369 |
|         `profileRoddeListAddCollection` |   0,394 |
|          `profileTreeListAddCollection` |   1,246 |
|                                         |         |
|  `profileArrayListAddCollectionAtIndex` |   2,413 |
| `profileLinkedListAddCollectionAtIndex` |  36,648 |
|  `profileRoddeListAddCollectionAtIndex` |   1,370 |
|   `profileTreeListAddCollectionAtIndex` |   5,880 |
|                                         |         |
|              `profileArrayListAddFirst` |   0,421 |
|             `profileLinkedListAddFirst` |   0,019 |
|              `profileRoddeListAddFirst` |   0,141 |
|               `profileTreeListAddFirst` |   0,272 |
|                                         |         |
|               `profileArrayListAddLast` |   0,016 |
|              `profileLinkedListAddLast` |   0,017 |
|               `profileRoddeListAddLast` |   0,035 |
|                `profileTreeListAddLast` |   0,290 |
|                                         |         |
|                   `profileArrayListGet` |   0,054 |
|                  `profileLinkedListGet` |  35,779 |
|                   `profileRoddeListGet` |   0,661 |
|                    `profileTreeListGet` |   0,468 |
|                                         |         |
|             `profileArrayListRemoveAll` |   0,809 |
|            `profileLinkedListRemoveAll` | 172,829 |
|             `profileRoddeListRemoveAll` |   1,273 |
|              `profileTreeListRemoveAll` | 699,206 |
|                                         |         |
|         `profileArrayListRemoveAtIndex` |  18,441 |
|        `profileLinkedListRemoveAtIndex` | 258,263 |
|         `profileRoddeListRemoveAtIndex` |  48,384 |
|          `profileTreeListRemoveAtIndex` |  10,126 |
|                                         |         |
|           `profileArrayListRemoveFirst` |  38,078 |
|          `profileLinkedListRemoveFirst` |   0,576 |
|           `profileRoddeListRemoveFirst` |   5,176 |
|            `profileTreeListRemoveFirst` |   3,377 |
|                                         |         |
|            `profileArrayListRemoveLast` |   0,089 |
|           `profileLinkedListRemoveLast` |   0,576 |
|            `profileRoddeListRemoveLast` |   0,863 |
|             `profileTreeListRemoveLast` |   3,666 |
|                                         |         |
|          `profileArrayListRemoveObject` |  46,814 |
|         `profileLinkedListRemoveObject` | 105,916 |
|          `profileRoddeListRemoveObject` | 191,653 |
|           `profileTreeListRemoveObject` | 306,468 |
|                                         |         |
|           `profileArrayListRemoveRange` |   0,099 |
|          `profileLinkedListRemoveRange` |   1,262 |
|           `profileRoddeListRemoveRange` |   1,212 |
|            `profileTreeListRemoveRange` |  13,690 |
|                                         |         |
|             `profileArrayListSortRange` |   8,449 |
|            `profileLinkedListSortRange` |   9,291 |
|             `profileRoddeListSortRange` |   9,229 |
|              `profileTreeListSortRange` |  10,035 |
|                                         |         |
|                   Total of  `ArrayList` | 116,396 |
|                   Total of `LinkedList` | 624,144 |
|                   Total of  `RoddeList` | 260,822 |
|                     Total of `TreeList` | 1055,277|

#### Larger size data


| Operation                               | ms      |
|-----------------------------------------|---------|
|            `profileArrayListAddAtIndex` |   0,665 |
|           `profileLinkedListAddAtIndex` |   8,242 |
|            `profileRoddeListAddAtIndex` |   0,814 |
|             `profileTreeListAddAtIndex` |   1,033 |
|                                         |         |
|         `profileArrayListAddCollection` |   0,615 |
|        `profileLinkedListAddCollection` |   0,678 |
|         `profileRoddeListAddCollection` |   0,656 |
|          `profileTreeListAddCollection` |   2,282 |
|                                         |         |
|  `profileArrayListAddCollectionAtIndex` |   5,185 |
| `profileLinkedListAddCollectionAtIndex` | 100,281 |
|  `profileRoddeListAddCollectionAtIndex` |   2,585 |
|   `profileTreeListAddCollectionAtIndex` |  10,216 |
|                                         |         |
|              `profileArrayListAddFirst` |   0,923 |
|             `profileLinkedListAddFirst` |   0,030 |
|              `profileRoddeListAddFirst` |   0,265 |
|               `profileTreeListAddFirst` |   0,485 |
|                                         |         |
|               `profileArrayListAddLast` |   0,022 |
|              `profileLinkedListAddLast` |   0,030 |
|               `profileRoddeListAddLast` |   0,051 |
|                `profileTreeListAddLast` |   0,495 |
|                                         |         |
|                   `profileArrayListGet` |   0,093 |
|                  `profileLinkedListGet` | 100,660 |
|                   `profileRoddeListGet` |   1,346 |
|                    `profileTreeListGet` |   0,815 |
|                                         |         |
|             `profileArrayListRemoveAll` |   1,022 |
|            `profileLinkedListRemoveAll` | 489,632 |
|             `profileRoddeListRemoveAll` |   2,407 |
|              `profileTreeListRemoveAll` | 1693,314|
|                                         |         |
|         `profileArrayListRemoveAtIndex` |  48,418 |
|        `profileLinkedListRemoveAtIndex` | 769,515 |
|         `profileRoddeListRemoveAtIndex` | 149,141 |
|          `profileTreeListRemoveAtIndex` |  17,782 |
|                                         |         |
|           `profileArrayListRemoveFirst` |  95,192 |
|          `profileLinkedListRemoveFirst` |   0,967 |
|           `profileRoddeListRemoveFirst` |  10,974 |
|            `profileTreeListRemoveFirst` |   5,901 |
|                                         |         |
|            `profileArrayListRemoveLast` |   0,142 |
|           `profileLinkedListRemoveLast` |   0,972 |
|            `profileRoddeListRemoveLast` |   1,345 |
|             `profileTreeListRemoveLast` |   6,554 |
|                                         |         |
|          `profileArrayListRemoveObject` | 145,457 |
|         `profileLinkedListRemoveObject` | 540,009 |
|          `profileRoddeListRemoveObject` | 519,061 |
|           `profileTreeListRemoveObject` | 837,337 |
|                                         |         |
|           `profileArrayListRemoveRange` |   0,176 |
|          `profileLinkedListRemoveRange` |   2,015 |
|           `profileRoddeListRemoveRange` |   1,962 |
|            `profileTreeListRemoveRange` |  22,675 |
|                                         |         |
|             `profileArrayListSortRange` |  28,842 |
|            `profileLinkedListSortRange` |  30,540 |
|             `profileRoddeListSortRange` |  31,064 |
|              `profileTreeListSortRange` |  33,371 |
|                                         |         |
|                    Total of `ArrayList` | 326,752 |
|                    Total of `LinkedList`| 2043,570|
|                    Total of `RoddeList` | 721,672 |
|                    Total of `TreeList`  | 2632,261|

### Benchmark without JMH

```
<<< Benchmark seed = 1654425745819 >>>

<<< Flags >>>
runSubListClear: true
runRemoveAll   : true
runSort        : true

=== WARMUP RUN ===
com.github.coderodde.util.IndexedLinkedList.addFirst in (ms): 53
java.util.LinkedList.addFirst in (ms): 11
java.util.ArrayList.addFirst in (ms): 617
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 36

com.github.coderodde.util.IndexedLinkedList.addLast in (ms): 0
java.util.LinkedList.addLast in (ms): 32
java.util.ArrayList.addLast in (ms): 18
org.apache.commons.collections4.list.TreeList.addLast in (ms): 41

com.github.coderodde.util.IndexedLinkedList.add(int, E) in (ms): 29
java.util.LinkedList.add(int, E) in (ms): 1873
java.util.ArrayList.add(int, E) in (ms): 115
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 28

com.github.coderodde.util.IndexedLinkedList.addAll(Collection) in (ms): 9
java.util.LinkedList.addAll(Collection) in (ms): 28
java.util.ArrayList.addAll(Collection) in (ms): 4
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 31

com.github.coderodde.util.IndexedLinkedList.addAll(int, Collection) in (ms): 38
java.util.LinkedList.addAll(int, Collection) in (ms): 1546
java.util.ArrayList.addAll(int, Collection) in (ms): 92
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 16

com.github.coderodde.util.IndexedLinkedList.get(int) in (ms): 10
java.util.LinkedList.get(int) in (ms): 2133
java.util.ArrayList.get(int) in (ms): 1
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 3

com.github.coderodde.util.IndexedLinkedList.removeFirst() in (ms): 29
java.util.LinkedList.removeFirst() in (ms): 1
java.util.ArrayList.removeFirst() in (ms): 243
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 3

com.github.coderodde.util.IndexedLinkedList.removeLast() in (ms): 0
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 0

com.github.coderodde.util.IndexedLinkedList.remove(int) in (ms): 42
java.util.LinkedList.remove(int) in (ms): 4067
java.util.ArrayList.remove(int) in (ms): 216
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 11

com.github.coderodde.util.IndexedLinkedList.remove(Object) in (ms): 109
java.util.LinkedList.remove(Object) in (ms): 56
java.util.ArrayList.remove(Object) in (ms): 281
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 139

com.github.coderodde.util.IndexedLinkedList.iterator().add() in (ms): 25
java.util.LinkedList.iterator().add() in (ms): 3
java.util.ArrayList.iterator().add() in (ms): 826
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 45

com.github.coderodde.util.IndexedLinkedList.iterator().remove() in (ms): 6
java.util.LinkedList.iterator().remove() in (ms): 15
java.util.ArrayList.iterator().remove() in (ms): 925
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 40

com.github.coderodde.util.IndexedLinkedList.stream() in (ms): 26
java.util.LinkedList.stream() in (ms): 10
java.util.ArrayList.stream() in (ms): 10
org.apache.commons.collections4.list.TreeList.stream() in (ms): 98

com.github.coderodde.util.IndexedLinkedList.stream().parallel() in (ms): 59
java.util.LinkedList.stream().parallel() in (ms): 55
java.util.ArrayList.stream().parallel() in (ms): 17
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 205

com.github.coderodde.util.IndexedLinkedList.removeAll() in (ms): 2
java.util.LinkedList.removeAll() in (ms): 126
java.util.ArrayList.removeAll() in (ms): 2
org.apache.commons.collections4.list.TreeList.removeAll() in (ms): 304

com.github.coderodde.util.IndexedLinkedList.subList(...).clear() in (ms): 10
java.util.LinkedList.subList(...).clear() in (ms): 39
java.util.ArrayList.subList(...).clear() in (ms): 5
org.apache.commons.collections4.list.TreeList.subList(...).clear() in (ms): 224

IndexedLinkedList.subList().sort() in (ms): 409
LinkedList.subList().sort() in (ms): 325
ArrayList.subList().sort() in (ms): 271
TreeList.subList().sort() in (ms): 329

--- Total time elapsed ---
com.github.coderodde.util.IndexedLinkedList in (ms): 856
java.util.LinkedList in (ms): 10320
java.util.ArrayList in (ms): 3643
org.apache.commons.collections4.list.TreeList in (ms): 1553

=== BENCHMARK RUN ===
com.github.coderodde.util.IndexedLinkedList.addFirst in (ms): 23
java.util.LinkedList.addFirst in (ms): 3
java.util.ArrayList.addFirst in (ms): 541
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 16

com.github.coderodde.util.IndexedLinkedList.addLast in (ms): 8
java.util.LinkedList.addLast in (ms): 2
java.util.ArrayList.addLast in (ms): 157
org.apache.commons.collections4.list.TreeList.addLast in (ms): 38

com.github.coderodde.util.IndexedLinkedList.add(int, E) in (ms): 22
java.util.LinkedList.add(int, E) in (ms): 1977
java.util.ArrayList.add(int, E) in (ms): 112
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 7

com.github.coderodde.util.IndexedLinkedList.addAll(Collection) in (ms): 12
java.util.LinkedList.addAll(Collection) in (ms): 4
java.util.ArrayList.addAll(Collection) in (ms): 4
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 7

com.github.coderodde.util.IndexedLinkedList.addAll(int, Collection) in (ms): 11
java.util.LinkedList.addAll(int, Collection) in (ms): 2084
java.util.ArrayList.addAll(int, Collection) in (ms): 91
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 14

com.github.coderodde.util.IndexedLinkedList.get(int) in (ms): 10
java.util.LinkedList.get(int) in (ms): 2408
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 1

com.github.coderodde.util.IndexedLinkedList.removeFirst() in (ms): 8
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 210
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 11

com.github.coderodde.util.IndexedLinkedList.removeLast() in (ms): 1
java.util.LinkedList.removeLast() in (ms): 1
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 0

com.github.coderodde.util.IndexedLinkedList.remove(int) in (ms): 23
java.util.LinkedList.remove(int) in (ms): 4684
java.util.ArrayList.remove(int) in (ms): 202
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 10

com.github.coderodde.util.IndexedLinkedList.remove(Object) in (ms): 53
java.util.LinkedList.remove(Object) in (ms): 38
java.util.ArrayList.remove(Object) in (ms): 245
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 87

com.github.coderodde.util.IndexedLinkedList.iterator().add() in (ms): 19
java.util.LinkedList.iterator().add() in (ms): 10
java.util.ArrayList.iterator().add() in (ms): 828
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 18

com.github.coderodde.util.IndexedLinkedList.iterator().remove() in (ms): 10
java.util.LinkedList.iterator().remove() in (ms): 16
java.util.ArrayList.iterator().remove() in (ms): 1265
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 43

com.github.coderodde.util.IndexedLinkedList.stream() in (ms): 1
java.util.LinkedList.stream() in (ms): 20
java.util.ArrayList.stream() in (ms): 16
org.apache.commons.collections4.list.TreeList.stream() in (ms): 15

com.github.coderodde.util.IndexedLinkedList.stream().parallel() in (ms): 3
java.util.LinkedList.stream().parallel() in (ms): 110
java.util.ArrayList.stream().parallel() in (ms): 3
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 206

com.github.coderodde.util.IndexedLinkedList.removeAll() in (ms): 0
java.util.LinkedList.removeAll() in (ms): 103
java.util.ArrayList.removeAll() in (ms): 1
org.apache.commons.collections4.list.TreeList.removeAll() in (ms): 256

com.github.coderodde.util.IndexedLinkedList.subList(...).clear() in (ms): 4
java.util.LinkedList.subList(...).clear() in (ms): 35
java.util.ArrayList.subList(...).clear() in (ms): 2
org.apache.commons.collections4.list.TreeList.subList(...).clear() in (ms): 235

IndexedLinkedList.subList().sort() in (ms): 315
LinkedList.subList().sort() in (ms): 321
ArrayList.subList().sort() in (ms): 299
TreeList.subList().sort() in (ms): 302

--- Total time elapsed ---
com.github.coderodde.util.IndexedLinkedList in (ms): 523
java.util.LinkedList in (ms): 11816
java.util.ArrayList in (ms): 3976
org.apache.commons.collections4.list.TreeList in (ms): 1266
```
