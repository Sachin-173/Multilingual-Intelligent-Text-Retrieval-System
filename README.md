# MULTILINGUAL TEXT RETRIEVAL SYSTEM
### Standalone Intelligent Search Engine with Autonomous Algorithm Selection
#### Web Application • Pure Java Backend • 100% Offline • Zero External AI/NLP APIs

---

## 1. Product Overview

The **Multilingual Text Retrieval System** is a standalone, web-based search engine engineered for fast, accurate text discovery across multiple languages.

Unlike traditional search systems that require manual configuration or external search engines, the system automatically analyzes user search queries and dynamically routes them to the optimal pattern matching algorithm.

* **Autonomous Query Routing**: Automatically chooses between exact pattern matching, fuzzy edit distance, prefix trees, and multi-pattern automata based on query characteristics.
* **Native Multilingual Support**: Direct UTF-8 Unicode processing for **English**, **తెలుగు (Telugu)**, **हिन्दी (Hindi)**, and **தமிழ் (Tamil)** without external translation services or converters.
* **Pure Standalone Architecture**: 100% offline with zero dependencies on external search engines (no Elasticsearch, no Apache Lucene) and zero cloud AI/NLP APIs.
* **Manually Implemented Core Algorithms**: Every retrieval algorithm and data structure is custom-built in pure Java.

---

## 2. Core Feature: Automatic Algorithm Selector

The user enters any query into the search box, and the system automatically determines the appropriate algorithm without requiring manual selection:

```
                  USER QUERY
                      │
                      ▼
               QUERY ANALYSIS
                      │
                      ▼
         AUTOMATIC ALGORITHM SELECTION
                      │
         ┌────────────┼────────────┬────────────┐
         │            │            │            │
         ▼            ▼            ▼            ▼
     Exact Word   Typo / Misspell Short Prefix  Multiple Words
         │            │            │            │
         ▼            ▼            ▼            ▼
     KMP Search   Levenshtein DP   Trie Tree    Aho-Corasick
     O(n + m)      Matrix O(nm)    O(m) Lookup  O(n + z)
                      │
                      ▼
                "Did You Mean?"
                      │
                      ▼
                 KMP on Target
         └────────────┼────────────┴────────────┘
                      │
                      ▼
               DOCUMENT RETRIEVAL
             (32 Multilingual Docs)
                      │
                      ▼
              RELEVANCE SCORING
                      │
                      ▼
             MERGESORT RANKING
              O(N log N) Stable
                      │
                      ▼
             SEARCH RESULT CARDS
           (with Highlighted Snippets)
```

### Automatic Selection Rules

| Query Example | Requirement Detected | Selected Algorithm | Search Algorithm | Reason |
| :--- | :--- | :--- | :--- | :--- |
| **`Hyderabad`** | Exact keyword search | **KMP (Knuth-Morris-Pratt)** | KMP | Exact word found in document vocabulary. KMP searches in linear $O(n + m)$ time without backtracking. |
| **`Hyderbad`** | Possible spelling variation | **Levenshtein Edit Distance (DP)** | KMP | Exact match not found; edit distance detects typo $\to$ suggests `Hyderabad` and searches. |
| **`hyd`** | Prefix search | **Trie (Prefix Tree)** | Trie | Query is short prefix ($\le 3$ chars); Trie explores character paths in $O(m)$ time for instant autocomplete. |
| **`India Telangana Hyderabad`** | Multiple keywords | **Aho-Corasick Automaton** | Aho-Corasick | Query contains multiple keywords; finite state machine matches all patterns in a single pass. |
| **`తెలంగాణ`** *(Telugu)* | Exact keyword search | **KMP (Knuth-Morris-Pratt)** | KMP | Indic Unicode Telugu script; direct UTF-16 character code point matching without external NLP. |
| **`भारत`** *(Hindi)* | Exact keyword search | **KMP (Knuth-Morris-Pratt)** | KMP | Indic Devanagari Hindi script matching. |
| **`இந்தியா`** *(Tamil)* | Exact keyword search | **KMP (Knuth-Morris-Pratt)** | KMP | Indic Tamil script matching. |

---

## 3. How to Run

### Method 1: In VS Code (One-Click Build)
1. Open the project folder in **VS Code**.
2. Press **`Ctrl + Shift + B`** on your keyboard (or click **Terminal** $\to$ **Run Build Task...**).
3. The web application will compile and launch automatically.
4. Your default browser will open to **`http://localhost:8080`**.

### Method 2: From the Terminal
In PowerShell or Command Prompt:
```powershell
.\run.bat
```
Or directly using Java:
```powershell
java "-Dfile.encoding=UTF-8" -cp bin Main
```
Then open your web browser and navigate to:
```
http://localhost:8080
```

### Method 3: Automated Algorithm Verification Suite
To run the automated verification suite covering all 42 tests:
```powershell
.\test.bat
```

---

## 4. Website Structure

The website consists of 5 clean, focused sections:

1. **Home (`#home`)**:
   - Clean hero section with title and subtitle: *"Search and discover information across multiple languages."*
   - Large central search bar with Search button.
   - Supported languages row: English | Telugu | Hindi | Tamil.
   - Four feature cards: Exact Search, Fuzzy Search, Prefix Search, Multilingual Search.

2. **Search (`#search`)**:
   - The primary search engine interface.
   - Large search box with clear and search buttons.
   - Quick query test chips (`Hyderabad`, `Hyderbad`, `hyd`, `India Telangana Hyderabad`, `తెలంగాణ`, `भारत`, `இந்தியா`).
   - **Search Analysis Card**:
     - Query analyzed
     - Detected requirement
     - Selected algorithm
     - Suggested typo correction (clickable button to search immediately)
     - Search algorithm
     - Expandable *"How was this found?"* explanation.
   - Prefix autocomplete suggestion pills when prefix queries are entered.
   - Search result cards showing Title, Category, Language tag, Relevance rank (High / Medium), highlighted text snippets, and "Read Document" reader button.

3. **Algorithms (`#algorithms`)**:
   - Beginner-friendly cards explaining the core retrieval algorithms:
     - **KMP (Knuth-Morris-Pratt)**: $O(n + m)$ linear pattern matching with prefix function.
     - **Rabin-Karp Rolling Hash**: Polynomial hash matching with $O(1)$ sliding updates.
     - **Trie (Prefix Tree)**: $O(m)$ character branch navigation for real-time autocomplete.
     - **Aho-Corasick Automaton**: $O(n + z)$ multi-pattern state machine with failure links.
     - **Levenshtein Edit Distance**: Dynamic programming matrix for spelling corrections.
     - **Suffix Array**: $O(n \log^2 n)$ sorted suffixes for fast substring indexing.
     - **LCP (Longest Common Prefix)**: $O(n)$ Kasai algorithm measuring shared prefix lengths.

4. **Documents (`#documents`)**:
   - Repository of 32 curated sample documents.
   - Categorized by Cities & Culture, Travel & Transport, Science & Space, Technology, Education, and Aviation.
   - Filter by language: All (32), English (8), Telugu (8), Hindi (8), Tamil (8).
   - Real-time text search filter.
   - Click "Read Document" to open the document modal reader.

5. **About (`#about`)**:
   - Product overview describing autonomous algorithm routing, native Indic script support, and standalone retrieval architecture.
   - 6-step retrieval workflow flowchart.

---

## 5. Technology Stack

* **Frontend**: HTML5, Modern Responsive CSS3, Vanilla ES6 JavaScript (Single Page Application).
* **Backend**: Java SE 17+ using the built-in HTTP server (`com.sun.net.httpserver.HttpServer`).
* **Storage**: In-memory document repository indexed from local UTF-8 data (`data/documents.txt`).
* **Port**: Runs locally on `http://localhost:8080`.
