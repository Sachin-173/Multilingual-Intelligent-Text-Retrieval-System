package datastructures;

/**
 * Manually implemented LIFO (Last-In, First-Out) Stack using a singly linked list.
 * Demonstrates node chaining, pointer manipulation, and stack operations.
 *
 * Time Complexity:
 * - push: O(1)
 * - pop: O(1)
 * - peek: O(1)
 *
 * Space Complexity: O(n)
 *
 * @param <T> Element type
 */
public class CustomStack<T> {

    private static class Node<E> {
        E data;
        Node<E> next;

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public CustomStack() {
        this.top = null;
        this.size = 0;
    }

    public void push(T item) {
        top = new Node<>(item, top);
        size++;
    }

    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        T item = top.data;
        top = top.next;
        size--;
        return item;
    }

    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return top.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        top = null;
        size = 0;
    }
}
