package pe.edu.pucp.ia.aco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pe.edu.pucp.ia.flowshop.util.FlowShopUtils;

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
		return FlowShopUtils.getScheduleMakespan(solution, graph);
	}

	public void improveSolution(double[][] graph) {
		double makespan = getSolutionMakespan(graph);
                
                System.out.println(getSolutionAsString());
                System.out.println("makeSpan:"+makespan);
                
		int[] localSolutionJobs = new int[solution.length];  //solucion local a calcular
		List<Integer> jobsList = new ArrayList<Integer>();   //lista de jobs inicial

		for (int job : solution) {
			jobsList.add(job);
		}

		List<Integer> localSolution = jobsList;   //solucion local donde se quitan y agregan trabajos

		int indexI = 0;
		boolean lessMakespan = true;

		while (indexI < (solution.length) && lessMakespan) {
                        localSolution = jobsList;
                        System.out.println("indexI:"+indexI);
			int jobI = localSolution.get(indexI);
			localSolution.remove(indexI);
			int indexJ = 0;
                        System.out.println("indexJ:"+indexJ);
			while (indexJ < solution.length && lessMakespan) {
				localSolution.add(indexJ, jobI);
				
                                int[] intermediateSolution = new int[solution.length];
                                int t=0;
                                for(int sol:localSolution){
                                    intermediateSolution[t] = sol;
                                    t++;
                                }
                                
                                System.out.println("intermediateSolution.length:"+intermediateSolution.length);
                                
                                for(int o=0;o<intermediateSolution.length;o++){
                                    System.out.print("|"+intermediateSolution[o]+"|");
                                }
                                
				double newMakespan = FlowShopUtils.getScheduleMakespan(intermediateSolution, graph);
                                
                                System.out.println("newMakespan:"+newMakespan);

				if (newMakespan < makespan) {
					makespan = newMakespan;
					lessMakespan = false;
				} else {
					localSolution.remove(indexJ);
				}
                                
                                indexJ++;
			}
			indexI++;
		}

		int k = 0;
		for (int job : localSolution) {
			localSolutionJobs[k] = job;
			k++;
		}
		solution = localSolutionJobs;
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