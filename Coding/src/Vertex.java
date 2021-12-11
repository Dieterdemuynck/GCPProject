import java.util.*;

public class Vertex{
    private final int id;
    Collection<Vertex> adjacentVertices = new java.util.ArrayList<>(Collections.emptyList());
    private Vertex reducedTo = null;
    private int color = -1;
    private int tabooTimer = 0;
    private int conflictCount = 0;

    public Vertex(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public int getDegree(){
        return adjacentVertices.size();
    }

    public int getSaturation(int colorCount){
        BitSet connectedColors = new BitSet(colorCount);  // Default all false
        int saturation = 0;
        for (Vertex vertex: adjacentVertices){
            if (!connectedColors.get(vertex.getColor())){
                // The vertex' color has not yet been found. Saturation can increase.
                connectedColors.flip(vertex.getColor());
                saturation++;
            }
        }
        return saturation;
    }

    public boolean[] getConnectedColors(int colorCount){
        boolean[] connectedColors = new boolean[colorCount];  // Default all false
        for (Vertex vertex: adjacentVertices){
            if (!connectedColors[vertex.getColor()]){
                // The vertex' color has not yet been found. Saturation can increase.
                connectedColors[vertex.getColor()] = true;
            }
        }
        return connectedColors;
    }

    public void addEdge(Vertex vertex){
        this.adjacentVertices.add(vertex);
    }

    public void setTabooTimer(int time, int A, int delta, int conflictCount){
        Random r = new Random();
        tabooTimer = time + r.nextInt(A+1) + delta * conflictCount;  // This *should* give a random integer
    }

    public int getTabooTimer(){
        return tabooTimer;
    }

    public void setColor(int color){
        this.color = color;
    }

    public int changeColor(int color){  // O(|E|)
        // specifically designed when changing from a feasible color, to a potentially infeasible color
        // This function is also designed to return the net amount of infeasible edges, if desired.
        int netInfeasibleEdges = 0;
        assert color != this.color;
        for (Vertex vertex: adjacentVertices){
            if (vertex.getColor() == color){
                // The new color causes a conflict.
                increaseConflictCount();
                vertex.increaseConflictCount();
                netInfeasibleEdges++;
            } else if (vertex.getColor() == this.color){
                // The old color caused a conflict. Changing the color will remove this conflict.
                decreaseConflictCount();
                vertex.decreaseConflictCount();
                netInfeasibleEdges--;
            }
        }
        this.color = color;
        return netInfeasibleEdges;
    }

    public int calculateNetInfeasibleEdgeCount(int color){  // O(|E|)
        // Calculates the amount of net infeasible edges after changing to the specified color
        // specifically designed when changing from a feasible color, to a potentially infeasible color
        // This function is also designed to return the net amount of infeasible edges, if desired.
        int netInfeasibleEdges = 0;
        for (Vertex vertex: adjacentVertices){
            if (vertex.getColor() == color){
                netInfeasibleEdges++;
            } else if (vertex.getColor() == this.color){
                // The old color caused a conflict. Changing the color will remove this conflict.
                netInfeasibleEdges--;
            }
        }
        return netInfeasibleEdges;
    }

    public void increaseConflictCount(){
        conflictCount++;
    }

    public void decreaseConflictCount(){
        assert conflictCount > 0;
        conflictCount--;
    }

    public boolean isInConflict(){
        return conflictCount == 0;
    }

    public int getConflictCount(){
        return conflictCount;
    }

    public int getColor(){
        if (reducedTo == null){
            return color;
        } else {
            return reducedTo.getColor();
        }
    }

    public void reduceTo(Vertex vertex){
        reducedTo = vertex;
        // We only remove the edge *TO* this vertex, allowing us to rebuild the original graph
        for (Vertex adjVertex: adjacentVertices){
            adjVertex.removeEdge(vertex);
        }
    }

    public Vertex getReducedTo(){
        return reducedTo;
    }

    public boolean isReduced(){
        return reducedTo != null;
    }

    public void removeEdge(Vertex vertex){
        adjacentVertices.remove(vertex);
    }
}
