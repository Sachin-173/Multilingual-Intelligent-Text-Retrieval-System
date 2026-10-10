package similarity;

import datastructures.CustomList;
import model.Document;

/**
 * Manually implemented Document Similarity Engine.
 * Combines Longest Common Subsequence (LCS) and Common Word Overlap / Jaccard Index.
 *
 * How it works:
 * 1. Tokenization:
 *    Manually extracts unique words from both documents by scanning characters
 *    and segmenting on spaces and punctuation (no regex or NLP libraries).
 *
 * 2. Longest Common Subsequence (LCS):
 *    Calculates the length of the longest subsequence present in both token sequences
 *    using Dynamic Programming:
 *    dp[i][j] = dp[i-1][j-1] + 1 if tokens match, else max(dp[i-1][j], dp[i][j-1]).
 *
 * 3. Jaccard Token Overlap:
 *    Computes |Tokens(A) INTERSECT Tokens(B)| / |Tokens(A) UNION Tokens(B)|.
 *
 * 4. Composite Similarity Score:
 *    CombinedScore = 0.5 * JaccardScore + 0.5 * LcsScore.
 *
 * Time Complexity:
 * - Tokenization: O(n + m)
 * - Jaccard:      O(wA * wB) where wA, wB are word counts
 * - LCS:          O(wA * wB)
 *
 * Space Complexity: O(wA * wB) for LCS DP matrix.
 */
public class DocumentSimilarity {

    public static class SimilarityReport {
        public final Document docA;
        public final Document docB;
        public final double overallSimilarityPercentage;
        public final double jaccardSimilarityPercentage;
        public final double lcsSimilarityPercentage;
        public final int commonWordCount;
        public final CustomList<String> commonWords;
        public final int lcsLength;

        public SimilarityReport(Document docA, Document docB,
                                double overallSimilarityPercentage,
                                double jaccardSimilarityPercentage,
                                double lcsSimilarityPercentage,
                                int commonWordCount, CustomList<String> commonWords,
                                int lcsLength) {
            this.docA = docA;
            this.docB = docB;
            this.overallSimilarityPercentage = overallSimilarityPercentage;
            this.jaccardSimilarityPercentage = jaccardSimilarityPercentage;
            this.lcsSimilarityPercentage = lcsSimilarityPercentage;
            this.commonWordCount = commonWordCount;
            this.commonWords = commonWords;
            this.lcsLength = lcsLength;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Document A: [").append(docA.getId()).append("] ").append(docA.getTitle())
              .append(" (").append(docA.getLanguage()).append(")\n");
            sb.append("Document B: [").append(docB.getId()).append("] ").append(docB.getTitle())
              .append(" (").append(docB.getLanguage()).append(")\n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("Overall Similarity Score:  %.1f%%\n", overallSimilarityPercentage));
            sb.append(String.format("Token Overlap (Jaccard):    %.1f%%\n", jaccardSimilarityPercentage));
            sb.append(String.format("Longest Common Subsequence: %.1f%% (Length: %d)\n", lcsSimilarityPercentage, lcsLength));
            sb.append("Common Words Count:        ").append(commonWordCount).append("\n");
            sb.append("Common Words Sample:       ");
            int limit = commonWords.size() < 12 ? commonWords.size() : 12;
            for (int i = 0; i < limit; i++) {
                sb.append(commonWords.get(i)).append(i < limit - 1 ? ", " : "");
            }
            if (commonWords.size() > limit) {
                sb.append("... (+").append(commonWords.size() - limit).append(" more)");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    /**
     * Compares two documents and computes their comprehensive similarity report.
     */
    public static SimilarityReport compareDocuments(Document docA, Document docB) {
        if (docA == null || docB == null) {
            return new SimilarityReport(docA, docB, 0, 0, 0, 0, new CustomList<>(), 0);
        }

        CustomList<String> tokensA = tokenize(docA.getContent());
        CustomList<String> tokensB = tokenize(docB.getContent());

        if (tokensA.size() == 0 || tokensB.size() == 0) {
            return new SimilarityReport(docA, docB, 0, 0, 0, 0, new CustomList<>(), 0);
        }

        // 1. Common words & Jaccard index
        CustomList<String> commonWords = new CustomList<>();
        for (int i = 0; i < tokensA.size(); i++) {
            String wordA = tokensA.get(i);
            if (!commonWords.contains(wordA)) {
                for (int j = 0; j < tokensB.size(); j++) {
                    if (wordA.equalsIgnoreCase(tokensB.get(j))) {
                        commonWords.add(wordA);
                        break;
                    }
                }
            }
        }

        // Unique word counts for Jaccard and Dice coefficient
        CustomList<String> uniqueA = getUniqueTokens(tokensA);
        CustomList<String> uniqueB = getUniqueTokens(tokensB);

        int intersection = commonWords.size();
        int union = uniqueA.size() + uniqueB.size() - intersection;
        double jaccard = union > 0 ? ((double) intersection / union) * 100.0 : 0.0;
        double dice = (uniqueA.size() + uniqueB.size()) > 0 ?
                (2.0 * intersection / (uniqueA.size() + uniqueB.size())) * 100.0 : 0.0;

        // 2. LCS on tokens (sample bounded up to 200 tokens for responsiveness)
        int sampleA = tokensA.size() < 200 ? tokensA.size() : 200;
        int sampleB = tokensB.size() < 200 ? tokensB.size() : 200;
        int lcs = computeLcsTokens(tokensA, tokensB, sampleA, sampleB);
        double lcsRatio = (sampleA + sampleB) > 0 ?
                (2.0 * lcs / (sampleA + sampleB)) * 100.0 : 0.0;

        // 3. Overall similarity (balanced between token overlap and sequence order)
        double overall = (dice * 0.6) + (lcsRatio * 0.4);
        if (overall > 100.0) overall = 100.0;

        return new SimilarityReport(docA, docB, overall, jaccard, lcsRatio,
                                    commonWords.size(), commonWords, lcs);
    }

    /**
     * Computes the Longest Common Subsequence length using 2D DP.
     */
    private static int computeLcsTokens(CustomList<String> a, CustomList<String> b, int n, int m) {
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            String tokA = a.get(i - 1);
            for (int j = 1; j <= m; j++) {
                String tokB = b.get(j - 1);
                if (tokA.equalsIgnoreCase(tokB)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    int opt1 = dp[i - 1][j];
                    int opt2 = dp[i][j - 1];
                    dp[i][j] = opt1 > opt2 ? opt1 : opt2;
                }
            }
        }
        return dp[n][m];
    }

    /**
     * Splits text into clean word tokens manually without regex or external tools.
     */
    public static CustomList<String> tokenize(String text) {
        CustomList<String> tokens = new CustomList<>();
        if (text == null || text.length() == 0) {
            return tokens;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Treat spaces and common punctuation as delimiters
            if (isDelimiter(c)) {
                if (sb.length() > 0) {
                    tokens.add(sb.toString().trim());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            tokens.add(sb.toString().trim());
        }
        return tokens;
    }

    private static boolean isDelimiter(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' ||
               c == '.' || c == ',' || c == '!' || c == '?' ||
               c == ';' || c == ':' || c == '"' || c == '\'' ||
               c == '(' || c == ')' || c == '[' || c == ']' ||
               c == '{' || c == '}' || c == '-' || c == '/' ||
               c == '\\' || c == '|' || c == '।' || c == '॥'; // Includes Indian danda punctuation
    }

    private static CustomList<String> getUniqueTokens(CustomList<String> tokens) {
        CustomList<String> unique = new CustomList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String tok = tokens.get(i);
            boolean exists = false;
            for (int j = 0; j < unique.size(); j++) {
                if (unique.get(j).equalsIgnoreCase(tok)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                unique.add(tok);
            }
        }
        return unique;
    }
}
