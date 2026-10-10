package graph;

import datastructures.CustomList;

/**
 * 2-Approximation Algorithm for Minimum Vertex Cover.
 *
 * What is Vertex Cover?
 * - A vertex cover of an undirected graph G = (V, E) is a subset of vertices V'
 *   such that every edge in E has at least one endpoint in V'.
 * - Finding the Minimum Vertex Cover is NP-Hard!
 *
 * How the 2-Approximation Works (Maximal Matching Approach):
 * 1. Initialize Vertex Cover set C = empty.
 * 2. While there are uncovered edges in E:
 *    a. Pick an arbitrary uncovered edge (u, v).
 *    b. Add BOTH u and v to C.
 *    c. Mark all edges incident to either u or v as covered (remove them from consideration).
 * 3. Return C.
 *
 * Approximation Ratio Proof (Why 2-approximation?):
 * - The set of edges M selected by the algorithm forms a "matching" (no two edges share a vertex).
 * - Any valid vertex cover (including the optimal cover C*) MUST pick at least one vertex
 *   for each edge in M to cover that edge.
 * - Therefore: |C*| >= |M|.
 * - The algorithm puts both endpoints of every edge in M into C: |C| = 2 * |M|.
 * - Combining these: |C| = 2 * |M| <= 2 * |C*|.
 * - Hence, |C| <= 2 * OPT!
 *
 * Time Complexity:  O(V + E)
 * Space Complexity: O(V + E)
 */
public class VertexCoverApproximation {

    public static class Edge {
        public final int u;
        public final int v;

        public Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }

        @Override
        public String toString() {
            return "(" + u + " - " + v + ")";
        }
    }

    public static class VertexCoverResult {
        public final CustomList<Integer> vertexCover;
        public final CustomList<Edge> maximalMatchingEdges;
        public final int totalVertices;
        public final int totalEdges;
        public final String[] vertexNames;

        public VertexCoverResult(CustomList<Integer> vertexCover,
                                 CustomList<Edge> maximalMatchingEdges,
                                 int totalVertices, int totalEdges,
                                 String[] vertexNames) {
            this.vertexCover = vertexCover;
            this.maximalMatchingEdges = maximalMatchingEdges;
            this.totalVertices = totalVertices;
            this.totalEdges = totalEdges;
            this.vertexNames = vertexNames;
        }

        public String getReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("VERTEX COVER 2-APPROXIMATION REPORT\n");
            sb.append("==================================================\n");
            sb.append("Total Graph Vertices: ").append(totalVertices).append("\n");
            sb.append("Total Graph Edges:    ").append(totalEdges).append("\n\n");

            sb.append("1. Selected Maximal Matching Edges (M):\n");
            for (int i = 0; i < maximalMatchingEdges.size(); i++) {
                Edge e = maximalMatchingEdges.get(i);
                sb.append("   Edge ").append(i + 1).append(": ")
                  .append(vertexNames[e.u]).append(" -- ").append(vertexNames[e.v]).append("\n");
            }

            sb.append("\n2. Constructed Vertex Cover (C):\n   Vertices: { ");
            for (int i = 0; i < vertexCover.size(); i++) {
                sb.append(vertexNames[vertexCover.get(i)])
                  .append(i < vertexCover.size() - 1 ? ", " : " ");
            }
            sb.append("}\n");
            sb.append("   Cover Size: ").append(vertexCover.size()).append(" vertices\n\n");

            sb.append("3. Theoretical Guarantee:\n");
            sb.append("   - Size of Matching |M| = ").append(maximalMatchingEdges.size()).append("\n");
            sb.append("   - Lower bound on Optimal Cover |OPT| >= ").append(maximalMatchingEdges.size()).append("\n");
            sb.append("   - Approximation Size |C| = 2 * |M| = ").append(vertexCover.size()).append("\n");
            sb.append("   - Proved Bound: |C| <= 2 * |OPT| (Guaranteed 2-Approximation)\n");
            sb.append("==================================================\n");
            return sb.toString();
        }
    }

    /**
     * Finds a 2-approximate vertex cover using maximal matching.
     */
    public static VertexCoverResult computeVertexCover(int numVertices, CustomList<Edge> edges, String[] names) {
        if (names == null || names.length != numVertices) {
            names = new String[numVertices];
            for (int i = 0; i < numVertices; i++) names[i] = "V" + i;
        }

        boolean[] inCover = new boolean[numVertices];
        CustomList<Integer> cover = new CustomList<>();
        CustomList<Edge> matching = new CustomList<>();

        // Iterate over all edges
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            int u = e.u;
            int v = e.v;

            // If neither endpoint is already in the cover, this edge is uncovered
            if (!inCover[u] && !inCover[v]) {
                // Add both endpoints to the cover
                inCover[u] = true;
                inCover[v] = true;
                cover.add(u);
                cover.add(v);

                // This edge is part of our maximal matching
                matching.add(e);
            }
        }

        return new VertexCoverResult(cover, matching, numVertices, edges.size(), names);
    }
}
