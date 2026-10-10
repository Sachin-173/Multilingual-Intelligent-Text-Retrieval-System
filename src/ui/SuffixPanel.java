package ui;

import data.DocumentStore;
import datastructures.CustomList;
import datastructures.IntList;
import model.Document;
import suffix.LCP;
import suffix.SuffixArray;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Screen demonstrating Suffix Array construction, Kasai's LCP algorithm,
 * Longest Repeated Substring (LRS) discovery, and Suffix Binary Search.
 */
public class SuffixPanel extends JPanel {

    private final DocumentStore documentStore;

    private JComboBox<String> docSelectorCombo;
    private JTextField customTextField;
    private JButton analyzeButton;
    private JTable saTable;
    private DefaultTableModel tableModel;
    private JLabel lrsLabel;
    private JTextArea vivaInfoArea;

    // Pattern search within Suffix Array
    private JTextField patternSearchField;
    private JButton patternSearchButton;
    private JLabel patternSearchResultLabel;

    private SuffixArray currentSA;
    private LCP currentLCP;

    public SuffixPanel(DocumentStore documentStore) {
        this.documentStore = documentStore;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        initUI();
    }

    private void initUI() {
        // --- Top Input Configuration Card ---
        JPanel topCard = new JPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));
        topCard.setBackground(UITheme.CARD_BG);
        topCard.setBorder(UITheme.createCardBorder("Suffix Structure Source & Configuration"));

        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        r1.setBackground(UITheme.CARD_BG);

        JLabel docLbl = new JLabel("Select Document:");
        docLbl.setFont(UITheme.FONT_BOLD);
        r1.add(docLbl);

        docSelectorCombo = new JComboBox<>();
        docSelectorCombo.setFont(UITheme.FONT_REGULAR);
        docSelectorCombo.setPreferredSize(new Dimension(320, 30));
        docSelectorCombo.addActionListener(e -> onDocSelected());
        r1.add(docSelectorCombo);

        JLabel txtLbl = new JLabel("Or Custom Text:");
        txtLbl.setFont(UITheme.FONT_BOLD);
        r1.add(txtLbl);

        customTextField = new JTextField("banana", 16);
        customTextField.setFont(UITheme.FONT_REGULAR);
        customTextField.setPreferredSize(new Dimension(160, 30));
        customTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        r1.add(customTextField);

        analyzeButton = UITheme.createPrimaryButton("ANALYZE SUFFIXES & LCP");
        analyzeButton.setPreferredSize(new Dimension(210, 32));
        analyzeButton.addActionListener(e -> runAnalysis());
        r1.add(analyzeButton);

        topCard.add(r1);

        // Row 2: Quick test strings & Binary Search
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        r2.setBackground(UITheme.CARD_BG);

        JLabel sampleLabel = new JLabel("Quick Samples:");
        sampleLabel.setFont(UITheme.FONT_SMALL);
        sampleLabel.setForeground(UITheme.TEXT_MUTED);
        r2.add(sampleLabel);

        r2.add(createSampleChip("banana", "ana"));
        r2.add(createSampleChip("mississippi", "ssi"));
        r2.add(createSampleChip("hyderabad", "hyd"));
        r2.add(createSampleChip("తెలంగాణ", "తెల"));

        r2.add(Box.createHorizontalStrut(20));

        JLabel searchLbl = new JLabel("Binary Search SA:");
        searchLbl.setFont(UITheme.FONT_BOLD);
        r2.add(searchLbl);

        patternSearchField = new JTextField("ana", 10);
        patternSearchField.setFont(UITheme.FONT_REGULAR);
        patternSearchField.setPreferredSize(new Dimension(100, 28));
        patternSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        r2.add(patternSearchField);

        patternSearchButton = UITheme.createSecondaryButton("Search Pattern");
        patternSearchButton.addActionListener(e -> searchInSuffixArray());
        r2.add(patternSearchButton);

        patternSearchResultLabel = new JLabel("Enter pattern to test O(m log n) binary search");
        patternSearchResultLabel.setFont(UITheme.FONT_SMALL);
        patternSearchResultLabel.setForeground(UITheme.TEXT_MUTED);
        r2.add(patternSearchResultLabel);

        topCard.add(r2);
        add(topCard, BorderLayout.NORTH);

        populateDocCombo();

        // --- Center Table & Explanation Area ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        centerPanel.setBackground(UITheme.BACKGROUND);

        // Left: Suffix Array + LCP Table
        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setBackground(UITheme.CARD_BG);
        leftPanel.setBorder(UITheme.createCardBorder("Suffix Array (SA) & Kasai LCP Table"));

        String[] cols = {"i", "SA[i]", "LCP[i]", "Suffix Preview"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        saTable = new JTable(tableModel);
        UITheme.styleTable(saTable);
        saTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        saTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        saTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        saTable.getColumnModel().getColumn(3).setPreferredWidth(280);

        JScrollPane tableScroll = new JScrollPane(saTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        leftPanel.add(tableScroll, BorderLayout.CENTER);

        // LRS Banner Card at bottom of left panel
        JPanel lrsBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        lrsBanner.setBackground(new Color(236, 253, 245)); // Soft Emerald
        lrsBanner.setBorder(BorderFactory.createLineBorder(new Color(167, 243, 208), 1));

        lrsLabel = new JLabel(" Longest Repeated Substring (LRS): None");
        lrsLabel.setFont(UITheme.FONT_BOLD);
        lrsLabel.setForeground(new Color(6, 95, 70));
        lrsBanner.add(lrsLabel);

        leftPanel.add(lrsBanner, BorderLayout.SOUTH);
        centerPanel.add(leftPanel);

        // Right: Viva Information & Kasai Algorithm Details
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(UITheme.CARD_BG);
        rightPanel.setBorder(UITheme.createCardBorder("Suffix Structures & Kasai's Algorithm (Viva Guide)"));

        vivaInfoArea = new JTextArea();
        vivaInfoArea.setFont(UITheme.FONT_MONO);
        vivaInfoArea.setLineWrap(true);
        vivaInfoArea.setWrapStyleWord(true);
        vivaInfoArea.setEditable(false);
        vivaInfoArea.setBackground(new Color(250, 250, 252));
        vivaInfoArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        vivaInfoArea.setText("Click 'ANALYZE SUFFIXES & LCP' to compute structures.\n\n" +
                "Key Concepts for Viva:\n" +
                "1. Suffix Array (SA): Lexicographical permutation of all suffix starting positions.\n" +
                "2. Suffix Array vs Suffix Tree: SA uses 4x to 8x less memory than Suffix Tree pointers.\n" +
                "3. Kasai's Algorithm: Computes LCP in linear O(n) time using the rank array.\n" +
                "4. Longest Repeated Substring (LRS): Corresponds to max(LCP[i]) in the LCP array.\n" +
                "5. Pattern Search: Binary search over SA takes O(m * log n) time.");

        JScrollPane vivaScroll = new JScrollPane(vivaInfoArea);
        vivaScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        rightPanel.add(vivaScroll, BorderLayout.CENTER);

        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createSampleChip(String text, String searchPattern) {
        JButton btn = new JButton(text);
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
            docSelectorCombo.setSelectedIndex(0);
            customTextField.setText(text);
            patternSearchField.setText(searchPattern);
            runAnalysis();
        });
        return btn;
    }

    private void populateDocCombo() {
        docSelectorCombo.addItem("-- Use Custom Text Below --");
        CustomList<Document> docs = documentStore.getAllDocuments();
        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            docSelectorCombo.addItem("[" + d.getId() + "] " + d.getTitle() + " (" + d.getLanguage() + ")");
        }
    }

    private void onDocSelected() {
        int idx = docSelectorCombo.getSelectedIndex();
        if (idx > 0) {
            Document d = documentStore.getAllDocuments().get(idx - 1);
            String sample = d.getContent();
            if (sample.length() > 140) {
                sample = sample.substring(0, 140);
            }
            customTextField.setText(sample);
        }
    }

    public void runAnalysis() {
        String text = customTextField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter or select text.", "Empty Text", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (text.length() > 200) {
            text = text.substring(0, 200);
        }

        currentSA = new SuffixArray(text);
        currentLCP = new LCP(currentSA);

        tableModel.setRowCount(0);
        int[] sa = currentSA.getSuffixArray();
        int[] lcp = currentLCP.getLcpArray();

        int limit = Math.min(sa.length, 60);
        for (int i = 0; i < limit; i++) {
            tableModel.addRow(new Object[]{
                i,
                sa[i],
                lcp[i],
                currentSA.getSuffixPreview(i, 35)
            });
        }

        String lrs = currentLCP.getLongestRepeatedSubstring();
        if (lrs != null && lrs.length() > 0) {
            lrsLabel.setText(" Longest Repeated Substring (LRS): \"" + lrs + "\" (Length: " + currentLCP.getMaxLcpLength() + ")");
        } else {
            lrsLabel.setText(" Longest Repeated Substring (LRS): None");
        }

        // Detailed Report
        StringBuilder sb = new StringBuilder();
        sb.append("KASAI'S LCP & SUFFIX ARRAY ANALYSIS\n");
        sb.append("==================================================\n");
        sb.append("Input Text Length: ").append(text.length()).append(" characters\n");
        sb.append("Total Suffixes:    ").append(sa.length).append("\n");
        sb.append("Max LCP Length:    ").append(currentLCP.getMaxLcpLength()).append("\n");
        sb.append("LRS Substring:     \"").append(lrs).append("\"\n\n");

        sb.append("Why Kasai's Algorithm Runs in O(n) Time:\n");
        sb.append("--------------------------------------------------\n");
        sb.append("1. Suffix order in text: i = 0, 1, 2, ..., n-1.\n");
        sb.append("2. Let h be LCP of suffix i and its predecessor in SA.\n");
        sb.append("3. For suffix i+1, LCP is at least (h - 1) because dropping\n");
        sb.append("   the first character from both suffixes preserves the remaining (h-1) matches!\n");
        sb.append("4. Since h decreases by at most 1 in each step and h <= n,\n");
        sb.append("   the total number of character increment operations is strictly <= 2n.\n");
        sb.append("5. Hence, Kasai's algorithm computes the entire LCP array in O(n) linear time.\n\n");

        sb.append("Binary Search in Suffix Array:\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Because suffixes in SA are lexicographically sorted, any substring\n");
        sb.append("corresponds to a contiguous range in the suffix array.\n");
        sb.append("We can find this range using two binary searches in O(m * log n) time.");

        vivaInfoArea.setText(sb.toString());
        vivaInfoArea.setCaretPosition(0);
    }

    private void searchInSuffixArray() {
        if (currentSA == null) {
            runAnalysis();
        }
        String pattern = patternSearchField.getText().trim();
        if (pattern.isEmpty() || currentSA == null) return;

        IntList matches = currentSA.search(pattern);
        if (matches.size() > 0) {
            StringBuilder sb = new StringBuilder("Found " + matches.size() + " matches at indices: [");
            for (int i = 0; i < matches.size(); i++) {
                sb.append(matches.get(i)).append(i < matches.size() - 1 ? ", " : "");
            }
            sb.append("]");
            patternSearchResultLabel.setText(sb.toString());
            patternSearchResultLabel.setForeground(new Color(6, 95, 70));
        } else {
            patternSearchResultLabel.setText("Pattern '" + pattern + "' not found in suffix array.");
            patternSearchResultLabel.setForeground(UITheme.DANGER);
        }
    }
}
