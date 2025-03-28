# Firetruck Multi-Stop Route Simulator 🚒

Ever wondered how a firetruck might find the quickest route when it needs to make multiple stops? This project brings that scenario to life! It's a Java Swing application that visually demonstrates finding the shortest path between locations on a map (represented as a graph), including any required stops along the way.

It uses Dijkstra's algorithm under the hood to calculate the optimal route, segment by segment, ensuring all specified intermediate stops are visited in order.

![Firetruck Shortest Path Finder](https://github.com/sushmitha978/FIRETRUCK-SHORTEST-PATH/blob/main/screenshot-firetruck-shortest-path-finder.png?raw=true)


## Features ✨

*   **Visual Graph:** Displays a network of locations (nodes) and routes (edges) with distances (weights).
*   **Interactive Selection:** Choose your starting point, final destination, and any number of intermediate stops.
*   **Ordered Multi-Stop Routing:** Calculates the shortest path that visits the selected start, intermediate stops (in the specified order), and end nodes.
*   **Dijkstra's Algorithm:** Leverages the classic algorithm to find the shortest path for each segment of the journey.
*   **Path Visualization:** Clearly highlights the calculated shortest route on the graph in a distinct color (Orange).
*   **Stop Management:** Easily add, remove, or clear the list of intermediate stops.
*   **Route Details:** Displays the sequence of nodes in the calculated path and the total travel distance.
*   **Clear UI:** Provides combo boxes for selection, a list for managing stops, buttons for actions, and a dedicated drawing panel.
*   **Node Highlighting:** Start (Green), End (Magenta), and Intermediate (Cyan) nodes are clearly marked.
*   **Reset Functionality:** Quickly reset all selections and clear the calculated path.

## How It Works 🤔

1.  **The Map:** The application uses a predefined `Graph` structure where locations are `Node` objects (with X/Y coordinates for drawing) and connections are edges with associated `Double` weights representing distance or travel time.
2.  **User Input:** You select the start node, end node, and build a list of intermediate stops using the UI controls.
3.  **Segment Calculation:** When you click "Find Multi-Stop Route", the application breaks the problem down:
    *   Find the shortest path from Start -> Intermediate Stop 1 (using Dijkstra).
    *   Find the shortest path from Intermediate Stop 1 -> Intermediate Stop 2 (using Dijkstra).
    *   ...and so on...
    *   Finally, find the shortest path from the Last Intermediate Stop -> End (using Dijkstra).
4.  **Stitching the Path:** The node sequences from each segment's shortest path are combined (removing duplicate nodes at the connection points) to form the complete multi-stop route.
5.  **Display:** The `DrawingPanel` renders the graph, highlights the selected nodes and the final calculated path, and even places a small firetruck icon at the starting node. The `JTextArea` shows the textual representation of the path and the total distance.

## Getting Started 🚀

1.  **Prerequisites:**
    *   Java Development Kit (JDK) installed (e.g., JDK 11 or later).
    *   An IDE like IntelliJ IDEA, Eclipse, or VS Code (optional, but helpful) OR just the command line.

2.  **Clone the Repository:**
    ```bash
    git clone https://github.com/sushmitha978/FIRETRUCK-SHORTEST-PATH
    cd FIRETRUCK-SHORTEST-PATH
    ```

3.  **Compile and Run:**
    *   **Using an IDE:** Open the project folder in your IDE and run the `FiretruckSimulator.java` file.
    *   **Using Command Line:** Navigate to the directory containing the `.java` file and run:
        ```bash
        # Compile the Java code
        javac FiretruckSimulator.java

        # Run the compiled class
        java FiretruckSimulator
        ```

4.  **Using the Simulator:**
    *   Select a "Start" node from the dropdown.
    *   Select an "End" node.
    *   To add intermediate stops:
        *   Choose a node from the "Intermediate Stops" dropdown.
        *   Click "Add Stop". The stop will appear in the list below, maintaining the order you add them.
    *   Use "Remove Selected" or "Clear All Stops" to manage the intermediate stops list.
    *   Click "Find Multi-Stop Route".
    *   Observe the highlighted path on the graph and the details in the text area below.
    *   Click "Reset All" to start over.

## Code Structure 🏗️

*   **`FiretruckSimulator.java`**: The main class extending `JFrame`. Handles GUI setup, layout, event listeners, and orchestrates the pathfinding process.
    *   **`DrawingPanel` (Inner Class):** Extends `JPanel`. Responsible for all custom drawing logic: nodes, edges, weights, the calculated path, and the firetruck icon.
    *   **`Node` (Static Inner Class):** Represents a location on the graph with an ID, coordinates, and adjacent nodes/weights.
    *   **`Graph` (Static Inner Class):** Holds the collection of `Node` objects and provides methods to add nodes and edges.
    *   **`Dijkstra` (Static Inner Class):** Contains the static `findShortestPath` method implementing Dijkstra's algorithm.
    *   **`DijkstraResult` (Static Inner Class):** A simple container to hold the resulting path (List of `Node`s) and its total distance.
