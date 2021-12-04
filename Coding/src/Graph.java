import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Graph implements GraphInterface{
    // TODO: Decide to keep this or change to/add a list with ALL vertices for constant search time
    private ArrayList<Vertex> vertices;
    private ArrayList<Vertex> reducedVertices;
    private ArrayList<Vertex> inConflictVertices;

    @Override
    public Collection<Integer> getNodes() {
        /*
         * Returns list of the "viable" or non-reduced nodes only.
         */
        Collection<Integer> vertexIds = new java.util.ArrayList<>(Collections.emptyList());
        for (Vertex vertex: vertices){
            vertexIds.add(vertex.getId());
        }
        return vertexIds;
    }

    @Override
    public int getNumberOfEdges() {
        int edgeCount = 0;
        for (Vertex vertex: vertices){
            edgeCount += vertex.adjacentVertices.size();
        }
        // So far, we counted every edge twice. (A has B in adjacentVertices, but B also has A)
        return edgeCount/2;
    }

    @Override
    public int getNumberOfNodes() {
        return vertices.size() + reducedVertices.size();
    }

    @Override
    public boolean areNeighbors(int u, int v) {
        Vertex vertexU = searchByID(u);
        Vertex vertexV = searchByID(v);
        if (vertexU.isReduced() || vertexV.isReduced()){
            throw new IllegalArgumentException("Vertices may not be reduced");
        }
        return vertexU.adjacentVertices.contains(vertexV);
    }

    @Override
    public int getDegree(int u) {
        return searchByID(u).getDegree();
    }

    @Override
    public void removeNode(int u) {
        /*
         * Fully removes a node from the graph.
         */
        Vertex vertexU = searchByID(u);
        vertices.remove(vertexU);
        for (Vertex adjVertex: vertexU.adjacentVertices){
            adjVertex.removeEdge(adjVertex);
        }
    }

    @Override
    public void removeEdge(int u, int v) {
        /*
         * Fully removes an edge from the graph.
         */
        Vertex vertexU = searchByID(u);
        Vertex vertexV = searchByID(v);
        vertexU.removeEdge(vertexV);
        vertexV.removeEdge(vertexU);
    }

    @Override
    public Collection<Integer> getNeighborsOf(int u) {
        Vertex vertex = searchByID(u);
        Collection<Integer> vertexIds = new java.util.ArrayList<>(Collections.emptyList());
        for (Vertex adjVertex: vertex.adjacentVertices){
            vertexIds.add(adjVertex.getId());
        }
        return vertexIds;
    }

    @Override
    public void applyReduction() {
        if (reducedVertices == null) {
            reducedVertices = new ArrayList<>();
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
                    reducedVertices.add(vertex2);
                } else if (vertex2.adjacentVertices.containsAll(vertex1.adjacentVertices)){
                    vertex1.reduceTo(vertex2);  // 1 is subset of 2
                    reducedVertices.add(vertex1);
                }
            }
        }

        // Remove the reduced vertices from the "main" graph
        vertices.removeAll(reducedVertices);
        reducedVertices.sort();  // TODO: understand Comparator objects and sort vertices based on IDs.
    }

    @Override
    public void applyConstructionHeuristic() {
        // TODO: decide wether to sort on degree before applying DSATUR
        // That should speed up the algorithm, but would make searchByIndex not work.
        int uncoloredCount = vertices.size();

    }

    public Vertex maximalSaturatedVertex(){
        for (Vertex vertex: vertices){

        }
        return null;
    }

    @Override
    public void applyStochasticLocalSearchAlgorithm() {

    }

    @Override
    public int getColor(int u) {
        return searchByID(u).getColor();
    }

    public Vertex searchByID(int id){
        Vertex vertex = searchByID(id, vertices);
        if (vertex == null){  // Vertex hasn't been found yet, perhaps it is part of the reduced vertices
            return searchByID(id, reducedVertices);
        }
        return vertex;
    }

    public Vertex searchByID(int id, ArrayList<Vertex> vertexCollection){
        int leftMost = 0;
        int rightMost = vertices.size() - 1;
        int center = rightMost/2;
        while (leftMost <= rightMost) {
            if (vertexCollection.get(center).getId() < id) {
                leftMost = center + 1;
            } else if (vertexCollection.get(center).getId() > id) {
                rightMost = center - 1;
            } else {
                return vertexCollection.get(center);  // found the element
            }
            center = (leftMost + rightMost) / 2;
        }
        return null;
    }
}
