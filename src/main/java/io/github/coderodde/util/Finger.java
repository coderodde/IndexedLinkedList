/*
 * The MIT License
 *
 * Copyright 2024 rodio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.coderodde.util;

import java.util.Objects;

/**
 * This class implements the actual finger. Each finger consists of two fields:
 * <ul>
 * <li>
 * <code>node</code> which points to the node in the internal linked list, and
 * </li>
 * <li>
 * <code>index</code> which is the appearance index of <code>node</code> in the
 * linked list.
 * </li>
 * </ul>
 *
 * @param <E> the type of the list's satellite data.
 * @version 1.7.0 (Jun 29, 2025)
 */
final class Finger<E> {

    /**
     * The pointed to {@link Node}.
     */
    Node<E> node;

    /**
     * The index at which the {@code node} appears in the list.
     */
    int index;

    /**
     * Constructs a new {@link Finger}.
     *
     * @param node the pointed node.
     * @param index the index of {@code node} in the actual list.
     */
    Finger(Node<E> node, int index) {
        this.node = node;
        this.index = index;
    }

    /**
     * Copy constructs this finger.
     *
     * @param finger the finger whose state to copy.
     */
    Finger(Finger<E> finger) {
        this.node = finger.node;
        this.index = finger.index;
    }

    /**
     * Returns the index of this finger. Used for research.
     *
     * @return the index of this finger.
     */
    public int getIndex() {
        return index;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (!o.getClass().equals(this.getClass())) {
            return false;
        }

        final Finger<E> other = (Finger<E>) o;

        return Objects.equals(index, other.index)
                && Objects.equals(node, other.node);
    }

    /**
     * Returns the textual representation of this finger.
     *
     * @return the textual representation.
     */
    @Override
    public String toString() {
        return String.format(
                "[index = %d, item = %s]",
                index, 
                node == null ? "null" : Objects.toString(node.item));
    }
}
