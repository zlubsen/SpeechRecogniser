package speechRecogniser.hmm;

import java.util.ArrayList;
import java.util.List;

import speechRecogniser.entity.FeatureVector;

/**
 * The state class is a state in a HMM.
 * @author Zeeger Lubsen
 *
 */
public class State {
	// List of transition out of this state
	protected List<Transition> stateTransitions;
	// List of emissions for an Observation. The index of the list is the index for the timeslice of the observation
	protected List<Double> stateEmissions;
	// Reference to the next state in the linked list
	protected State nextState;
	// Reference to the previous state in the linked list
	protected State previousState;
	// State number in the HMM
	protected int stateNumber;
	// Name of the phoneme this state is part of, for backtracing the word out of a best path.
	protected String phoneme;

	// Mean, variance and gconst of a trained HMM
	private FeatureVector mean;
	private FeatureVector variance;
	private double gconst;
	
	/**
	 * Create a new state with no references
	 */
	public State() {
		stateTransitions = new ArrayList<Transition>();
		stateEmissions = new ArrayList<Double>();
		nextState = null;
		previousState = null;
	}
	
	/**
	 * Copy constructor for duplicating states.
	 * References to other states and transitions need to be set manually
	 * @param <b>state</b> The state to duplicate
	 */
	public State( State state ) {
		// set refences to static data
		this.stateEmissions = state.stateEmissions;
		this.stateNumber = state.stateNumber;
		this.gconst = state.gconst;
		this.mean = state.mean;
		this.variance = state.variance;
		this.phoneme = state.phoneme;
		
		// init dynamic data
		this.nextState = null;
		this.previousState = null;
		this.stateTransitions = new ArrayList<Transition>();
	}
	
	public State getNextState() {
		return this.nextState;
	}
	public State getPreviousState() {
		return this.previousState;
	}
	
	/**
	 * Add a transition to the state.
	 * Assumes the state and toState are already part of the same HMM
	 */
	public void addTransition( double probability, State toState ) {
		Transition t = new Transition( probability, toState );
		stateTransitions.add( t );
		if( toState != null )
			t.setStepCount( toState.stateNumber - this.stateNumber );
		else
			t.setStepCount( 1 );
	}
	public List<Transition> getTransitions() {
		return this.stateTransitions;
	}
	public List<Double> getEmissions() {
		return this.stateEmissions;
	}
	public double getEmission( int sliceIndex ) {
		if( sliceIndex < stateEmissions.size() )
			return stateEmissions.get( sliceIndex );
		return 0;
	}
	public void addEmission( double probability ) {
		stateEmissions.add( probability );
	}
	
	public int getStateNumber() {
		return stateNumber;
	}
	public void setStateNumber( int value ) {
		stateNumber = value;
	}
	public String getPhoneme() {
		return phoneme;
	}
	public void setPhoneme( String value ) {
		phoneme = value;
	}
	
	public FeatureVector getMean() {
		return this.mean;
	}
	public void setMean( FeatureVector value ) {
		this.mean = value;
	}
	
	public FeatureVector getVariance() {
		return this.variance;
	}
	public void setVariance( FeatureVector value ) {
		this.variance = value;
	}
	
	public double getGCONSTValue() {
		return this.gconst;
	}
	public void setGCONSTValue( double value ) {
		this.gconst = value;
	}
	
	public String toString() {
		return  phoneme + "-" + stateNumber;
	}
}
