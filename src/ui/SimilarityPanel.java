package ui;

import data.DocumentStore;
import datastructures.CustomList;
import model.Document;
import similarity.DocumentSimilarity;

import javax.swing.*;
import java.awt.*;

/**
 * Modern screen for pairwise document similarity analysis.
 * Combines token overlap (Jaccard / Dice) and Longest Common Subsequence (LCS) DP.
 */
public class SimilarityPanel extends JPanel {

    private final DocumentStore documentStore;

    private JComboBox<String> docACombo;
    private JComboBox<String> docBCombo;
    private JButton compareButton;
    private JProgressBar similarityBar;
    private JLabel scoreBadgeLabel;
    private JLabel scoreDetailLabel;
    private JTextArea reportArea;
    private JTextArea docAPreview;
    private JTextArea docBPreview;

    public SimilarityPanel(DocumentStore documentStore) {
        this.documentStore = documentStore;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        initUI();
    }

    private void initUI() {
        // --- Top Selection Card ---
        JPanel topCard = new JPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));
        topCard.setBackground(UITheme.CARD_BG);
        topCard.setBorder(UITheme.createCardBorder("Document Comparison Selection"));

        JPanel selectRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        selectRow.setBackground(UITheme.CARD_BG);

        JLabel lblA = new JLabel("Document A:");
        lblA.setFont(UITheme.FONT_BOLD);
        selectRow.add(lblA);

        docACombo = new JComboBox<>();
        docACombo.setFont(UITheme.FONT_REGULAR);
        docACombo.setPreferredSize(new Dimension(300, 30));
        selectRow.add(docACombo);

        JLabel lblB = new JLabel("Document B:");
        lblB.setFont(UITheme.FONT_BOLD);
        selectRow.add(lblB);

        docBCombo = new JComboBox<>();
        docBCombo.setFont(UITheme.FONT_REGULAR);
        docBCombo.setPreferredSize(new Dimension(300, 30));
        selectRow.add(docBCombo);

        compareButton = UITheme.createPrimaryButton("COMPARE DOCUMENTS");
        compareButton.setPreferredSize(new Dimension(200, 32));
        compareButton.addActionListener(e -> runComparison());
        selectRow.add(compareButton);

        topCard.add(selectRow);

        // Score summary gauge row
        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        scoreRow.setBackground(UITheme.CARD_BG);

        scoreBadgeLabel = new JLabel("  -- %  ");
        scoreBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreBadgeLabel.setOpaque(true);
        scoreBadgeLabel.setBackground(new Color(241, 245, 249));
        scoreBadgeLabel.setForeground(UITheme.TEXT_MUTED);
        scoreBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        scoreRow.add(scoreBadgeLabel);

        similarityBar = new JProgressBar(0, 100);
        similarityBar.setStringPainted(true);
        similarityBar.setFont(UITheme.FONT_BOLD);
        similarityBar.setPreferredSize(new Dimension(240, 24));
        similarityBar.setForeground(UITheme.PRIMARY);
        scoreRow.add(similarityBar);

        scoreDetailLabel = new JLabel("Select two documents above and click COMPARE DOCUMENTS.");
        scoreDetailLabel.setFont(UITheme.FONT_REGULAR);
        scoreDetailLabel.setForeground(UITheme.TEXT_MUTED);
        scoreRow.add(scoreDetailLabel);

        topCard.add(scoreRow);
        add(topCard, BorderLayout.NORTH);

        populateDocumentCombos();

        // --- Center Area: Report + Side-by-Side Content ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        centerPanel.setBackground(UITheme.BACKGROUND);

        // Left: Similarity Metrics Report
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(UITheme.CARD_BG);
        leftPanel.setBorder(UITheme.createCardBorder("Dynamic Programming (LCS) & Overlap Metrics"));

        reportArea = new JTextArea();
        reportArea.setFont(UITheme.FONT_MONO);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(250, 250, 252));
        reportArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        reportArea.setText("Select two documents above to view similarity metrics:\n\n" +
                "- Token Overlap / Jaccard & Dice Index\n" +
                "- Longest Common Subsequence (LCS) DP Score\n" +
                "- Shared Vocabulary Keywords\n" +
                "- Multi-language Cross-document comparison");

        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        leftPanel.add(reportScroll, BorderLayout.CENTER);
        centerPanel.add(leftPanel);

        // Right: Side-by-Side Document Text
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        rightPanel.setBackground(UITheme.BACKGROUND);

        JPanel pA = new JPanel(new BorderLayout());
        pA.setBackground(UITheme.CARD_BG);
        pA.setBorder(UITheme.createCardBorder("Document A Content"));
        docAPreview = new JTextArea();
        docAPreview.setFont(UITheme.FONT_REGULAR);
        docAPreview.setLineWrap(true);
        docAPreview.setWrapStyleWord(true);
        docAPreview.setEditable(false);
        docAPreview.setBackground(new Color(250, 250, 252));
        docAPreview.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane spA = new JScrollPane(docAPreview);
        spA.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        pA.add(spA, BorderLayout.CENTER);
        rightPanel.add(pA);

        JPanel pB = new JPanel(new BorderLayout());
        pB.setBackground(UITheme.CARD_BG);
        pB.setBorder(UITheme.createCardBorder("Document B Content"));
        docBPreview = new JTextArea();
        docBPreview.setFont(UITheme.FONT_REGULAR);
        docBPreview.setLineWrap(true);
        docBPreview.setWrapStyleWord(true);
        docBPreview.setEditable(false);
        docBPreview.setBackground(new Color(250, 250, 252));
        docBPreview.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane spB = new JScrollPane(docBPreview);
        spB.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        pB.add(spB, BorderLayout.CENTER);
        rightPanel.add(pB);

        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void populateDocumentCombos() {
        docACombo.removeAllItems();
        docBCombo.removeAllItems();

        CustomList<Document> docs = documentStore.getAllDocuments();
        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            String item = "[" + d.getId() + "] " + d.getTitle() + " (" + d.getLanguage() + ")";
            docACombo.addItem(item);
            docBCombo.addItem(item);
        }

        if (docs.size() >= 2) {
            docACombo.setSelectedIndex(0);
            docBCombo.setSelectedIndex(1);
        }
    }

    public void runComparison() {
        int idxA = docACombo.getSelectedIndex();
        int idxB = docBCombo.getSelectedIndex();

        CustomList<Document> docs = documentStore.getAllDocuments();
        if (idxA < 0 || idxA >= docs.size() || idxB < 0 || idxB >= docs.size()) {
            return;
        }

        Document docA = docs.get(idxA);
        Document docB = docs.get(idxB);

        docAPreview.setText(docA.getTitle() + " (" + docA.getLanguage() + ")\n\n" + docA.getContent());
        docAPreview.setCaretPosition(0);
        docBPreview.setText(docB.getTitle() + " (" + docB.getLanguage() + ")\n\n" + docB.getContent());
        docBPreview.setCaretPosition(0);

        DocumentSimilarity.SimilarityReport report = DocumentSimilarity.compareDocuments(docA, docB);

        int scoreInt = (int) Math.round(report.overallSimilarityPercentage);
        similarityBar.setValue(scoreInt);
        similarityBar.setString(scoreInt + "%");

        scoreBadgeLabel.setText(String.format("  %.1f%%  ", report.overallSimilarityPercentage));
        if (scoreInt >= 60) {
            scoreBadgeLabel.setBackground(new Color(209, 250, 229)); // Light Green
            scoreBadgeLabel.setForeground(new Color(6, 95, 70));
        } else if (scoreInt >= 30) {
            scoreBadgeLabel.setBackground(new Color(224, 242, 254)); // Light Blue
            scoreBadgeLabel.setForeground(new Color(3, 105, 161));
        } else {
            scoreBadgeLabel.setBackground(new Color(241, 245, 249)); // Light Gray
            scoreBadgeLabel.setForeground(UITheme.TEXT_MUTED);
        }

        scoreDetailLabel.setText(String.format(
            "Common Words: %d | LCS Length: %d | Dice Overlap: %.1f%%",
            report.commonWordCount, report.lcsLength, report.jaccardSimilarityPercentage
        ));

        reportArea.setText(report.toString() + "\n" +
                "Algorithm Analysis:\n" +
                "1. Tokenization segmenting words without external NLP libraries.\n" +
                "2. LCS DP finds order-preserving matching words (subsequence).\n" +
                "3. Dice Overlap measures bag-of-words keyword intersection.\n" +
                "4. Formula: Combined = 0.6 * Dice + 0.4 * LCS_Ratio.");
        reportArea.setCaretPosition(0);
    }
}
