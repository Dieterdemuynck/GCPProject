public class Main {
    public static void main(String[] args) {
        String path = "D:\\UNIDOCS\\1Sem1\\Gegevensstructuren&Algoritmen\\GCPProject\\Coding\\src\\DIMACSGraphs\\";
        Graph graph1 = new Graph(path + "le450_5b.col");

        graph1.applyReduction();

        graph1.applyConstructionHeuristic();
        System.out.println(graph1.getColorCount());

        graph1.applyStochasticLocalSearchAlgorithm();
        System.out.println(graph1.getColorCount());
    }
}
