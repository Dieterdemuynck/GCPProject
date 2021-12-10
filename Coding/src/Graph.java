import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Graph implements GraphInterface{
    private Vertex[] vertices;
    private ArrayList<Vertex> validVertices;
    private ArrayList<Vertex> inConflictVertices;
    private int colorCount = 0;

    public Graph(String fileLocation){
        try {
            Scanner scanner = new Scanner(new File(fileLocation));
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if (input.charAt(0) == 'e') {  // e node1 node2
                    String[] vertexIdStrings = input.split(" ");
                    assert vertices != null;  // IntelliJ recommended this? what it does? No one knows?

                    // in the DIMACS notation, vertex ids starts at 1. Here, we start at 0.
                    Vertex vertex1 = vertices[Integer.parseInt(vertexIdStrings[1]) - 1];
                    Vertex vertex2 = vertices[Integer.parseInt(vertexIdStrings[2]) - 1];
                    vertex1.addConnection(vertex2);
                    vertex2.addConnection(vertex1);

                } else if (input.charAt(0) == 'p'){
                    // create vertices:
                    int vertexCount = Integer.parseInt(input.split(" ")[2]);  // p edges nodeAmount edgeAmount
                    vertices = new Vertex[vertexCount];

                    for (int i = 0; i < vertexCount; i++){
                        vertices[i] = new Vertex(i);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

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
        Vertex vertexU = vertices[u];
        Vertex vertexV = vertices[v];
        if (vertexU.isReduced() || vertexV.isReduced()){
            throw new IllegalArgumentException("Vertices may not be reduced vertices");
        }
        return vertexU.adjacentVertices.contains(vertexV);
    }

    @Override
    public int getDegree(int u) {
        return vertices[u].getDegree();
    }

    @Override
    public void removeNode(int u) {
        /*
         * Fully removes a node from the graph.
         */
        Vertex vertexU = vertices[u];
        validVertices.remove(vertexU);
        for (Vertex adjVertex: vertexU.adjacentVertices){
            adjVertex.removeEdge(vertexU);
        }
        vertices[u] = null;  // I believe this deletes all references to the vertex, which means the garbage collector will delete it
    }

    @Override
    public void removeEdge(int u, int v) {
        /*
         * Fully removes an edge from the graph.
         */
        Vertex vertexU = vertices[u];
        Vertex vertexV = vertices[v];
        vertexU.removeEdge(vertexV);
        vertexV.removeEdge(vertexU);
    }

    @Override
    public Collection<Integer> getNeighborsOf(int u) {
        Vertex vertex = vertices[u];
        Collection<Integer> vertexIds = new java.util.ArrayList<>(Collections.emptyList());
        for (Vertex adjVertex: vertex.adjacentVertices){
            vertexIds.add(adjVertex.getId());
        }
        return vertexIds;
    }

    @Override
    public void applyReduction() {
        if (validVertices == null) {
            validVertices = new ArrayList<>(Arrays.asList(vertices));
            // TODO: This may not make a copy, but rather just wrap the original.
            // According to some guy on StackOverflow, the "new ArrayList..." iterates over the elements,
            // properly creating a copy of the array.
        }

        for (int i = 0, verticesSize = vertices.length; i < verticesSize; i++) {
            Vertex vertex1 = vertices[i];

            // Skip any already reduced vertices.
            if (vertex1.getReducedTo() != null){
                continue;
            }
            for (int j = i + 1, size = vertices.length; j < size; j++) {
                Vertex vertex2 = vertices[j];

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
                colorCount++;  // A new color has been added, colorCount must increase.
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
        HashMap<Integer,Integer> validColoring = new HashMap<>();
        for (Vertex vertex: validVertices){
            validColoring.put(vertex.getId(), vertex.getColor());
        }

        // LOOP 1: loop as long as we can
        // "Mess up" the coloring: entirely delete the "last" color (with the largest index),
        // and figure out which vertices are in conflict
        // TODO: mess up coloring
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < 10000) {  // TODO: replace with true, move to loop 2
            Random r = new Random();  // object for calculating a random integer

            // Messing up the current coloring by removing the last color:
            for (Vertex vertex: validVertices){
                if (vertex.getColor() == colorCount - 1){
                    vertex.setColor(r.nextInt(colorCount - 1));
                    // TODO: Reconsider "inConflict" array: possibility for double vertex addition.
                    // Perhaps this can be optimized using a bitset?
                    boolean newConflict = false;
                    for (Vertex adjVertex: vertex.adjacentVertices){
                        if (adjVertex.getColor() == vertex.getColor()){
                            inConflictVertices.add(adjVertex);
                            newConflict = true;
                        }
                    }
                    if (newConflict){
                        inConflictVertices.add(vertex);
                    }
                }
            }

            // LOOP 2: as long as time hasn't exceeded
            // Look for vertices which improve the coloring, using the restricted tabu-1-exchange.
            // TODO: improve coloring using Tabu-1-ex
            // TODO: find good value for elapsed time check

            // END LOOP 2
            // Update validColoring with new coloring

        }
        // END LOOP 1
        // Restore coloring using last validColoring
        for (Vertex vertex: validVertices){
            vertex.setColor(validColoring.get(vertex.getId()));
        }
    }



    @Override
    public int getColor(int u) {
        return vertices[u].getColor();
    }

}


class ReverseDegreeComparator implements Comparator<Vertex>{

    @Override
    public int compare(Vertex v0, Vertex v1) {
        return Integer.compare(v1.getDegree(), v0.getDegree());
    }
}
