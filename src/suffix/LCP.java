package suffix;

/**
 * Longest Common Prefix (LCP) Array using Kasai's Algorithm.
 *
 * What is the LCP Array?
 * - An auxiliary array to the Suffix Array (SA).
 * - LCP[i] stores the length of the longest common prefix between two consecutive
 *   suffixes in the sorted suffix array: suffix SA[i-1] and suffix SA[i].
 * - LCP[0] is typically defined as 0.
 *
 * How Kasai's Algorithm Works in O(n) Time:
 * 1. Computes the inverse array (rank array) where rank[SA[i]] = i.
 * 2. Processes suffixes in original text order: i = 0, 1, 2, ..., n-1.
 * 3. Crucial Invariant:
 *    If suffix i has LCP of length h with its preceding suffix in SA,
 *    then suffix i+1 will have an LCP of at least (h - 1) with its preceding suffix.
 * 4. Therefore, h decreases by at most 1 per step, making total increments bounded by 2n.
 *
 * Applications:
 * - Finding the Longest Repeated Substring (LRS) in linear time:
 *   LRS is simply the maximum value in the LCP array.
 * - Substring frequency analysis and genome/text compression.
 *
 * Time Complexity:  O(n) linear time after Suffix Array construction.
 * Space Complexity: O(n) auxiliary space for rank and LCP arrays.
 */
public class LCP {

    private final SuffixArray suffixArray;
    private final int[] lcpArray;
    private String longestRepeatedSubstring;
    private int maxLcpLength;
    private int maxLcpIndex;

    public LCP(SuffixArray suffixArray) {
        this.suffixArray = suffixArray;
        int n = suffixArray.length();
        this.lcpArray = new int[n];
        this.longestRepeatedSubstring = "";
        this.maxLcpLength = 0;
        this.maxLcpIndex = -1;

        if (n > 0) {
            buildKasaiLcp();
        }
    }

    private void buildKasaiLcp() {
        String text = suffixArray.getText();
        int[] sa = suffixArray.getSuffixArray();
        int n = text.length();

        // 1. Build rank (inverse suffix) array: rank[sa[i]] = i
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int h = 0; // Current LCP length tracker
        lcpArray[0] = 0;

        // 2. Iterate through suffixes in text order
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                // Predecessor suffix in sorted suffix array
                int j = sa[rank[i] - 1];

                // Match characters while within bounds
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) {
                    h++;
                }

                lcpArray[rank[i]] = h;

                // Track Longest Repeated Substring
                if (h > maxLcpLength) {
                    maxLcpLength = h;
                    maxLcpIndex = rank[i];
                    longestRepeatedSubstring = text.substring(i, i + h);
                }

                // Kasai's invariant: decrement h by at most 1 for next iteration
                if (h > 0) {
                    h--;
                }
            }
        }
    }

    public int[] getLcpArray() {
        return lcpArray;
    }

    public SuffixArray getSuffixArray() {
        return suffixArray;
    }

    public String getLongestRepeatedSubstring() {
        return longestRepeatedSubstring;
    }

    public int getMaxLcpLength() {
        return maxLcpLength;
    }

    public int getMaxLcpIndex() {
        return maxLcpIndex;
    }

    public int length() {
        return lcpArray.length;
    }

    /**
     * Formats the Suffix Array and LCP Table for display and viva explanation.
     */
    public String getAnalysisReport(int maxRows) {
        StringBuilder sb = new StringBuilder();
        int n = suffixArray.length();
        int limit = n < maxRows ? n : maxRows;

        sb.append(String.format("%-5s | %-8s | %-5s | %s\n", "i", "SA[i]", "LCP", "Suffix Preview"));
        sb.append("------------------------------------------------------------\n");

        for (int i = 0; i < limit; i++) {
            int saVal = suffixArray.getSuffixArray()[i];
            int lcpVal = lcpArray[i];
            String preview = suffixArray.getSuffixPreview(i, 35);
            // Replace newlines with spaces for clean table formatting
            preview = preview.replace('\n', ' ').replace('\r', ' ');
            sb.append(String.format("%-5d | %-8d | %-5d | %s\n", i, saVal, lcpVal, preview));
        }

        if (n > limit) {
            sb.append("... (and ").append(n - limit).append(" more suffixes)\n");
        }

        sb.append("------------------------------------------------------------\n");
        sb.append("Longest Repeated Substring (LRS):\n");
        if (maxLcpLength > 0) {
            sb.append("  Length: ").append(maxLcpLength).append("\n");
            sb.append("  Text:   \"").append(longestRepeatedSubstring).append("\"\n");
        } else {
            sb.append("  No repeated substrings found.\n");
        }

        return sb.toString();
    }
}
