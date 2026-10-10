package test;

import data.DocumentStore;
import datastructures.CustomList;
import model.SearchResult;
import search.SearchEngine;

/**
 * Validates document loading from data/documents.txt,
 * UTF-8 Indic script parsing, and high-level SearchEngine queries.
 */
public class SearchEngineVerification {

    public static void main(String[] args) {
        System.out.println("Starting SearchEngine & Dataset Verification...");
        DocumentStore store = new DocumentStore();
        store.loadDocuments();

        System.out.println("Loaded documents count: " + store.size());
        if (store.size() < 20) {
            System.err.println("FAILED: Expected at least 20 documents, found: " + store.size());
            System.exit(1);
        }

        SearchEngine engine = new SearchEngine(store);

        // 1. Exact Search: Hyderabad
        System.out.println("\n1. Testing Exact Search: 'Hyderabad' (KMP)");
        CustomList<SearchResult> r1 = engine.searchExact("Hyderabad", "All", "KMP");
        System.out.println("Matches found: " + r1.size());
        if (r1.size() == 0) {
            System.err.println("FAILED: No matches for 'Hyderabad'");
            System.exit(1);
        }
        System.out.println("Top Match: [" + r1.get(0).getDocument().getId() + "] " + r1.get(0).getDocument().getTitle());

        // 2. Fuzzy Search: Hyderbad (typo)
        System.out.println("\n2. Testing Fuzzy Search: 'Hyderbad'");
        SearchEngine.FuzzySearchResult fRes = engine.searchFuzzy("Hyderbad", "All", 2);
        System.out.println("Suggestion: " + (fRes.suggestion != null ? fRes.suggestion.toString() : "None"));
        System.out.println("Fuzzy Results Count: " + fRes.results.size());
        if (fRes.suggestion == null || !"Hyderabad".equalsIgnoreCase(fRes.suggestion.getSuggestedWord())) {
            System.err.println("FAILED: Typo suggestion for 'Hyderbad' failed.");
            System.exit(1);
        }

        // 3. Prefix Search: hyd
        System.out.println("\n3. Testing Prefix Search: 'hyd'");
        SearchEngine.PrefixSearchResult pRes = engine.searchPrefix("hyd", "All");
        System.out.println("Autocompletions discovered: " + pRes.autocompletions.size());
        for (int i = 0; i < Math.min(5, pRes.autocompletions.size()); i++) {
            System.out.println("  - " + pRes.autocompletions.get(i));
        }
        System.out.println("Prefix Results Count: " + pRes.results.size());

        // 4. Multi-Pattern Search: India Telangana Hyderabad
        System.out.println("\n4. Testing Multi-Pattern Search: 'India', 'Telangana', 'Hyderabad'");
        CustomList<String> patterns = new CustomList<>();
        patterns.add("India");
        patterns.add("Telangana");
        patterns.add("Hyderabad");
        CustomList<SearchResult> mpRes = engine.searchMultiPattern(patterns, "All");
        System.out.println("Multi-Pattern Results Count: " + mpRes.size());

        // 5. Telugu Search: తెలంగాణ
        System.out.println("\n5. Testing Telugu Search: 'తెలంగాణ'");
        CustomList<SearchResult> telRes = engine.searchExact("తెలంగాణ", "Telugu", "KMP");
        System.out.println("Telugu Matches: " + telRes.size());
        if (telRes.size() == 0) {
            System.err.println("FAILED: No matches for Telugu query");
            System.exit(1);
        }

        // 6. Hindi Search: भारत
        System.out.println("\n6. Testing Hindi Search: 'भारत'");
        CustomList<SearchResult> hinRes = engine.searchExact("भारत", "Hindi", "KMP");
        System.out.println("Hindi Matches: " + hinRes.size());
        if (hinRes.size() == 0) {
            System.err.println("FAILED: No matches for Hindi query");
            System.exit(1);
        }

        // 7. Tamil Search: இந்தியா
        System.out.println("\n7. Testing Tamil Search: 'இந்தியா'");
        CustomList<SearchResult> tamRes = engine.searchExact("இந்தியா", "Tamil", "KMP");
        System.out.println("Tamil Matches: " + tamRes.size());
        if (tamRes.size() == 0) {
            System.err.println("FAILED: No matches for Tamil query");
            System.exit(1);
        }

        System.out.println("\n>>> ALL RETRIEVAL & MULTILINGUAL TESTS PASSED SUCCESSFULLY! <<<");
    }
}
