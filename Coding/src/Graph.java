import java.util.Collection;

public class Graph implements GraphInterface{
    @Override
    public Collection<Integer> getNodes() {
        return null;
    }

    @Override
    public int getNumberOfEdges() {
        return 0;
    }

    @Override
    public int getNumberOfNodes() {
        return 0;
    }

    @Override
    public boolean areNeighbors(int u, int v) {
        return false;
    }

    @Override
    public int getDegree(int u) {
        return 0;
    }

    @Override
    public void removeNode(int u) {

    }

    @Override
    public void removeEdge(int u, int v) {

    }

    @Override
    public Collection<Integer> getNeighborsOf(int u) {
        return null;
    }

    @Override
    public void applyReduction() {

    }

    @Override
    public void applyConstructionHeuristic() {

    }

    @Override
    public void applyStochasticLocalSearchAlgorithm() {

    }

    @Override
    public int getColor(int u) {
        return 0;
    }
}
