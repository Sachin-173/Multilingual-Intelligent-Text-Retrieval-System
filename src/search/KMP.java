package search;

import datastructures.IntList;

/**
 * Knuth-Morris-Pratt (KMP) Pattern Matching Algorithm.
 *
 * How it works:
 * 1. Preprocessing:
 *    Computes the Longest Proper Prefix which is also a Suffix (LPS) array for the pattern.
 *    LPS[i] stores the length of the longest proper prefix of pattern[0..i] that is also a suffix of pattern[0..i].
 *
 * 2. Searching:
 *    When a mismatch occurs after 'j' matching characters:
 *    - Instead of shifting by just 1 position and re-checking matched characters (like Naive search),
 *    - KMP uses LPS[j - 1] to set the new matching index in the pattern.
 *    - Text pointer 'i' NEVER moves backwards!
 *
 * Characteristics:
 * - Deterministic linear time string matching.
 * - Optimal for searching repetitive or structured texts and Indian scripts.
 *
 * Time Complexity:
 * - Preprocessing (LPS construction): O(m)
 * - Searching:                        O(n)
 * - Overall Time Complexity:          O(n + m)
 *
 * Space Complexity:
 * - LPS Array:                        O(m) auxiliary space
 */
public class KMP {

    public static class KMPSearchResult {
        public final IntList matchPositions;
        public final int[] lpsArray;
        public final long executionTimeNs;
        public final long preprocessingTimeNs;
        public final int comparisonsCount;

        public KMPSearchResult(IntList matchPositions, int[] lpsArray,
                               long executionTimeNs, long preprocessingTimeNs,
                               int comparisonsCount) {
            this.matchPositions = matchPositions;
            this.lpsArray = lpsArray;
            this.executionTimeNs = executionTimeNs;
            this.preprocessingTimeNs = preprocessingTimeNs;
            this.comparisonsCount = comparisonsCount;
        }

        public String getLpsExplanation(String pattern) {
            StringBuilder sb = new StringBuilder();
            sb.append("Pattern: ").append(pattern).append("\n");
            sb.append("LPS Table:\n");
            sb.append("Index:  ");
            for (int i = 0; i < lpsArray.length; i++) {
                sb.append(String.format("%3d ", i));
            }
            sb.append("\nChar:   ");
            for (int i = 0; i < pattern.length(); i++) {
                sb.append(String.format("%3c ", pattern.charAt(i)));
            }
            sb.append("\nValue:  ");
            for (int val : lpsArray) {
                sb.append(String.format("%3d ", val));
            }
            sb.append("\n\nLPS Preprocessing Explanation:\n");
            sb.append("LPS[i] represents the length of the longest prefix of pattern[0..i]\n");
            sb.append("that matches a proper suffix of pattern[0..i]. Upon a mismatch at j,\n");
            sb.append("we fall back to j = LPS[j-1] without ever rewinding the text pointer i.");
            return sb.toString();
        }
    }

    /**
     * Builds the Longest Prefix Suffix (LPS) array for the given pattern.
     * LPS array precomputation takes O(m) time and O(m) space.
     *
     * @param pattern The search pattern
     * @return Integer array of size m containing prefix-suffix lengths
     */
    public static int[] computeLPSArray(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0; // length of previous longest prefix suffix
        int i = 1;

        lps[0] = 0; // lps[0] is always 0 because proper prefix cannot be the whole string

        // Loop calculates lps[i] for i = 1 to m-1
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else { // (pattern.charAt(i) != pattern.charAt(len))
                if (len != 0) {
                    // Fall back to the previous known prefix suffix
                    len = lps[len - 1];
                    // Note: i is NOT incremented here
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    /**
     * Searches for all occurrences of pattern in text using the KMP algorithm.
     *
     * @param text    The source document text
     * @param pattern The search pattern
     * @return KMPSearchResult containing match positions, LPS table, and metrics
     */
    public static KMPSearchResult search(String text, String pattern) {
        IntList matches = new IntList();
        if (text == null || pattern == null) {
            return new KMPSearchResult(matches, new int[0], 0, 0, 0);
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            return new KMPSearchResult(matches, new int[0], 0, 0, 0);
        }

        long preStart = System.nanoTime();
        int[] lps = computeLPSArray(pattern);
        long preEnd = System.nanoTime();
        long preTime = preEnd - preStart;

        int comparisons = 0;
        long searchStart = System.nanoTime();

        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            comparisons++;
            if (pattern.charAt(j) == text.charAt(i)) {
                j++;
                i++;
            }

            if (j == m) {
                // Match found at index (i - j)
                matches.add(i - j);
                // Continue searching for next occurrence using LPS
                j = lps[j - 1];
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                // Mismatch after j matches
                if (j != 0) {
                    j = lps[j - 1]; // Shift pattern using LPS table
                } else {
                    i++; // No match at first char of pattern, move text pointer
                }
            }
        }

        long searchEnd = System.nanoTime();
        long totalTime = (searchEnd - searchStart) + preTime;

        return new KMPSearchResult(matches, lps, totalTime, preTime, comparisons);
    }
}
