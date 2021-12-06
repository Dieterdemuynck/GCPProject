import java.util.*;

public class Graph implements GraphInterface{
    private ArrayList<Vertex> vertices;
    private ArrayList<Vertex> validVertices;
    private ArrayList<Vertex> inConflictVertices;
    private int colorCount = 0;
    private HashMap<Integer,Integer> validColoring;

    @Override
    public Collection<Integer> getNodes() {
        /*
         * Returns list of the valid, non-reduced nodes only.
         */
        Collection<Integer> vertexIds = new java.util.ArrayList<>(Collections.emptyList());
        for (Vertex vertex: validVertices){
            vertexIds.add(vertex.getId());
        }
        return vertexIds;
    }

    @Override
    public int getNumberOfEdges() {
        int edgeCount = 0;
        for (Vertex vertex: validVertices){
            edgeCount += vertex.adjacentVertices.size();
        }
        // So far, we counted every edge twice. (A has B in adjacentVertices, but B also has A)
        return edgeCount/2;
    }

    @Override
    public int getNumberOfNodes() {
        return validVertices.size();
    }

    @Override
    public boolean areNeighbors(int u, int v) {
        Vertex vertexU = vertices.get(u);
        Vertex vertexV = vertices.get(v);
        if (vertexU.isReduced() || vertexV.isReduced()){
            throw new IllegalArgumentException("Vertices may not be reduced vertices");
        }
        return vertexU.adjacentVertices.contains(vertexV);
    }

    @Override
    public int getDegree(int u) {
        return vertices.get(u).getDegree();
    }

    @Override
    public void removeNode(int u) {
        /*
         * Fully removes a node from the graph.
         */
        Vertex vertexU = vertices.get(u);
        validVertices.remove(vertexU);
        for (Vertex adjVertex: vertexU.adjacentVertices){
            adjVertex.removeEdge(vertexU);
        }
        vertices.set(u, null);  // I believe this deletes all references to the vertex, which means the garbage collector will delete it
    }

    @Override
    public void removeEdge(int u, int v) {
        /*
         * Fully removes an edge from the graph.
         */
        Vertex vertexU = vertices.get(u);
        Vertex vertexV = vertices.get(v);
        vertexU.removeEdge(vertexV);
        vertexV.removeEdge(vertexU);
    }

    @Override
    public Collection<Integer> getNeighborsOf(int u) {
        Vertex vertex = vertices.get(u);
        Collection<Integer> vertexIds = new java.util.ArrayList<>(Collections.emptyList());
        for (Vertex adjVertex: vertex.adjacentVertices){
            vertexIds.add(adjVertex.getId());
        }
        return vertexIds;
    }

    @Override
    public void applyReduction() {
        if (validVertices == null) {
            validVertices = new ArrayList<>();
            Collections.copy(validVertices, vertices);
        }

        for (int i = 0, verticesSize = vertices.size(); i < verticesSize; i++) {
            Vertex vertex1 = vertices.get(i);

            // Skip any already reduced vertices.
            if (vertex1.getReducedTo() != null){
                continue;
            }
            for (int j = i + 1, size = vertices.size(); j < size; j++) {
                Vertex vertex2 = vertices.get(j);

                // Skip any already reduced vertices.
                if (vertex2.getReducedTo() != null){
                    continue;
                }

                if (vertex1.adjacentVertices.containsAll(vertex2.adjacentVertices)){
                    vertex2.reduceTo(vertex1);  // 2 is subset of 1
                    validVertices.set(j, null);
                } else if (vertex2.adjacentVertices.containsAll(vertex1.adjacentVertices)){
                    vertex1.reduceTo(vertex2);  // 1 is subset of 2
                    validVertices.set(i, null);
                }
            }
        }

        // Remove the remaining null objects from the validVertices list, and sort by degree.
        // "Degree-sorting" will be helpful for applying the DSATUR algorithm.
        // Note: degree must be decreasing.
        validVertices.removeAll(Collections.singleton(null));
        validVertices.sort(new ReverseDegreeComparator());  // yea this should work
    }

    @Override
    public void applyConstructionHeuristic() {
        for (int uncoloredCount = validVertices.size(); uncoloredCount > 0; uncoloredCount--){
            Vertex maxSaturVertex = maximalSaturatedVertex();  // O(n*k) with k the number of connected edges
            boolean[] connectedColors = maxSaturVertex.getConnectedColors(colorCount);  // O(k)
            int minimalColor = colorCount;
            for (int color = 0; color < connectedColors.length; color++){
                if (!connectedColors[color]){
                    minimalColor = color;
                    break;
                }
            }
            if (minimalColor == colorCount){
                colorCount++;
            }
            maxSaturVertex.setColor(minimalColor);
        }

    }

    public Vertex maximalSaturatedVertex(){
        Vertex maxSaturVertex = null;
        int maxSaturation = -1;

        for (Vertex vertex: validVertices){
            if (vertex.getColor() == -1 && vertex.getSaturation(colorCount) > maxSaturation){
                // The vertex is uncolored and, so far, is the vertex with the largest saturation.
                maxSaturVertex = vertex;
            }
        }
        return maxSaturVertex;
    }

    @Override
    public void applyStochasticLocalSearchAlgorithm() {
        int tabooClock = 0;

        // First, we save the current coloring.
        validColoring = new HashMap<>();
        for (Vertex vertex: validVertices){
            validColoring.put(vertex.getId(), vertex.getColor());
        }

        // LOOP 1: as long as time hasn't exceeded
        // "Mess up" the coloring: entirely delete the "last" color (with the largest index),
        // and figure out which vertices are in conflict
        // TODO: mess up coloring

        // LOOP 2: as long as coloring isn't valid
        // Look for vertices which improve the coloring, using the restricted tabu-1-exchange.
        // TODO: improve coloring using Tabu-1-ex

        // END LOOP 2
        // Update validColoring with new coloring

        // END LOOP 1
        // Restore coloring using last validColoring
    }

    @Override
    public int getColor(int u) {
        return vertices.get(u).getColor();
    }

}


class ReverseDegreeComparator implements Comparator<Vertex>{

    @Override
    public int compare(Vertex v0, Vertex v1) {
        return Integer.compare(v1.getDegree(), v0.getDegree());
    }
}
