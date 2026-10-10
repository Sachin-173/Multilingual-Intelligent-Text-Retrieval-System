package ui;

import data.DocumentStore;
import datastructures.CustomList;
import model.Document;
import model.SearchResult;
import search.SearchEngine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern, clean Search Panel for the Multilingual Text Retrieval System.
 * Supports Auto-Detect Smart Mode, Exact, Fuzzy, Prefix, and Multi-Pattern search.
 */
public class SearchPanel extends JPanel {

    private final DocumentStore documentStore;
    private final SearchEngine searchEngine;

    // Controls
    private JTextField searchField;
    private JComboBox<String> languageCombo;
    private JComboBox<String> searchTypeCombo;
    private JComboBox<String> algorithmCombo;
    private JButton searchButton;
    private JButton clearButton;
    private JLabel suggestionLabel;
    private JLabel statusLabel;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JTextArea documentPreviewArea;
    private JLabel previewHeaderLabel;

    private CustomList<SearchResult> currentResults;

    public SearchPanel(DocumentStore documentStore, SearchEngine searchEngine) {
        this.documentStore = documentStore;
        this.searchEngine = searchEngine;
        this.currentResults = new CustomList<>();

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        initUI();
    }

    private void initUI() {
        // --- Top Control Card ---
        JPanel topCard = new JPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));
        topCard.setBackground(UITheme.CARD_BG);
        topCard.setBorder(UITheme.createCardBorder("Search Query & Intelligent Retrieval"));

        // Row 1: Search Query + Buttons
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setBackground(UITheme.CARD_BG);

        JLabel queryLabel = new JLabel("Query: ");
        queryLabel.setFont(UITheme.FONT_BOLD);
        queryLabel.setForeground(UITheme.TEXT_DARK);
        row1.add(queryLabel, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setFont(UITheme.FONT_REGULAR);
        searchField.setPreferredSize(new Dimension(380, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchField.addActionListener(e -> performSearch());
        row1.add(searchField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UITheme.CARD_BG);

        searchButton = UITheme.createPrimaryButton("SEARCH");
        searchButton.setPreferredSize(new Dimension(100, 34));
        searchButton.addActionListener(e -> performSearch());
        btnPanel.add(searchButton);

        clearButton = UITheme.createSecondaryButton("Clear");
        clearButton.setPreferredSize(new Dimension(80, 34));
        clearButton.addActionListener(e -> clearSearch());
        btnPanel.add(clearButton);

        row1.add(btnPanel, BorderLayout.EAST);
        topCard.add(row1);
        topCard.add(Box.createVerticalStrut(10));

        // Row 2: Selectors (Language, Search Type, Algorithm)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row2.setBackground(UITheme.CARD_BG);

        row2.add(createFieldLabel("Language:"));
        languageCombo = new JComboBox<>(new String[]{"All", "English", "Telugu", "Hindi", "Tamil"});
        languageCombo.setFont(UITheme.FONT_REGULAR);
        languageCombo.setPreferredSize(new Dimension(110, 30));
        row2.add(languageCombo);

        row2.add(createFieldLabel("Search Type:"));
        searchTypeCombo = new JComboBox<>(new String[]{
            "✨ Auto-Detect (Smart Mode)",
            "Exact Search",
            "Fuzzy Search (Edit Distance)",
            "Prefix Search (Trie)",
            "Multi-Pattern Search (Aho-Corasick)"
        });
        searchTypeCombo.setFont(UITheme.FONT_REGULAR);
        searchTypeCombo.setPreferredSize(new Dimension(220, 30));
        searchTypeCombo.addActionListener(e -> updateAlgorithmVisibility());
        row2.add(searchTypeCombo);

        JLabel algoLabel = createFieldLabel("Algorithm:");
        row2.add(algoLabel);
        algorithmCombo = new JComboBox<>(new String[]{
            "Auto-Selected",
            "KMP (Knuth-Morris-Pratt)",
            "Rabin-Karp (Rolling Hash)",
            "Naive Search"
        });
        algorithmCombo.setFont(UITheme.FONT_REGULAR);
        algorithmCombo.setPreferredSize(new Dimension(190, 30));
        algorithmCombo.setEnabled(false);
        row2.add(algorithmCombo);

        topCard.add(row2);
        topCard.add(Box.createVerticalStrut(8));

        // Row 3: Quick Example Test Chips for Instant Viva Testing
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row3.setBackground(UITheme.CARD_BG);

        JLabel sampleLabel = new JLabel("Quick Queries:");
        sampleLabel.setFont(UITheme.FONT_SMALL);
        sampleLabel.setForeground(UITheme.TEXT_MUTED);
        row3.add(sampleLabel);

        row3.add(createQuickChip("Hyderabad", "All"));
        row3.add(createQuickChip("Hyderbad", "All"));
        row3.add(createQuickChip("hyd", "All"));
        row3.add(createQuickChip("India Telangana Hyderabad", "All"));
        row3.add(createQuickChip("తెలంగాణ", "Telugu"));
        row3.add(createQuickChip("भारत", "Hindi"));
        row3.add(createQuickChip("இந்தியா", "Tamil"));

        topCard.add(row3);
        topCard.add(Box.createVerticalStrut(6));

        // Row 4: Typo / Did You Mean Banner
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row4.setBackground(UITheme.CARD_BG);
        suggestionLabel = new JLabel(" ");
        suggestionLabel.setFont(UITheme.FONT_BOLD);
        suggestionLabel.setForeground(new Color(220, 38, 38)); // Red highlight
        suggestionLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        suggestionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String text = suggestionLabel.getText();
                if (text.startsWith("Did you mean: '")) {
                    int end = text.indexOf("'", 15);
                    if (end > 15) {
                        String suggested = text.substring(15, end);
                        searchField.setText(suggested);
                        performSearch();
                    }
                }
            }
        });
        row4.add(suggestionLabel);
        topCard.add(row4);

        add(topCard, BorderLayout.NORTH);

        // --- Center Results Area ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBackground(UITheme.BACKGROUND);

        statusLabel = new JLabel(" Ready. Enter a query or click any Quick Query chip above to begin.");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        centerPanel.add(statusLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Document Title", "Language", "Matched Snippet / Context", "Score", "Algorithm", "Matches", "Time (ms)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        UITheme.styleTable(resultsTable);

        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(330);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(65);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        resultsTable.getColumnModel().getColumn(6).setPreferredWidth(65);
        resultsTable.getColumnModel().getColumn(7).setPreferredWidth(75);

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedDocument();
            }
        });

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        // Bottom Document Preview Pane
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(UITheme.CARD_BG);
        previewPanel.setBorder(UITheme.createCardBorder("Selected Document Details"));
        previewPanel.setPreferredSize(new Dimension(100, 185));

        previewHeaderLabel = new JLabel(" Select any row in the results table to inspect full document content.");
        previewHeaderLabel.setFont(UITheme.FONT_SMALL);
        previewHeaderLabel.setForeground(UITheme.TEXT_MUTED);
        previewHeaderLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        previewPanel.add(previewHeaderLabel, BorderLayout.NORTH);

        documentPreviewArea = new JTextArea();
        documentPreviewArea.setFont(UITheme.FONT_REGULAR);
        documentPreviewArea.setLineWrap(true);
        documentPreviewArea.setWrapStyleWord(true);
        documentPreviewArea.setEditable(false);
        documentPreviewArea.setBackground(new Color(250, 250, 252));
        documentPreviewArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane previewScroll = new JScrollPane(documentPreviewArea);
        previewScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        previewPanel.add(previewScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, previewPanel);
        splitPane.setResizeWeight(0.62);
        splitPane.setDividerSize(6);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    private JButton createQuickChip(String query, String lang) {
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
            searchField.setText(query);
            languageCombo.setSelectedItem(lang);
            performSearch();
        });
        return btn;
    }

    private void updateAlgorithmVisibility() {
        int idx = searchTypeCombo.getSelectedIndex();
        // Index 1 is Exact Search
        algorithmCombo.setEnabled(idx == 1);
        if (idx == 0) {
            algorithmCombo.setSelectedIndex(0); // Auto-Selected
        } else if (idx == 1 && algorithmCombo.getSelectedIndex() == 0) {
            algorithmCombo.setSelectedIndex(1); // Default to KMP
        }
    }

    public void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search query.", "Empty Query", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String language = (String) languageCombo.getSelectedItem();
        int searchTypeIndex = searchTypeCombo.getSelectedIndex();
        suggestionLabel.setText(" ");
        tableModel.setRowCount(0);
        documentPreviewArea.setText("");
        previewHeaderLabel.setText(" Inspecting results for: '" + query + "'");

        long totalStart = System.nanoTime();
        String autoReason = null;

        if (searchTypeIndex == 0) {
            // ✨ Smart Auto-Detect Mode
            SearchEngine.AutoSearchResult autoRes = searchEngine.searchAuto(query, language);
            currentResults = autoRes.results;
            autoReason = autoRes.reason;

            if (autoRes.suggestion != null && autoRes.suggestion.getDistance() > 0) {
                suggestionLabel.setText(autoRes.suggestion.toString() + " [Click to apply]");
            } else if (autoRes.autocompletions != null && autoRes.autocompletions.size() > 0) {
                StringBuilder sb = new StringBuilder("Trie Autocompletions: ");
                int limit = Math.min(6, autoRes.autocompletions.size());
                for (int i = 0; i < limit; i++) {
                    sb.append(autoRes.autocompletions.get(i)).append(i < limit - 1 ? ", " : "");
                }
                suggestionLabel.setText(sb.toString());
            }

        } else if (searchTypeIndex == 1) {
            // Exact Search
            String algoChoice = (String) algorithmCombo.getSelectedItem();
            String algoKey = "KMP";
            if (algoChoice != null && algoChoice.startsWith("Rabin-Karp")) {
                algoKey = "Rabin-Karp";
            } else if (algoChoice != null && algoChoice.startsWith("Naive")) {
                algoKey = "Naive";
            }

            currentResults = searchEngine.searchExact(query, language, algoKey);

            if (currentResults.size() == 0) {
                SearchEngine.FuzzySearchResult fRes = searchEngine.searchFuzzy(query, language, 2);
                if (fRes.suggestion != null && fRes.suggestion.getDistance() > 0) {
                    suggestionLabel.setText(fRes.suggestion.toString() + " [Click to apply]");
                }
            }

        } else if (searchTypeIndex == 2) {
            // Fuzzy Search (Edit Distance)
            SearchEngine.FuzzySearchResult fRes = searchEngine.searchFuzzy(query, language, 2);
            currentResults = fRes.results;
            if (fRes.suggestion != null && fRes.suggestion.getDistance() > 0) {
                suggestionLabel.setText(fRes.suggestion.toString() + " [Click to apply]");
            }

        } else if (searchTypeIndex == 3) {
            // Prefix Search (Trie)
            SearchEngine.PrefixSearchResult pRes = searchEngine.searchPrefix(query, language);
            currentResults = pRes.results;
            if (pRes.autocompletions.size() > 0) {
                StringBuilder sb = new StringBuilder("Trie Autocompletions: ");
                int limit = Math.min(6, pRes.autocompletions.size());
                for (int i = 0; i < limit; i++) {
                    sb.append(pRes.autocompletions.get(i)).append(i < limit - 1 ? ", " : "");
                }
                suggestionLabel.setText(sb.toString());
            }

        } else if (searchTypeIndex == 4) {
            // Multi-Pattern Search (Aho-Corasick)
            CustomList<String> patterns = new CustomList<>();
            String[] tokens = query.split("[,\\s]+");
            for (String tok : tokens) {
                if (!tok.trim().isEmpty()) {
                    patterns.add(tok.trim());
                }
            }
            currentResults = searchEngine.searchMultiPattern(patterns, language);
        }

        long totalEnd = System.nanoTime();
        double elapsedMs = (totalEnd - totalStart) / 1_000_000.0;

        // Populate table
        for (int i = 0; i < currentResults.size(); i++) {
            SearchResult sr = currentResults.get(i);
            Document doc = sr.getDocument();
            tableModel.addRow(new Object[]{
                doc.getId(),
                doc.getTitle(),
                doc.getLanguage(),
                sr.getMatchedSnippet(),
                String.format("%.1f", sr.getScore()),
                sr.getAlgorithmUsed(),
                sr.getMatchCount(),
                String.format("%.3f", sr.getExecutionTimeMs())
            });
        }

        String prefixNote = (autoReason != null) ? "[" + autoReason + "] " : "";

        if (currentResults.size() > 0) {
            statusLabel.setText(String.format(
                "%sFound %d matching documents in %.2f ms | Ranked by MergeSort (Top score: %.1f)",
                prefixNote, currentResults.size(), elapsedMs, currentResults.get(0).getScore()
            ));
            resultsTable.setRowSelectionInterval(0, 0);
        } else {
            statusLabel.setText(prefixNote + "No matching documents found for query: '" + query + "'");
        }
    }

    private void showSelectedDocument() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentResults.size()) {
            SearchResult sr = currentResults.get(selectedRow);
            Document doc = sr.getDocument();

            previewHeaderLabel.setText(String.format(
                " Document #%d : %s  |  Language: %s  |  Score: %.1f  |  Algorithm: %s",
                doc.getId(), doc.getTitle(), doc.getLanguage(), sr.getScore(), sr.getAlgorithmUsed()
            ));

            documentPreviewArea.setText(doc.getContent());
            documentPreviewArea.setCaretPosition(0);
        }
    }

    private void clearSearch() {
        searchField.setText("");
        suggestionLabel.setText(" ");
        tableModel.setRowCount(0);
        documentPreviewArea.setText("");
        previewHeaderLabel.setText(" Select any row in the results table to inspect full document content.");
        statusLabel.setText(" Ready. Search cleared.");
    }
}
