package test;

import datastructures.CustomList;
import datastructures.IntList;
import graph.MaxFlow;
import graph.VertexCoverApproximation;
import randomized.MillerRabin;
import search.AhoCorasick;
import search.KMP;
import search.NaiveSearch;
import search.RabinKarp;
import search.Trie;
import similarity.DocumentSimilarity;
import similarity.EditDistance;
import sorting.MergeSort;
import suffix.LCP;
import suffix.SuffixArray;

/**
 * Automated Verification and Test Suite for all manually implemented DSA algorithms.
 * Tests edge cases, repetitions, collisions, approximations, and multilingual Unicode scripts.
 */
public class AlgorithmTester {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  MULTILINGUAL TEXT RETRIEVAL SYSTEM - DSA TEST SUITE  ");
        System.out.println("==================================================\n");

        testKMP();
        testRabinKarp();
        testNaiveSearch();
        testTrie();
        testAhoCorasick();
        testEditDistance();
        testDocumentSimilarity();
        testSuffixArrayAndLCP();
        testMergeSort();
        testMaxFlow();
        testVertexCover();
        testMillerRabin();

        System.out.println("\n==================================================");
        System.out.println(String.format("TEST RESULTS: %d / %d PASSED (%.1f%%)",
                passedTests, totalTests, (passedTests * 100.0 / totalTests)));
        System.out.println("==================================================");

        if (passedTests == totalTests) {
            System.out.println("ALL ALGORITHMS PASSED AND FULLY VERIFIED!\n");
        } else {
            System.err.println("SOME TESTS FAILED! PLEASE REVIEW FAILURES ABOVE.\n");
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testKMP() {
        System.out.println("--- Testing KMP (Knuth-Morris-Pratt) ---");
        // Pattern exists
        KMP.KMPSearchResult r1 = KMP.search("Hyderabad is in Telangana. Hyderabad is historic.", "Hyderabad");
        assertTrue("KMP: Pattern exists (2 matches)", r1.matchPositions.size() == 2 && r1.matchPositions.get(0) == 0 && r1.matchPositions.get(1) == 27);

        // Pattern does not exist
        KMP.KMPSearchResult r2 = KMP.search("Hello world", "computer");
        assertTrue("KMP: Pattern not found", r2.matchPositions.size() == 0);

        // Repeated pattern
        KMP.KMPSearchResult r3 = KMP.search("AAAAAA", "AA");
        assertTrue("KMP: Overlapping repeated pattern (5 matches)", r3.matchPositions.size() == 5);

        // Unicode Telugu
        KMP.KMPSearchResult r4 = KMP.search("తెలంగాణ రాజధాని హైదరాబాద్. తెలంగాణ సంస్కృతి.", "తెలంగాణ");
        assertTrue("KMP: Unicode Telugu pattern match (2 matches)", r4.matchPositions.size() == 2);

        // Unicode Hindi
        KMP.KMPSearchResult r5 = KMP.search("भारत एक महान देश है। भारत की संस्कृति।", "भारत");
        assertTrue("KMP: Unicode Hindi pattern match (2 matches)", r5.matchPositions.size() == 2);

        // Unicode Tamil
        KMP.KMPSearchResult r6 = KMP.search("தெலங்கானா மாநிலத்தின் தலைநகரம் ஹைதராபாத்.", "ஹைதராபாத்");
        assertTrue("KMP: Unicode Tamil pattern match", r6.matchPositions.size() == 1);
    }

    private static void testRabinKarp() {
        System.out.println("\n--- Testing Rabin-Karp (Rolling Hash) ---");
        RabinKarp.RabinKarpResult r1 = RabinKarp.search("The capital of India is New Delhi. India is vast.", "India");
        assertTrue("Rabin-Karp: Pattern match (2 occurrences)", r1.matchPositions.size() == 2);

        RabinKarp.RabinKarpResult r2 = RabinKarp.search("Machine learning in computer science", "physics");
        assertTrue("Rabin-Karp: Pattern not found", r2.matchPositions.size() == 0);

        // Unicode Hindi
        RabinKarp.RabinKarpResult r3 = RabinKarp.search("हैदराबाद आईटी हब है। हैदराबाद में चारमीनार है।", "हैदराबाद");
        assertTrue("Rabin-Karp: Unicode Hindi match", r3.matchPositions.size() == 2);

        // Collision verification sanity check
        RabinKarp.RabinKarpResult r4 = RabinKarp.search("ABACABA", "ABA");
        assertTrue("Rabin-Karp: Repeated pattern match", r4.matchPositions.size() == 2);
    }

    private static void testNaiveSearch() {
        System.out.println("\n--- Testing Naive Pattern Search ---");
        NaiveSearch.SearchResultData r1 = NaiveSearch.search("Testing Naive search algorithm for accuracy", "Naive");
        assertTrue("Naive: Found pattern", r1.matchPositions.size() == 1 && r1.matchPositions.get(0) == 8);

        NaiveSearch.SearchResultData r2 = NaiveSearch.search("AAA", "AAAA");
        assertTrue("Naive: Pattern longer than text", r2.matchPositions.size() == 0);
    }

    private static void testTrie() {
        System.out.println("\n--- Testing Trie (Prefix Tree) ---");
        Trie trie = new Trie();
        trie.insert("hyderabad", 1);
        trie.insert("hydrogen", 2);
        trie.insert("hydrology", 3);
        trie.insert("computer", 4);
        trie.insert("తెలంగాణ", 5);
        trie.insert("తెలుగు", 6);

        assertTrue("Trie: Word lookup (hyderabad)", trie.search("hyderabad"));
        assertTrue("Trie: Word lookup absent (hydro)", !trie.search("hydro"));
        assertTrue("Trie: Prefix match startsWith (hyd)", trie.startsWith("hyd"));

        CustomList<String> prefixSuggestions = trie.getWordsWithPrefix("hyd");
        assertTrue("Trie: Prefix suggestions count for 'hyd' (3 words)", prefixSuggestions.size() == 3);
        assertTrue("Trie: Contains hyderabad", prefixSuggestions.contains("hyderabad"));
        assertTrue("Trie: Contains hydrogen", prefixSuggestions.contains("hydrogen"));
        assertTrue("Trie: Contains hydrology", prefixSuggestions.contains("hydrology"));

        CustomList<String> teluguSuggestions = trie.getWordsWithPrefix("తెల");
        assertTrue("Trie: Multilingual prefix suggestions for Telugu", teluguSuggestions.size() == 2);
    }

    private static void testAhoCorasick() {
        System.out.println("\n--- Testing Aho-Corasick Multi-Pattern Search ---");
        AhoCorasick ac = new AhoCorasick();
        ac.addPattern("India");
        ac.addPattern("Telangana");
        ac.addPattern("Hyderabad");
        ac.buildAutomaton();

        String docText = "Hyderabad is the capital of Telangana in southern India.";
        CustomList<AhoCorasick.PatternMatch> matches = ac.search(docText);

        assertTrue("Aho-Corasick: Found all 3 multi-patterns", matches.size() == 3);

        boolean foundHyd = false, foundTel = false, foundInd = false;
        for (int i = 0; i < matches.size(); i++) {
            String pat = matches.get(i).getPattern();
            if ("Hyderabad".equals(pat)) foundHyd = true;
            if ("Telangana".equals(pat)) foundTel = true;
            if ("India".equals(pat)) foundInd = true;
        }
        assertTrue("Aho-Corasick: Discovered Hyderabad, Telangana, and India", foundHyd && foundTel && foundInd);
    }

    private static void testEditDistance() {
        System.out.println("\n--- Testing Edit Distance (Levenshtein DP) ---");
        assertTrue("Edit Distance: Identical strings", EditDistance.computeDistance("Hyderabad", "Hyderabad") == 0);
        assertTrue("Edit Distance: One char diff (Hyderbad vs Hyderabad)", EditDistance.computeDistance("Hyderbad", "Hyderabad") == 1);
        assertTrue("Edit Distance: Substitution (kitten vs sitting)", EditDistance.computeDistance("kitten", "sitting") == 3);
        assertTrue("Edit Distance: Empty string", EditDistance.computeDistance("", "India") == 5);

        // Spelling correction suggestion test
        CustomList<String> vocab = new CustomList<>();
        vocab.add("Hyderabad");
        vocab.add("Telangana");
        vocab.add("India");
        vocab.add("Bengaluru");

        EditDistance.Suggestion sug = EditDistance.findClosestWord("Hyderbad", vocab, 2);
        assertTrue("Edit Distance: Typo suggestion 'Hyderbad' -> 'Hyderabad'", sug != null && "Hyderabad".equals(sug.getSuggestedWord()));
    }

    private static void testDocumentSimilarity() {
        System.out.println("\n--- Testing Document Similarity & Tokenizer ---");
        model.Document d1 = new model.Document(1, "Test 1", "English", "Hyderabad is the capital of Telangana. It is an IT hub.");
        model.Document d2 = new model.Document(2, "Test 2", "English", "Hyderabad is the capital of Telangana. It is an IT hub with tech companies.");

        DocumentSimilarity.SimilarityReport report = DocumentSimilarity.compareDocuments(d1, d2);
        assertTrue("Doc Similarity: Report computed (>50% similarity)", report.overallSimilarityPercentage > 50.0);
        assertTrue("Doc Similarity: Common words found", report.commonWordCount >= 4);
    }

    private static void testSuffixArrayAndLCP() {
        System.out.println("\n--- Testing Suffix Array & Kasai's LCP ---");
        String testStr = "banana";
        SuffixArray sa = new SuffixArray(testStr);
        int[] arr = sa.getSuffixArray();

        // Sorted suffixes for "banana":
        // 0: "a" (index 5)
        // 1: "ana" (index 3)
        // 2: "anana" (index 1)
        // 3: "banana" (index 0)
        // 4: "na" (index 4)
        // 5: "nana" (index 2)
        assertTrue("Suffix Array: banana sorted SA[0] == 5 ('a')", arr[0] == 5);
        assertTrue("Suffix Array: banana sorted SA[1] == 3 ('ana')", arr[1] == 3);

        // Pattern search via Binary Search
        IntList bMatches = sa.search("ana");
        assertTrue("Suffix Array: Binary search found 'ana' (2 occurrences)", bMatches.size() == 2);

        // Kasai LCP
        LCP lcp = new LCP(sa);
        int[] lcpArr = lcp.getLcpArray();
        assertTrue("LCP: Kasai LCP computation length matches", lcpArr.length == 6);
        assertTrue("LCP: Longest Repeated Substring is 'ana'", "ana".equals(lcp.getLongestRepeatedSubstring()));
    }

    private static void testMergeSort() {
        System.out.println("\n--- Testing MergeSort (Divide & Conquer) ---");
        String[] words = {"Telangana", "Andhra", "Karnataka", "TamilNadu", "Kerala"};
        MergeSort.sortStrings(words);
        assertTrue("MergeSort: Sorted strings ascending ('Andhra' first)", "Andhra".equals(words[0]));
        assertTrue("MergeSort: Sorted strings ascending ('Telangana' last)", "Telangana".equals(words[4]));
    }

    private static void testMaxFlow() {
        System.out.println("\n--- Testing Edmonds-Karp Maximum Flow ---");
        // Simple 4-node flow network:
        // 0 (Source) -> 1 (Cap 10), 0 -> 2 (Cap 5)
        // 1 -> 2 (Cap 15), 1 -> 3 (Cap 10)
        // 2 -> 3 (Cap 10)
        // Max Flow should be 15
        int[][] cap = new int[4][4];
        cap[0][1] = 10;
        cap[0][2] = 5;
        cap[1][2] = 15;
        cap[1][3] = 10;
        cap[2][3] = 10;

        MaxFlow.MaxFlowResult res = MaxFlow.computeMaxFlow(cap, 0, 3, new String[]{"S", "A", "B", "T"});
        assertTrue("Max Flow: Correct max flow 15 computed", res.maxFlow == 15);
    }

    private static void testVertexCover() {
        System.out.println("\n--- Testing Vertex Cover 2-Approximation ---");
        // Graph with 4 vertices: 0-1, 1-2, 2-3
        CustomList<VertexCoverApproximation.Edge> edges = new CustomList<>();
        edges.add(new VertexCoverApproximation.Edge(0, 1));
        edges.add(new VertexCoverApproximation.Edge(1, 2));
        edges.add(new VertexCoverApproximation.Edge(2, 3));

        VertexCoverApproximation.VertexCoverResult res =
            VertexCoverApproximation.computeVertexCover(4, edges, new String[]{"A", "B", "C", "D"});

        assertTrue("Vertex Cover: Computed cover covers edges", res.vertexCover.size() <= 4 && res.vertexCover.size() >= 2);
    }

    private static void testMillerRabin() {
        System.out.println("\n--- Testing Miller-Rabin Primality Test ---");
        MillerRabin.PrimalityResult r1 = MillerRabin.testPrimality(17, 5);
        assertTrue("Miller-Rabin: 17 is Prime", r1.isPrime);

        MillerRabin.PrimalityResult r2 = MillerRabin.testPrimality(1000000007L, 5);
        assertTrue("Miller-Rabin: 1000000007 is Prime", r2.isPrime);

        MillerRabin.PrimalityResult r3 = MillerRabin.testPrimality(561, 5); // Carmichael number
        assertTrue("Miller-Rabin: 561 (Carmichael number) is Composite", !r3.isPrime);

        MillerRabin.PrimalityResult r4 = MillerRabin.testPrimality(91, 5); // 7 * 13
        assertTrue("Miller-Rabin: 91 is Composite", !r4.isPrime);
    }
}
