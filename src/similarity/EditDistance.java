package similarity;

import datastructures.CustomList;

/**
 * Levenshtein Edit Distance using Dynamic Programming.
 *
 * How it works:
 * 1. Measures the minimum number of single-character edits (insertions, deletions, or substitutions)
 *    required to transform string s1 into string s2.
 *
 * 2. Recurrence Relation:
 *    Let dp[i][j] be the edit distance between s1[0..i-1] and s2[0..j-1]:
 *    - Base cases:
 *        dp[i][0] = i (cost of deleting all i characters)
 *        dp[0][j] = j (cost of inserting all j characters)
 *    - Transition:
 *        If s1[i-1] == s2[j-1]:
 *            dp[i][j] = dp[i-1][j-1] (no operation needed)
 *        Else:
 *            dp[i][j] = 1 + min(
 *                dp[i-1][j],    // Deletion
 *                dp[i][j-1],    // Insertion
 *                dp[i-1][j-1]   // Substitution
 *            )
 *
 * Characteristics:
 * - Optimal substructure and overlapping subproblems.
 * - Powers fuzzy search, typo tolerance ("Did you mean?"), and document similarity.
 * - Works natively on Unicode Indic text (Telugu, Hindi, Tamil) as well as English.
 *
 * Time Complexity:  O(n * m)
 * Space Complexity: O(n * m) for full DP table (O(min(n, m)) if space-optimized).
 */
public class EditDistance {

    public static class Suggestion {
        private final String originalQuery;
        private final String suggestedWord;
        private final int distance;
        private final double similarity;

        public Suggestion(String originalQuery, String suggestedWord, int distance, double similarity) {
            this.originalQuery = originalQuery;
            this.suggestedWord = suggestedWord;
            this.distance = distance;
            this.similarity = similarity;
        }

        public String getOriginalQuery() {
            return originalQuery;
        }

        public String getSuggestedWord() {
            return suggestedWord;
        }

        public int getDistance() {
            return distance;
        }

        public double getSimilarity() {
            return similarity;
        }

        @Override
        public String toString() {
            return "Did you mean: '" + suggestedWord + "'? (Edit Distance: " + distance +
                   ", Match: " + String.format("%.1f", similarity * 100) + "%)";
        }
    }

    /**
     * Computes the Levenshtein edit distance between two strings using DP.
     */
    public static int computeDistance(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return s2.length();
        if (s2 == null) return s1.length();

        int n = s1.length();
        int m = s2.length();

        if (n == 0) return m;
        if (m == 0) return n;

        int[][] dp = new int[n + 1][m + 1];

        // Base cases
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }

        // Fill DP table
        for (int i = 1; i <= n; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char c2 = s2.charAt(j - 1);

                if (c1 == c2) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int deleteCost = dp[i - 1][j];
                    int insertCost = dp[i][j - 1];
                    int replaceCost = dp[i - 1][j - 1];

                    int minCost = deleteCost;
                    if (insertCost < minCost) minCost = insertCost;
                    if (replaceCost < minCost) minCost = replaceCost;

                    dp[i][j] = 1 + minCost;
                }
            }
        }

        return dp[n][m];
    }

    /**
     * Returns the 2D DP matrix for visual representation in college viva demonstrations.
     */
    public static int[][] getDpMatrix(String s1, String s2) {
        int n = s1.length();
        int m = s2.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char c2 = s2.charAt(j - 1);
                if (c1 == c2) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int d = dp[i - 1][j];
                    int ins = dp[i][j - 1];
                    int rep = dp[i - 1][j - 1];
                    int min = d < ins ? (d < rep ? d : rep) : (ins < rep ? ins : rep);
                    dp[i][j] = 1 + min;
                }
            }
        }
        return dp;
    }

    /**
     * Calculates similarity score ratio between 0.0 (completely dissimilar) and 1.0 (identical).
     */
    public static double computeSimilarityRatio(String s1, String s2) {
        if (s1 == null && s2 == null) return 1.0;
        if (s1 == null || s2 == null) return 0.0;

        int maxLen = s1.length() > s2.length() ? s1.length() : s2.length();
        if (maxLen == 0) return 1.0;

        int dist = computeDistance(s1, s2);
        return 1.0 - ((double) dist / maxLen);
    }

    /**
     * Searches vocabulary for closest word within a maximum distance threshold.
     */
    public static Suggestion findClosestWord(String queryWord, CustomList<String> vocabulary, int maxDistance) {
        if (queryWord == null || vocabulary == null || vocabulary.size() == 0) {
            return null;
        }

        String bestWord = null;
        int bestDist = Integer.MAX_VALUE;

        for (int i = 0; i < vocabulary.size(); i++) {
            String candidate = vocabulary.get(i);
            if (candidate.equalsIgnoreCase(queryWord)) {
                // Exact match already
                return new Suggestion(queryWord, candidate, 0, 1.0);
            }

            int dist = computeDistance(queryWord.toLowerCase(), candidate.toLowerCase());
            if (dist < bestDist && dist <= maxDistance) {
                bestDist = dist;
                bestWord = candidate;
            }
        }

        if (bestWord != null && bestDist <= maxDistance) {
            double sim = computeSimilarityRatio(queryWord.toLowerCase(), bestWord.toLowerCase());
            return new Suggestion(queryWord, bestWord, bestDist, sim);
        }

        return null;
    }
}
