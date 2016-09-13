package speechRecogniser.hmm;

public class Transition {
	// The probability the transition is taken
	private double probability;
	// The state the transition moves to
	private State toState;
	// stepCnt is the number of nodes the transition travels in the HMM when represented as a list
	private int stepCnt;
	
	/**
	 * Create a new Transition to a state with a probability
	 * @param <b>transProbability</b> The probability the transition is taken
	 * @param <bnextState</b> The state the transition moves to
	 */
	public Transition( double transProbability, State nextState ) {
		this.probability = transProbability;
		this.toState = nextState;
	}
	
	public double getProbability() {
		return this.probability;
	}
	public void setProbability( double value ) {
		this.probability = value;
	}
	
	public void setNextState( State state ) {
		this.toState = state;
	}
	public State getNextState() {
		return this.toState;
	}
	
	public void setStepCount( int value ) {
		this.stepCnt = value;
	}
	public int getStepCount() {
		return this.stepCnt;
	}
	
	public String toString() {
		return toState + ":" + stepCnt + ":" + probability; 
	}
}
