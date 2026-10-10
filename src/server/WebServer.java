package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import data.DocumentStore;
import datastructures.CustomList;
import graph.MaxFlow;
import graph.VertexCoverApproximation;
import model.Document;
import model.SearchResult;
import randomized.MillerRabin;
import search.AlgorithmSelector;
import search.KMP;
import search.NaiveSearch;
import search.RabinKarp;
import search.SearchEngine;
import similarity.DocumentSimilarity;
import similarity.EditDistance;
import suffix.LCP;
import suffix.SuffixArray;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Built-in zero-dependency local HTTP API server using Java SE standard com.sun.net.httpserver.
 * Serves the full-featured Multilingual Text Retrieval Website and REST APIs.
 * 100% offline, requires NO external libraries or Spring Boot.
 */
public class WebServer {

    private final int port;
    private final DocumentStore documentStore;
    private final SearchEngine searchEngine;
    private HttpServer server;

    public WebServer(int port, DocumentStore documentStore, SearchEngine searchEngine) {
        this.port = port;
        this.documentStore = documentStore;
        this.searchEngine = searchEngine;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Core Search & Retrieval REST API Endpoints
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/search/fuzzy", new FuzzySearchHandler());
        server.createContext("/api/search/prefix", new PrefixSearchHandler());
        server.createContext("/api/search/multi", new MultiSearchHandler());

        // Comparison & Similarity REST APIs
        server.createContext("/api/benchmark", new BenchmarkHandler());
        server.createContext("/api/algorithm/compare", new BenchmarkHandler());
        server.createContext("/api/similarity", new SimilarityHandler());
        server.createContext("/api/suffix", new SuffixHandler());

        // Dataset & Algorithm Knowledge Base APIs
        server.createContext("/api/documents", new DocumentsHandler());
        server.createContext("/api/algorithms", new AlgorithmsInfoHandler());

        // Advanced Algorithm Lab APIs
        server.createContext("/api/maxflow", new MaxFlowHandler());
        server.createContext("/api/advanced/maxflow", new MaxFlowHandler());
        server.createContext("/api/vertexcover", new VertexCoverHandler());
        server.createContext("/api/advanced/vertexcover", new VertexCoverHandler());
        server.createContext("/api/millerrabin", new MillerRabinHandler());
        server.createContext("/api/advanced/miller-rabin", new MillerRabinHandler());

        // Website Static Files & SPA Route Handler (serves frontend/ and web/)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // Default executor
        server.start();
        System.out.println("==================================================================");
        System.out.println("  MULTILINGUAL TEXT RETRIEVAL SYSTEM - WEBSITE ONLINE");
        System.out.println("  Website URL: http://localhost:" + port);
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // --- Static File & SPA Route Serving ---
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Single Page Application (SPA) routes mapping
            if (path.equals("/") || path.isEmpty() ||
                path.equals("/search") || path.equals("/algorithms") ||
                path.equals("/documents") || path.equals("/dataset") ||
                path.equals("/about")) {
                path = "/index.html";
            }

            File file = resolveFile(path);

            if (file != null && file.exists() && file.isFile()) {
                String mime = getMimeType(file.getName());
                exchange.getResponseHeaders().set("Content-Type", mime + "; charset=utf-8");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

                byte[] bytes = readAllBytes(file);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "404 Not Found - Path: " + path;
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        private File resolveFile(String path) {
            // Check frontend/, then web/, then root
            File f = new File("frontend" + path);
            if (f.exists()) return f;

            f = new File("web" + path);
            if (f.exists()) return f;

            f = new File("." + path);
            if (f.exists()) return f;

            f = new File("../frontend" + path);
            if (f.exists()) return f;

            f = new File("../web" + path);
            if (f.exists()) return f;

            return null;
        }

        private String getMimeType(String name) {
            if (name.endsWith(".html")) return "text/html";
            if (name.endsWith(".css")) return "text/css";
            if (name.endsWith(".js")) return "application/javascript";
            if (name.endsWith(".json")) return "application/json";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }

        private byte[] readAllBytes(File file) throws IOException {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int read;
                while ((read = fis.read(buf)) != -1) {
                    bos.write(buf, 0, read);
                }
                return bos.toByteArray();
            }
        }
    }

    // --- Search API Handler (Auto & General) ---
    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = getParam(exchange, "q");
            String lang = getParam(exchange, "lang");
            String type = getParam(exchange, "type");
            String algo = getParam(exchange, "algo");

            if (lang == null || lang.isEmpty()) lang = "All";
            if (type == null || type.isEmpty()) type = "auto";

            CustomList<SearchResult> results;
            String detectedReq;
            String selectedAlgo;
            String secondaryAlgo;
            String reason;
            String suggestion = null;
            CustomList<String> autocompletions = new CustomList<>();
            long startTime = System.nanoTime();

            if ("auto".equalsIgnoreCase(type) || "Automatic".equalsIgnoreCase(type)) {
                AlgorithmSelector.SelectionDecision decision =
                    AlgorithmSelector.analyzeAndSelect(query, lang, documentStore);
                
                detectedReq = decision.getRequirement().name();
                selectedAlgo = decision.getSelectedAlgorithm();
                secondaryAlgo = decision.getSecondaryAlgorithm();
                reason = decision.getReason();
                suggestion = decision.getSuggestion();
                autocompletions = decision.getAutocompletions();

                if (decision.getRequirement() == AlgorithmSelector.SearchRequirement.MULTI_PATTERN_SEARCH) {
                    String[] tokens = query != null ? query.trim().split("[,\\s]+") : new String[0];
                    CustomList<String> pList = new CustomList<>();
                    for (String t : tokens) if (!t.trim().isEmpty()) pList.add(t.trim());
                    results = searchEngine.searchMultiPattern(pList, decision.getDetectedLanguage());
                } else if (decision.getRequirement() == AlgorithmSelector.SearchRequirement.PREFIX_SEARCH) {
                    SearchEngine.PrefixSearchResult pRes = searchEngine.searchPrefix(query, decision.getDetectedLanguage());
                    results = pRes.results;
                } else if (decision.getRequirement() == AlgorithmSelector.SearchRequirement.FUZZY_SEARCH) {
                    SearchEngine.FuzzySearchResult fRes = searchEngine.searchFuzzy(query, decision.getDetectedLanguage(), 2);
                    results = fRes.results;
                } else {
                    results = searchEngine.searchExact(query, decision.getDetectedLanguage(), "KMP");
                }
            } else if ("exact".equalsIgnoreCase(type) || "Exact Search".equalsIgnoreCase(type)) {
                detectedReq = "EXACT_SEARCH";
                selectedAlgo = algo != null ? algo : "KMP";
                secondaryAlgo = "MergeSort";
                reason = "User explicitly specified exact search.";
                results = searchEngine.searchExact(query, lang, selectedAlgo);
            } else if ("fuzzy".equalsIgnoreCase(type) || "Fuzzy Search".equalsIgnoreCase(type)) {
                detectedReq = "FUZZY_SEARCH";
                selectedAlgo = "Levenshtein Edit Distance (DP)";
                secondaryAlgo = "KMP";
                reason = "User explicitly requested fuzzy spelling correction.";
                SearchEngine.FuzzySearchResult fRes = searchEngine.searchFuzzy(query, lang, 2);
                results = fRes.results;
                if (fRes.suggestion != null && fRes.suggestion.getDistance() > 0) {
                    suggestion = fRes.suggestion.getSuggestedWord();
                }
            } else if ("prefix".equalsIgnoreCase(type) || "Prefix Search".equalsIgnoreCase(type)) {
                detectedReq = "PREFIX_SEARCH";
                selectedAlgo = "Trie (Prefix Tree)";
                secondaryAlgo = "MergeSort";
                reason = "User explicitly requested prefix search.";
                SearchEngine.PrefixSearchResult pRes = searchEngine.searchPrefix(query, lang);
                results = pRes.results;
                autocompletions = pRes.autocompletions;
            } else {
                detectedReq = "MULTI_PATTERN_SEARCH";
                selectedAlgo = "Aho-Corasick Automaton";
                secondaryAlgo = "MergeSort";
                reason = "User explicitly requested multi-keyword search.";
                CustomList<String> pList = new CustomList<>();
                if (query != null) {
                    for (String tok : query.split("[,\\s]+")) {
                        if (!tok.trim().isEmpty()) pList.add(tok.trim());
                    }
                }
                results = searchEngine.searchMultiPattern(pList, lang);
            }

            double totalTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;

            String json = buildSearchResponseJson(query, detectedReq, selectedAlgo, secondaryAlgo,
                reason, suggestion, autocompletions, results, totalTimeMs);

            sendJsonResponse(exchange, json);
        }
    }

    // --- Dedicated Fuzzy Search API Handler ---
    private class FuzzySearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = getParam(exchange, "q");
            String lang = getParam(exchange, "lang");
            if (lang == null || lang.isEmpty()) lang = "All";

            long startTime = System.nanoTime();
            SearchEngine.FuzzySearchResult fRes = searchEngine.searchFuzzy(query, lang, 2);
            double totalTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;

            String suggestion = fRes.suggestion != null ? fRes.suggestion.getSuggestedWord() : null;
            String reason = "Computed minimum edit distance using dynamic programming matrix.";

            String json = buildSearchResponseJson(query, "FUZZY_SEARCH", "Levenshtein Edit Distance (DP)",
                "KMP", reason, suggestion, new CustomList<>(), fRes.results, totalTimeMs);

            sendJsonResponse(exchange, json);
        }
    }

    // --- Dedicated Prefix Search API Handler ---
    private class PrefixSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = getParam(exchange, "q");
            String lang = getParam(exchange, "lang");
            if (lang == null || lang.isEmpty()) lang = "All";

            long startTime = System.nanoTime();
            SearchEngine.PrefixSearchResult pRes = searchEngine.searchPrefix(query, lang);
            double totalTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;

            String reason = "Explored prefix subtree in Trie without hashing.";

            String json = buildSearchResponseJson(query, "PREFIX_SEARCH", "Trie (Prefix Tree)",
                "MergeSort", reason, null, pRes.autocompletions, pRes.results, totalTimeMs);

            sendJsonResponse(exchange, json);
        }
    }

    // --- Dedicated Multi-Pattern API Handler ---
    private class MultiSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = getParam(exchange, "q");
            String lang = getParam(exchange, "lang");
            if (lang == null || lang.isEmpty()) lang = "All";

            CustomList<String> patterns = new CustomList<>();
            if (query != null) {
                for (String t : query.split("[,\\s]+")) {
                    if (!t.trim().isEmpty()) patterns.add(t.trim());
                }
            }

            long startTime = System.nanoTime();
            CustomList<SearchResult> results = searchEngine.searchMultiPattern(patterns, lang);
            double totalTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;

            String reason = "Simultaneously searched " + patterns.size() + " patterns via Aho-Corasick automaton.";

            String json = buildSearchResponseJson(query, "MULTI_PATTERN_SEARCH", "Aho-Corasick Automaton",
                "MergeSort", reason, null, new CustomList<>(), results, totalTimeMs);

            sendJsonResponse(exchange, json);
        }
    }

    private static String buildSearchResponseJson(String query,
                                                  String detectedReq,
                                                  String selectedAlgo,
                                                  String secondaryAlgo,
                                                  String reason,
                                                  String suggestion,
                                                  CustomList<String> autocompletions,
                                                  CustomList<SearchResult> results,
                                                  double totalTimeMs) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"query\":\"").append(escape(query)).append("\",");
        json.append("\"detectedRequirement\":\"").append(escape(detectedReq)).append("\",");
        json.append("\"selectedAlgorithm\":\"").append(escape(selectedAlgo)).append("\",");
        json.append("\"secondaryAlgorithm\":\"").append(escape(secondaryAlgo)).append("\",");
        json.append("\"reason\":").append(reason != null ? "\"" + escape(reason) + "\"" : "null").append(",");
        json.append("\"suggestion\":").append(suggestion != null ? "\"" + escape(suggestion) + "\"" : "null").append(",");
        json.append("\"matchCount\":").append(results != null ? results.size() : 0).append(",");
        json.append("\"executionTimeMs\":").append(String.format("%.3f", totalTimeMs)).append(",");
        json.append("\"resolvedMode\":\"").append(escape(detectedReq)).append(" (").append(escape(selectedAlgo)).append(")\",");

        json.append("\"autocompletions\":[");
        if (autocompletions != null) {
            for (int i = 0; i < autocompletions.size(); i++) {
                json.append("\"").append(escape(autocompletions.get(i))).append("\"");
                if (i < autocompletions.size() - 1) json.append(",");
            }
        }
        json.append("],");

        json.append("\"results\":[");
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                SearchResult sr = results.get(i);
                Document d = sr.getDocument();
                json.append("{");
                json.append("\"documentId\":").append(d.getId()).append(",");
                json.append("\"documentTitle\":\"").append(escape(d.getTitle())).append("\",");
                json.append("\"language\":\"").append(escape(d.getLanguage())).append("\",");
                json.append("\"category\":\"").append(escape(d.getCategory())).append("\",");
                json.append("\"snippet\":\"").append(escape(sr.getMatchedSnippet())).append("\",");
                json.append("\"content\":\"").append(escape(d.getContent())).append("\",");
                json.append("\"score\":").append(String.format("%.1f", sr.getScore())).append(",");
                json.append("\"matchScore\":").append(Math.min(100, (int) Math.round(sr.getScore()))).append(",");
                String rankLevel = sr.getScore() >= 85 ? "High" : (sr.getScore() >= 50 ? "Medium" : "Relevant");
                json.append("\"rankLevel\":\"").append(rankLevel).append("\",");
                json.append("\"algorithmUsed\":\"").append(escape(sr.getAlgorithmUsed())).append("\",");
                json.append("\"matchType\":\"").append(escape(sr.getMatchType())).append("\",");
                json.append("\"matchCount\":").append(sr.getMatchCount()).append(",");
                json.append("\"searchTimeMs\":").append(String.format("%.3f", sr.getExecutionTimeMs()));
                json.append("}");
                if (i < results.size() - 1) json.append(",");
            }
        }
        json.append("]}");
        return json.toString();
    }

    // --- Benchmark API Handler ---
    private class BenchmarkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = getParam(exchange, "q");
            String lang = getParam(exchange, "lang");
            if (query == null || query.isEmpty()) query = "Hyderabad";
            if (lang == null || lang.isEmpty()) lang = "All";

            CustomList<Document> docs = documentStore.getDocumentsByLanguage(lang);

            // 1. Naive
            int naiveMatches = 0, naiveComp = 0;
            long naiveTimeNs = 0;
            for (int i = 0; i < docs.size(); i++) {
                Document d = docs.get(i);
                NaiveSearch.SearchResultData r = NaiveSearch.search(d.getTitle() + " " + d.getContent(), query);
                naiveMatches += r.matchPositions.size();
                naiveComp += r.comparisonsCount;
                naiveTimeNs += r.executionTimeNs;
            }

            // 2. KMP
            int kmpMatches = 0, kmpComp = 0;
            long kmpTimeNs = 0;
            KMP.KMPSearchResult sampleKmp = null;
            for (int i = 0; i < docs.size(); i++) {
                Document d = docs.get(i);
                KMP.KMPSearchResult r = KMP.search(d.getTitle() + " " + d.getContent(), query);
                kmpMatches += r.matchPositions.size();
                kmpComp += r.comparisonsCount;
                kmpTimeNs += r.executionTimeNs;
                if (sampleKmp == null) sampleKmp = r;
            }

            // 3. Rabin-Karp
            int rkMatches = 0, rkComp = 0;
            long rkTimeNs = 0;
            for (int i = 0; i < docs.size(); i++) {
                Document d = docs.get(i);
                RabinKarp.RabinKarpResult r = RabinKarp.search(d.getTitle() + " " + d.getContent(), query);
                rkMatches += r.matchPositions.size();
                rkComp += r.comparisonsCount;
                rkTimeNs += r.executionTimeNs;
            }

            String lpsExplanation = sampleKmp != null ? sampleKmp.getLpsExplanation(query) : "";

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"query\":\"").append(escape(query)).append("\",");
            json.append("\"documentsCount\":").append(docs.size()).append(",");

            json.append("\"naiveMatches\":").append(naiveMatches).append(",");
            json.append("\"naiveTimeMs\":\"").append(String.format("%.3f", naiveTimeNs / 1_000_000.0)).append("\",");
            json.append("\"naiveTimeUs\":").append(naiveTimeNs / 1_000).append(",");
            json.append("\"naiveOps\":").append(naiveComp).append(",");

            json.append("\"kmpMatches\":").append(kmpMatches).append(",");
            json.append("\"kmpTimeMs\":\"").append(String.format("%.3f", kmpTimeNs / 1_000_000.0)).append("\",");
            json.append("\"kmpTimeUs\":").append(kmpTimeNs / 1_000).append(",");
            json.append("\"kmpOps\":").append(kmpComp).append(",");

            json.append("\"rkMatches\":").append(rkMatches).append(",");
            json.append("\"rkTimeMs\":\"").append(String.format("%.3f", rkTimeNs / 1_000_000.0)).append("\",");
            json.append("\"rkTimeUs\":").append(rkTimeNs / 1_000).append(",");
            json.append("\"rkOps\":").append(rkComp).append(",");

            json.append("\"lpsBreakdown\":\"").append(escape(lpsExplanation)).append("\"");
            json.append("}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    // --- Similarity API Handler ---
    private class SimilarityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int idA = 1, idB = 2;
            try {
                String aStr = getParam(exchange, "docA");
                if (aStr == null) aStr = getParam(exchange, "a");
                String bStr = getParam(exchange, "docB");
                if (bStr == null) bStr = getParam(exchange, "b");
                if (aStr != null) idA = Integer.parseInt(aStr);
                if (bStr != null) idB = Integer.parseInt(bStr);
            } catch (Exception ignored) {}

            Document docA = documentStore.getDocumentById(idA);
            Document docB = documentStore.getDocumentById(idB);
            if (docA == null || docB == null) {
                docA = documentStore.getAllDocuments().get(0);
                docB = documentStore.getAllDocuments().get(1);
            }

            DocumentSimilarity.SimilarityReport report = DocumentSimilarity.compareDocuments(docA, docB);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"score\":").append(String.format("%.2f", report.overallSimilarityPercentage / 100.0)).append(",");
            json.append("\"overallScore\":").append(String.format("%.1f", report.overallSimilarityPercentage)).append(",");
            json.append("\"diceScore\":").append(String.format("%.2f", report.jaccardSimilarityPercentage / 100.0)).append(",");
            json.append("\"lcsRatio\":").append(String.format("%.2f", report.lcsSimilarityPercentage / 100.0)).append(",");
            json.append("\"lcsLength\":").append(report.lcsLength).append(",");
            json.append("\"docATitle\":\"").append(escape(docA.getTitle())).append("\",");
            json.append("\"docAContent\":\"").append(escape(docA.getContent())).append("\",");
            json.append("\"docBTitle\":\"").append(escape(docB.getTitle())).append("\",");
            json.append("\"docBContent\":\"").append(escape(docB.getContent())).append("\",");

            json.append("\"commonTokens\":[");
            for (int i = 0; i < report.commonWords.size(); i++) {
                json.append("\"").append(escape(report.commonWords.get(i))).append("\"");
                if (i < report.commonWords.size() - 1) json.append(",");
            }
            json.append("]}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    // --- Suffix & LCP API Handler ---
    private class SuffixHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String text = getParam(exchange, "text");
            if (text == null || text.isEmpty()) text = "banana";
            if (text.length() > 200) text = text.substring(0, 200);

            SuffixArray sa = new SuffixArray(text);
            LCP lcp = new LCP(sa);

            int[] saArr = sa.getSuffixArray();
            int[] lcpArr = lcp.getLcpArray();

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"text\":\"").append(escape(text)).append("\",");
            json.append("\"longestRepeatedSubstring\":\"").append(escape(lcp.getLongestRepeatedSubstring())).append("\",");
            json.append("\"lrsLength\":").append(lcp.getMaxLcpLength()).append(",");
            json.append("\"rows\":[");
            int limit = Math.min(saArr.length, 60);
            for (int i = 0; i < limit; i++) {
                json.append("{");
                json.append("\"rank\":").append(i).append(",");
                json.append("\"suffixIndex\":").append(saArr[i]).append(",");
                json.append("\"lcp\":").append(lcpArr[i]).append(",");
                json.append("\"suffixPreview\":\"").append(escape(sa.getSuffixPreview(i, 35))).append("\"");
                json.append("}");
                if (i < limit - 1) json.append(",");
            }
            json.append("]}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    // --- Documents API Handler ---
    private class DocumentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            CustomList<Document> docs = documentStore.getAllDocuments();
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < docs.size(); i++) {
                Document d = docs.get(i);
                json.append("{");
                json.append("\"id\":").append(d.getId()).append(",");
                json.append("\"title\":\"").append(escape(d.getTitle())).append("\",");
                json.append("\"language\":\"").append(escape(d.getLanguage())).append("\",");
                json.append("\"category\":\"").append(escape(d.getCategory())).append("\",");
                json.append("\"content\":\"").append(escape(d.getContent())).append("\"");
                json.append("}");
                if (i < docs.size() - 1) json.append(",");
            }
            json.append("]");

            sendJsonResponse(exchange, json.toString());
        }
    }

    // --- Algorithms Info Handler ---
    private static class AlgorithmsInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "[" +
                "{\"name\":\"KMP (Knuth-Morris-Pratt)\",\"category\":\"String Matching\",\"whatItDoes\":\"Finds an exact pattern inside text efficiently without text-pointer backtracking.\",\"usedFor\":\"Exact document searching.\",\"timeComplexity\":\"O(n + m)\",\"spaceComplexity\":\"O(m)\"}," +
                "{\"name\":\"Rabin-Karp Rolling Hash\",\"category\":\"String Matching\",\"whatItDoes\":\"Computes rolling polynomial hashes to test potential substring matches in O(1) sliding time.\",\"usedFor\":\"Hash-based text filtering and verification.\",\"timeComplexity\":\"Average O(n + m), Worst O(n * m)\",\"spaceComplexity\":\"O(1)\"}," +
                "{\"name\":\"Trie (Prefix Tree)\",\"category\":\"Tree Data Structure\",\"whatItDoes\":\"Tree structure storing word characters along paths for sub-millisecond prefix navigation.\",\"usedFor\":\"Search autocomplete and prefix exploration.\",\"timeComplexity\":\"O(m) Lookup\",\"spaceComplexity\":\"O(m * alphabet)\"}," +
                "{\"name\":\"Aho-Corasick Automaton\",\"category\":\"Multi-Pattern Automaton\",\"whatItDoes\":\"Finite-state automaton with failure links locating multiple patterns in a single linear pass.\",\"usedFor\":\"Simultaneous multi-keyword search queries.\",\"timeComplexity\":\"O(n + z)\",\"spaceComplexity\":\"O(total pattern lengths)\"}," +
                "{\"name\":\"Levenshtein Edit Distance\",\"category\":\"Dynamic Programming\",\"whatItDoes\":\"Dynamic programming matrix computing minimum insertions, deletions, and substitutions.\",\"usedFor\":\"Fuzzy search and automatic 'Did you mean?' typo suggestions.\",\"timeComplexity\":\"O(n * m)\",\"spaceComplexity\":\"O(n * m)\"}," +
                "{\"name\":\"Suffix Array\",\"category\":\"String Analysis\",\"whatItDoes\":\"Lexicographically sorted array of all text suffixes allowing fast binary search matching.\",\"usedFor\":\"Substring retrieval and document pattern indexing.\",\"timeComplexity\":\"O(n log^2 n)\",\"spaceComplexity\":\"O(n)\"}," +
                "{\"name\":\"LCP (Longest Common Prefix)\",\"category\":\"String Analysis\",\"whatItDoes\":\"Computes longest common prefixes between consecutive sorted suffixes using Kasai's algorithm.\",\"usedFor\":\"Discovering longest repeated substrings and text overlap.\",\"timeComplexity\":\"O(n)\",\"spaceComplexity\":\"O(n)\"}" +
                "]";
            sendJsonResponse(exchange, json);
        }
    }

    // --- Max Flow API Handler ---
    private static class MaxFlowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int[][] capacity = new int[4][4];
            capacity[0][1] = 10; capacity[0][2] = 5;
            capacity[1][2] = 15; capacity[1][3] = 10;
            capacity[2][3] = 10;
            String[] names = {"Source (S)", "Vertex A", "Vertex B", "Sink (T)"};

            MaxFlow.MaxFlowResult res = MaxFlow.computeMaxFlow(capacity, 0, 3, names);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"maxFlow\":").append(res.maxFlow).append(",");
            json.append("\"log\":\"").append(escape(res.getDetailedReport())).append("\"");
            json.append("}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    // --- Vertex Cover API Handler ---
    private static class VertexCoverHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int n = 4;
            String[] names = {"A", "B", "C", "D"};
            CustomList<VertexCoverApproximation.Edge> edges = new CustomList<>();
            edges.add(new VertexCoverApproximation.Edge(0, 1));
            edges.add(new VertexCoverApproximation.Edge(1, 2));
            edges.add(new VertexCoverApproximation.Edge(2, 3));

            VertexCoverApproximation.VertexCoverResult res =
                VertexCoverApproximation.computeVertexCover(n, edges, names);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"size\":").append(res.vertexCover.size()).append(",");
            json.append("\"log\":\"").append(escape(res.getReport())).append("\"");
            json.append("}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    // --- Miller-Rabin API Handler ---
    private static class MillerRabinHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String numStr = getParam(exchange, "n");
            long num = 1000000007L;
            try {
                if (numStr != null) num = Long.parseLong(numStr);
            } catch (Exception ignored) {}

            MillerRabin.PrimalityResult res = MillerRabin.testPrimality(num, 5);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"number\":").append(res.number).append(",");
            json.append("\"isPrime\":").append(res.isPrime).append(",");
            json.append("\"log\":\"").append(escape(res.getReport())).append("\"");
            json.append("}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String getParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length >= 2 && kv[0].equalsIgnoreCase(key)) {
                try {
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\b') sb.append("\\b");
            else if (c == '\f') sb.append("\\f");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else sb.append(c);
        }
        return sb.toString();
    }
}
