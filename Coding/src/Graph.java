import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class Graph implements GraphInterface{
    private ArrayList<Vertex> vertices;
    private ArrayList<Vertex> validVertices;
    private ArrayList<Vertex> inConflictVertices;

    @Override
    public Collection<Integer> getNodes() {
        /*
         * Returns list of the valid or non-reduced nodes only.
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
        validVertices.removeAll(Collections.singleton(null));
        validVertices.sort(new DegreeComparator());  // yea this should work
    }

    @Override
    public void applyConstructionHeuristic() {
        for (int uncoloredCount = validVertices.size(); uncoloredCount > 0; uncoloredCount--){
            Vertex maxSaturVertex = maximalSaturatedVertex();
        }

    }

    public Vertex maximalSaturatedVertex(){
        // TODO: fix for finding a first vertex
        Vertex maxSaturVertex = null;
        int maxSaturation = 0;
        int currentSaturation;
        for (Vertex vertex: validVertices){
            currentSaturation = vertex.getSaturation();
            if (vertex.getColor() == -1 && currentSaturation > maxSaturation){
                maxSaturVertex = vertex;
            }
        }
        return maxSaturVertex;
    }

    @Override
    public void applyStochasticLocalSearchAlgorithm() {

    }

    @Override
    public int getColor(int u) {
        return vertices.get(u).getColor();
    }

}


class DegreeComparator implements Comparator<Vertex>{

    @Override
    public int compare(Vertex v0, Vertex v1) {
        return Integer.compare(v0.getDegree(), v1.getDegree());
    }
}
