package graph;

import datastructures.CustomList;
import datastructures.CustomQueue;

/**
 * Edmonds-Karp Algorithm for Maximum Flow (BFS implementation of Ford-Fulkerson).
 *
 * Concepts:
 * - Flow Network: A directed graph where each edge has a non-negative capacity.
 * - Source (s):   Node where flow originates.
 * - Sink (t):     Node where flow terminates.
 * - Residual Graph: Represents remaining available capacities. For flow f on edge (u, v),
 *                   residual capacity is c(u, v) - f(u, v), and backward edge has capacity f(u, v).
 * - Augmenting Path: A directed path from source to sink in the residual graph with capacity > 0.
 *
 * How Edmonds-Karp Works:
 * 1. Initialize maximum flow = 0 and residual graph equal to initial capacities.
 * 2. While an augmenting path exists from source to sink (found using BFS):
 *    a. Find bottleneck capacity: delta = min residual capacity along path.
 *    b. Augment flow: For each edge (u, v) on path:
 *       - residual[u][v] -= delta
 *       - residual[v][u] += delta (reverse edge)
 *    c. maxFlow += delta
 * 3. When no augmenting path remains, maxFlow is optimal (Max-Flow Min-Cut Theorem).
 *
 * Time Complexity:  O(V * E^2)
 * Space Complexity: O(V^2) for capacity and residual matrices.
 */
public class MaxFlow {

    public static class FlowStep {
        public final String augmentingPath;
        public final int bottleneckCapacity;
        public final int cumulativeFlow;

        public FlowStep(String augmentingPath, int bottleneckCapacity, int cumulativeFlow) {
            this.augmentingPath = augmentingPath;
            this.bottleneckCapacity = bottleneckCapacity;
            this.cumulativeFlow = cumulativeFlow;
        }
    }

    public static class MaxFlowResult {
        public final int maxFlow;
        public final int[][] initialCapacity;
        public final int[][] residualGraph;
        public final CustomList<FlowStep> steps;
        public final String[] vertexNames;

        public MaxFlowResult(int maxFlow, int[][] initialCapacity, int[][] residualGraph,
                             CustomList<FlowStep> steps, String[] vertexNames) {
            this.maxFlow = maxFlow;
            this.initialCapacity = initialCapacity;
            this.residualGraph = residualGraph;
            this.steps = steps;
            this.vertexNames = vertexNames;
        }

        public String getDetailedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("EDMONDS-KARP MAXIMUM FLOW REPORT\n");
            sb.append("==================================================\n");
            sb.append("Source: ").append(vertexNames[0]).append(", Sink: ")
              .append(vertexNames[vertexNames.length - 1]).append("\n");
            sb.append("Total Augmenting Paths Found: ").append(steps.size()).append("\n\n");

            for (int i = 0; i < steps.size(); i++) {
                FlowStep s = steps.get(i);
                sb.append(String.format("Step %d: Path [%s] -> Bottleneck Flow = %d (Total so far: %d)\n",
                        (i + 1), s.augmentingPath, s.bottleneckCapacity, s.cumulativeFlow));
            }

            sb.append("--------------------------------------------------\n");
            sb.append("MAXIMUM FLOW: ").append(maxFlow).append("\n");
            sb.append("==================================================\n");
            return sb.toString();
        }
    }

    /**
     * Solves Maximum Flow from source (s) to sink (t) using Edmonds-Karp.
     *
     * @param capacity Matrix of capacities between vertices
     * @param s        Source vertex index
     * @param t        Sink vertex index
     * @param names    Optional vertex labels
     * @return MaxFlowResult containing max flow and detailed step breakdown
     */
    public static MaxFlowResult computeMaxFlow(int[][] capacity, int s, int t, String[] names) {
        int V = capacity.length;
        int[][] residual = new int[V][V];
        int[][] initial = new int[V][V];

        for (int u = 0; u < V; u++) {
            for (int v = 0; v < V; v++) {
                residual[u][v] = capacity[u][v];
                initial[u][v] = capacity[u][v];
            }
        }

        if (names == null || names.length != V) {
            names = new String[V];
            for (int i = 0; i < V; i++) names[i] = "V" + i;
        }

        int[] parent = new int[V];
        int maxFlow = 0;
        CustomList<FlowStep> steps = new CustomList<>();

        // Augment flow while path exists from s to t in residual graph
        while (bfsFindPath(residual, s, t, parent, V)) {
            // Find bottleneck capacity along the BFS path
            int pathFlow = Integer.MAX_VALUE;
            int curr = t;
            while (curr != s) {
                int prev = parent[curr];
                if (residual[prev][curr] < pathFlow) {
                    pathFlow = residual[prev][curr];
                }
                curr = prev;
            }

            // Update residual capacities along the augmenting path
            curr = t;
            StringBuilder pathStr = new StringBuilder();
            while (curr != s) {
                int prev = parent[curr];
                residual[prev][curr] -= pathFlow;
                residual[curr][prev] += pathFlow; // Add reverse edge flow
                if (pathStr.length() > 0) {
                    pathStr.insert(0, " -> ");
                }
                pathStr.insert(0, names[curr]);
                curr = prev;
            }
            pathStr.insert(0, names[s] + " -> ");

            maxFlow += pathFlow;
            steps.add(new FlowStep(pathStr.toString(), pathFlow, maxFlow));
        }

        return new MaxFlowResult(maxFlow, initial, residual, steps, names);
    }

    /**
     * Standard BFS to find if an augmenting path exists from source to sink in residual network.
     */
    private static boolean bfsFindPath(int[][] residual, int s, int t, int[] parent, int V) {
        boolean[] visited = new boolean[V];
        for (int i = 0; i < V; i++) {
            visited[i] = false;
            parent[i] = -1;
        }

        CustomQueue<Integer> queue = new CustomQueue<>();
        queue.enqueue(s);
        visited[s] = true;

        while (!queue.isEmpty()) {
            int u = queue.dequeue();

            for (int v = 0; v < V; v++) {
                if (!visited[v] && residual[u][v] > 0) {
                    parent[v] = u;
                    visited[v] = true;
                    if (v == t) {
                        return true; // Path to sink found
                    }
                    queue.enqueue(v);
                }
            }
        }

        return visited[t];
    }
}
