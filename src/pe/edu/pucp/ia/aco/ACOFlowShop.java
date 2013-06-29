package pe.edu.pucp.ia.aco;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pe.edu.pucp.ia.aco.config.ProblemConfiguration;

public class ACOFlowShop {

	private double[][] graph;
	private double pheromoneTrails[][] = null;
	private Ant antColony[] = null;

	private int numberOfJobs;
	private int numberOfAnts;

	public int[] bestTour;
	String bestTourAsString = "";
	public double bestTourLength = -1.0;

	public ACOFlowShop(double[][] graph) {
		this.numberOfJobs = graph.length;
		this.numberOfAnts = ProblemConfiguration.NUMBER_OF_ANTS;
		this.graph = graph;
		this.pheromoneTrails = new double[numberOfJobs][numberOfJobs];
		this.antColony = new Ant[numberOfAnts];
		for (int j = 0; j < antColony.length; j++) {
			antColony[j] = new Ant(numberOfJobs);
		}
	}

	public int[] solveProblem() {
		for (int i = 0; i < numberOfJobs; i++) {
			for (int j = 0; j < numberOfJobs; j++) {
				// TODO (cgavidia): Apply MAX-MIN policy for initial pheromone
				pheromoneTrails[i][j] = ProblemConfiguration.MAXIMUM_PHEROMONE;
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
		System.out.println("Best tour length: " + bestTourLength);
		System.out.println("Best tour:" + bestTourAsString);
		return bestTour.clone();
	}

	private void updatePheromoneTrails() {
		for (int i = 0; i < numberOfJobs; i++) {
			for (int j = 0; j < numberOfJobs; j++) {
				double newValue = pheromoneTrails[i][j]
						* ProblemConfiguration.EVAPORATION;
				if (newValue >= ProblemConfiguration.MINIMUM_PHEROMONE) {
					pheromoneTrails[i][j] = newValue;
				} else {
					pheromoneTrails[i][j] = ProblemConfiguration.MINIMUM_PHEROMONE;
				}
			}
		}

		Ant bestAnt = getBestAnt();
		double contribution = ProblemConfiguration.Q
				/ getSolutionMakespan(bestAnt.getSolution(), graph);
		for (int i = 0; i < numberOfJobs; i++) {
			double newValue = pheromoneTrails[bestAnt.getSolution()[i]][i]
					+ contribution;
			if (newValue <= ProblemConfiguration.MAXIMUM_PHEROMONE) {
				pheromoneTrails[bestAnt.getSolution()[i]][i] = newValue;
			} else {
				pheromoneTrails[bestAnt.getSolution()[i]][i] = ProblemConfiguration.MAXIMUM_PHEROMONE;
			}
		}
	}

	private void buildSolutions() {
		for (Ant ant : antColony) {
			while (ant.getCurrentIndex() < numberOfJobs) {
				int nextNode = ant.selectNextNode(pheromoneTrails, graph);
				ant.visitNode(nextNode);
			}
			if (ant.isValidSolution()) {
				System.out.println("ant.solutionAsString(): "
						+ ant.getSolutionAsString());
			}
			ant.setSolution(getLocalSolution(
					getSolutionMakespan(ant.getSolution(), graph),
					ant.getSolution(), graph));
		}
	}

	private void initialize() {
		for (Ant ant : antColony) {
			ant.setCurrentIndex(0);
			ant.clear();
		}
	}

	private Ant getBestAnt() {
		Ant bestAnt = antColony[0];
		for (Ant ant : antColony) {
			if (getSolutionMakespan(ant.getSolution(), graph) < getSolutionMakespan(
					bestAnt.getSolution(), graph)) {
				bestAnt = ant;
			}
		}
		return bestAnt;
	}

	private Ant updateBestAnt() {
		Ant bestAnt = getBestAnt();
		if (bestTour == null
				|| bestTourLength > getSolutionMakespan(bestAnt.getSolution(),
						graph)) {
			bestTour = bestAnt.getSolution().clone();
			bestTourLength = getSolutionMakespan(bestAnt.getSolution(), graph);
			bestTourAsString = bestAnt.getSolutionAsString();
		}
		return bestAnt;
	}

	public static void main(String... args) {
		try {
			double[][] graph = getProblemGraphFromFile("/home/cptanalatriste/github/ACOFlowShop/src/flowshop_default.data");
			ACOFlowShop acoFlowShop = new ACOFlowShop(graph);
			acoFlowShop.solveProblem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int[] getLocalSolution(double makespan, int[] jobs, double[][] graph) {
		int[] localSolutionJobs = new int[jobs.length];
		List<Integer> jobsList = new ArrayList<Integer>();

		for (int job : jobs) {
			jobsList.add(job);
		}

		List<Integer> localSolution = jobsList;

		int indexI = 0;
		boolean lessMakespan = true;
		try{
			while (indexI < (jobs.length) && lessMakespan) {
				int jobI = localSolution.get(indexI);
				localSolution.remove(indexI);
				int indexJ = 0;
				while (indexJ < jobs.length && lessMakespan) {
					localSolution.add(indexJ, jobI);
					// Transformar a Arreglo para hallar el makespan de la nueva
					// permutación
					localSolution.toArray();
					double newMakespan = getSolutionMakespan(jobs, graph);
	
					if (newMakespan < makespan) {
						makespan = newMakespan;
						lessMakespan = false;
					} else {
						localSolution.remove(indexJ);
					}
				}
				indexI++;
			}
	
			int k = 0;
			for (int job : localSolution) {
				localSolutionJobs[k] = job;
				k++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return localSolutionJobs;
	}

	public double getSolutionMakespan(int[] solution, double[][] graph) {
		int machines = graph[0].length;
		double[] machinesTime = new double[machines];
		double tiempo = 0;

		for (int job : solution) {
			for (int i = 0; i < machines; i++) {
				tiempo = graph[job][i];
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

	public static double[][] getProblemGraphFromFile(String path)
			throws IOException {
		double graph[][] = null;
		FileReader fr = new FileReader(path);
		BufferedReader buf = new BufferedReader(fr);
		String line;
		int i = 0;

		while ((line = buf.readLine()) != null) {
			if (i > 0) {
				String splitA[] = line.split(" ");
				LinkedList<String> split = new LinkedList<String>();
				for (String s : splitA) {
					if (!s.isEmpty()) {
						split.add(s);
					}
				}
				int j = 0;
				for (String s : split) {
					if (!s.isEmpty()) {
						graph[i - 1][j++] = Integer.parseInt(s);
					}
				}
			} else {
				String firstLine[] = line.split(" ");
				String numberOfJobs = firstLine[0];
				String numberOfMachines = firstLine[1];

				if (graph == null) {
					graph = new double[Integer.parseInt(numberOfJobs)][Integer
							.parseInt(numberOfMachines)];
				}
			}
			i++;
		}
		return graph;
	}
}
