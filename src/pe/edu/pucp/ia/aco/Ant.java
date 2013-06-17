package pe.edu.pucp.ia.aco;

import java.util.Random;

import pe.edu.pucp.ia.aco.config.ProblemConfiguration;

public class Ant {

	private int currentIndex = 0;
	private int solution[];
	public boolean visited[];

	public Ant(int graphLenght) {
		this.solution = new int[graphLenght];
		this.visited = new boolean[graphLenght];
	}

	public void visitNode(int visitedNode) {
		solution[currentIndex + 1] = visitedNode;
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
			int jobWithMaximumPheromone = 0;
			for (int i = 0; i < graph.length; i++) {
				double currentJobPheromone = trails[i][currentIndex];
				if (currentJobPheromone > jobWithMaximumPheromone) {
					jobWithMaximumPheromone = i;
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

	public int[] getSolution() {
		return solution;
	}

	private double[] getProbabilities(double[][] trails, double[][] graph) {
		double probabilities[] = new double[solution.length];

		double denom = 0.0;
		for (int l = 0; l < trails.length; l++) {
			if (!isNodeVisited(l)) {
				denom += trails[l][currentIndex];

			}
		}

		for (int j = 0; j < solution.length; j++) {
			if (isNodeVisited(j)) {
				probabilities[j] = 0.0;
			} else {
				double numerator = trails[j][currentIndex];
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

	public double getSolutionMakespan(double[][] graph) {
		int machines = graph[0].length;
		double[] machinesTime = new double[machines];
		double tiempo = 0;

		for (int job : solution) {
			for (int i = 0; i < machines; i++) {
				tiempo = graph[job][i];
				// tiempo = graph[job - 1][i];
				if (i == 0) {
					machinesTime[i] = machinesTime[i] + tiempo;
				} else {
					if (machinesTime[i] > machinesTime[i - 1]) {
						machinesTime[i] = machinesTime[i] + tiempo;
					} else {
						machinesTime[i] = machinesTime[i - 1] + tiempo;
					}
				}
			}
		}
		return machinesTime[machines - 1];
	}

	public String solutionAsString() {
		String solutionString = new String();
		for (int i = 1; i < solution.length; i++) {
			solutionString = solutionString + " " + solution[i];
		}
		return solutionString;
	}
}
