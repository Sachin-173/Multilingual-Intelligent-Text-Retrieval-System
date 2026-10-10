package randomized;

import datastructures.CustomList;

/**
 * Miller-Rabin Randomized Primality Testing Algorithm.
 *
 * What is Miller-Rabin?
 * - A randomized, probabilistic algorithm used to determine whether a given number n is prime.
 * - Unlike trial division (which takes O(sqrt(n))), Miller-Rabin runs in O(k * log^3 n) time,
 *   making it viable for large cryptographic primes.
 *
 * Mathematical Foundations:
 * 1. Fermat's Little Theorem: If p is prime, a^(p-1) == 1 (mod p).
 * 2. Non-trivial Square Roots of 1:
 *    If p is prime, the only solutions to x^2 == 1 (mod p) are x == 1 or x == -1 (i.e. p - 1).
 * 3. Decomposition:
 *    Write n - 1 = (2^s) * d, where d is odd.
 * 4. Test:
 *    For a chosen base 'a', compute x = (a^d) mod n.
 *    - If x == 1 or x == n - 1, 'a' does not reveal compositeness (pass round).
 *    - Square x up to s-1 times: if x becomes n - 1 at any point, pass round.
 *    - If it never reaches n - 1, 'n' is DEFINITELY COMPOSITE.
 *
 * Error Bound:
 * - If n is composite, the probability of false positive (witness failure) in 1 round is <= 1/4.
 * - After k independent rounds, error probability is <= (1/4)^k.
 * - For k = 10, error rate is < 0.00000095 (less than 1 in a million!).
 *
 * Time Complexity:  O(k * log^3 n) using modular exponentiation
 * Space Complexity: O(1) auxiliary space.
 */
public class MillerRabin {

    public static class TestStep {
        public final int round;
        public final long base;
        public final boolean passed;
        public final String detail;

        public TestStep(int round, long base, boolean passed, String detail) {
            this.round = round;
            this.base = base;
            this.passed = passed;
            this.detail = detail;
        }
    }

    public static class PrimalityResult {
        public final long number;
        public final boolean isPrime;
        public final String status; // "Composite", "Prime", "Probably Prime"
        public final int roundsRun;
        public final double errorProbability;
        public final CustomList<TestStep> steps;

        public PrimalityResult(long number, boolean isPrime, String status,
                               int roundsRun, double errorProbability,
                               CustomList<TestStep> steps) {
            this.number = number;
            this.isPrime = isPrime;
            this.status = status;
            this.roundsRun = roundsRun;
            this.errorProbability = errorProbability;
            this.steps = steps;
        }

        public String getReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("MILLER-RABIN RANDOMIZED PRIMALITY TEST REPORT\n");
            sb.append("==================================================\n");
            sb.append("Tested Number:    ").append(number).append("\n");
            sb.append("Final Verdict:    ").append(status).append("\n");
            sb.append("Rounds Tested:    ").append(roundsRun).append("\n");
            if (isPrime && number > 3) {
                sb.append(String.format("Error Probability: <= (1/4)^%d = %.10f\n", roundsRun, errorProbability));
            }
            sb.append("--------------------------------------------------\n");
            sb.append("Round Details:\n");
            for (int i = 0; i < steps.size(); i++) {
                TestStep s = steps.get(i);
                sb.append("  Round ").append(s.round).append(" (Base a=").append(s.base).append("): ")
                  .append(s.detail).append("\n");
            }
            sb.append("==================================================\n");
            return sb.toString();
        }
    }

    // Simple Linear Congruential Generator (LCG) to generate random bases without java.util.Random
    private static long lcgSeed = 123456789L;

    private static synchronized long nextRandom(long min, long max) {
        lcgSeed = (lcgSeed * 6364136223846793005L + 1442695040888963407L);
        long positive = lcgSeed & 0x7FFFFFFFFFFFFFFFL;
        long range = (max - min + 1);
        return min + (positive % range);
    }

    /**
     * Tests primality of n with k iterations.
     */
    public static PrimalityResult testPrimality(long n, int k) {
        CustomList<TestStep> steps = new CustomList<>();

        if (n <= 1) {
            steps.add(new TestStep(1, 0, false, "Numbers <= 1 are not prime by definition."));
            return new PrimalityResult(n, false, "Composite (or <= 1)", 1, 0.0, steps);
        }
        if (n == 2 || n == 3) {
            steps.add(new TestStep(1, n, true, n + " is a known base prime."));
            return new PrimalityResult(n, true, "Prime (Definite)", 1, 0.0, steps);
        }
        if (n % 2 == 0) {
            steps.add(new TestStep(1, 2, false, n + " is even and > 2, hence composite."));
            return new PrimalityResult(n, false, "Composite (Even Number)", 1, 0.0, steps);
        }

        // Decompose n - 1 into (2^s) * d
        long d = n - 1;
        int s = 0;
        while (d % 2 == 0) {
            d /= 2;
            s++;
        }

        // Deterministic bases for small numbers, or random selection
        long[] presetBases = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};

        for (int r = 1; r <= k; r++) {
            long a;
            if (r - 1 < presetBases.length && presetBases[r - 1] < n - 1) {
                a = presetBases[r - 1];
            } else {
                a = nextRandom(2, n - 2);
            }

            long x = powerMod(a, d, n);

            if (x == 1 || x == n - 1) {
                steps.add(new TestStep(r, a, true, "a^d mod n = " + x + " (Inconclusive, passes round)"));
                continue;
            }

            boolean composite = true;
            for (int rStep = 1; rStep < s; rStep++) {
                x = multiplyMod(x, x, n);
                if (x == n - 1) {
                    composite = false;
                    steps.add(new TestStep(r, a, true, "x reached (n-1) at squaring step " + rStep + " (Passes round)"));
                    break;
                }
            }

            if (composite) {
                steps.add(new TestStep(r, a, false, "Witness found: x never reached (n-1). DEFINITELY COMPOSITE!"));
                return new PrimalityResult(n, false, "Composite", r, 0.0, steps);
            }
        }

        double errorProb = Math.pow(0.25, k);
        return new PrimalityResult(n, true, "Probably Prime", k, errorProb, steps);
    }

    /**
     * Computes (base^exp) % mod manually in O(log exp) time.
     */
    private static long powerMod(long base, long exp, long mod) {
        long res = 1;
        base = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                res = multiplyMod(res, base, mod);
            }
            base = multiplyMod(base, base, mod);
            exp >>= 1;
        }
        return res;
    }

    /**
     * Prevents 64-bit overflow during multiplication (a * b) % mod.
     */
    private static long multiplyMod(long a, long b, long mod) {
        long res = 0;
        a %= mod;
        while (b > 0) {
            if ((b & 1) == 1) {
                res = (res + a) % mod;
            }
            a = (a * 2) % mod;
            b >>= 1;
        }
        return res;
    }
}
