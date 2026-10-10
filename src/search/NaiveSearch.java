package search;

import datastructures.IntList;

/**
 * Naive (Brute-Force) String Matching Algorithm.
 *
 * How it works:
 * 1. Slide the pattern of length 'm' over the text of length 'n', one position at a time.
 * 2. At every shift index i (from 0 to n - m), check if pattern matches text[i .. i+m-1].
 * 3. Character by character comparison proceeds until a mismatch occurs or full pattern matches.
 * 4. If all 'm' characters match, record index i as a successful match.
 *
 * Characteristics:
 * - Does not preprocess either the pattern or the text.
 * - Incurs redundant re-comparisons when characters repeat.
 * - Serves as the benchmark baseline against which advanced algorithms are evaluated.
 *
 * Time Complexity:
 * - Best Case:  O(n)      (immediate mismatch on first character at each position)
 * - Worst Case: O(n * m)  (e.g., text: "AAAAAA...", pattern: "AAAB")
 * - Average:    O(n + m)  for random alphabets, but degrades on repetitive patterns.
 *
 * Space Complexity: O(1) auxiliary space (excluding match indices list).
 */
public class NaiveSearch {

    public static class SearchResultData {
        public final IntList matchPositions;
        public final long executionTimeNs;
        public final int comparisonsCount;

        public SearchResultData(IntList matchPositions, long executionTimeNs, int comparisonsCount) {
            this.matchPositions = matchPositions;
            this.executionTimeNs = executionTimeNs;
            this.comparisonsCount = comparisonsCount;
        }
    }

    /**
     * Searches for all occurrences of pattern in text using the Naive Brute-Force approach.
     * Manual character comparison with zero external helper functions.
     *
     * @param text    The source document text
     * @param pattern The search query pattern
     * @return SearchResultData containing match positions, execution time (ns), and comparison count
     */
    public static SearchResultData search(String text, String pattern) {
        IntList matches = new IntList();
        if (text == null || pattern == null) {
            return new SearchResultData(matches, 0, 0);
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            return new SearchResultData(matches, 0, 0);
        }

        int comparisons = 0;
        long startTime = System.nanoTime();

        // Slide the pattern over text one position at a time
        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            // Check character by character
            while (j < m) {
                comparisons++;
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break; // Mismatch encountered, shift pattern by 1
                }
                j++;
            }

            // If pattern fully matched
            if (j == m) {
                matches.add(i);
            }
        }

        long endTime = System.nanoTime();
        return new SearchResultData(matches, (endTime - startTime), comparisons);
    }
}
