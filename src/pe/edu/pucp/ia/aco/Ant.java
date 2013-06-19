package pe.edu.pucp.ia.aco;

import java.util.Random;

public class Ant {

	private int currentIndex = 0;
	private int solution[];
	public boolean visited[];

	public Ant(int graphLenght) {
		this.solution = new int[graphLenght];
		this.visited = new boolean[graphLenght];
	}

	public void visitNode(int visitedNode) {
		solution[currentIndex] = visitedNode;
		visited[visitedNode] = true;
		currentIndex++;
	}

	public boolean isNodeVisited(int node) {
		return visited[node];
	}

	public int selectNextNode(double[][] trails, double[][] graph) {
		int nextNode = 0;
		Random random = new Random();
		// Probability Setting from Paper
		double randomValue = random.nextDouble();
		double bestChoiceProbability = ((double) graph.length - 4)
				/ graph.length;
		if (randomValue < bestChoiceProbability) {
			double currentMaximumFeromone = -1;
			for (int i = 0; i < graph.length; i++) {
				double currentFeromone = trails[i][currentIndex];
				if (!isNodeVisited(i)
						&& currentFeromone > currentMaximumFeromone) {
					nextNode = i;
					currentMaximumFeromone = currentFeromone;
				}
			}
			return nextNode;
		} else {
			double probabilities[] = getProbabilities(trails, graph);
			double r = randomValue;
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

	public String getSolutionAsString() {
		String solutionString = new String();
		for (int i = 0; i < solution.length; i++) {
			solutionString = solutionString + " " + solution[i];
		}
		return solutionString;
	}

	public boolean isValidSolution() {
		boolean isValid = true;
		for (int i = 0; i < visited.length; i++) {
			if (visited[i] == false) {
				throw new RuntimeException("Incomplete tour: "
						+ getSolutionAsString());
			}
		}
		return isValid;
	}
}
