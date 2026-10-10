package search;

import datastructures.IntList;

/**
 * Rabin-Karp String Matching Algorithm using Polynomial Rolling Hash.
 *
 * How it works:
 * 1. Hashes the pattern of length 'm' using a polynomial hash function:
 *    Hash(S) = (S[0]*d^(m-1) + S[1]*d^(m-2) + ... + S[m-1]*d^0) mod q
 *    where 'd' is a radix constant and 'q' is a prime modulus.
 *
 * 2. Hashes the first window text[0 .. m-1] in O(m) time.
 *
 * 3. Rolling Hash Update:
 *    As the window slides from index i to i+1, the new hash is computed in O(1) time:
 *    Hash(next) = (d * (Hash(prev) - text[i] * d^(m-1)) + text[i+m]) mod q
 *
 * 4. Verification & Collision Handling:
 *    When pattern hash == window hash, a character-by-character check is performed
 *    to guard against spurious hash collisions (false positives).
 *
 * Characteristics:
 * - Extremely efficient for average text searching and multi-pattern extension.
 * - Robust across Unicode Indian languages (Telugu, Hindi, Tamil) by using 64-bit arithmetic.
 *
 * Time Complexity:
 * - Preprocessing: O(m)
 * - Average Search Time: O(n + m)
 * - Worst Case:          O(n * m) (when spurious hash collisions occur at every index)
 *
 * Space Complexity: O(1) auxiliary space.
 */
public class RabinKarp {

    // Radix base and large prime modulus
    public static final int RADIX = 257;
    public static final long MODULUS = 1_000_000_007L;

    public static class RabinKarpResult {
        public final IntList matchPositions;
        public final int collisionCount;
        public final long executionTimeNs;
        public final int comparisonsCount;

        public RabinKarpResult(IntList matchPositions, int collisionCount,
                               long executionTimeNs, int comparisonsCount) {
            this.matchPositions = matchPositions;
            this.collisionCount = collisionCount;
            this.executionTimeNs = executionTimeNs;
            this.comparisonsCount = comparisonsCount;
        }
    }

    /**
     * Searches for all occurrences of pattern in text using the Rabin-Karp rolling hash.
     *
     * @param text    The source document text
     * @param pattern The search query pattern
     * @return RabinKarpResult containing match positions, collision count, and metrics
     */
    public static RabinKarpResult search(String text, String pattern) {
        IntList matches = new IntList();
        if (text == null || pattern == null) {
            return new RabinKarpResult(matches, 0, 0, 0);
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            return new RabinKarpResult(matches, 0, 0, 0);
        }

        long startTime = System.nanoTime();
        int collisions = 0;
        int comparisons = 0;

        // Compute h = (RADIX^(m-1)) % MODULUS
        long h = 1;
        for (int i = 0; i < m - 1; i++) {
            h = (h * RADIX) % MODULUS;
        }

        long patternHash = 0;
        long windowHash = 0;

        // Calculate initial hash values for pattern and the first window of text
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * RADIX + textChar(pattern, i)) % MODULUS;
            windowHash = (windowHash * RADIX + textChar(text, i)) % MODULUS;
        }

        // Slide the window over the text
        for (int i = 0; i <= n - m; i++) {
            // If hashes match, verify character by character to confirm match or collision
            if (patternHash == windowHash) {
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    comparisons++;
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    matches.add(i);
                } else {
                    // Spurious hit / hash collision!
                    collisions++;
                }
            }

            // Calculate rolling hash for next window: text[i+1 .. i+m]
            if (i < n - m) {
                long leadingCharVal = (textChar(text, i) * h) % MODULUS;
                windowHash = (windowHash - leadingCharVal + MODULUS) % MODULUS;
                windowHash = (windowHash * RADIX + textChar(text, i + m)) % MODULUS;
            }
        }

        long endTime = System.nanoTime();
        return new RabinKarpResult(matches, collisions, (endTime - startTime), comparisons);
    }

    private static long textChar(String s, int index) {
        return (long) s.charAt(index);
    }
}
