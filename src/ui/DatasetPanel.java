package ui;

import data.DocumentStore;
import datastructures.CustomList;
import model.Document;
import similarity.DocumentSimilarity;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Modern screen for browsing, filtering, and inspecting the local multilingual dataset.
 */
public class DatasetPanel extends JPanel {

    private final DocumentStore documentStore;

    private JComboBox<String> langFilterCombo;
    private JTextField filterTextField;
    private JLabel statsLabel;
    private JTable docTable;
    private DefaultTableModel tableModel;
    private JTextArea previewArea;
    private JLabel previewHeaderLabel;

    private CustomList<Document> displayedDocs;

    public DatasetPanel(DocumentStore documentStore) {
        this.documentStore = documentStore;
        this.displayedDocs = new CustomList<>();

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        initUI();
        refreshTable();
    }

    private void initUI() {
        // --- Top Control Card ---
        JPanel topCard = new JPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));
        topCard.setBackground(UITheme.CARD_BG);
        topCard.setBorder(UITheme.createCardBorder("Multilingual Dataset Browser & Filters"));

        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        r1.setBackground(UITheme.CARD_BG);

        JLabel langLbl = new JLabel("Filter Language:");
        langLbl.setFont(UITheme.FONT_BOLD);
        r1.add(langLbl);

        langFilterCombo = new JComboBox<>(new String[]{"All", "English", "Telugu", "Hindi", "Tamil"});
        langFilterCombo.setFont(UITheme.FONT_REGULAR);
        langFilterCombo.setPreferredSize(new Dimension(110, 30));
        langFilterCombo.addActionListener(e -> refreshTable());
        r1.add(langFilterCombo);

        JLabel filterLbl = new JLabel("Title Filter:");
        filterLbl.setFont(UITheme.FONT_BOLD);
        r1.add(filterLbl);

        filterTextField = new JTextField(18);
        filterTextField.setFont(UITheme.FONT_REGULAR);
        filterTextField.setPreferredSize(new Dimension(200, 30));
        filterTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        filterTextField.addActionListener(e -> refreshTable());
        r1.add(filterTextField);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        filterBtn.setPreferredSize(new Dimension(80, 30));
        filterBtn.addActionListener(e -> refreshTable());
        r1.add(filterBtn);

        JButton reloadBtn = UITheme.createSecondaryButton("Reload Dataset");
        reloadBtn.setPreferredSize(new Dimension(130, 30));
        reloadBtn.addActionListener(e -> {
            documentStore.loadDocuments();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Dataset reloaded successfully! Total documents: " + documentStore.size(), "Reloaded", JOptionPane.INFORMATION_MESSAGE);
        });
        r1.add(reloadBtn);

        topCard.add(r1);

        // Row 2: Quick language pills
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        r2.setBackground(UITheme.CARD_BG);
        JLabel quickLbl = new JLabel("Quick Filter:");
        quickLbl.setFont(UITheme.FONT_SMALL);
        quickLbl.setForeground(UITheme.TEXT_MUTED);
        r2.add(quickLbl);

        r2.add(createLanguagePill("All Languages", "All"));
        r2.add(createLanguagePill("English (7)", "English"));
        r2.add(createLanguagePill("తెలుగు (7)", "Telugu"));
        r2.add(createLanguagePill("हिन्दी (7)", "Hindi"));
        r2.add(createLanguagePill("தமிழ் (7)", "Tamil"));

        topCard.add(r2);
        add(topCard, BorderLayout.NORTH);

        // --- Center Area: Table + Preview Split ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBackground(UITheme.BACKGROUND);

        statsLabel = new JLabel(" Loading dataset...");
        statsLabel.setFont(UITheme.FONT_SMALL);
        statsLabel.setForeground(UITheme.TEXT_MUTED);
        centerPanel.add(statsLabel, BorderLayout.NORTH);

        String[] cols = {"ID", "Document Title", "Language", "Tokens", "Content Preview"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        docTable = new JTable(tableModel);
        UITheme.styleTable(docTable);
        docTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        docTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        docTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        docTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        docTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        docTable.getColumnModel().getColumn(4).setPreferredWidth(450);

        docTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedDocPreview();
            }
        });

        JScrollPane tableScroll = new JScrollPane(docTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        // Preview Area
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(UITheme.CARD_BG);
        previewPanel.setBorder(UITheme.createCardBorder("Full Document Content (UTF-8 Native Encoding)"));
        previewPanel.setPreferredSize(new Dimension(100, 200));

        previewHeaderLabel = new JLabel(" Select any document from the table above to view full text.");
        previewHeaderLabel.setFont(UITheme.FONT_SMALL);
        previewHeaderLabel.setForeground(UITheme.TEXT_MUTED);
        previewHeaderLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        previewPanel.add(previewHeaderLabel, BorderLayout.NORTH);

        previewArea = new JTextArea();
        previewArea.setFont(UITheme.FONT_REGULAR);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setEditable(false);
        previewArea.setBackground(new Color(250, 250, 252));
        previewArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane sp = new JScrollPane(previewArea);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        previewPanel.add(sp, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, previewPanel);
        split.setResizeWeight(0.6);
        split.setDividerSize(6);
        centerPanel.add(split, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createLanguagePill(String label, String lang) {
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
            langFilterCombo.setSelectedItem(lang);
            refreshTable();
        });
        return btn;
    }

    public void refreshTable() {
        String lang = (String) langFilterCombo.getSelectedItem();
        String titleFilter = filterTextField.getText().trim().toLowerCase();

        tableModel.setRowCount(0);
        displayedDocs = new CustomList<>();

        CustomList<Document> all = documentStore.getAllDocuments();
        int enCount = 0, teCount = 0, hiCount = 0, taCount = 0;

        for (int i = 0; i < all.size(); i++) {
            Document d = all.get(i);
            String docLang = d.getLanguage();
            if ("English".equalsIgnoreCase(docLang)) enCount++;
            else if ("Telugu".equalsIgnoreCase(docLang)) teCount++;
            else if ("Hindi".equalsIgnoreCase(docLang)) hiCount++;
            else if ("Tamil".equalsIgnoreCase(docLang)) taCount++;

            // Match language
            if (lang != null && !lang.equalsIgnoreCase("All") && !docLang.equalsIgnoreCase(lang)) {
                continue;
            }
            // Match title substring
            if (!titleFilter.isEmpty() && !d.getTitle().toLowerCase().contains(titleFilter)) {
                continue;
            }

            displayedDocs.add(d);
            CustomList<String> tokens = DocumentSimilarity.tokenize(d.getContent());
            String preview = d.getContent().replace('\n', ' ');
            if (preview.length() > 90) {
                preview = preview.substring(0, 90) + "...";
            }

            tableModel.addRow(new Object[]{
                d.getId(),
                d.getTitle(),
                d.getLanguage(),
                tokens.size(),
                preview
            });
        }

        statsLabel.setText(String.format(
            " Showing %d documents | Total Dataset: %d (English: %d, Telugu: %d, Hindi: %d, Tamil: %d) | Stored in data/documents.txt",
            displayedDocs.size(), all.size(), enCount, teCount, hiCount, taCount
        ));

        if (displayedDocs.size() > 0) {
            docTable.setRowSelectionInterval(0, 0);
        } else {
            previewArea.setText("");
            previewHeaderLabel.setText(" No documents match the selected filters.");
        }
    }

    private void showSelectedDocPreview() {
        int row = docTable.getSelectedRow();
        if (row >= 0 && row < displayedDocs.size()) {
            Document d = displayedDocs.get(row);
            previewHeaderLabel.setText(String.format(" Document #%d : %s  |  Language: %s", d.getId(), d.getTitle(), d.getLanguage()));
            previewArea.setText(d.getContent());
            previewArea.setCaretPosition(0);
        }
    }
}
