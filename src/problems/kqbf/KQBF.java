package problems.kqbf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Arrays;
import problems.Evaluator;
import solutions.Solution;

/**
 * A quadractic binary function (KQBF) is a function that can be expressed as the
 * sum of quadractic terms: f(x) = \sum{i,j}{a_{ij}*x_i*x_j}. In matricial form
 * a KQBF can be expressed as f(x) = x'.A.x 
 * The problem of minimizing a KQBF is NP-hard [1], even when no constraints
 * are considered.
 * 
 * [1] Kochenberger, et al. The unconstrained binary quadratic programming
 * problem: a survey. J Comb Optim (2014) 28:58–81. DOI
 * 10.1007/s10878-014-9734-0.
 * 
 * @author ccavellucci, fusberti
 *
 */
public class KQBF implements Evaluator<Integer> {

	/**
	 * Dimension of the domain.
	 */
	public final Integer size;

	/**
	 * Maximum weight.
	 */
	public Double weight;
	
	/**
	 * The array of weights for the domain.
	 */
	public Double[] weights;
	
	/**
	 * The array of numbers representing the domain.
	 */
	public final Double[] variables;

	/**
	 * The matrix A of coefficients for the KQBF f(x) = x'.A.x
	 */
	public Double[][] A;

	/**
	 * The constructor for QuadracticBinaryFunction class. The filename of the
	 * input for setting matrix of coefficients A of the KQBF. The dimension of
	 * the array of variables x is returned from the {@link #readInput} method.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the KQBF.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public KQBF(String filename) throws IOException {
		size = readInput(filename);
		variables = allocateVariables();
	}

	/**
	 * Evaluates the value of a solution by transforming it into a vector. This
	 * is required to perform the matrix multiplication which defines a KQBF.
	 * 
	 * @param sol
	 *            the solution which will be evaluated.
	 */
	public void setVariables(Solution<Integer> sol) {

		resetVariables();
		if (!sol.isEmpty()) {
			for (Integer elem : sol) {
				variables[elem] = 1.0;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#getDomainSize()
	 */
	@Override
	public Integer getDomainSize() {
		return size;
	}

	/**
	 * {@inheritDoc} In the case of a KQBF, the evaluation correspond to
	 * computing a matrix multiplication x'.A.x. A better way to evaluate this
	 * function when at most two variables are modified is given by methods
	 * {@link #evaluateInsertionKQBF(int)}, {@link #evaluateRemovalKQBF(int)} and
	 * {@link #evaluateExchangeKQBF(int,int)}.
	 * 
	 * @return The evaluation of the KQBF.
	 */
	@Override
	public Double evaluate(Solution<Integer> sol) {

		setVariables(sol);
		sol.weight = evaluateWeightKQBF();
		return sol.cost = evaluateKQBF();

	}
	
	/**
	 * Evaluates a KQBF weight by calculating sum of all variables in solution weight
	 * KQBF: f(x) = x'.A.x .
	 * 
	 * @return The value of the KQBF weight.
	 */
	public Double evaluateWeightKQBF() {
		Double sum = 0.0;

		for (int i = 0; i < size; i++) {
			if (variables[i] == 1) {
				sum += weights[i];
			}
		}

		return sum;
	}

	/**
	 * Evaluates a KQBF by calculating the matrix multiplication that defines the
	 * KQBF: f(x) = x'.A.x .
	 * 
	 * @return The value of the KQBF.
	 */
	public Double evaluateKQBF() {

		Double aux = (double) 0, sum = (double) 0;
		Double vecAux[] = new Double[size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				aux += variables[j] * A[i][j];
			}
			vecAux[i] = aux;
			sum += aux * variables[i];
			aux = (double) 0;
		}

		return sum;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {

		setVariables(sol);
		return evaluateInsertionKQBF(elem);

	}

	/**
	 * Determines the contribution to the KQBF objective function from the
	 * insertion of an element.
	 * 
	 * @param i
	 *            Index of the element being inserted into the solution.
	 * @return Ihe variation of the objective function resulting from the
	 *         insertion.
	 */
	public Double evaluateInsertionKQBF(int i) {

		if (variables[i] == 1)
			return 0.0;

		return evaluateContributionKQBF(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateRemovalCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {

		setVariables(sol);
		return evaluateRemovalKQBF(elem);

	}

	/**
	 * Determines the contribution to the KQBF objective function from the
	 * removal of an element.
	 * 
	 * @param i
	 *            Index of the element being removed from the solution.
	 * @return The variation of the objective function resulting from the
	 *         removal.
	 */
	public Double evaluateRemovalKQBF(int i) {

		if (variables[i] == 0)
			return 0.0;

		return -evaluateContributionKQBF(i);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateExchangeCost(java.lang.Object,
	 * java.lang.Object, solutions.Solution)
	 */
	@Override
	public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {

		setVariables(sol);
		return evaluateExchangeKQBF(elemIn, elemOut);

	}

	/**
	 * Determines the contribution to the KQBF objective function from the
	 * exchange of two elements one belonging to the solution and the other not.
	 * 
	 * @param in
	 *            The index of the element that is considered entering the
	 *            solution.
	 * @param out
	 *            The index of the element that is considered exiting the
	 *            solution.
	 * @return The variation of the objective function resulting from the
	 *         exchange.
	 */
	public Double evaluateExchangeKQBF(int in, int out) {

		Double sum = 0.0;

		if (in == out)
			return 0.0;
		if (variables[in] == 1)
			return evaluateRemovalKQBF(out);
		if (variables[out] == 0)
			return evaluateInsertionKQBF(in);

		sum += evaluateContributionKQBF(in);
		sum -= evaluateContributionKQBF(out);
		sum -= (A[in][out] + A[out][in]);

		return sum;
	}

	/**
	 * Determines the contribution to the KQBF objective function from the
	 * insertion of an element. This method is faster than evaluating the whole
	 * solution, since it uses the fact that only one line and one column from
	 * matrix A needs to be evaluated when inserting a new element into the
	 * solution. This method is different from {@link #evaluateInsertionKQBF(int)},
	 * since it disregards the fact that the element might already be in the
	 * solution.
	 * 
	 * @param i
	 *            index of the element being inserted into the solution.
	 * @return the variation of the objective function resulting from the
	 *         insertion.
	 */
	private Double evaluateContributionKQBF(int i) {

		Double sum = 0.0;

		for (int j = 0; j < size; j++) {
			if (i != j)
				sum += variables[j] * (A[i][j] + A[j][i]);
		}
		sum += A[i][i];

		return sum;
	}

	/**
	 * Responsible for setting the KQBF function parameters by reading the
	 * necessary input from an external file. this method reads the domain's
	 * dimension and matrix {@link #A}.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the black
	 *            box function.
	 * @return The dimension of the domain.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	protected Integer readInput(String filename) throws IOException {

		Reader fileInst = new BufferedReader(new FileReader(filename));
		StreamTokenizer stok = new StreamTokenizer(fileInst);

		stok.nextToken();
		Integer _size = (int) stok.nval;
		
		stok.nextToken();
		weight = stok.nval;

		A = new Double[_size][_size];
		weights = new Double[_size];
		
		for (int i = 0; i < _size; i++) {
			stok.nextToken();
			weights[i] = stok.nval;
		}

		for (int i = 0; i < _size; i++) {
			for (int j = i; j < _size; j++) {
				stok.nextToken();
				A[i][j] = stok.nval;
				if (j>i)
					A[j][i] = 0.0;
			}
		}

		return _size;

	}

	/**
	 * Reserving the required memory for storing the values of the domain
	 * variables.
	 * 
	 * @return a pointer to the array of domain variables.
	 */
	protected Double[] allocateVariables() {
		Double[] _variables = new Double[size];
		return _variables;
	}

	/**
	 * Reset the domain variables to their default values.
	 */
	public void resetVariables() {
		Arrays.fill(variables, 0.0);
	}

	/**
	 * Prints matrix {@link #A}.
	 */
	public void printMatrix() {

		for (int i = 0; i < size; i++) {
			for (int j = i; j < size; j++) {
				System.out.print(A[i][j] + " ");
			}
			System.out.println();
		}

	}

	/**
	 * A main method for testing the KQBF class.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		KQBF kqbf = new KQBF("instances/kqbf/qbf040");
		kqbf.printMatrix();
		Double maxVal = Double.NEGATIVE_INFINITY;
		
		// evaluates randomly generated values for the domain, saving the best
		// one.
		for (int i = 0; i < 10000000; i++) {
			for (int j = 0; j < kqbf.size; j++) {
				if (Math.random() < 0.5)
					kqbf.variables[j] = 0.0;
				else
					kqbf.variables[j] = 1.0;
			}
			//System.out.println("x = " + Arrays.toString(kqbf.variables));
			Double eval = kqbf.evaluateKQBF();
			//System.out.println("f(x) = " + eval);
			if (maxVal < eval)
				maxVal = eval;
		}
		System.out.println("maxVal = " + maxVal);

		// evaluates the zero array.
		for (int j = 0; j < kqbf.size; j++) {
			kqbf.variables[j] = 0.0;
		}
		System.out.println("x = " + Arrays.toString(kqbf.variables));
		System.out.println("f(x) = " + kqbf.evaluateKQBF());

		// evaluates the all-ones array.
		for (int j = 0; j < kqbf.size; j++) {
			kqbf.variables[j] = 1.0;
		}
		System.out.println("x = " + Arrays.toString(kqbf.variables));
		System.out.println("f(x) = " + kqbf.evaluateKQBF());
		
		

	}

}
