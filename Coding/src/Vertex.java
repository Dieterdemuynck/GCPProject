import java.util.*;

public class Vertex{
    private final int id;
    Collection<Vertex> adjacentVertices = new java.util.ArrayList<>(Collections.emptyList());
    private Vertex reducedTo = null;
    private int color = -1;
    private int tabooTimer = 0;

    public Vertex(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public int getDegree(){
        return adjacentVertices.size();
    }

    public int getSaturation(){
        // TODO: using the known colorCount, we could optimize the .contains to a constant search time.
        List<Integer> connectedColors = new ArrayList<>();
        int saturation = 0;
        for (Vertex vertex: adjacentVertices){
            if (connectedColors.contains(vertex.getColor())){
                saturation++;
            } else {
                connectedColors.add(vertex.getColor());
            }
        }
        return saturation;
    }

    public void addVertices(Collection<Vertex> adjacentVertices){
        this.adjacentVertices = adjacentVertices;
    }

    public void setTabooTimer(int time, int A, int delta, int conflictCount){
        Random r = new Random();
        tabooTimer = time + r.nextInt(A + 1) + delta * conflictCount;  // This *should* give a random integer
    }

    public int getTabooTimer(){
        return tabooTimer;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
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
