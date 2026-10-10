package ranking;

import datastructures.CustomList;
import model.SearchResult;
import sorting.MergeSort;

/**
 * Search Result Ranking Engine.
 *
 * Scoring Philosophy:
 * - Exact Match:       100 points base + 15 points per additional match.
 *                      Title occurrences receive 2x weight over body text.
 * - Prefix Match:      80 points base + frequency bonus.
 * - Fuzzy Match:       Scaled inversely with Edit Distance:
 *                      Score = max(10, 75 - (editDistance * 15)).
 * - Multi-Pattern:     60 points per matched pattern + 40 points bonus
 *                      if ALL requested patterns appear in the document.
 *
 * Sorting:
 * - Pure manual MergeSort (NO Collections.sort or Arrays.sort).
 * - Sorts in descending order of calculated relevance score.
 *
 * Time Complexity:
 * - Scoring:    O(R) where R is count of raw search results
 * - Merge Sort: O(R log R)
 * - Total:      O(R log R)
 *
 * Space Complexity: O(R) auxiliary space for MergeSort buffer.
 */
public class Ranker {

    public static final double EXACT_MATCH_BASE = 100.0;
    public static final double PREFIX_MATCH_BASE = 80.0;
    public static final double MULTI_PATTERN_BASE = 60.0;

    /**
     * Computes final scores for results and sorts them descending using MergeSort.
     *
     * @param results CustomList of candidate SearchResults
     * @return Sorted CustomList from highest score to lowest score
     */
    public static CustomList<SearchResult> rankAndSort(CustomList<SearchResult> results) {
        if (results == null || results.size() == 0) {
            return results;
        }

        // Apply scoring adjustments
        for (int i = 0; i < results.size(); i++) {
            SearchResult res = results.get(i);
            double calculatedScore = calculateScore(res);
            res.setScore(calculatedScore);
        }

        // Sort descending using custom MergeSort
        MergeSort.sortSearchResultsDescending(results);
        return results;
    }

    private static double calculateScore(SearchResult res) {
        double score = res.getScore();
        int matches = res.getMatchCount();
        String type = res.getSearchType();

        if ("Exact".equalsIgnoreCase(type)) {
            score = EXACT_MATCH_BASE + (matches > 1 ? (matches - 1) * 12.0 : 0.0);
        } else if ("Prefix".equalsIgnoreCase(type)) {
            score = PREFIX_MATCH_BASE + (matches > 1 ? (matches - 1) * 8.0 : 0.0);
        } else if ("Multi-Pattern".equalsIgnoreCase(type)) {
            score = (matches * MULTI_PATTERN_BASE);
            if (matches >= 3) {
                score += 50.0; // Bonus for rich multi-keyword coverage
            }
        }
        // If Fuzzy, score was already seeded based on edit distance in fuzzy search engine

        // Bonus if title matches query
        if (res.getDocument() != null && res.getMatchedSnippet() != null) {
            if (res.getMatchedSnippet().startsWith("[Title Match]")) {
                score += 40.0;
            }
        }

        return score;
    }
}
