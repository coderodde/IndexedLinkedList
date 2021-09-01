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
cd C:\Users\rodde\Documents\LinkedList; "JAVA_HOME=C:\\Program Files\\Java\\jdk-16.0.2" cmd /c "\"C:\\Program Files\\NetBeans-12.4\\netbeans\\java\\maven\\bin\\mvn.cmd\" -Dexec.args=\"${exec.vmArgs} -classpath %classpath ${exec.mainClass} ${exec.appArgs}\" -Dexec.executable=\"C:\\Program Files\\Java\\jdk-16.0.2\\bin\\java.exe\" -Dexec.mainClass=com.github.coderodde.util.benchmark.LinkedListBenchmarkRunner -Dexec.vmArgs= -Dexec.appArgs= -Dmaven.ext.class.path=\"C:\\Program Files\\NetBeans-12.4\\netbeans\\java\\maven-nblib\\netbeans-eventspy.jar\" -Dfile.encoding=UTF-8 org.codehaus.mojo:exec-maven-plugin:3.0.0:exec"
Running NetBeans Compile On Save execution. Phase execution is skipped and output directories of dependency projects (with Compile on Save turned on) will be used instead of their jar artifacts.
Invalid macro definition.

C:\Users\rodde\Documents\LinkedList>echo off 
Scanning for projects...

----------------< com.github.coderodde.util:LinkedList >----------------
Building LinkedList 1.6
--------------------------------[ jar ]---------------------------------

--- exec-maven-plugin:3.0.0:exec (default-cli) @ LinkedList ---
<<< LinkedList seed = 1629956432123 >>>

=== WARMUP RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 1688
java.util.LinkedList.addFirst in (ms): 4
java.util.ArrayList.addFirst in (ms): 768
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 53

com.github.coderodde.util.LinkedList.addLast in (ms): 10
java.util.LinkedList.addLast in (ms): 6
java.util.ArrayList.addLast in (ms): 5
org.apache.commons.collections4.list.TreeList.addLast in (ms): 52

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 171
java.util.LinkedList.add(int, E) in (ms): 2691
java.util.ArrayList.add(int, E) in (ms): 222
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 22

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 11
java.util.LinkedList.addAll(Collection) in (ms): 11
java.util.ArrayList.addAll(Collection) in (ms): 7
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 42

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 69
java.util.LinkedList.addAll(int, Collection) in (ms): 2370
java.util.ArrayList.addAll(int, Collection) in (ms): 189
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 21

com.github.coderodde.util.LinkedList.get(int) in (ms): 13
java.util.LinkedList.get(int) in (ms): 1436
java.util.ArrayList.get(int) in (ms): 3
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 2

com.github.coderodde.util.LinkedList.removeFirst() in (ms): 118
java.util.LinkedList.removeFirst() in (ms): 1
java.util.ArrayList.removeFirst() in (ms): 159
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 2

com.github.coderodde.util.LinkedList.removeLast() in (ms): 17
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 0
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 2

com.github.coderodde.util.LinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 3
java.util.ArrayList.remove(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 1

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 80
java.util.LinkedList.remove(Object) in (ms): 15
java.util.ArrayList.remove(Object) in (ms): 96
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 45

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 546
java.util.LinkedList.iterator().add() in (ms): 14
java.util.ArrayList.iterator().add() in (ms): 1294
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 39

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 843
java.util.LinkedList.iterator().remove() in (ms): 17
java.util.ArrayList.iterator().remove() in (ms): 1456
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 39

com.github.coderodde.util.LinkedList.stream() in (ms): 74
java.util.LinkedList.stream() in (ms): 18
java.util.ArrayList.stream() in (ms): 24
org.apache.commons.collections4.list.TreeList.stream() in (ms): 32

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 38
java.util.LinkedList.stream().parallel() in (ms): 39
java.util.ArrayList.stream().parallel() in (ms): 31
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 251

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 3678
java.util.LinkedList in (ms): 6625
java.util.ArrayList in (ms): 4254
org.apache.commons.collections4.list.TreeList in (ms): 603

=== BENCHMARK RUN ===
com.github.coderodde.util.LinkedList.addFirst in (ms): 1358
java.util.LinkedList.addFirst in (ms): 6
java.util.ArrayList.addFirst in (ms): 702
org.apache.commons.collections4.list.TreeList.addFirst in (ms): 24

com.github.coderodde.util.LinkedList.addLast in (ms): 11
java.util.LinkedList.addLast in (ms): 3
java.util.ArrayList.addLast in (ms): 5
org.apache.commons.collections4.list.TreeList.addLast in (ms): 44

com.github.coderodde.util.LinkedList.add(int, E) in (ms): 138
java.util.LinkedList.add(int, E) in (ms): 2507
java.util.ArrayList.add(int, E) in (ms): 144
org.apache.commons.collections4.list.TreeList.add(int, E) in (ms): 4

com.github.coderodde.util.LinkedList.addAll(Collection) in (ms): 9
java.util.LinkedList.addAll(Collection) in (ms): 3
java.util.ArrayList.addAll(Collection) in (ms): 5
org.apache.commons.collections4.list.TreeList.addAll(Collection) in (ms): 13

com.github.coderodde.util.LinkedList.addAll(int, Collection) in (ms): 56
java.util.LinkedList.addAll(int, Collection) in (ms): 2657
java.util.ArrayList.addAll(int, Collection) in (ms): 116
org.apache.commons.collections4.list.TreeList.addAll(int, Collection) in (ms): 18

com.github.coderodde.util.LinkedList.get(int) in (ms): 9
java.util.LinkedList.get(int) in (ms): 1471
java.util.ArrayList.get(int) in (ms): 0
org.apache.commons.collections4.list.TreeList.get(int) in (ms): 1

com.github.coderodde.util.LinkedList.removeFirst() in (ms): 115
java.util.LinkedList.removeFirst() in (ms): 0
java.util.ArrayList.removeFirst() in (ms): 115
org.apache.commons.collections4.list.TreeList.removeFirst() in (ms): 1

com.github.coderodde.util.LinkedList.removeLast() in (ms): 7
java.util.LinkedList.removeLast() in (ms): 0
java.util.ArrayList.removeLast() in (ms): 1
org.apache.commons.collections4.list.TreeList.removeLast() in (ms): 1

com.github.coderodde.util.LinkedList.remove(int) in (ms): 0
java.util.LinkedList.remove(int) in (ms): 1
java.util.ArrayList.remove(int) in (ms): 2
org.apache.commons.collections4.list.TreeList.remove(int) in (ms): 0

com.github.coderodde.util.LinkedList.remove(Object) in (ms): 34
java.util.LinkedList.remove(Object) in (ms): 5
java.util.ArrayList.remove(Object) in (ms): 58
org.apache.commons.collections4.list.TreeList.remove(Object) in (ms): 13

com.github.coderodde.util.LinkedList.iterator().add() in (ms): 639
java.util.LinkedList.iterator().add() in (ms): 17
java.util.ArrayList.iterator().add() in (ms): 1136
org.apache.commons.collections4.list.TreeList.iterator().add() in (ms): 23

com.github.coderodde.util.LinkedList.iterator().remove() in (ms): 613
java.util.LinkedList.iterator().remove() in (ms): 7
java.util.ArrayList.iterator().remove() in (ms): 1416
org.apache.commons.collections4.list.TreeList.iterator().remove() in (ms): 59

com.github.coderodde.util.LinkedList.stream() in (ms): 8
java.util.LinkedList.stream() in (ms): 7
java.util.ArrayList.stream() in (ms): 5
org.apache.commons.collections4.list.TreeList.stream() in (ms): 13

com.github.coderodde.util.LinkedList.stream().parallel() in (ms): 4
java.util.LinkedList.stream().parallel() in (ms): 206
java.util.ArrayList.stream().parallel() in (ms): 5
org.apache.commons.collections4.list.TreeList.stream().parallel() in (ms): 28

--- Total time elapsed ---
com.github.coderodde.util.LinkedList in (ms): 3001
java.util.LinkedList in (ms): 6890
java.util.ArrayList in (ms): 3710
org.apache.commons.collections4.list.TreeList in (ms): 242
```
