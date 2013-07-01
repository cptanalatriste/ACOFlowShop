package pe.edu.pucp.ia.aco.config;

public class ProblemConfiguration {

	public static final String FILE_DATASET = "/home/cptanalatriste/github/ACOFlowShop/src/flowshop_default.data";

	public static final int NUMBER_OF_ANTS = 1;
	public static final double ALPHA = 1;
	public static final double BETA = 5;
	public static final double EVAPORATION = 0.5;
	public static final int Q = 1;
	public static final double MAXIMUM_PHEROMONE = 1.0;
	public static final double MINIMUM_PHEROMONE = MAXIMUM_PHEROMONE / 5;
	public static final int MAX_ITERATIONS = 10000;
}
