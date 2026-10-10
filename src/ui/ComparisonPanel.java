package ui;

import data.DocumentStore;
import datastructures.CustomList;
import model.Document;
import search.KMP;
import search.NaiveSearch;
import search.RabinKarp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Screen comparing performance of string matching algorithms:
 * 1. Naive Brute-Force Search
 * 2. Knuth-Morris-Pratt (KMP)
 * 3. Rabin-Karp (Polynomial Rolling Hash)
 */
public class ComparisonPanel extends JPanel {

    private final DocumentStore documentStore;

    private JTextField queryField;
    private JComboBox<String> languageCombo;
    private JButton compareButton;
    private JTable comparisonTable;
    private DefaultTableModel tableModel;
    private JTextArea explanationArea;

    // Benchmark scorecard cards
    private JLabel naiveCardLabel;
    private JLabel kmpCardLabel;
    private JLabel rkCardLabel;

    public ComparisonPanel(DocumentStore documentStore) {
        this.documentStore = documentStore;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        initUI();
    }

    private void initUI() {
        // --- Top Input Card ---
        JPanel topCard = new JPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));
        topCard.setBackground(UITheme.CARD_BG);
        topCard.setBorder(UITheme.createCardBorder("Algorithm Benchmark Parameters"));

        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        r1.setBackground(UITheme.CARD_BG);

        JLabel queryLabel = new JLabel("Test Pattern / Query:");
        queryLabel.setFont(UITheme.FONT_BOLD);
        r1.add(queryLabel);

        queryField = new JTextField("Hyderabad", 18);
        queryField.setFont(UITheme.FONT_REGULAR);
        queryField.setPreferredSize(new Dimension(200, 32));
        queryField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        queryField.addActionListener(e -> runBenchmark());
        r1.add(queryField);

        JLabel langLabel = new JLabel("Language:");
        langLabel.setFont(UITheme.FONT_BOLD);
        r1.add(langLabel);

        languageCombo = new JComboBox<>(new String[]{"All", "English", "Telugu", "Hindi", "Tamil"});
        languageCombo.setFont(UITheme.FONT_REGULAR);
        languageCombo.setPreferredSize(new Dimension(110, 30));
        r1.add(languageCombo);

        compareButton = UITheme.createPrimaryButton("COMPARE ALGORITHMS");
        compareButton.setPreferredSize(new Dimension(200, 32));
        compareButton.addActionListener(e -> runBenchmark());
        r1.add(compareButton);

        topCard.add(r1);

        // Row 2: Quick query chips
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        r2.setBackground(UITheme.CARD_BG);
        JLabel sampleLabel = new JLabel("Quick Benchmarks:");
        sampleLabel.setFont(UITheme.FONT_SMALL);
        sampleLabel.setForeground(UITheme.TEXT_MUTED);
        r2.add(sampleLabel);

        r2.add(createBenchmarkChip("Hyderabad", "All"));
        r2.add(createBenchmarkChip("Telangana", "All"));
        r2.add(createBenchmarkChip("India", "All"));
        r2.add(createBenchmarkChip("తెలంగాణ", "Telugu"));
        r2.add(createBenchmarkChip("भारत", "Hindi"));
        r2.add(createBenchmarkChip("இந்தியா", "Tamil"));

        topCard.add(r2);
        topCard.add(Box.createVerticalStrut(8));

        // Row 3: 3-column scorecard banner
        JPanel scorecardPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        scorecardPanel.setBackground(UITheme.CARD_BG);

        scorecardPanel.add(createAlgorithmCard("1. Naive Search", "O(n * m)", new Color(241, 245, 249), UITheme.TEXT_DARK, 0));
        scorecardPanel.add(createAlgorithmCard("2. KMP Algorithm", "O(n + m)", new Color(238, 242, 255), UITheme.PRIMARY, 1));
        scorecardPanel.add(createAlgorithmCard("3. Rabin-Karp", "Avg O(n + m)", new Color(236, 253, 245), UITheme.ACCENT, 2));

        topCard.add(scorecardPanel);
        add(topCard, BorderLayout.NORTH);

        // --- Center Metrics Table & Analysis ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(UITheme.BACKGROUND);

        String[] columns = {
            "Algorithm", "Total Matches", "Time (ms)", "Time (µs)", "Comparisons / Operations", "Time Complexity", "Space Complexity"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        comparisonTable = new JTable(tableModel);
        UITheme.styleTable(comparisonTable);
        comparisonTable.setRowHeight(32);

        JScrollPane tableScroll = new JScrollPane(comparisonTable);
        tableScroll.setPreferredSize(new Dimension(100, 135));
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        centerPanel.add(tableScroll, BorderLayout.NORTH);

        // Viva Explanation Area
        JPanel explanationPanel = new JPanel(new BorderLayout());
        explanationPanel.setBackground(UITheme.CARD_BG);
        explanationPanel.setBorder(UITheme.createCardBorder("Viva Examination Analysis & Preprocessing Breakdown"));

        explanationArea = new JTextArea();
        explanationArea.setFont(UITheme.FONT_MONO);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setEditable(false);
        explanationArea.setBackground(new Color(250, 250, 252));
        explanationArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        explanationArea.setText("Click 'COMPARE ALGORITHMS' to benchmark Naive, KMP, and Rabin-Karp on the current dataset.\n\n" +
                "Key College Viva Note:\n" +
                "- Real execution time measured via System.nanoTime() depends on CPU cache, string length, and repetitive patterns.\n" +
                "- KMP is mathematically guaranteed O(n + m) time because it never backtracks the text pointer.\n" +
                "- Rabin-Karp achieves average O(n + m) time using rolling hash, with character checks only on hash collision.");

        JScrollPane explanationScroll = new JScrollPane(explanationArea);
        explanationScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        explanationPanel.add(explanationScroll, BorderLayout.CENTER);

        centerPanel.add(explanationPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createAlgorithmCard(String title, String complexity, Color bg, Color accent, int cardIndex) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BOLD);
        titleLbl.setForeground(accent);
        card.add(titleLbl, BorderLayout.NORTH);

        JLabel compLbl = new JLabel("Complexity: " + complexity);
        compLbl.setFont(UITheme.FONT_SMALL);
        compLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel statLbl = new JLabel("Pending benchmark run");
        statLbl.setFont(UITheme.FONT_REGULAR);
        statLbl.setForeground(UITheme.TEXT_DARK);

        if (cardIndex == 0) naiveCardLabel = statLbl;
        else if (cardIndex == 1) kmpCardLabel = statLbl;
        else if (cardIndex == 2) rkCardLabel = statLbl;

        JPanel body = new JPanel(new GridLayout(2, 1));
        body.setOpaque(false);
        body.add(statLbl);
        body.add(compLbl);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JButton createBenchmarkChip(String query, String lang) {
        JButton btn = new JButton(query);
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
            queryField.setText(query);
            languageCombo.setSelectedItem(lang);
            runBenchmark();
        });
        return btn;
    }

    public void runBenchmark() {
        String pattern = queryField.getText().trim();
        if (pattern.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a test query.", "Empty Pattern", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String lang = (String) languageCombo.getSelectedItem();
        CustomList<Document> docs = documentStore.getDocumentsByLanguage(lang);

        tableModel.setRowCount(0);

        // Warmup JIT compiler
        for (int i = 0; i < Math.min(3, docs.size()); i++) {
            Document d = docs.get(i);
            NaiveSearch.search(d.getContent(), pattern);
            KMP.search(d.getContent(), pattern);
            RabinKarp.search(d.getContent(), pattern);
        }

        // 1. Benchmark Naive Search
        int naiveMatches = 0;
        int naiveComparisons = 0;
        long naiveTimeNs = 0;
        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            NaiveSearch.SearchResultData r = NaiveSearch.search(d.getTitle() + " " + d.getContent(), pattern);
            naiveMatches += r.matchPositions.size();
            naiveComparisons += r.comparisonsCount;
            naiveTimeNs += r.executionTimeNs;
        }

        // 2. Benchmark KMP
        int kmpMatches = 0;
        int kmpComparisons = 0;
        long kmpTimeNs = 0;
        KMP.KMPSearchResult sampleKmpRes = null;
        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            KMP.KMPSearchResult r = KMP.search(d.getTitle() + " " + d.getContent(), pattern);
            kmpMatches += r.matchPositions.size();
            kmpComparisons += r.comparisonsCount;
            kmpTimeNs += r.executionTimeNs;
            if (sampleKmpRes == null) sampleKmpRes = r;
        }

        // 3. Benchmark Rabin-Karp
        int rkMatches = 0;
        int rkComparisons = 0;
        int rkCollisions = 0;
        long rkTimeNs = 0;
        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            RabinKarp.RabinKarpResult r = RabinKarp.search(d.getTitle() + " " + d.getContent(), pattern);
            rkMatches += r.matchPositions.size();
            rkComparisons += r.comparisonsCount;
            rkCollisions += r.collisionCount;
            rkTimeNs += r.executionTimeNs;
        }

        // Update Scorecard Cards
        naiveCardLabel.setText(String.format("Matches: %d | Time: %.3f ms", naiveMatches, naiveTimeNs / 1_000_000.0));
        kmpCardLabel.setText(String.format("Matches: %d | Time: %.3f ms", kmpMatches, kmpTimeNs / 1_000_000.0));
        rkCardLabel.setText(String.format("Matches: %d | Time: %.3f ms", rkMatches, rkTimeNs / 1_000_000.0));

        // Add to Table
        tableModel.addRow(new Object[]{
            "Naive Search", naiveMatches,
            String.format("%.3f", naiveTimeNs / 1_000_000.0),
            String.format("%d", naiveTimeNs / 1_000),
            naiveComparisons + " comparisons",
            "Worst: O(n * m)", "O(1)"
        });

        tableModel.addRow(new Object[]{
            "Knuth-Morris-Pratt (KMP)", kmpMatches,
            String.format("%.3f", kmpTimeNs / 1_000_000.0),
            String.format("%d", kmpTimeNs / 1_000),
            kmpComparisons + " comparisons",
            "Linear: O(n + m)", "O(m) LPS Array"
        });

        tableModel.addRow(new Object[]{
            "Rabin-Karp", rkMatches,
            String.format("%.3f", rkTimeNs / 1_000_000.0),
            String.format("%d", rkTimeNs / 1_000),
            rkComparisons + " char checks (" + rkCollisions + " collisions)",
            "Avg: O(n + m), Worst: O(n*m)", "O(1)"
        });

        // Detailed Viva Explanation
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("  ALGORITHM BENCHMARK REPORT: Query = '").append(pattern).append("' across ")
          .append(docs.size()).append(" documents (").append(lang).append(")\n");
        sb.append("========================================================================================\n\n");

        sb.append("1. Knuth-Morris-Pratt (KMP) LPS Preprocessing Details:\n");
        if (sampleKmpRes != null) {
            sb.append(sampleKmpRes.getLpsExplanation(pattern)).append("\n\n");
        }

        sb.append("2. Rabin-Karp Hash Function & Collision Analysis:\n");
        sb.append("   - Hash Equation: Hash(W) = sum(char[i] * ").append(RabinKarp.RADIX)
          .append("^(m-1-i)) mod ").append(RabinKarp.MODULUS).append("\n");
        sb.append("   - Total Hash Collisions (False Positives): ").append(rkCollisions).append("\n");
        sb.append("   - Why Modulo Arithmetic? Prevents integer overflow while bounding hash within [0..q-1].\n\n");

        sb.append("3. Academic Viva Explanation & Analysis:\n");
        sb.append("   - Why isn't KMP always faster on small texts? KMP requires O(m) preprocessing to construct\n");
        sb.append("     the LPS array. For very short queries or random text with no repetitive characters,\n");
        sb.append("     Naive search may have negligible overhead.\n");
        sb.append("   - When does KMP excel? Repetitive texts (e.g. 'AAAA...AB'), DNA sequences, and formal grammars\n");
        sb.append("     where Naive search degrades into its worst-case O(n * m).\n");
        sb.append("   - Rabin-Karp advantage: Excels when searching multiple patterns of equal length because\n");
        sb.append("     rolling hash can be compared against a hash set in O(1).\n");
        sb.append("   - Note: Real hardware timing varies with CPU scheduling, thread state, and memory caching.");

        explanationArea.setText(sb.toString());
        explanationArea.setCaretPosition(0);
    }
}
