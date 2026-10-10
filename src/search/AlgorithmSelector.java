package search;

import data.DocumentStore;
import datastructures.CustomList;
import similarity.EditDistance;

/**
 * Intelligent Algorithm Selector for the Multilingual Text Retrieval System.
 * Automatically analyzes user query characteristics and selects the optimal DSA algorithm.
 * 
 * Rules:
 * 1. Multi-Pattern: Query contains multiple distinct keywords -> Aho-Corasick Automaton
 * 2. Prefix Search: Query is short (<= 3 chars) or matches prefix in Trie -> Trie Prefix Tree
 * 3. Exact Search: Query matches known vocabulary or document text exactly -> KMP / Rabin-Karp
 * 4. Fuzzy Search: No exact matches but close to known vocabulary (distance 1-2) -> Levenshtein Edit Distance (DP) + KMP
 * 5. Default Fallback: KMP exact search with MergeSort relevance ranking
 */
public class AlgorithmSelector {

    public enum SearchRequirement {
        EXACT_SEARCH,
        PREFIX_SEARCH,
        FUZZY_SEARCH,
        MULTI_PATTERN_SEARCH,
        HASH_SEARCH
    }

    public static class SelectionDecision {
        private final String query;
        private final SearchRequirement requirement;
        private final String selectedAlgorithm;
        private final String secondaryAlgorithm;
        private final String reason;
        private final String suggestion;
        private final CustomList<String> autocompletions;
        private final String detectedLanguage;

        public SelectionDecision(String query,
                                 SearchRequirement requirement,
                                 String selectedAlgorithm,
                                 String secondaryAlgorithm,
                                 String reason,
                                 String suggestion,
                                 CustomList<String> autocompletions,
                                 String detectedLanguage) {
            this.query = query;
            this.requirement = requirement;
            this.selectedAlgorithm = selectedAlgorithm;
            this.secondaryAlgorithm = secondaryAlgorithm;
            this.reason = reason;
            this.suggestion = suggestion;
            this.autocompletions = autocompletions != null ? autocompletions : new CustomList<>();
            this.detectedLanguage = detectedLanguage;
        }

        public String getQuery() { return query; }
        public SearchRequirement getRequirement() { return requirement; }
        public String getSelectedAlgorithm() { return selectedAlgorithm; }
        public String getSecondaryAlgorithm() { return secondaryAlgorithm; }
        public String getReason() { return reason; }
        public String getSuggestion() { return suggestion; }
        public CustomList<String> getAutocompletions() { return autocompletions; }
        public String getDetectedLanguage() { return detectedLanguage; }
    }

    /**
     * Analyzes the query and returns the optimal algorithm selection decision.
     */
    public static SelectionDecision analyzeAndSelect(String query, String specifiedLanguage, DocumentStore documentStore) {
        if (query == null || query.trim().isEmpty()) {
            return new SelectionDecision(
                "",
                SearchRequirement.EXACT_SEARCH,
                "None",
                "None",
                "Empty query provided.",
                null,
                new CustomList<>(),
                specifiedLanguage != null ? specifiedLanguage : "All"
            );
        }

        String trimmed = query.trim();
        String detectedLang = detectScript(trimmed);
        String effectiveLang = (specifiedLanguage == null || "All".equalsIgnoreCase(specifiedLanguage))
                ? (detectedLang != null ? detectedLang : "All")
                : specifiedLanguage;

        // Rule 1: Check for Multi-Pattern (multiple distinct keywords separated by space or comma)
        String[] tokens = trimmed.split("[,\\s]+");
        CustomList<String> validKeywords = new CustomList<>();
        for (String t : tokens) {
            if (!t.trim().isEmpty()) {
                validKeywords.add(t.trim());
            }
        }

        if (validKeywords.size() > 1) {
            return new SelectionDecision(
                trimmed,
                SearchRequirement.MULTI_PATTERN_SEARCH,
                "Aho-Corasick Automaton",
                "MergeSort",
                "Query contains " + validKeywords.size() + " distinct keywords. Selected Aho-Corasick to simultaneously match all patterns in a single linear pass.",
                null,
                new CustomList<>(),
                effectiveLang
            );
        }

        // Rule 2: Check for Prefix Search (length <= 3 or non-terminal prefix with Trie completions)
        Trie trie = documentStore.getPrefixTrie();
        CustomList<String> trieCompletions = trie != null ? trie.getWordsWithPrefix(trimmed.toLowerCase()) : new CustomList<>();

        if (trimmed.length() <= 3 && trieCompletions.size() > 0) {
            return new SelectionDecision(
                trimmed,
                SearchRequirement.PREFIX_SEARCH,
                "Trie (Prefix Tree)",
                "MergeSort",
                "Query is a short prefix ('" + trimmed + "') matching " + trieCompletions.size() + " words in vocabulary. Selected Trie for O(m) prefix exploration.",
                null,
                trieCompletions,
                effectiveLang
            );
        }

        // Rule 3: Check for Exact Match in vocabulary or documents
        boolean exactMatchExists = false;
        if (trie != null && trie.search(trimmed.toLowerCase())) {
            exactMatchExists = true;
        }

        if (exactMatchExists) {
            return new SelectionDecision(
                trimmed,
                SearchRequirement.EXACT_SEARCH,
                "KMP (Knuth-Morris-Pratt)",
                "MergeSort",
                "Exact word found in vocabulary. Selected KMP for guaranteed O(n + m) linear search with LPS table preprocessing.",
                null,
                new CustomList<>(),
                effectiveLang
            );
        }

        // Rule 4: Check for Typo / Fuzzy Search via Levenshtein Edit Distance
        EditDistance.Suggestion suggestion = EditDistance.findClosestWord(trimmed, documentStore.getVocabulary(), 2);
        if (suggestion != null && suggestion.getDistance() > 0) {
            return new SelectionDecision(
                trimmed,
                SearchRequirement.FUZZY_SEARCH,
                "Levenshtein Edit Distance (DP)",
                "KMP",
                "Exact match not found. Detected probable spelling variation of '" + suggestion.getSuggestedWord() + "' (Edit Distance = " + suggestion.getDistance() + "). Selected Dynamic Programming for typo correction.",
                suggestion.getSuggestedWord(),
                new CustomList<>(),
                effectiveLang
            );
        }

        // Rule 5: If trie has completions for longer prefix, use Trie
        if (trieCompletions.size() > 0) {
            return new SelectionDecision(
                trimmed,
                SearchRequirement.PREFIX_SEARCH,
                "Trie (Prefix Tree)",
                "MergeSort",
                "Prefix occurrences discovered in vocabulary. Selected Trie for prefix retrieval.",
                null,
                trieCompletions,
                effectiveLang
            );
        }

        // Default: Exact KMP with MergeSort ranking
        return new SelectionDecision(
            trimmed,
            SearchRequirement.EXACT_SEARCH,
            "KMP (Knuth-Morris-Pratt)",
            "MergeSort",
            "Single keyword query. Selected KMP for deterministic linear string matching.",
            null,
            new CustomList<>(),
            effectiveLang
        );
    }

    /**
     * Automatically detects Indic script Unicode blocks.
     */
    public static String detectScript(String text) {
        if (text == null) return null;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u0C00' && c <= '\u0C7F') return "Telugu";
            if (c >= '\u0900' && c <= '\u097F') return "Hindi";
            if (c >= '\u0B80' && c <= '\u0BFF') return "Tamil";
        }
        return "English";
    }
}
