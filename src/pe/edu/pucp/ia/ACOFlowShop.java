package pe.edu.pucp.ia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import pe.edu.pucp.ia.aco.Ant;
import pe.edu.pucp.ia.config.ProblemConfiguration;

public class ACOFlowShop {

	private double[][] graph;
	private double pheromoneTrails[][] = null;
	private Ant antColony[] = null;

	private int numberOfNodes;
	private int numberOfAnts;

	public int[] bestTour;
	String bestTourAsString = "";
	public double bestTourLength;

	public ACOFlowShop(double[][] graph) {
		this.numberOfNodes = graph.length;
		this.numberOfAnts = (int) (graph.length * ProblemConfiguration.NUM_ANT_FACTOR);
		this.graph = graph;
		this.pheromoneTrails = new double[numberOfNodes][numberOfNodes];
		this.antColony = new Ant[numberOfAnts];
		for (int j = 0; j < antColony.length; j++) {
			antColony[j] = new Ant(numberOfNodes);
		}
	}

	private void updatePheromoneTrails() {
		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {
				pheromoneTrails[i][j] *= ProblemConfiguration.EVAPORATION;
			}
		}

		for (Ant ant : antColony) {
			double contribution = ProblemConfiguration.Q
					/ ant.getTourLength(graph);
			for (int i = 0; i < numberOfNodes - 1; i++) {
				pheromoneTrails[ant.getTour()[i]][ant.getTour()[i + 1]] += contribution;
			}
			pheromoneTrails[ant.getTour()[numberOfNodes - 1]][ant.getTour()[0]] += contribution;
		}
	}

	public static void main(String... args) {
		try {
			double[][] graph = getProblemGraphFromFile("/home/cptanalatriste/github/ACOFlowShop/src/tspadata1.txt");
			ACOFlowShop acoFlowShop = new ACOFlowShop(graph);
			acoFlowShop.solveProblem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildSolutions() {
		for (Ant ant : antColony) {
			while (ant.getCurrentIndex() < numberOfNodes - 1) {
				int nextNode = ant.selectNextNode(pheromoneTrails, graph);
				ant.visitNode(nextNode);
			}
		}
	}

	private void initialize() {
		Random random = new Random();
		for (Ant ant : antColony) {
			ant.setCurrentIndex(-1);
			ant.clear();
			ant.visitNode(random.nextInt(numberOfNodes));
			ant.setCurrentIndex(0);
		}
	}

	private Ant updateBestAnt() {
		Ant bestAnt = antColony[0];
		for (Ant ant : antColony) {
			if (ant.getTourLength(graph) < bestAnt.getTourLength(graph)) {
				bestAnt = ant;
			}
		}
		bestTour = bestAnt.getTour().clone();
		bestTourLength = bestAnt.getTourLength(graph);
		bestTourAsString = bestAnt.tourToString();
		return bestAnt;
	}

	public int[] solveProblem() {
		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {
				pheromoneTrails[i][j] = ProblemConfiguration.INITIAL_PHEROMONE;

			}
		}

		int iteration = 0;
		while (iteration < ProblemConfiguration.MAX_ITERATIONS) {
			initialize();
			buildSolutions();
			updatePheromoneTrails();
			updateBestAnt();
			iteration++;
		}
		System.out.println("Best tour length: "
				+ (bestTourLength - numberOfNodes));
		System.out.println("Best tour:" + bestTourAsString);
		return bestTour.clone();
	}

	public static double[][] getProblemGraphFromFile(String path)
			throws IOException {
		double graph[][] = null;
		FileReader fr = new FileReader(path);
		BufferedReader buf = new BufferedReader(fr);
		String line;
		int i = 0;

		while ((line = buf.readLine()) != null) {
			String splitA[] = line.split(" ");
			LinkedList<String> split = new LinkedList<String>();
			for (String s : splitA) {
				if (!s.isEmpty()) {
					split.add(s);
				}
			}

			if (graph == null) {
				graph = new double[split.size()][split.size()];
			}
			int j = 0;

			for (String s : split) {
				if (!s.isEmpty()) {
					graph[i][j++] = Double.parseDouble(s) + 1;
				}
			}
			i++;
		}
		return graph;
	}
}
