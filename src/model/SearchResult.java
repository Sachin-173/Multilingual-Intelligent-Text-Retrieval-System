package model;

import datastructures.IntList;

/**
 * Encapsulates the result of a search query executed against a Document.
 * Stores match metrics, relevance score, matched positions, and algorithm metadata.
 */
public class SearchResult {
    private final Document document;
    private final int matchCount;
    private double score;
    private final String matchedSnippet;
    private final String algorithmUsed;
    private final IntList matchPositions;
    private final String searchType;
    private final long executionTimeNs;

    public SearchResult(Document document, int matchCount, double score,
                        String matchedSnippet, String algorithmUsed,
                        IntList matchPositions, String searchType,
                        long executionTimeNs) {
        this.document = document;
        this.matchCount = matchCount;
        this.score = score;
        this.matchedSnippet = matchedSnippet;
        this.algorithmUsed = algorithmUsed;
        this.matchPositions = matchPositions;
        this.searchType = searchType;
        this.executionTimeNs = executionTimeNs;
    }

    public Document getDocument() {
        return document;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getMatchedSnippet() {
        return matchedSnippet;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public IntList getMatchPositions() {
        return matchPositions;
    }

    public String getSearchType() {
        return searchType;
    }

    public String getMatchType() {
        return searchType;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }
}
