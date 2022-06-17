package problems.kqbf;

import java.io.IOException;

/**
 * Class representing the inverse of the Quadractic Binary Function
 * ({@link QBF}), which is used since the GRASP is set by
 * default as a minimization procedure.
 * 
 * @author ccavellucci, fusberti
 */
public class KQBF_Inverse extends KQBF {

	/**
	 * Constructor for the QBF_Inverse class.
	 * 
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public KQBF_Inverse(String filename) throws IOException {
		super(filename);
	}


	/* (non-Javadoc)
	 * @see problems.qbf.QBF#evaluate()
	 */
	@Override
	public Double evaluateKQBF() {
		return -super.evaluateKQBF();
	}
	
	/* (non-Javadoc)
	 * @see problems.qbf.QBF#evaluateInsertion(int)
	 */
	@Override
	public Double evaluateInsertionKQBF(int i) {	
		return -super.evaluateInsertionKQBF(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.qbf.QBF#evaluateRemoval(int)
	 */
	@Override
	public Double evaluateRemovalKQBF(int i) {
		return -super.evaluateRemovalKQBF(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.qbf.QBF#evaluateExchange(int, int)
	 */
	@Override
	public Double evaluateExchangeKQBF(int in, int out) {
		return -super.evaluateExchangeKQBF(in,out);
	}

}
