package suffix;

import datastructures.IntList;
import sorting.MergeSort;

/**
 * Manually implemented Suffix Array data structure.
 *
 * What is a Suffix Array?
 * - A Suffix Array SA is an integer array representing the lexicographical order
 *   of all suffixes of a string text of length n.
 * - SA[i] stores the starting index of the i-th lexicographically smallest suffix.
 *
 * How it works:
 * 1. Construction:
 *    Generates an array of all suffix indices [0, 1, 2, ..., n-1].
 *    Sorts these indices using manual MergeSort and character-by-character comparison.
 *
 * 2. Substring Search via Binary Search:
 *    Since suffixes in SA are sorted in alphabetical order, any substring occurrence
 *    appears as a prefix of a range of suffixes.
 *    Using binary search over SA, we can locate all occurrences in O(m * log n) time.
 *
 * Time Complexity:
 * - Construction: O(n^2 * log n) using MergeSort (or O(n log^2 n) prefix doubling)
 * - Pattern Search: O(m * log n) where m is pattern length and n is text length
 *
 * Space Complexity: O(n) for suffix indices array.
 */
public class SuffixArray {

    private final String text;
    private final int[] suffixArray;
    private final int n;

    public SuffixArray(String text) {
        this.text = text != null ? text : "";
        this.n = this.text.length();
        this.suffixArray = new int[n];
        buildSuffixArray();
    }

    private void buildSuffixArray() {
        for (int i = 0; i < n; i++) {
            suffixArray[i] = i;
        }
        if (n > 1) {
            MergeSort.sortSuffixIndices(text, suffixArray);
        }
    }

    public int[] getSuffixArray() {
        return suffixArray;
    }

    public String getText() {
        return text;
    }

    public int length() {
        return n;
    }

    /**
     * Gets suffix starting at index suffixArray[i].
     */
    public String getSuffix(int i) {
        if (i < 0 || i >= n) return "";
        return text.substring(suffixArray[i]);
    }

    /**
     * Gets a preview snippet of the suffix at position i.
     */
    public String getSuffixPreview(int i, int maxLen) {
        if (i < 0 || i >= n) return "";
        int start = suffixArray[i];
        int end = start + maxLen;
        if (end > n) end = n;
        String sub = text.substring(start, end);
        if (start + maxLen < n) {
            sub += "...";
        }
        return sub;
    }

    /**
     * Binary searches the Suffix Array to find all starting positions of the pattern.
     * Time Complexity: O(m * log n)
     */
    public IntList search(String pattern) {
        IntList matches = new IntList();
        if (pattern == null || pattern.length() == 0 || n == 0) {
            return matches;
        }

        int m = pattern.length();

        // 1. Find lower bound (first suffix having pattern as prefix)
        int low = 0, high = n - 1;
        int firstMatch = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = comparePatternWithSuffix(pattern, suffixArray[mid]);
            if (cmp <= 0) {
                if (cmp == 0) firstMatch = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        if (firstMatch == -1) {
            return matches; // Pattern not found
        }

        // 2. Find upper bound (last suffix having pattern as prefix)
        low = firstMatch;
        high = n - 1;
        int lastMatch = firstMatch;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = comparePatternWithSuffix(pattern, suffixArray[mid]);
            if (cmp >= 0) {
                if (cmp == 0) lastMatch = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        // Collect all matching starting indices
        for (int i = firstMatch; i <= lastMatch; i++) {
            matches.add(suffixArray[i]);
        }

        return matches;
    }

    private int comparePatternWithSuffix(String pattern, int suffixStart) {
        int m = pattern.length();
        int len = (n - suffixStart) < m ? (n - suffixStart) : m;

        for (int i = 0; i < len; i++) {
            char cp = pattern.charAt(i);
            char cs = text.charAt(suffixStart + i);
            if (cp != cs) {
                return cp - cs;
            }
        }

        if ((n - suffixStart) < m) {
            return 1; // Suffix is shorter than pattern, so pattern is greater
        }
        return 0; // Suffix starts with pattern
    }
}
