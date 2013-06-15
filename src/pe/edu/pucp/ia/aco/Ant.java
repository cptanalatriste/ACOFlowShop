package pe.edu.pucp.ia.aco;

import java.util.Random;

import pe.edu.pucp.ia.config.ProblemConfiguration;

public class Ant {

	private int currentIndex = 0;
	private int tour[];
	public boolean visited[];

	public Ant(int graphLenght) {
		this.tour = new int[graphLenght];
		this.visited = new boolean[graphLenght];
	}

	public void visitNode(int visitedNode) {
		tour[currentIndex + 1] = visitedNode;
		visited[visitedNode] = true;
		currentIndex++;
	}

	public boolean isNodeVisited(int node) {
		return visited[node];
	}

	public int selectNextNode(double[][] trails, double[][] graph) {
		int nextNode = 0;
		Random random = new Random();
		if (random.nextDouble() < ProblemConfiguration.RANDOM_SELECTION_PROBABILITY) {
			int t = random.nextInt(graph.length - currentIndex);
			int j = -1;
			for (int i = 0; i < graph.length; i++) {
				if (!isNodeVisited(i)) {
					j++;
				}
				if (j == t) {
					nextNode = i;
					return nextNode;
				}
			}
		} else {
			double probabilities[] = getProbabilities(trails, graph);
			double r = random.nextDouble();
			double total = 0;
			for (int i = 0; i < graph.length; i++) {
				total += probabilities[i];
				if (total >= r) {
					nextNode = i;
					return nextNode;
				}
			}
		}
		return nextNode;
	}

	public int[] getTour() {
		return tour;
	}

	private double[] getProbabilities(double[][] trails, double[][] graph) {
		double probabilities[] = new double[tour.length];
		int currentNode = tour[currentIndex];

		double denom = 0.0;
		for (int l = 0; l < graph.length; l++) {
			if (!isNodeVisited(l)) {
				denom += Math.pow(trails[currentNode][l],
						ProblemConfiguration.ALPHA)
						* Math.pow(1.0 / graph[currentNode][l],
								ProblemConfiguration.BETA);

			}
		}

		for (int j = 0; j < tour.length; j++) {
			if (isNodeVisited(j)) {
				probabilities[j] = 0.0;
			} else {
				double numerator = Math.pow(trails[currentNode][j],
						ProblemConfiguration.ALPHA)
						* Math.pow(1.0 / graph[currentNode][j],
								ProblemConfiguration.BETA);
				probabilities[j] = numerator / denom;
			}
		}
		return probabilities;
	}

	public void clear() {
		for (int i = 0; i < visited.length; i++) {
			visited[i] = false;

		}
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public double getTourLength(double[][] graph) {
		double length = graph[tour[graph.length - 1]][tour[0]];
		for (int i = 0; i < graph.length - 1; i++) {
			length += graph[tour[i]][tour[i + 1]];
		}
		return length;
	}

	public String tourToString() {
		String t = new String();
		for (int i : tour)
			t = t + " " + i;
		return t;
	}
}
