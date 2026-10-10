package ui;

import datastructures.CustomList;
import graph.MaxFlow;
import graph.VertexCoverApproximation;
import randomized.MillerRabin;

import javax.swing.*;
import java.awt.*;

/**
 * Screen demonstrating advanced BTech CSE DSA curriculum concepts:
 * 1. Network Flow: Edmonds-Karp Maximum Flow (BFS augmenting paths)
 * 2. Approximation Algorithms: 2-Approximation for Minimum Vertex Cover
 * 3. Randomized Algorithms: Miller-Rabin Primality Test
 */
public class AdvancedLabPanel extends JPanel {

    private JTabbedPane tabbedPane;

    // Max Flow Components
    private JComboBox<String> flowPresetCombo;
    private JButton runFlowButton;
    private JTextArea flowReportArea;

    // Vertex Cover Components
    private JComboBox<String> vcPresetCombo;
    private JButton runVcButton;
    private JTextArea vcReportArea;

    // Miller-Rabin Components
    private JTextField numberField;
    private JSpinner roundsSpinner;
    private JButton runMillerRabinButton;
    private JTextArea mrReportArea;

    public AdvancedLabPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        initUI();
    }

    private void initUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        tabbedPane.addTab("1. Edmonds-Karp Max Flow", createMaxFlowPanel());
        tabbedPane.addTab("2. Vertex Cover 2-Approximation", createVertexCoverPanel());
        tabbedPane.addTab("3. Miller-Rabin Primality Test", createMillerRabinPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ---------------- MAX FLOW TAB ----------------
    private JPanel createMaxFlowPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setBorder(UITheme.createCardBorder("Network Flow Configuration"));

        topBar.add(new JLabel("Flow Network Preset:"));
        flowPresetCombo = new JComboBox<>(new String[]{
            "Preset 1: 4 Vertices (S, A, B, T) - Standard Textbook",
            "Preset 2: 6 Vertices (Source, v1, v2, v3, v4, Sink)",
            "Preset 3: Computer Network Router Bandwidth Model"
        });
        flowPresetCombo.setFont(UITheme.FONT_REGULAR);
        topBar.add(flowPresetCombo);

        runFlowButton = UITheme.createPrimaryButton("COMPUTE MAXIMUM FLOW");
        runFlowButton.addActionListener(e -> executeMaxFlow());
        topBar.add(runFlowButton);

        panel.add(topBar, BorderLayout.NORTH);

        flowReportArea = new JTextArea();
        flowReportArea.setFont(UITheme.FONT_MONO);
        flowReportArea.setEditable(false);
        flowReportArea.setBackground(new Color(250, 250, 252));

        JScrollPane scroll = new JScrollPane(flowReportArea);
        scroll.setBorder(UITheme.createCardBorder("Edmonds-Karp Residual Graph & Augmenting Path Log"));
        panel.add(scroll, BorderLayout.CENTER);

        executeMaxFlow();
        return panel;
    }

    private void executeMaxFlow() {
        int preset = flowPresetCombo.getSelectedIndex();
        int[][] capacity;
        String[] names;

        if (preset == 0) {
            capacity = new int[4][4];
            capacity[0][1] = 10;
            capacity[0][2] = 5;
            capacity[1][2] = 15;
            capacity[1][3] = 10;
            capacity[2][3] = 10;
            names = new String[]{"Source (S)", "Vertex A", "Vertex B", "Sink (T)"};
        } else if (preset == 1) {
            // 6-node network
            capacity = new int[6][6];
            capacity[0][1] = 16; capacity[0][2] = 13;
            capacity[1][2] = 10; capacity[1][3] = 12;
            capacity[2][1] = 4;  capacity[2][4] = 14;
            capacity[3][2] = 9;  capacity[3][5] = 20;
            capacity[4][3] = 7;  capacity[4][5] = 4;
            names = new String[]{"s", "v1", "v2", "v3", "v4", "t"};
        } else {
            // Router Bandwidth Model
            capacity = new int[5][5];
            capacity[0][1] = 100; capacity[0][2] = 80;
            capacity[1][3] = 70;  capacity[2][3] = 60;
            capacity[1][4] = 30;  capacity[3][4] = 120;
            names = new String[]{"Gateway-S", "Router-1", "Router-2", "Switch-3", "Host-T"};
        }

        MaxFlow.MaxFlowResult result = MaxFlow.computeMaxFlow(capacity, 0, capacity.length - 1, names);
        flowReportArea.setText(result.getDetailedReport());
        flowReportArea.setCaretPosition(0);
    }

    // ---------------- VERTEX COVER TAB ----------------
    private JPanel createVertexCoverPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setBorder(UITheme.createCardBorder("Graph Configuration"));

        topBar.add(new JLabel("Graph Model Preset:"));
        vcPresetCombo = new JComboBox<>(new String[]{
            "Graph 1: Path Graph (A - B - C - D)",
            "Graph 2: Bipartite Graph (K_3,3 Topology)",
            "Graph 3: Star Graph (Center S connected to A, B, C, D, E)"
        });
        vcPresetCombo.setFont(UITheme.FONT_REGULAR);
        topBar.add(vcPresetCombo);

        runVcButton = UITheme.createPrimaryButton("RUN 2-APPROXIMATION");
        runVcButton.addActionListener(e -> executeVertexCover());
        topBar.add(runVcButton);

        panel.add(topBar, BorderLayout.NORTH);

        vcReportArea = new JTextArea();
        vcReportArea.setFont(UITheme.FONT_MONO);
        vcReportArea.setEditable(false);
        vcReportArea.setBackground(new Color(250, 250, 252));

        JScrollPane scroll = new JScrollPane(vcReportArea);
        scroll.setBorder(UITheme.createCardBorder("Maximal Matching & 2-Approximation Analysis"));
        panel.add(scroll, BorderLayout.CENTER);

        executeVertexCover();
        return panel;
    }

    private void executeVertexCover() {
        int preset = vcPresetCombo.getSelectedIndex();
        int n;
        String[] names;
        CustomList<VertexCoverApproximation.Edge> edges = new CustomList<>();

        if (preset == 0) {
            n = 4;
            names = new String[]{"A", "B", "C", "D"};
            edges.add(new VertexCoverApproximation.Edge(0, 1));
            edges.add(new VertexCoverApproximation.Edge(1, 2));
            edges.add(new VertexCoverApproximation.Edge(2, 3));
        } else if (preset == 1) {
            n = 6;
            names = new String[]{"U1", "U2", "U3", "V1", "V2", "V3"};
            edges.add(new VertexCoverApproximation.Edge(0, 3));
            edges.add(new VertexCoverApproximation.Edge(0, 4));
            edges.add(new VertexCoverApproximation.Edge(1, 3));
            edges.add(new VertexCoverApproximation.Edge(1, 5));
            edges.add(new VertexCoverApproximation.Edge(2, 4));
            edges.add(new VertexCoverApproximation.Edge(2, 5));
        } else {
            n = 6;
            names = new String[]{"Center (S)", "Leaf-1", "Leaf-2", "Leaf-3", "Leaf-4", "Leaf-5"};
            for (int i = 1; i < 6; i++) {
                edges.add(new VertexCoverApproximation.Edge(0, i));
            }
        }

        VertexCoverApproximation.VertexCoverResult res =
                VertexCoverApproximation.computeVertexCover(n, edges, names);

        vcReportArea.setText(res.getReport());
        vcReportArea.setCaretPosition(0);
    }

    // ---------------- MILLER-RABIN TAB ----------------
    private JPanel createMillerRabinPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setBorder(UITheme.createCardBorder("Primality Test Parameters"));

        topBar.add(new JLabel("Test Number (n):"));
        numberField = new JTextField("1000000007", 14);
        numberField.setFont(UITheme.FONT_REGULAR);
        topBar.add(numberField);

        topBar.add(new JLabel("Testing Rounds (k):"));
        roundsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        roundsSpinner.setPreferredSize(new Dimension(55, 26));
        topBar.add(roundsSpinner);

        runMillerRabinButton = UITheme.createPrimaryButton("RUN MILLER-RABIN TEST");
        runMillerRabinButton.addActionListener(e -> executeMillerRabin());
        topBar.add(runMillerRabinButton);

        // Preset quick test buttons
        JButton p1 = UITheme.createSecondaryButton("Prime: 1000000007");
        p1.addActionListener(e -> { numberField.setText("1000000007"); executeMillerRabin(); });
        topBar.add(p1);

        JButton p2 = UITheme.createSecondaryButton("Carmichael: 561");
        p2.addActionListener(e -> { numberField.setText("561"); executeMillerRabin(); });
        topBar.add(p2);

        JButton p3 = UITheme.createSecondaryButton("Prime: 982451653");
        p3.addActionListener(e -> { numberField.setText("982451653"); executeMillerRabin(); });
        topBar.add(p3);

        panel.add(topBar, BorderLayout.NORTH);

        mrReportArea = new JTextArea();
        mrReportArea.setFont(UITheme.FONT_MONO);
        mrReportArea.setEditable(false);
        mrReportArea.setBackground(new Color(250, 250, 252));

        JScrollPane scroll = new JScrollPane(mrReportArea);
        scroll.setBorder(UITheme.createCardBorder("Probabilistic Primality Verdict & Round Witnesses"));
        panel.add(scroll, BorderLayout.CENTER);

        executeMillerRabin();
        return panel;
    }

    private void executeMillerRabin() {
        String input = numberField.getText().trim();
        long num;
        try {
            num = Long.parseLong(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int k = (Integer) roundsSpinner.getValue();
        MillerRabin.PrimalityResult res = MillerRabin.testPrimality(num, k);
        mrReportArea.setText(res.getReport());
        mrReportArea.setCaretPosition(0);
    }
}
