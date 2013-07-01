package pe.edu.pucp.ia.aco;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.UnsupportedLookAndFeelException;

import pe.edu.pucp.ia.aco.config.ProblemConfiguration;
import pe.edu.pucp.ia.aco.view.SchedulingFrame;

/**
 * Appies the MAX-MIN Ant System algorithm to Flow-Shop Problem instance.
 * 
 * @author Carlos G. Gavidia (cgavidia@acm.org)
 * @author Adrián Pareja (adrian@pareja.com)
 * 
 */
public class ACOFlowShop {

	private double[][] graph;
	private double pheromoneTrails[][] = null;
	private Ant antColony[] = null;

	private int numberOfJobs;
	private int numberOfAnts;

	public int[] bestTour;
	String bestScheduleAsString = "";
	public double bestScheduleMakespan = -1.0;

	public ACOFlowShop(double[][] graph) {
		this.numberOfJobs = graph.length;
		System.out.println("Number of Jobs: " + numberOfJobs);
		this.numberOfAnts = ProblemConfiguration.NUMBER_OF_ANTS;
		System.out.println("Number of Ants in Colony: " + numberOfAnts);
		this.graph = graph;
		this.pheromoneTrails = new double[numberOfJobs][numberOfJobs];
		this.antColony = new Ant[numberOfAnts];
		for (int j = 0; j < antColony.length; j++) {
			antColony[j] = new Ant(numberOfJobs);
		}
	}

	public static void main(String... args) {
		System.out.println("ACO FOR FLOW SHOP SCHEDULLING");
		System.out.println("=============================");

		try {
			String fileDataset = ProblemConfiguration.FILE_DATASET;
			System.out.println("Data file: " + fileDataset);
			double[][] graph = getProblemGraphFromFile(fileDataset);
			ACOFlowShop acoFlowShop = new ACOFlowShop(graph);
			acoFlowShop.solveProblem();
			acoFlowShop.showSolution();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showSolution() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
				.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				javax.swing.UIManager.setLookAndFeel(info.getClassName());
				break;
			}
		}

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				SchedulingFrame frame = new SchedulingFrame();
				frame.setVisible(true);
				frame.setSolutionMakespan(bestScheduleMakespan);
				frame.setProblemGraph(graph);
				frame.setSolution(bestTour);
			}
		});
	}

	/**
	 * Solves a Flow-Shop instance using Ant Colony Optimization.
	 * 
	 * @return Array representing a solution.
	 */
	public int[] solveProblem() {
		System.out.println("INITIALIZING PHEROMONE MATRIX");
		double initialPheromoneValue = ProblemConfiguration.MAXIMUM_PHEROMONE;
		System.out.println("Initial pheromone value: " + initialPheromoneValue);
		for (int i = 0; i < numberOfJobs; i++) {
			for (int j = 0; j < numberOfJobs; j++) {
				pheromoneTrails[i][j] = initialPheromoneValue;
			}
		}

		int iteration = 0;
		System.out.println("STARTING ITERATIONS");
		System.out.println("Number of iterations: "
				+ ProblemConfiguration.MAX_ITERATIONS);

		while (iteration < ProblemConfiguration.MAX_ITERATIONS) {
			System.out.println("Current iteration: " + iteration);
			clearAntSolutions();
			buildSolutions();
			updatePheromoneTrails();
			updateBestSolution();
			iteration++;
		}
		System.out.println("EXECUTION FINISHED");
		System.out.println("Best schedule makespam: " + bestScheduleMakespan);
		System.out.println("Best schedule:" + bestScheduleAsString);
		return bestTour.clone();
	}

	/**
	 * Updates pheromone trail values
	 */
	private void updatePheromoneTrails() {
		System.out.println("UPDATING PHEROMONE TRAILS");

		System.out.println("Performing evaporation on all edges");
		System.out.println("Evaporation ratio: "
				+ ProblemConfiguration.EVAPORATION);

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

		System.out.println("Depositing pheromone on Best Ant trail.");
		Ant bestAnt = getBestAnt();
		double contribution = ProblemConfiguration.Q
				/ bestAnt.getSolutionMakespan(graph);
		System.out.println("Contibution for best ant: " + contribution);

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

	/**
	 * Build a solution for every Ant in the Colony.
	 */
	private void buildSolutions() {
		System.out.println("BUILDING ANT SOLUTIONS");
		int antCounter = 0;
		for (Ant ant : antColony) {
			System.out.println("Current ant: " + antCounter);
			while (ant.getCurrentIndex() < numberOfJobs) {
				int nextNode = ant.selectNextNode(pheromoneTrails, graph);
				ant.visitNode(nextNode);
			}
			System.out.println("Original Solution > Makespan: "
					+ ant.getSolutionMakespan(graph) + ", Schedule: "
					+ ant.getSolutionAsString());
			ant.improveSolution(graph);
			System.out.println("After Local Search > Makespan: "
					+ ant.getSolutionMakespan(graph) + ", Schedule: "
					+ ant.getSolutionAsString());
			antCounter++;
		}
	}

	/**
	 * Clears solution build for every Ant in the colony.
	 */
	private void clearAntSolutions() {
		System.out.println("CLEARING ANT SOLUTIONS");
		for (Ant ant : antColony) {
			ant.setCurrentIndex(0);
			ant.clear();
		}
	}

	/**
	 * Returns the best performing Ant in Colony
	 * 
	 * @return The Best Ant
	 */
	private Ant getBestAnt() {
		Ant bestAnt = antColony[0];
		for (Ant ant : antColony) {
			if (ant.getSolutionMakespan(graph) < bestAnt
					.getSolutionMakespan(graph)) {
				bestAnt = ant;
			}
		}
		return bestAnt;
	}

	/**
	 * Selects the best solution found so far.
	 * 
	 * @return
	 */
	private void updateBestSolution() {
		System.out.println("GETTING BEST SOLUTION FOUND");
		Ant bestAnt = getBestAnt();
		if (bestTour == null
				|| bestScheduleMakespan > bestAnt.getSolutionMakespan(graph)) {
			bestTour = bestAnt.getSolution().clone();
			bestScheduleMakespan = bestAnt.getSolutionMakespan(graph);
			bestScheduleAsString = bestAnt.getSolutionAsString();
		}
		System.out.println("Best solution so far > Makespan: "
				+ bestScheduleMakespan + ", Schedule: " + bestScheduleAsString);
	}

	/**
	 * 
	 * Reads a text file and returns a problem matrix.
	 * 
	 * @param path
	 *            File to read.
	 * @return Problem matrix.
	 * @throws IOException
	 */
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