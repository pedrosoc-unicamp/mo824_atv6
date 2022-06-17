package solutions;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Solution<E> extends ArrayList<E> {
	
	public Double cost = Double.POSITIVE_INFINITY;
	public Double weight = 0.0;
	
	public Solution() {
		super();
	}
	
	public Solution(Double cost, Double weight) {
		super();
		this.cost = cost;
		this.weight = weight;
	}
	
	public Solution(Solution<E> sol) {
		super(sol);
		this.cost = sol.cost;
		this.weight = sol.weight;
	}

	@Override
	public String toString() {
		return "Solution: cost=[" + cost + "], weight=[" + weight + "], size=[" + this.size() + "], elements=" + super.toString();
	}

}

