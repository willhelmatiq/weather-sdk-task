package com.kameleoon.weather.cache;

/**
 * Minimal doubly-linked list used internally by WeatherCache.
 * Supports O(1) insert/remove/move operations via direct Node references.
 */
final class DoublyLinkedList<K, V> {

    static final class Node<K, V> {
        final K key;
        V value;
        long timestampSec;
        Node<K, V> prev, next;

        Node(K key, V value, long ts) {
            this.key = key;
            this.value = value;
            this.timestampSec = ts;
        }
    }

    private final Node<K, V> head = new Node<>(null, null, 0);
    private final Node<K, V> tail = new Node<>(null, null, 0);

    DoublyLinkedList() {
        head.next = tail;
        tail.prev = head;
    }

    Node<K, V> first() {
        return head.next == tail ? null : head.next;
    }

    Node<K, V> last()  {
        return tail.prev == head ? null : tail.prev;
    }

    void moveToEnd(Node<K, V> node) {
        unlink(node);
        insertBeforeTail(node);
    }

    void insertBeforeTail(Node<K, V> node) {
        Node<K, V> prevNode = tail.prev;
        prevNode.next = node;
        node.prev = prevNode;
        node.next = tail;
        tail.prev = node;
    }

    void unlink(Node<K, V> node) {
        Node<K, V> prevNode = node.prev;
        Node<K, V> nextNode = node.next;
        if (prevNode != null) {
            prevNode.next = nextNode;
        }
        if (nextNode != null) {
            nextNode.prev = prevNode;
        }
        node.prev = node.next = null;
    }

    void clear() {
        head.next = tail;
        tail.prev = head;
    }
}
