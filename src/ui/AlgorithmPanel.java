package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Screen providing comprehensive DSA algorithm information, complexity tables,
 * and viva examination Q&A cards for 2nd-year college evaluations.
 */
public class AlgorithmPanel extends JPanel {

    private JComboBox<String> algorithmSelector;
    private JTextArea detailsArea;

    private static final String[] ALGORITHM_LIST = {
        "1. Knuth-Morris-Pratt (KMP)",
        "2. Rabin-Karp (Rolling Hash)",
        "3. Naive String Matching",
        "4. Trie (Prefix Tree)",
        "5. Aho-Corasick Multi-Pattern Automaton",
        "6. Levenshtein Edit Distance (DP)",
        "7. Merge Sort (Divide & Conquer)",
        "8. Suffix Array & Kasai's LCP",
        "9. Edmonds-Karp Maximum Flow",
        "10. Vertex Cover 2-Approximation",
        "11. Miller-Rabin Randomized Primality Test"
    };

    public AlgorithmPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        initUI();
        displayAlgorithmInfo(0);
    }

    private void initUI() {
        // --- Top Selection Card ---
        JPanel topCard = new JPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));
        topCard.setBackground(UITheme.CARD_BG);
        topCard.setBorder(UITheme.createCardBorder("Choose DSA Algorithm for Explanation & Viva Revision"));

        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        r1.setBackground(UITheme.CARD_BG);
        JLabel selectLbl = new JLabel("Select Algorithm:");
        selectLbl.setFont(UITheme.FONT_BOLD);
        r1.add(selectLbl);

        algorithmSelector = new JComboBox<>(ALGORITHM_LIST);
        algorithmSelector.setFont(UITheme.FONT_BOLD);
        algorithmSelector.setPreferredSize(new Dimension(340, 30));
        algorithmSelector.addActionListener(e -> displayAlgorithmInfo(algorithmSelector.getSelectedIndex()));
        r1.add(algorithmSelector);

        topCard.add(r1);

        // Row 2: Quick algorithm chips
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        r2.setBackground(UITheme.CARD_BG);
        JLabel quickLbl = new JLabel("Quick Jump:");
        quickLbl.setFont(UITheme.FONT_SMALL);
        quickLbl.setForeground(UITheme.TEXT_MUTED);
        r2.add(quickLbl);

        r2.add(createAlgoChip("KMP", 0));
        r2.add(createAlgoChip("Rabin-Karp", 1));
        r2.add(createAlgoChip("Naive", 2));
        r2.add(createAlgoChip("Trie", 3));
        r2.add(createAlgoChip("Aho-Corasick", 4));
        r2.add(createAlgoChip("Edit Distance", 5));
        r2.add(createAlgoChip("Merge Sort", 6));
        r2.add(createAlgoChip("Suffix Array & LCP", 7));
        r2.add(createAlgoChip("Max Flow", 8));
        r2.add(createAlgoChip("Vertex Cover", 9));
        r2.add(createAlgoChip("Miller-Rabin", 10));

        topCard.add(r2);
        add(topCard, BorderLayout.NORTH);

        // --- Center Details Area ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UITheme.CARD_BG);
        centerPanel.setBorder(UITheme.createCardBorder("Algorithm Specification, Complexity Analysis & Viva Q&A"));

        detailsArea = new JTextArea();
        detailsArea.setFont(UITheme.FONT_MONO);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setEditable(false);
        detailsArea.setBackground(new Color(250, 250, 252));

        JScrollPane scroll = new JScrollPane(detailsArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createAlgoChip(String label, int index) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_SMALL);
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(UITheme.PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            algorithmSelector.setSelectedIndex(index);
        });
        return btn;
    }

    private void displayAlgorithmInfo(int index) {
        StringBuilder sb = new StringBuilder();

        switch (index) {
            case 0: // KMP
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: KNUTH-MORRIS-PRATT (KMP) STRING MATCHING\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Searches for all occurrences of a pattern of length 'm' inside a text of length 'n'\n");
                sb.append("   in strictly linear deterministic time without ever rewinding the text pointer.\n\n");
                sb.append("2. BASIC IDEA & CORE CONCEPT:\n");
                sb.append("   - When a mismatch occurs, instead of shifting the pattern by 1 and re-evaluating\n");
                sb.append("     characters already matched (like Naive search), KMP utilizes information from\n");
                sb.append("     the pattern itself via the Longest Prefix Suffix (LPS) array.\n");
                sb.append("   - LPS[i] stores the length of the longest proper prefix of pattern[0..i] that is also\n");
                sb.append("     a suffix of pattern[0..i]. On mismatch at index j, new j = LPS[j - 1].\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Preprocessing Time:  O(m) to build the LPS array.\n");
                sb.append("   - Searching Time:       O(n) - text pointer 'i' moves only forward.\n");
                sb.append("   - Total Time:           O(n + m) guaranteed in all cases (Best, Average, Worst).\n");
                sb.append("   - Space Complexity:     O(m) auxiliary space for the LPS array.\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Primary engine for Exact Search in English, Telugu, Hindi, and Tamil documents.\n\n");
                sb.append("5. COLLEGE VIVA QUESTIONS & ANSWERS:\n");
                sb.append("   Q: Why does the text index 'i' never decrease in KMP?\n");
                sb.append("   A: Because any valid shift that could lead to a match is guaranteed to preserve the\n");
                sb.append("      longest prefix-suffix boundary already computed in the LPS table.\n");
                sb.append("   Q: What is the worst-case time complexity of KMP compared to Naive?\n");
                sb.append("   A: KMP is strictly O(n + m), whereas Naive degrades to O(n * m) on repetitive patterns.\n");
                break;

            case 1: // Rabin-Karp
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: RABIN-KARP STRING MATCHING (POLYNOMIAL ROLLING HASH)\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Matches patterns in text using numerical hash values. Readily generalizable to multi-pattern search.\n\n");
                sb.append("2. BASIC IDEA & CORE CONCEPT:\n");
                sb.append("   - Treats character strings as numbers in base 'd' (radix) modulo a large prime 'q'.\n");
                sb.append("   - Computes Hash(pattern) and Hash(text[0..m-1]) in O(m) time.\n");
                sb.append("   - Rolling Hash: As the window shifts, computes the new hash in O(1) time by subtracting the\n");
                sb.append("     outgoing character and adding the incoming character.\n");
                sb.append("   - Spurious Hit (Collision): If Hash(window) == Hash(pattern), performs character check.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Preprocessing Time:  O(m)\n");
                sb.append("   - Average Search Time: O(n + m)\n");
                sb.append("   - Worst-case Time:     O(n * m) (if hash function produces collisions at every shift)\n");
                sb.append("   - Space Complexity:     O(1) auxiliary space.\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Exact search engine alternative and Algorithm Comparison benchmark.\n\n");
                sb.append("5. COLLEGE VIVA QUESTIONS & ANSWERS:\n");
                sb.append("   Q: What is a spurious hit in Rabin-Karp?\n");
                sb.append("   A: When two different strings produce the same hash value modulo q (a hash collision).\n");
                sb.append("   Q: Why do we choose a large prime modulus 'q'?\n");
                sb.append("   A: A large prime uniformly distributes hash values and drastically minimizes collision probability.\n");
                break;

            case 2: // Naive Search
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: NAIVE (BRUTE-FORCE) PATTERN MATCHING\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Serves as the foundational baseline algorithm for pattern matching.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Slides the pattern over the text position by position: i from 0 to n - m.\n");
                sb.append("   - Compares characters until a mismatch is found or all 'm' characters match.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Best Case:  O(n)\n");
                sb.append("   - Worst Case: O(n * m) (e.g. text 'AAAAAAAA', pattern 'AAAB')\n");
                sb.append("   - Space:      O(1)\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Serves as the comparison benchmark in the Algorithm Comparison screen.\n");
                break;

            case 3: // Trie
                sb.append("========================================================================================\n");
                sb.append("DATA STRUCTURE: TRIE (PREFIX TREE)\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Retrieval tree optimized for prefix queries, word lookups, and dictionary autocomplete.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Tree where each node represents a character. Root represents the empty string.\n");
                sb.append("   - All descendants of a node share a common string prefix.\n");
                sb.append("   - Implemented manually using dynamic child arrays without java.util.Map.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Insertion: O(m) where m is word length.\n");
                sb.append("   - Lookup:    O(m) - completely independent of the total number of words in dataset!\n");
                sb.append("   - Space:     O(Total characters in all words * branch fanout).\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Prefix Search, real-time autocomplete suggestions (e.g. 'hyd' -> 'hyderabad').\n");
                break;

            case 4: // Aho-Corasick
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: AHO-CORASICK MULTI-PATTERN SEARCH AUTOMATON\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Locates all occurrences of multiple search patterns simultaneously in a single linear pass.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Phase 1: Builds a Trie containing all search patterns.\n");
                sb.append("   - Phase 2: Uses Breadth-First Search (BFS) to compute failure links (analogous to KMP LPS).\n");
                sb.append("   - Phase 3: Traverses document text in O(n) time. Emits all matching patterns at each state.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Construction: O(sum of pattern lengths)\n");
                sb.append("   - Search:       O(n + z) where z is number of pattern occurrences\n");
                sb.append("   - Space:        O(sum of pattern lengths)\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Multi-Pattern Search mode (e.g. searching 'India', 'Telangana', 'Hyderabad' simultaneously).\n");
                break;

            case 5: // Edit Distance
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: LEVENSHTEIN EDIT DISTANCE (DYNAMIC PROGRAMMING)\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Measures minimum single-character edit operations (insert, delete, replace) between strings.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Recurrence: dp[i][j] = dp[i-1][j-1] if char match;\n");
                sb.append("     else 1 + min(dp[i-1][j] (del), dp[i][j-1] (ins), dp[i-1][j-1] (rep)).\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Time:  O(n * m)\n");
                sb.append("   - Space: O(n * m) for full DP table\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Fuzzy search and spelling correction ('Did you mean: Hyderabad' for 'Hyderbad').\n");
                break;

            case 6: // Merge Sort
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: MERGE SORT (DIVIDE AND CONQUER)\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Stable, comparison-based sorting algorithm guaranteeing O(n log n) worst-case time.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Divide array into two halves at midpoint.\n");
                sb.append("   - Recursively sort both halves.\n");
                sb.append("   - Merge the two sorted halves into a temporary buffer in linear time.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Best, Average, Worst Case: O(n log n)\n");
                sb.append("   - Space: O(n) auxiliary memory for merge buffer\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Sorting search results descending by score in Ranker.java.\n");
                sb.append("   - Sorting suffixes in SuffixArray.java.\n");
                break;

            case 7: // Suffix Array & Kasai LCP
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: SUFFIX ARRAY & KASAI'S LCP ALGORITHM\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Represents all suffixes of a text in lexicographical order for rapid substring queries.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Suffix Array: Stores starting index of sorted suffixes.\n");
                sb.append("   - Kasai's Algorithm: Computes LCP in linear O(n) time using the property that\n");
                sb.append("     LCP(i+1) >= LCP(i) - 1.\n");
                sb.append("   - Longest Repeated Substring (LRS): Discovered at max(LCP[i]).\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - LCP Construction: O(n) linear time.\n");
                sb.append("   - Substring Search: O(m * log n) via binary search.\n");
                sb.append("   - Space: O(n)\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Suffix & LCP Analysis panel.\n");
                break;

            case 8: // Max Flow
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: EDMONDS-KARP MAXIMUM FLOW (BFS FORD-FULKERSON)\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Finds maximum possible flow from source to sink in a directed capacity network.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Uses Breadth-First Search (BFS) to find the shortest augmenting path (fewest edges).\n");
                sb.append("   - Finds bottleneck capacity along path, pushes flow, and updates residual edges.\n");
                sb.append("   - Terminates when no s-t path exists in residual network.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Time: O(V * E^2)\n");
                sb.append("   - Space: O(V^2)\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Advanced Algorithm Lab.\n");
                break;

            case 9: // Vertex Cover
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: VERTEX COVER 2-APPROXIMATION (MAXIMAL MATCHING)\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Finds a near-optimal vertex cover for NP-Hard Minimum Vertex Cover in polynomial time.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Greedily pick an uncovered edge (u, v).\n");
                sb.append("   - Add BOTH u and v to the vertex cover.\n");
                sb.append("   - Remove all edges incident to u or v.\n");
                sb.append("   - Guarantee: |C| <= 2 * |OPT|.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Time: O(V + E)\n");
                sb.append("   - Space: O(V + E)\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Advanced Algorithm Lab.\n");
                break;

            case 10: // Miller-Rabin
                sb.append("========================================================================================\n");
                sb.append("ALGORITHM: MILLER-RABIN RANDOMIZED PRIMALITY TEST\n");
                sb.append("========================================================================================\n\n");
                sb.append("1. PURPOSE:\n");
                sb.append("   Probabilistic primality test running in polynomial time for large integers.\n\n");
                sb.append("2. BASIC IDEA:\n");
                sb.append("   - Decompose n - 1 = (2^s) * d.\n");
                sb.append("   - Test base 'a': compute x = a^d mod n.\n");
                sb.append("   - If x != 1 and x != n - 1, square x up to s-1 times.\n");
                sb.append("   - If x never reaches n - 1, 'a' is a witness that n is composite.\n");
                sb.append("   - Error rate after k independent rounds: <= (1/4)^k.\n\n");
                sb.append("3. COMPLEXITY:\n");
                sb.append("   - Time: O(k * log^3 n)\n");
                sb.append("   - Space: O(1)\n\n");
                sb.append("4. WHERE USED IN THIS PROJECT:\n");
                sb.append("   - Advanced Algorithm Lab.\n");
                break;
        }

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }
}
