package multiple;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// Main application class
public class FiretruckSimulator extends JFrame {

    private Graph graph;
    private DrawingPanel drawingPanel;
    private JComboBox<String> startNodeComboBox;
    private JComboBox<String> endNodeComboBox;
    private JComboBox<String> intermediateNodeComboBox; // To select nodes to add
    private JButton addStopButton;
    private JButton removeStopButton;
    private JButton clearStopsButton;
    private JList<String> intermediateStopsList;
    private DefaultListModel<String> intermediateStopsModel;
    private JButton findPathButton;
    private JButton resetButton;
    private JTextArea resultArea;

    private Node selectedStartNode = null;
    private Node selectedEndNode = null;
    private List<Node> selectedIntermediateNodes = new ArrayList<>(); // Store actual Node objects
    private List<Node> shortestPath = null;
    private double shortestPathDistance = -1;

    // --- Constants for Drawing ---
    private static final int NODE_DIAMETER = 30;
    private static final int PADDING = 50;
    private static final Color NODE_COLOR = new Color(100, 150, 255); // Light Blue
    private static final Color EDGE_COLOR = Color.DARK_GRAY;
    private static final Color PATH_COLOR = Color.ORANGE;
    private static final Color START_NODE_COLOR = Color.GREEN;
    private static final Color END_NODE_COLOR = Color.MAGENTA;
    private static final Color INTERMEDIATE_NODE_COLOR = Color.CYAN; // Color for intermediate stops
    private static final Color FIRETRUCK_COLOR = Color.RED;
    private static final Color WEIGHT_COLOR = Color.BLACK;
    private static final Stroke EDGE_STROKE = new BasicStroke(1.5f);
    private static final Stroke PATH_STROKE = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Font WEIGHT_FONT = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font NODE_FONT = new Font("Arial", Font.BOLD, 11);
    private static final int FIRETRUCK_WIDTH = 20;
    private static final int FIRETRUCK_HEIGHT = 10;

    // Constructor
    public FiretruckSimulator() {
        super("Firetruck Multi-Stop Simulator (Dijkstra)");
        this.graph = createSampleGraph();

        initComponents();
        layoutComponents();
        attachListeners();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Increased size slightly for more controls
        setSize(1200, 900);
        setLocationRelativeTo(null);
    }

    // --- Graph Creation (Using the complex one from previous step) ---
    private Graph createSampleGraph() {
        Graph g = new Graph();
        // Nodes
        g.addNode("FS1", 80, 100); g.addNode("J1", 200, 80); g.addNode("J2", 150, 250);
        g.addNode("Hosp", 300, 280); g.addNode("Mall", 450, 150); g.addNode("J3", 350, 50);
        g.addNode("Schl", 600, 80); g.addNode("Park", 550, 300); g.addNode("J4", 400, 400);
        g.addNode("FS2", 100, 500); g.addNode("J5", 280, 520); g.addNode("Res1", 450, 550);
        g.addNode("Res2", 650, 450); g.addNode("Fac", 800, 500); g.addNode("J6", 750, 300);
        g.addNode("Airp", 900, 150); g.addNode("J7", 700, 100);
        // Edges
        g.addEdge("FS1", "J1", 12); g.addEdge("FS1", "J2", 18); g.addEdge("J1", "J3", 15);
        g.addEdge("J1", "Mall", 25); g.addEdge("J2", "Hosp", 10); g.addEdge("J2", "J4", 20);
        g.addEdge("J2", "FS2", 28); g.addEdge("Hosp", "Mall", 16); g.addEdge("Hosp", "J4", 12);
        g.addEdge("Mall", "J3", 10); g.addEdge("Mall", "Schl", 22); g.addEdge("Mall", "Park", 18);
        g.addEdge("J3", "Schl", 25); g.addEdge("Schl", "J7", 8); g.addEdge("Schl", "Park", 15);
        g.addEdge("Park", "J4", 14); g.addEdge("Park", "J6", 20); g.addEdge("Park", "Res2", 18);
        g.addEdge("J4", "J5", 15); g.addEdge("J4", "Res1", 10); g.addEdge("FS2", "J5", 18);
        g.addEdge("J5", "Res1", 16); g.addEdge("Res1", "Res2", 24); g.addEdge("Res1", "Fac", 35);
        g.addEdge("Res2", "Fac", 15); g.addEdge("Res2", "J6", 10); g.addEdge("Fac", "J6", 12);
        g.addEdge("J6", "Airp", 28); g.addEdge("J6", "J7", 20); g.addEdge("J7", "Airp", 18);
        return g;
    }

    // --- GUI Initialization ---
    private void initComponents() {
        drawingPanel = new DrawingPanel();
        startNodeComboBox = new JComboBox<>();
        endNodeComboBox = new JComboBox<>();
        intermediateNodeComboBox = new JComboBox<>(); // For selecting stops to add

        intermediateStopsModel = new DefaultListModel<>();
        intermediateStopsList = new JList<>(intermediateStopsModel);
        intermediateStopsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        intermediateStopsList.setVisibleRowCount(4); // Show 4 items at a time

        addStopButton = new JButton("Add Stop");
        removeStopButton = new JButton("Remove Selected");
        removeStopButton.setEnabled(false); // Enabled when item selected
        clearStopsButton = new JButton("Clear All Stops");

        findPathButton = new JButton("Find Multi-Stop Route"); // Updated text
        resetButton = new JButton("Reset All");
        resultArea = new JTextArea(5, 80); // Increased rows slightly
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Route Details"));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        // Populate combo boxes
        List<String> nodeIds = new ArrayList<>(graph.getNodes().keySet());
        Collections.sort(nodeIds);
        if (nodeIds.isEmpty()) {
             // Handle case of empty graph (though createSampleGraph ensures it's not)
             findPathButton.setEnabled(false);
             addStopButton.setEnabled(false);
        } else {
            for (String id : nodeIds) {
                startNodeComboBox.addItem(id);
                endNodeComboBox.addItem(id);
                intermediateNodeComboBox.addItem(id); // All nodes can be potential stops
            }
            // Set initial selection
            startNodeComboBox.setSelectedItem("FS1");
            endNodeComboBox.setSelectedItem("Fac");
            intermediateNodeComboBox.setSelectedIndex(0); // Default selection for adding
             if (startNodeComboBox.getSelectedIndex() == -1) startNodeComboBox.setSelectedIndex(0);
             if (endNodeComboBox.getSelectedIndex() == -1) endNodeComboBox.setSelectedIndex(Math.min(1, nodeIds.size() - 1));
        }
    }

    // --- GUI Layout ---
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10)); // Main layout gaps

        // --- Top Control Panel ---
        JPanel topControlPanel = new JPanel();
        topControlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5); // Padding around components
        gbc.anchor = GridBagConstraints.WEST;

        // Start Node
        gbc.gridx = 0; gbc.gridy = 0;
        topControlPanel.add(new JLabel("Start:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        topControlPanel.add(startNodeComboBox, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; // Reset fill

        // End Node
        gbc.gridx = 0; gbc.gridy = 1;
        topControlPanel.add(new JLabel("End:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        topControlPanel.add(endNodeComboBox, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        // Intermediate Stops Label
        gbc.gridx = 3; gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
        topControlPanel.add(new JLabel("Intermediate Stops (in order):"), gbc);

        // Intermediate Stops List
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridheight = 3; gbc.fill = GridBagConstraints.VERTICAL;
        topControlPanel.add(new JScrollPane(intermediateStopsList), gbc);
        gbc.gridheight = 1; gbc.fill = GridBagConstraints.NONE; // Reset height/fill

        // Add Stop Section
        gbc.gridx = 4; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        topControlPanel.add(intermediateNodeComboBox, gbc);
        gbc.gridx = 5; gbc.gridy = 1;
        topControlPanel.add(addStopButton, gbc);

        // Remove/Clear Stop Buttons
        gbc.gridx = 4; gbc.gridy = 2;
        topControlPanel.add(removeStopButton, gbc);
        gbc.gridx = 5; gbc.gridy = 2;
        topControlPanel.add(clearStopsButton, gbc);

        // --- Bottom Control Panel (Find/Reset Buttons) ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bottomButtonPanel.add(findPathButton);
        bottomButtonPanel.add(resetButton);

        // --- Result Panel ---
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.add(bottomButtonPanel, BorderLayout.NORTH);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);


        // --- Main Layout ---
        add(topControlPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);

        // Adjust column weights for resizing behavior (optional but nice)
        gbc.weightx = 1.0; // Allow combo boxes to expand horizontally if needed
        gbc.gridx = 1; gbc.gridy = 0; topControlPanel.add(startNodeComboBox, gbc);
        gbc.gridy = 1; topControlPanel.add(endNodeComboBox, gbc);
        gbc.weightx = 0.0; // Reset weight
    }

    // --- Event Listeners ---
    private void attachListeners() {
        findPathButton.addActionListener(e -> findAndDisplayMultiStopPath());
        resetButton.addActionListener(e -> resetVisualization());
        addStopButton.addActionListener(e -> addIntermediateStop());
        removeStopButton.addActionListener(e -> removeIntermediateStop());
        clearStopsButton.addActionListener(e -> clearIntermediateStops());

        // Enable/Disable "Remove" button based on list selection
        intermediateStopsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Avoid reacting multiple times during selection change
                removeStopButton.setEnabled(intermediateStopsList.getSelectedIndex() != -1);
            }
        });

        // Update selected node highlights when combo boxes change
         ActionListener mainComboBoxListener = e -> {
            updateSelectedNodesFromUI();
            drawingPanel.repaint(); // Redraw to show new highlights immediately
        };
        startNodeComboBox.addActionListener(mainComboBoxListener);
        endNodeComboBox.addActionListener(mainComboBoxListener);

        // Initial selection update
        updateSelectedNodesFromUI();
    }

    // --- Stop Management Logic ---
    private void addIntermediateStop() {
        String stopId = (String) intermediateNodeComboBox.getSelectedItem();
        if (stopId != null && !intermediateStopsModel.contains(stopId)) {
            // Optional: Prevent adding start/end as intermediate? For now, allow it.
            // String startId = (String) startNodeComboBox.getSelectedItem();
            // String endId = (String) endNodeComboBox.getSelectedItem();
            // if (stopId.equals(startId) || stopId.equals(endId)) {
            //     JOptionPane.showMessageDialog(this,"Cannot add Start or End node as an intermediate stop.", "Info", JOptionPane.INFORMATION_MESSAGE);
            //     return;
            // }
            intermediateStopsModel.addElement(stopId);
            updateSelectedNodesFromUI(); // Update internal list and repaint
            drawingPanel.repaint();
        } else if (stopId != null && intermediateStopsModel.contains(stopId)) {
             JOptionPane.showMessageDialog(this,"Stop '" + stopId + "' is already in the list.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void removeIntermediateStop() {
        int selectedIndex = intermediateStopsList.getSelectedIndex();
        if (selectedIndex != -1) {
            intermediateStopsModel.remove(selectedIndex);
            updateSelectedNodesFromUI(); // Update internal list and repaint
            drawingPanel.repaint();
        }
    }

    private void clearIntermediateStops() {
        if (!intermediateStopsModel.isEmpty()) {
            intermediateStopsModel.clear();
            updateSelectedNodesFromUI(); // Update internal list and repaint
            drawingPanel.repaint();
        }
    }


    // --- Core Path Finding Logic (Multi-Stop) ---
    private void findAndDisplayMultiStopPath() {
        updateSelectedNodesFromUI(); // Ensure current selections are reflected

        if (selectedStartNode == null || selectedEndNode == null) {
            resultArea.setText("Please select valid Start and End nodes.");
            clearPathResults();
            drawingPanel.repaint();
            return;
        }

        // Build the full sequence of waypoints
        List<Node> waypoints = new ArrayList<>();
        waypoints.add(selectedStartNode);
        waypoints.addAll(selectedIntermediateNodes); // Add intermediate stops in order
        waypoints.add(selectedEndNode);

        // Remove consecutive duplicates (e.g., Start -> A -> A -> End becomes Start -> A -> End)
        waypoints = removeConsecutiveDuplicates(waypoints);

        if (waypoints.size() < 2) {
             resultArea.setText("Route requires at least two distinct points (Start and End).\nStart and End might be the same after removing duplicates.");
             clearPathResults();
             // If start == end, show that
             if (selectedStartNode.equals(selectedEndNode) && selectedIntermediateNodes.isEmpty()) {
                 shortestPath = List.of(selectedStartNode);
                 shortestPathDistance = 0.0;
                 resultArea.setText("Start and End nodes are the same. No intermediate stops.\nTotal Distance: 0.0");
             }
             drawingPanel.repaint();
             return;
        }

        // Calculate path segment by segment
        List<Node> fullPath = new LinkedList<>();
        double totalDistance = 0.0;
        boolean possible = true;

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Node segmentStart = waypoints.get(i);
            Node segmentEnd = waypoints.get(i + 1);

            // If segment start and end are the same, skip Dijkstra call
            if (segmentStart.equals(segmentEnd)) {
                // Add the node once if it's the very first segment and path is empty
                if (fullPath.isEmpty()) {
                    fullPath.add(segmentStart);
                }
                continue; // Distance is 0 for this segment
            }

            DijkstraResult segmentResult = Dijkstra.findShortestPath(graph, segmentStart, segmentEnd);

            if (!segmentResult.hasPath()) {
                resultArea.setText("Cannot find path from '" + segmentStart.getId() + "' to '" + segmentEnd.getId() + "'.\nFull multi-stop route is impossible.");
                clearPathResults(); // Clear previous results
                possible = false;
                break; // Stop processing further segments
            }

            // Add segment path nodes
            List<Node> segmentPathNodes = segmentResult.getPath();
            if (!segmentPathNodes.isEmpty()) {
                if (fullPath.isEmpty()) {
                    fullPath.addAll(segmentPathNodes); // Add all nodes for the first segment
                } else {
                    // Add all nodes *except the first one* (which is the end of the previous segment)
                    fullPath.addAll(segmentPathNodes.subList(1, segmentPathNodes.size()));
                }
            }
            totalDistance += segmentResult.getDistance();
        }

        // Display final results if the whole path was possible
        if (possible) {
            shortestPath = fullPath;
            shortestPathDistance = totalDistance;

            StringBuilder pathStr = new StringBuilder("Full Route: ");
            if(shortestPath.isEmpty() && waypoints.size() >= 1) { // Handle case Start=Stop1=...=End
                pathStr.append(waypoints.get(0).getId());
            } else {
                for (int i = 0; i < shortestPath.size(); i++) {
                    pathStr.append(shortestPath.get(i).getId());
                    if (i < shortestPath.size() - 1) {
                        pathStr.append(" -> ");
                    }
                }
            }
            pathStr.append(String.format("%nTotal Distance: %.1f units", shortestPathDistance));
            resultArea.setText(pathStr.toString());
        } else {
            // Error message already set, ensure visualization is cleared
             clearPathResults();
        }

        // Redraw the panel to show the full path, highlights, and truck
        drawingPanel.repaint();
    }

     // Helper to remove consecutive duplicates from the waypoint list
    private List<Node> removeConsecutiveDuplicates(List<Node> list) {
        if (list.size() < 2) return list;
        List<Node> result = new ArrayList<>();
        result.add(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            if (!list.get(i).equals(list.get(i - 1))) {
                result.add(list.get(i));
            }
        }
        return result;
    }

    private void resetVisualization() {
        // Reset selections in combo boxes and list
        if (startNodeComboBox.getItemCount() > 0) startNodeComboBox.setSelectedIndex(0);
        if (endNodeComboBox.getItemCount() > 1) endNodeComboBox.setSelectedIndex(1);
        else if (endNodeComboBox.getItemCount() > 0) endNodeComboBox.setSelectedIndex(0);
        intermediateStopsModel.clear();

        // Clear internal state and results
        clearPathResults();
        updateSelectedNodesFromUI(); // Update highlights based on reset selections

        resultArea.setText("");
        drawingPanel.repaint();
    }

     // Clears calculated path and distance, keeps node selections
    private void clearPathResults() {
        shortestPath = null;
        shortestPathDistance = -1;
    }

    // Updates the internal selected Node objects based on all UI selections
     private void updateSelectedNodesFromUI() {
        String startId = (String) startNodeComboBox.getSelectedItem();
        String endId = (String) endNodeComboBox.getSelectedItem();
        selectedStartNode = (startId != null) ? graph.getNode(startId) : null;
        selectedEndNode = (endId != null) ? graph.getNode(endId) : null;

        // Update intermediate nodes list from the JList model
        selectedIntermediateNodes.clear();
        for (int i = 0; i < intermediateStopsModel.size(); i++) {
            String stopId = intermediateStopsModel.getElementAt(i);
            Node stopNode = graph.getNode(stopId);
            if (stopNode != null) {
                selectedIntermediateNodes.add(stopNode);
            } else {
                 // This shouldn't happen if nodes are added via ComboBox, but good practice
                 System.err.println("Warning: Intermediate stop ID '" + stopId + "' not found in graph.");
            }
        }
        // Note: We don't clear the path results here, only update selections.
    }


    // --- Inner Class: Drawing Panel ---
    class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            setupAntialiasing(g2d);

            if (graph == null) return;

            drawEdgesAndWeights(g2d);
            drawShortestPath(g2d);
            drawNodes(g2d);
            drawFiretruck(g2d);
        }

        private void setupAntialiasing(Graphics2D g2d) {
             g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        private void drawEdgesAndWeights(Graphics2D g2d) {
             g2d.setStroke(EDGE_STROKE);
             g2d.setFont(WEIGHT_FONT);
             FontMetrics fm = g2d.getFontMetrics();

             Set<String> drawnEdges = new HashSet<>(); // Avoid drawing A->B and B->A separately

             for (Node node : graph.getNodes().values()) {
                 for (Map.Entry<Node, Double> entry : node.getAdjacencies().entrySet()) {
                     Node neighbor = entry.getKey();
                     String edgeId1 = node.getId() + "-" + neighbor.getId();
                     String edgeId2 = neighbor.getId() + "-" + node.getId();

                     // Only draw if this edge pair hasn't been drawn
                     if (!drawnEdges.contains(edgeId1) && !drawnEdges.contains(edgeId2)) {
                         double weight = entry.getValue();
                         int x1 = node.getX(); int y1 = node.getY();
                         int x2 = neighbor.getX(); int y2 = neighbor.getY();

                         // Draw Edge Line
                         g2d.setColor(EDGE_COLOR);
                         g2d.drawLine(x1, y1, x2, y2);

                         // Draw Edge Weight
                         drawWeightText(g2d, fm, weight, x1, y1, x2, y2);

                         drawnEdges.add(edgeId1); // Mark as drawn
                     }
                 }
             }
        }

        private void drawWeightText(Graphics2D g2d, FontMetrics fm, double weight, int x1, int y1, int x2, int y2) {
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;
            String weightStr = String.format("%.0f", weight);
            int textWidth = fm.stringWidth(weightStr);
            int textHeight = fm.getAscent();

            // Calculate angle for offset (more robust placement)
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int offsetX = (int) (Math.sin(angle) * 8); // Perpendicular offset
            int offsetY = (int) (-Math.cos(angle) * 8);

             // Adjust offset if text goes "under" the line based on angle
            if (angle > 0 && angle < Math.PI) { // Primarily downward slope
                 // No change needed typically
            } else { // Primarily upward slope
                 offsetY -= textHeight/2; // Shift slightly more
            }


            int textX = midX + offsetX;
            int textY = midY + offsetY + textHeight / 2; // Center vertically better


            // Background for readability
            g2d.setColor(getBackground()); // Use panel background color
            g2d.fillRect(textX - textWidth / 2 - 2, textY - textHeight, textWidth + 4, textHeight + 2);

            // Weight Text
            g2d.setColor(WEIGHT_COLOR);
            g2d.drawString(weightStr, textX - textWidth / 2, textY - 1); // Draw centered
        }


        private void drawShortestPath(Graphics2D g2d) {
            if (shortestPath != null && shortestPath.size() > 1 && Double.isFinite(shortestPathDistance)) {
                g2d.setColor(PATH_COLOR);
                g2d.setStroke(PATH_STROKE);
                for (int i = 0; i < shortestPath.size() - 1; i++) {
                    Node u = shortestPath.get(i);
                    Node v = shortestPath.get(i + 1);
                    g2d.drawLine(u.getX(), u.getY(), v.getX(), v.getY());
                }
            }
        }

        private void drawNodes(Graphics2D g2d) {
             g2d.setFont(NODE_FONT);
             FontMetrics nodeFm = g2d.getFontMetrics();

             // Convert list of intermediate nodes to a Set for quick lookup
             Set<Node> intermediateSet = new HashSet<>(selectedIntermediateNodes);

             for (Node node : graph.getNodes().values()) {
                 int nodeCenterX = node.getX();
                 int nodeCenterY = node.getY();
                 int nodeDrawX = nodeCenterX - NODE_DIAMETER / 2;
                 int nodeDrawY = nodeCenterY - NODE_DIAMETER / 2;

                 // Determine node color: Check Intermediate first, then Start/End
                 Color currentNodeColor = NODE_COLOR; // Default
                 if (intermediateSet.contains(node)) {
                     currentNodeColor = INTERMEDIATE_NODE_COLOR;
                 }
                 // Start/End override intermediate color if they are the same node
                 if (node.equals(selectedStartNode)) {
                     currentNodeColor = START_NODE_COLOR;
                 } else if (node.equals(selectedEndNode)) {
                     currentNodeColor = END_NODE_COLOR;
                 }


                 // Draw node circle
                 g2d.setColor(currentNodeColor);
                 g2d.fillOval(nodeDrawX, nodeDrawY, NODE_DIAMETER, NODE_DIAMETER);
                 g2d.setColor(Color.BLACK); // Border
                 g2d.drawOval(nodeDrawX, nodeDrawY, NODE_DIAMETER, NODE_DIAMETER);

                 // Draw Node ID centered
                 String nodeId = node.getId();
                 int textWidth = nodeFm.stringWidth(nodeId);
                 int textHeight = nodeFm.getAscent();
                 g2d.setColor(Color.BLACK); // Text color
                 g2d.drawString(nodeId, nodeCenterX - textWidth / 2, nodeCenterY + textHeight / 2 - 2);
             }
        }

         private void drawFiretruck(Graphics2D g2d) {
             // Draw at the start node only if a valid path exists
              if (shortestPath != null && !shortestPath.isEmpty() && Double.isFinite(shortestPathDistance) && selectedStartNode != null) {
                 // Use selectedStartNode's position directly
                 int truckX = selectedStartNode.getX() - FIRETRUCK_WIDTH / 2;
                 int truckY = selectedStartNode.getY() - NODE_DIAMETER / 2 - FIRETRUCK_HEIGHT - 3; // Position above

                 g2d.setColor(FIRETRUCK_COLOR);
                 g2d.fillRect(truckX, truckY, FIRETRUCK_WIDTH, FIRETRUCK_HEIGHT);

                 // Simple details
                 g2d.setColor(Color.DARK_GRAY);
                 g2d.fillOval(truckX + 2, truckY + FIRETRUCK_HEIGHT - 3, 5, 5); // Wheel 1
                 g2d.fillOval(truckX + FIRETRUCK_WIDTH - 7, truckY + FIRETRUCK_HEIGHT - 3, 5, 5); // Wheel 2
             }
         }

        @Override
        public Dimension getPreferredSize() {
            // Calculate bounds based on graph nodes
            int maxX = PADDING * 2, maxY = PADDING * 2;
            if (graph != null && !graph.getNodes().isEmpty()) {
                maxX = graph.getNodes().values().stream().mapToInt(Node::getX).max().orElse(maxX - PADDING) + PADDING;
                maxY = graph.getNodes().values().stream().mapToInt(Node::getY).max().orElse(maxY - PADDING) + PADDING;
            }
            // Ensure minimum size
            return new Dimension(Math.max(maxX, 800), Math.max(maxY, 600));
        }
    }

    // --- Inner Class: Node (Unchanged) ---
    static class Node {
        private final String id; private final int x, y;
        private final Map<Node, Double> adjacencies;
        public Node(String id, int x, int y) { this.id = id; this.x = x; this.y = y; this.adjacencies = new HashMap<>(); }
        public String getId() { return id; } public int getX() { return x; } public int getY() { return y; }
        public Map<Node, Double> getAdjacencies() { return adjacencies; }
        public void addNeighbor(Node neighbor, double weight) { adjacencies.put(neighbor, weight); }
        @Override public String toString() { return "Node{" + id + '}'; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Node node = (Node) o; return id.equals(node.id); }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    // --- Inner Class: Graph (Unchanged) ---
    static class Graph {
        private final Map<String, Node> nodes;
        public Graph() { this.nodes = new HashMap<>(); }
        public void addNode(String id, int x, int y) { if (!nodes.containsKey(id)) nodes.put(id, new Node(id, x, y)); else System.err.println("Warning: Node ID '" + id + "' exists."); }
        public Node getNode(String id) { return nodes.get(id); }
        public Map<String, Node> getNodes() { return Collections.unmodifiableMap(nodes); }
        public void addEdge(String id1, String id2, double weight) { addEdge(id1, id2, weight, true); }
        public void addEdge(String id1, String id2, double weight, boolean undirected) {
            Node n1 = nodes.get(id1), n2 = nodes.get(id2);
            if (n1 != null && n2 != null && weight >= 0) { n1.addNeighbor(n2, weight); if (undirected) n2.addNeighbor(n1, weight); }
            else if (weight < 0) System.err.println("Warning: Negative edge weight (" + id1 + "<->" + id2 + ").");
            else System.err.println("Warning: Add edge failed for " + id1 + ", " + id2 + ". Node(s) not found?");
        }
    }

    // --- Inner Class: Dijkstra's Algorithm (Unchanged) ---
    static class Dijkstra {
        public static DijkstraResult findShortestPath(Graph graph, Node startNode, Node endNode) {
            Map<Node, Double> dist = new HashMap<>(); Map<Node, Node> pred = new HashMap<>();
            PriorityQueue<Map.Entry<Node, Double>> pq = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
            Set<Node> visited = new HashSet<>();
            graph.getNodes().values().forEach(node -> dist.put(node, Double.POSITIVE_INFINITY));
            dist.put(startNode, 0.0); pq.add(new AbstractMap.SimpleEntry<>(startNode, 0.0));
            while (!pq.isEmpty()) {
                Node curr = pq.poll().getKey(); if (!visited.add(curr)) continue; if (curr.equals(endNode)) break;
                for (Map.Entry<Node, Double> adj : curr.getAdjacencies().entrySet()) {
                    Node neighbor = adj.getKey(); double edgeW = adj.getValue();
                    if (visited.contains(neighbor)) continue;
                    double newDist = dist.get(curr) + edgeW;
                    if (newDist < dist.get(neighbor)) {
                        dist.put(neighbor, newDist); pred.put(neighbor, curr);
                        pq.add(new AbstractMap.SimpleEntry<>(neighbor, newDist));
                    }
                }
            }
            LinkedList<Node> path = new LinkedList<>(); Node step = endNode; double finalDist = dist.get(endNode);
            if (Double.isInfinite(finalDist) && !startNode.equals(endNode)) return new DijkstraResult(null, Double.POSITIVE_INFINITY);
            path.addFirst(step); while (pred.containsKey(step)) { step = pred.get(step); path.addFirst(step); }
            if (!path.isEmpty() && !path.getFirst().equals(startNode)) return new DijkstraResult(null, Double.POSITIVE_INFINITY); // Path reconstruction sanity check
            return new DijkstraResult(path, finalDist);
        }
    }

    // --- Inner Class: Dijkstra Result Holder (Added hasPath helper) ---
    static class DijkstraResult {
        private final List<Node> path; private final double distance;
        public DijkstraResult(List<Node> path, double distance) {
            this.path = (Double.isFinite(distance) || (path != null && !path.isEmpty())) ? path : null; // Ensure path is null if distance infinite
            this.distance = distance;
        }
        public List<Node> getPath() { return path; }
        public double getDistance() { return distance; }
        public boolean hasPath() { return path != null && !path.isEmpty() && Double.isFinite(distance); }
    }

    // --- Main Method ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Optional: Set Look and Feel for better appearance
            try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                 System.err.println("Couldn't set system look and feel.");
            }
            new FiretruckSimulator().setVisible(true);
        });
    }
}