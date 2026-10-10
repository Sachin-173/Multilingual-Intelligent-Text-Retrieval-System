package search;

import data.DocumentStore;
import datastructures.CustomList;
import datastructures.IntList;
import model.Document;
import model.SearchResult;
import ranking.Ranker;
import similarity.DocumentSimilarity;
import similarity.EditDistance;

/**
 * High-level Multilingual Search Retrieval Engine.
 * Integrates Naive Search, KMP, Rabin-Karp, Trie, Aho-Corasick, Edit Distance, and Ranker.
 */
public class SearchEngine {

    private final DocumentStore documentStore;

    public SearchEngine(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    /**
     * Executes Exact String Matching using the chosen algorithm (KMP, Rabin-Karp, or Naive).
     */
    public CustomList<SearchResult> searchExact(String query, String language, String algorithm) {
        CustomList<SearchResult> rawResults = new CustomList<>();
        if (query == null || query.trim().length() == 0) {
            return rawResults;
        }

        String trimmedQuery = query.trim();
        String lowerQuery = trimmedQuery.toLowerCase();
        CustomList<Document> docs = documentStore.getDocumentsByLanguage(language);

        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            String titleText = doc.getTitle().toLowerCase();
            String contentText = doc.getContent().toLowerCase();
            IntList titleMatches;
            IntList contentMatches;
            long timeNs = 0;

            if ("Naive".equalsIgnoreCase(algorithm)) {
                NaiveSearch.SearchResultData tRes = NaiveSearch.search(titleText, lowerQuery);
                NaiveSearch.SearchResultData cRes = NaiveSearch.search(contentText, lowerQuery);
                titleMatches = tRes.matchPositions;
                contentMatches = cRes.matchPositions;
                timeNs = tRes.executionTimeNs + cRes.executionTimeNs;
            } else if ("Rabin-Karp".equalsIgnoreCase(algorithm) || "RabinKarp".equalsIgnoreCase(algorithm)) {
                RabinKarp.RabinKarpResult tRes = RabinKarp.search(titleText, lowerQuery);
                RabinKarp.RabinKarpResult cRes = RabinKarp.search(contentText, lowerQuery);
                titleMatches = tRes.matchPositions;
                contentMatches = cRes.matchPositions;
                timeNs = tRes.executionTimeNs + cRes.executionTimeNs;
            } else { // Default to KMP
                KMP.KMPSearchResult tRes = KMP.search(titleText, lowerQuery);
                KMP.KMPSearchResult cRes = KMP.search(contentText, lowerQuery);
                titleMatches = tRes.matchPositions;
                contentMatches = cRes.matchPositions;
                timeNs = tRes.executionTimeNs + cRes.executionTimeNs;
            }

            int totalMatches = titleMatches.size() + contentMatches.size();
            if (totalMatches > 0) {
                String snippet = extractSnippet(doc, trimmedQuery, titleMatches, contentMatches);
                SearchResult sr = new SearchResult(
                    doc, totalMatches, 0.0, snippet,
                    algorithm != null ? algorithm : "KMP",
                    contentMatches, "Exact", timeNs
                );
                rawResults.add(sr);
            }
        }

        return Ranker.rankAndSort(rawResults);
    }

    /**
     * Executes Fuzzy Search using Levenshtein Edit Distance.
     * Generates spelling correction suggestions ("Did you mean?").
     */
    public static class FuzzySearchResult {
        public final CustomList<SearchResult> results;
        public final EditDistance.Suggestion suggestion;

        public FuzzySearchResult(CustomList<SearchResult> results, EditDistance.Suggestion suggestion) {
            this.results = results;
            this.suggestion = suggestion;
        }
    }

    public FuzzySearchResult searchFuzzy(String query, String language, int maxDistance) {
        CustomList<SearchResult> rawResults = new CustomList<>();
        if (query == null || query.trim().length() == 0) {
            return new FuzzySearchResult(rawResults, null);
        }

        String trimmedQuery = query.trim();
        long startTime = System.nanoTime();

        // 1. Check vocabulary for closest spelling correction
        EditDistance.Suggestion suggestion = EditDistance.findClosestWord(
            trimmedQuery, documentStore.getVocabulary(), maxDistance
        );

        String effectiveQuery = trimmedQuery;
        if (suggestion != null && suggestion.getDistance() > 0) {
            effectiveQuery = suggestion.getSuggestedWord();
        }

        CustomList<Document> docs = documentStore.getDocumentsByLanguage(language);

        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            CustomList<String> tokens = DocumentSimilarity.tokenize(doc.getTitle() + " " + doc.getContent());

            int bestDistForDoc = Integer.MAX_VALUE;
            String matchedWord = null;
            int fuzzyMatchCount = 0;

            for (int t = 0; t < tokens.size(); t++) {
                String tok = tokens.get(t);
                // Exact or fuzzy match against query or suggestion
                int d1 = EditDistance.computeDistance(trimmedQuery.toLowerCase(), tok.toLowerCase());
                int d2 = suggestion != null ? EditDistance.computeDistance(effectiveQuery.toLowerCase(), tok.toLowerCase()) : Integer.MAX_VALUE;
                int d = d1 < d2 ? d1 : d2;

                if (d <= maxDistance) {
                    fuzzyMatchCount++;
                    if (d < bestDistForDoc) {
                        bestDistForDoc = d;
                        matchedWord = tok;
                    }
                }
            }

            if (fuzzyMatchCount > 0) {
                double baseScore = Math.max(20.0, 85.0 - (bestDistForDoc * 20.0));
                double finalScore = baseScore + (fuzzyMatchCount * 5.0);
                String snippet = "Matched token: '" + matchedWord + "' (Edit Distance: " + bestDistForDoc + ")";
                long timeNs = System.nanoTime() - startTime;

                SearchResult sr = new SearchResult(
                    doc, fuzzyMatchCount, finalScore, snippet,
                    "EditDistance (DP)", new IntList(), "Fuzzy", timeNs
                );
                rawResults.add(sr);
            }
        }

        CustomList<SearchResult> ranked = Ranker.rankAndSort(rawResults);
        return new FuzzySearchResult(ranked, suggestion);
    }

    /**
     * Executes Prefix Search using Trie.
     */
    public static class PrefixSearchResult {
        public final CustomList<SearchResult> results;
        public final CustomList<String> autocompletions;

        public PrefixSearchResult(CustomList<SearchResult> results, CustomList<String> autocompletions) {
            this.results = results;
            this.autocompletions = autocompletions;
        }
    }

    public PrefixSearchResult searchPrefix(String prefix, String language) {
        CustomList<SearchResult> rawResults = new CustomList<>();
        if (prefix == null || prefix.trim().length() == 0) {
            return new PrefixSearchResult(rawResults, new CustomList<>());
        }

        String cleanPrefix = prefix.trim().toLowerCase();
        long startTime = System.nanoTime();

        Trie trie = documentStore.getPrefixTrie();
        CustomList<String> suggestions = trie.getWordsWithPrefix(cleanPrefix);
        IntList docIds = trie.getDocumentIdsWithPrefix(cleanPrefix);

        CustomList<Document> filteredDocs = documentStore.getDocumentsByLanguage(language);

        for (int i = 0; i < docIds.size(); i++) {
            int id = docIds.get(i);
            Document doc = documentStore.getDocumentById(id);
            if (doc != null && filteredDocs.contains(doc)) {
                // Count occurrences of prefix words in doc
                int matchCount = 0;
                StringBuilder snippetSb = new StringBuilder("Prefix words: ");
                CustomList<String> docTokens = DocumentSimilarity.tokenize(doc.getTitle() + " " + doc.getContent());

                for (int t = 0; t < docTokens.size(); t++) {
                    String tok = docTokens.get(t);
                    if (tok.toLowerCase().startsWith(cleanPrefix.toLowerCase())) {
                        matchCount++;
                        if (snippetSb.length() < 60) {
                            snippetSb.append(tok).append(", ");
                        }
                    }
                }

                long timeNs = System.nanoTime() - startTime;
                SearchResult sr = new SearchResult(
                    doc, matchCount > 0 ? matchCount : 1, 0.0,
                    snippetSb.toString(), "Trie",
                    new IntList(), "Prefix", timeNs
                );
                rawResults.add(sr);
            }
        }

        CustomList<SearchResult> ranked = Ranker.rankAndSort(rawResults);
        return new PrefixSearchResult(ranked, suggestions);
    }

    /**
     * Executes Multi-Pattern String Search using Aho-Corasick automaton.
     */
    public CustomList<SearchResult> searchMultiPattern(CustomList<String> patterns, String language) {
        CustomList<SearchResult> rawResults = new CustomList<>();
        if (patterns == null || patterns.size() == 0) {
            return rawResults;
        }

        AhoCorasick ac = new AhoCorasick();
        for (int i = 0; i < patterns.size(); i++) {
            ac.addPattern(patterns.get(i));
        }
        ac.buildAutomaton();

        CustomList<Document> docs = documentStore.getDocumentsByLanguage(language);

        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            long startTime = System.nanoTime();

            CustomList<AhoCorasick.PatternMatch> titleMatches = ac.search(doc.getTitle());
            CustomList<AhoCorasick.PatternMatch> contentMatches = ac.search(doc.getContent());
            long timeNs = System.nanoTime() - startTime;

            int totalMatches = titleMatches.size() + contentMatches.size();
            if (totalMatches > 0) {
                // Build summary of which patterns matched
                CustomList<String> matchedPatterns = new CustomList<>();
                for (int m = 0; m < titleMatches.size(); m++) {
                    String p = titleMatches.get(m).getPattern();
                    if (!matchedPatterns.contains(p)) matchedPatterns.add(p);
                }
                for (int m = 0; m < contentMatches.size(); m++) {
                    String p = contentMatches.get(m).getPattern();
                    if (!matchedPatterns.contains(p)) matchedPatterns.add(p);
                }

                StringBuilder snippet = new StringBuilder("Patterns matched: ");
                for (int p = 0; p < matchedPatterns.size(); p++) {
                    snippet.append("[").append(matchedPatterns.get(p)).append("] ");
                }

                SearchResult sr = new SearchResult(
                    doc, totalMatches, 0.0, snippet.toString(),
                    "Aho-Corasick", new IntList(), "Multi-Pattern", timeNs
                );
                rawResults.add(sr);
            }
        }

        return Ranker.rankAndSort(rawResults);
    }

    public static class AutoSearchResult {
        public final CustomList<SearchResult> results;
        public final String detectedSearchType;
        public final String detectedAlgorithm;
        public final String detectedLanguage;
        public final String reason;
        public final EditDistance.Suggestion suggestion;
        public final CustomList<String> autocompletions;

        public AutoSearchResult(CustomList<SearchResult> results,
                                String detectedSearchType,
                                String detectedAlgorithm,
                                String detectedLanguage,
                                String reason,
                                EditDistance.Suggestion suggestion,
                                CustomList<String> autocompletions) {
            this.results = results;
            this.detectedSearchType = detectedSearchType;
            this.detectedAlgorithm = detectedAlgorithm;
            this.detectedLanguage = detectedLanguage;
            this.reason = reason;
            this.suggestion = suggestion;
            this.autocompletions = autocompletions;
        }
    }

    /**
     * Automatically and intelligently selects the best search type and DSA algorithm
     * based on pattern count, prefix characteristics, exact vocabulary matches, and edit distance.
     */
    public AutoSearchResult searchAuto(String query, String specifiedLanguage) {
        if (query == null || query.trim().length() == 0) {
            return new AutoSearchResult(new CustomList<>(), "EXACT_SEARCH", "None", specifiedLanguage, "Query is empty", null, new CustomList<>());
        }

        AlgorithmSelector.SelectionDecision decision =
            AlgorithmSelector.analyzeAndSelect(query, specifiedLanguage, documentStore);

        CustomList<SearchResult> results;
        if (decision.getRequirement() == AlgorithmSelector.SearchRequirement.MULTI_PATTERN_SEARCH) {
            String[] tokens = query.trim().split("[,\\s]+");
            CustomList<String> patternList = new CustomList<>();
            for (String t : tokens) {
                if (!t.trim().isEmpty()) patternList.add(t.trim());
            }
            results = searchMultiPattern(patternList, decision.getDetectedLanguage());
        } else if (decision.getRequirement() == AlgorithmSelector.SearchRequirement.PREFIX_SEARCH) {
            PrefixSearchResult pRes = searchPrefix(query.trim(), decision.getDetectedLanguage());
            results = pRes.results;
        } else if (decision.getRequirement() == AlgorithmSelector.SearchRequirement.FUZZY_SEARCH) {
            FuzzySearchResult fRes = searchFuzzy(query.trim(), decision.getDetectedLanguage(), 2);
            results = fRes.results;
        } else {
            results = searchExact(query.trim(), decision.getDetectedLanguage(), "KMP");
        }

        EditDistance.Suggestion sugg = decision.getSuggestion() != null ?
            new EditDistance.Suggestion(query.trim(), decision.getSuggestion(), 1, 0.9) : null;

        return new AutoSearchResult(
            results,
            decision.getRequirement().name(),
            decision.getSelectedAlgorithm(),
            decision.getDetectedLanguage(),
            decision.getReason(),
            sugg,
            decision.getAutocompletions()
        );
    }

    public static String detectScriptLanguage(String text) {
        if (text == null) return null;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u0C00' && c <= '\u0C7F') return "Telugu";
            if (c >= '\u0900' && c <= '\u097F') return "Hindi";
            if (c >= '\u0B80' && c <= '\u0BFF') return "Tamil";
        }
        return null;
    }

    private String extractSnippet(Document doc, String query, IntList titleMatches, IntList contentMatches) {
        if (titleMatches.size() > 0 && contentMatches.size() == 0) {
            return "[Title Match] " + doc.getTitle();
        }

        if (contentMatches.size() > 0) {
            int pos = contentMatches.get(0);
            int start = Math.max(0, pos - 30);
            int end = Math.min(doc.getContent().length(), pos + query.length() + 30);
            String snippet = doc.getContent().substring(start, end).replace('\n', ' ');
            return (start > 0 ? "..." : "") + snippet + (end < doc.getContent().length() ? "..." : "");
        }

        return doc.getTitle();
    }
}
