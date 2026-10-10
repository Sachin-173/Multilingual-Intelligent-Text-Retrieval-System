package sorting;

import datastructures.CustomList;
import model.SearchResult;

/**
 * MergeSort: A classic Divide-and-Conquer sorting algorithm.
 *
 * How it works:
 * 1. Divide: Split the array into two halves at the midpoint.
 * 2. Conquer: Recursively sort each half.
 * 3. Combine (Merge): Merge the two sorted halves into a single sorted sequence.
 *
 * Characteristics:
 * - Stable sorting algorithm (preserves original order of equal keys).
 * - Not in-place (requires O(n) auxiliary temporary memory).
 *
 * Time Complexity:
 * - Best Case:    O(n log n)
 * - Average Case: O(n log n)
 * - Worst Case:   O(n log n)
 *
 * Space Complexity: O(n) auxiliary space for temporary merge buffers.
 *
 * Used in this project to:
 * - Rank SearchResults by score in descending order.
 * - Sort suffix arrays and vocabulary lists.
 */
public class MergeSort {

    /**
     * Sorts a CustomList of SearchResults in descending order of score.
     */
    public static void sortSearchResultsDescending(CustomList<SearchResult> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        int n = list.size();
        SearchResult[] array = new SearchResult[n];
        for (int i = 0; i < n; i++) {
            array[i] = list.get(i);
        }

        SearchResult[] temp = new SearchResult[n];
        mergeSortResults(array, temp, 0, n - 1);

        for (int i = 0; i < n; i++) {
            list.set(i, array[i]);
        }
    }

    private static void mergeSortResults(SearchResult[] array, SearchResult[] temp, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = left + (right - left) / 2;
        mergeSortResults(array, temp, left, mid);
        mergeSortResults(array, temp, mid + 1, right);
        mergeResults(array, temp, left, mid, right);
    }

    private static void mergeResults(SearchResult[] array, SearchResult[] temp, int left, int mid, int right) {
        int i = left;
        int j = mid + 1;
        int k = left;

        // Descending order: higher scores first
        while (i <= mid && j <= right) {
            if (array[i].getScore() >= array[j].getScore()) {
                temp[k++] = array[i++];
            } else {
                temp[k++] = array[j++];
            }
        }

        while (i <= mid) {
            temp[k++] = array[i++];
        }
        while (j <= right) {
            temp[k++] = array[j++];
        }

        for (int p = left; p <= right; p++) {
            array[p] = temp[p];
        }
    }

    /**
     * Sorts an array of strings in lexicographical order (ascending).
     */
    public static void sortStrings(String[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        String[] temp = new String[array.length];
        mergeSortStrings(array, temp, 0, array.length - 1);
    }

    private static void mergeSortStrings(String[] array, String[] temp, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = left + (right - left) / 2;
        mergeSortStrings(array, temp, left, mid);
        mergeSortStrings(array, temp, mid + 1, right);
        mergeStrings(array, temp, left, mid, right);
    }

    private static void mergeStrings(String[] array, String[] temp, int left, int mid, int right) {
        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            if (compareStrings(array[i], array[j]) <= 0) {
                temp[k++] = array[i++];
            } else {
                temp[k++] = array[j++];
            }
        }

        while (i <= mid) {
            temp[k++] = array[i++];
        }
        while (j <= right) {
            temp[k++] = array[j++];
        }

        for (int p = left; p <= right; p++) {
            array[p] = temp[p];
        }
    }

    /**
     * Manually compares two strings character by character without using String.compareTo.
     */
    public static int compareStrings(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        int lenA = a.length();
        int lenB = b.length();
        int minLen = lenA < lenB ? lenA : lenB;

        for (int i = 0; i < minLen; i++) {
            char ca = a.charAt(i);
            char cb = b.charAt(i);
            if (ca != cb) {
                return ca - cb;
            }
        }
        return lenA - lenB;
    }

    /**
     * Sorts suffix indices based on text comparison.
     */
    public static void sortSuffixIndices(String text, int[] suffixArray) {
        if (suffixArray == null || suffixArray.length <= 1) {
            return;
        }
        int[] temp = new int[suffixArray.length];
        mergeSortSuffixes(text, suffixArray, temp, 0, suffixArray.length - 1);
    }

    private static void mergeSortSuffixes(String text, int[] sa, int[] temp, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = left + (right - left) / 2;
        mergeSortSuffixes(text, sa, temp, left, mid);
        mergeSortSuffixes(text, sa, temp, mid + 1, right);
        mergeSuffixes(text, sa, temp, left, mid, right);
    }

    private static void mergeSuffixes(String text, int[] sa, int[] temp, int left, int mid, int right) {
        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            if (compareSuffixes(text, sa[i], sa[j]) <= 0) {
                temp[k++] = sa[i++];
            } else {
                temp[k++] = sa[j++];
            }
        }

        while (i <= mid) {
            temp[k++] = sa[i++];
        }
        while (j <= right) {
            temp[k++] = sa[j++];
        }

        for (int p = left; p <= right; p++) {
            sa[p] = temp[p];
        }
    }

    private static int compareSuffixes(String text, int idx1, int idx2) {
        int n = text.length();
        int i = idx1;
        int j = idx2;

        while (i < n && j < n) {
            char c1 = text.charAt(i);
            char c2 = text.charAt(j);
            if (c1 != c2) {
                return c1 - c2;
            }
            i++;
            j++;
        }
        // If one is prefix of another, shorter suffix is smaller
        return (n - idx1) - (n - idx2);
    }
}
