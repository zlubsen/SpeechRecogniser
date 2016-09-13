package speechRecogniser.hmm;

import java.util.*;

/**
 * HMM represents the HMM for (trained) phonemes, word composed of phonemes and composite automatons for Viterbi
 * For constructing a model, the HMM is implemented as a doubly-linked-list.
 * When actually traversing the model in Viterbi, moves from state to state are made through transitions.
 * The class provides methods to compose HMMs (phonemes) into larger HMMs (words or composed Viterbi automaton) 
 */
public class HMM {
	// Name of the model. Should be the name of a phoneme, word or algorithm
	private String hmmName;
	// Reference to the first state in the linked-list HMM
	private State firstState;
	// Reference to the last state in the linked-list HMM
	private State lastState;
	// Actual number of states in the model
	private int numberOfStates;
	// Number of states read from the input file for phonemes
	private int readNumberOfStates;
	
	public HMM( String name ) {
		this.hmmName = name;
		firstState = null;
		lastState = null;
		numberOfStates = 0;
	}
	
	/**
	 * Append a new state to the tail of this HMM (list). This does not set transitions!
	 * @param <b>newState</b> The state to be appended to the HMM
	 */
	public void appendState( State newState ) {
		if( newState != null ) {
			if( lastState != null ) {
				lastState.nextState = newState;
				newState.previousState = lastState;
				newState.nextState = null;
			}
			
			lastState = newState;
			if( firstState == null )
				firstState = newState;
			
			numberOfStates++;
		}
	}
	
	/**
	 * Appends a sequence of states to the tail of the HMM. This does not set transitions!
	 * @pre <b>stateSequence</b> is not null
	 * @post <b>stateSequence</b> is added to the tail of the HMM
	 * @param <b>stateSequence</b> The sequence of states to be appended to the HMM
	 */
	public void appendStateSequence( State stateSequence ) {
		if( lastState != null ) {
			// Append the sequence to lastState
			lastState.nextState = stateSequence;
			stateSequence.previousState = lastState;
		} else {
			// HMM is empty, stateSequence is appended to the start
			firstState = stateSequence;
		}
		
		// Set lastState and count the number of appended states
		State temp = stateSequence;
		int counter = 0;
		while( temp != null ) {
			counter++;
			lastState = temp;
			temp = temp.nextState;
		}
		numberOfStates += counter;
	}
	
	public String getName() {
		return this.hmmName;
	}
	
	public State getFirstState() {
		return this.firstState;
	}
	public State getLastState() {
		return this.lastState;
	}
	public void setLastState( State value ) {
		this.lastState = value;
	}
	
	public int getNumberOfStates() {
		return this.numberOfStates;
	}
	
	public int getReadNumberOfStates() {
		return this.readNumberOfStates;
	}
	public void setReadNumberOfStates( int value ) {
		this.readNumberOfStates = value;
	}
	
	/**
	 * Gets the last transition out of the model (which should not have a reference to a state)
	 * Needed to tie together phonemes when composing words.
	 * @return Last transition out of the model
	 */
	public Transition getTailConnectTransition() {
		Transition result = null;
		if( lastState != null ) {
			for( Transition t : this.lastState.getTransitions() ) {
				if( t.getNextState() == null )
					result = t;
			}
		}
		return result;
	}

	/**
	 * Get an enumeration of the complete automaton for Viterbi algorithm.
	 * The method follows the transitions out of the first state,
	 * then assumes the states to be a linked-list.
	 * Sets the state numbers of each state incrementally.
	 * @return A List of all the states in the HMM
	 */
	public List<State> getStatesList() {
		int stateNumber = 0;
		List<State> enmStates = new ArrayList<State>();
		firstState.setStateNumber( stateNumber );
		enmStates.add( firstState );
		stateNumber++;
		
		// Evaluate all paths
		for( Transition t : firstState.getTransitions() ) {
			State nextState = t.getNextState();
			// Traverse all paths
			while( nextState != null && nextState != lastState ) {
				enmStates.add( nextState );
				nextState.setStateNumber( stateNumber );
				stateNumber++;
				nextState = nextState.getNextState();
			}
		}
		
		enmStates.add( lastState );
		lastState.setStateNumber( stateNumber );
		
		return enmStates;
	}
	
	// for debugging
	public String mapStates() {
		String output = "map: ";
		State state = firstState;
		while( state != null ) {
			output += state.toString() + " : ";
			state = state.nextState;
		}
		return output;
	}
	
	public String toString() {
		String output = this.getName();
		State state = firstState;
		while( state != null ) {
			output += "\t" + state.toString() + "\n";
			state = state.nextState;
		}
		state = firstState;
		while( state != null ) {
			output += state.getStateNumber(); 
			for( Transition t : state.getTransitions() ) {
				output += "\t" + t.toString();
			}
			output += "\n";
			state = state.nextState;
		}
		return output;
	}
}
