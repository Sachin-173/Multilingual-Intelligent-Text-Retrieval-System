package datastructures;

/**
 * Manually implemented FIFO (First-In, First-Out) Queue using a singly linked list.
 * Essential for Breadth-First Search (BFS) in Aho-Corasick failure link computation
 * and Edmonds-Karp maximum flow augmenting path search.
 *
 * Time Complexity:
 * - enqueue: O(1)
 * - dequeue: O(1)
 * - peek: O(1)
 *
 * Space Complexity: O(n)
 *
 * @param <T> Element type
 */
public class CustomQueue<T> {

    private static class Node<E> {
        E data;
        Node<E> next;

        Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public CustomQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void enqueue(T item) {
        Node<T> newNode = new Node<>(item);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        T item = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return item;
    }

    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return head.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}
