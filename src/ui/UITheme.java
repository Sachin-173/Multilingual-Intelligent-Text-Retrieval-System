package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Visual styling design system for the Multilingual Text Retrieval System.
 * Implements a clean, modern college project aesthetic with:
 * - Slate/Navy header and sidebar
 * - Soft neutral card backgrounds with subtle borders
 * - Native Indic typography (Nirmala UI / Segoe UI)
 * - Zebra-striped tables with comfortable padding
 * - Color-coded badge pills for languages and algorithms
 */
public class UITheme {

    // Color Palette
    public static final Color HEADER_BG = new Color(15, 23, 42);       // Dark Slate #0f172a
    public static final Color SIDEBAR_BG = new Color(30, 41, 59);      // Slate 800 #1e293b
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);   // Slate 700
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235); // Royal Blue 600
    public static final Color PRIMARY = new Color(37, 99, 235);        // Royal Blue
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color ACCENT = new Color(16, 185, 129);        // Emerald Green
    public static final Color WARNING = new Color(245, 158, 11);       // Amber
    public static final Color DANGER = new Color(239, 68, 68);         // Rose Red
    public static final Color BACKGROUND = new Color(248, 250, 252);   // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color CARD_SUBTLE = new Color(241, 245, 249);  // Slate 100
    public static final Color TEXT_DARK = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200

    // Language Badge Colors
    public static final Color LANG_EN_BG = new Color(224, 242, 254);   // Sky Blue
    public static final Color LANG_EN_FG = new Color(3, 105, 161);
    public static final Color LANG_TE_BG = new Color(243, 232, 255);   // Purple/Violet
    public static final Color LANG_TE_FG = new Color(126, 34, 206);
    public static final Color LANG_HI_BG = new Color(254, 243, 199);   // Amber
    public static final Color LANG_HI_FG = new Color(180, 83, 9);
    public static final Color LANG_TA_BG = new Color(209, 250, 229);   // Emerald
    public static final Color LANG_TA_FG = new Color(4, 120, 87);

    // Multilingual Font Hierarchy (Nirmala UI is Windows' native Indic script font)
    private static final String INDIC_FONT_NAME = getAvailableIndicFont();

    public static final Font FONT_TITLE = new Font(INDIC_FONT_NAME, Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font(INDIC_FONT_NAME, Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font(INDIC_FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font(INDIC_FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(INDIC_FONT_NAME, Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private static String getAvailableIndicFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (String name : fontNames) {
            if ("Nirmala UI".equalsIgnoreCase(name)) return "Nirmala UI";
        }
        for (String name : fontNames) {
            if ("Segoe UI".equalsIgnoreCase(name)) return "Segoe UI";
        }
        return "Dialog";
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(7, 15, 7, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static Border createCardBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                title,
                0, 0, FONT_BOLD, TEXT_DARK
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }

    /**
     * Applies modern UI styling to JTable:
     * - Comfortable row height
     * - Clean header with soft background
     * - Subtle zebra striping
     * - Smooth selection color
     * - Centered alignments for numbers and badges
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(224, 231, 255)); // Soft Indigo
        table.setSelectionForeground(new Color(30, 27, 75));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));

        // Default cell renderer with padding and zebra stripes
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 250, 252)); // Light Slate
                    }
                    c.setForeground(TEXT_DARK);
                }

                // Language Badge column highlight (typically column 2)
                if (value != null && (column == 2 || "Language".equalsIgnoreCase(tbl.getColumnName(column)))) {
                    String str = value.toString();
                    if ("English".equalsIgnoreCase(str)) {
                        c.setForeground(LANG_EN_FG);
                    } else if ("Telugu".equalsIgnoreCase(str)) {
                        c.setForeground(LANG_TE_FG);
                    } else if ("Hindi".equalsIgnoreCase(str)) {
                        c.setForeground(LANG_HI_FG);
                    } else if ("Tamil".equalsIgnoreCase(str)) {
                        c.setForeground(LANG_TA_FG);
                    }
                }

                return c;
            }
        });
    }
}
