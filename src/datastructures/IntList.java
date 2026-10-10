package datastructures;

/**
 * Manually implemented dynamic array for primitive integers.
 * Used for storing match indices, LCP values, suffix ranks, and node IDs
 * without the autoboxing overhead of standard wrapper objects.
 *
 * Time Complexity:
 * - get / set: O(1)
 * - add (amortized): O(1)
 *
 * Space Complexity: O(n)
 */
public class IntList {
    private int[] data;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    public IntList() {
        this.data = new int[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public IntList(int initialCapacity) {
        if (initialCapacity <= 0) {
            initialCapacity = DEFAULT_CAPACITY;
        }
        this.data = new int[initialCapacity];
        this.size = 0;
    }

    public void add(int value) {
        ensureCapacity();
        data[size++] = value;
    }

    public int get(int index) {
        checkIndex(index);
        return data[index];
    }

    public void set(int index, int value) {
        checkIndex(index);
        data[index] = value;
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            if (data[i] == value) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        size = 0;
    }

    public int[] toArray() {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i];
        }
        return result;
    }

    private void ensureCapacity() {
        if (size >= data.length) {
            int newCapacity = data.length * 2;
            int[] newData = new int[newCapacity];
            for (int i = 0; i < size; i++) {
                newData[i] = data[i];
            }
            data = newData;
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
    }
}
