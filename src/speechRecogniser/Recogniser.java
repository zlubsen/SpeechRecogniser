package speechRecogniser;

import java.util.ArrayList;
import java.util.List;

import speechRecogniser.entity.BackPointer;
import speechRecogniser.entity.Observation;
import speechRecogniser.entity.Word;
import speechRecogniser.hmm.*;

/**
 * The Recogniser tries to recognise the word in the Observation using the Viterbi algorithm
 * It constructs a single HMM from all words in the Lexicon and runs Viterbi on this model
 * @author Zeeger Lubsen
 */
public class Recogniser {
	HMM theAutomaton;
	Lexicon theLexicon;
	Observation theObservation;
	Word recognisedWord;
	
	public Recogniser( Lexicon aLexicon, Observation anObservation ) {
		this.theLexicon = aLexicon;
		this.theObservation = anObservation;
		constructAutomaton( aLexicon );
		recogniseWord();
	}

	private void constructAutomaton( Lexicon theLexicon ) {
		// Create a head and tail nonemitting state
		int stateCounter = 0;
		State head, tail;
		theAutomaton = new HMM( "Viterbi Automaton" );
		head = new State();
		theAutomaton.appendState( head );			// also sets statenumber to 1 for head
		tail = new State();
		
		// connect each wordModel in the lexicon through a transition from the head,
		// and fix the end of the wordModel to the tail of theAutomaton
		for( Word word : theLexicon.getWords() ) {
			// Get the HMM that is constructed for each word by the Lexicon 
			HMM wordModel = word.getModel();
			// log P(1) => 0; Every word is equally likely in the first non-emitting state of theAutomaton
			head.addTransition( 0, wordModel.getFirstState() );
			// Set the last transition of the model to the common non-emitting end-state
			Transition t = wordModel.getTailConnectTransition();
			t.setNextState( tail );
			
			// count number of added states to set statenumbers for theAutomaton
			stateCounter += wordModel.getNumberOfStates();
		}
		theAutomaton.setReadNumberOfStates( stateCounter + 2 );		// no of states plus head and tail
		theAutomaton.setLastState( tail );					// not-so-clean hack to set reference to last state, needed for getStateList
	}
	
	public void recogniseWord() {
		if( theObservation != null && theAutomaton != null )
		viterbi( theObservation, theAutomaton );
	}
	
	/**
	 * Implementation of the Viterbi algorithm, performs the actual recognising.
	 * The algorithms puts all states in the stateGraph in a list to traverse the graph.
	 *  
	 * @param <b>observation</b> The observation to match
	 * @param <b>stateGraph</b> The HMM to reconstruct the observation
	 */
	private void viterbi( Observation observation, HMM stateGraph ) {
		// Get list of states in the stateGraph
		List<State> stateList = stateGraph.getStatesList();
		int numberOfStates = stateList.size();
		// The last state still needs a number...
		stateGraph.getLastState().setStateNumber( numberOfStates-1 );
		// Number of timeslices in the observation
		int observationLength = observation.getFeatureVectors().size();

		// fix start transitions probabilities to match the number of transitions out of the start state
		// transitions start -> other : log(1/numberOfStates-2) => -log(numberOfStates-2)
		double startTransitionProbability = -Math.log( numberOfStates-2 );
		for( Transition t : stateGraph.getFirstState().getTransitions() ) {
			t.setProbability( startTransitionProbability );
		}
		
		// Initialise viterbi and traceback matrices
		double[][] viterbi = new double[ numberOfStates ][ observationLength + 2 ];
		BackPointer[][] traceback = new BackPointer[ numberOfStates ][ observationLength + 2 ];

		// init matrix, log P(1) => 0
		viterbi[ 0 ][ 0 ] = 0;
		// init the borders of the matrix to zero
		for( int stateno = 1; stateno < numberOfStates; stateno++ )
			viterbi[ stateno ][ 0 ] = Double.NEGATIVE_INFINITY;		// log(0) => -Infinity
	    for( int slice = 1; slice < observationLength + 2; slice++ )
	    	viterbi[ 0 ][ slice ] = Double.NEGATIVE_INFINITY;		// log(0) => -Infinity
		
		for( int timeslice = 0; timeslice <= observationLength; timeslice++ ) {			// for each time step t from 0 to T do
			for( State state : stateList ) {											// for each state s from 0 to num-states do
				for( Transition t : state.getTransitions() ) {							// for each transmission s' from s specified by state-graph
					int stateNo = state.getStateNumber();								// s
					int nextStateNo = t.getNextState().getStateNumber();				// s'
					int nextTimeSlice = timeslice + 1;									// t+1
					
					double previousPathProb = viterbi[ stateNo ][ timeslice ];			// viterbi[ s, t ]
					double transProb = t.getProbability();								// a[ s, s' ]
					double obsLikelihood = t.getNextState().getEmission( timeslice /*+1*/);	// Bs'[ Ot ]
					double newScore = previousPathProb + transProb + obsLikelihood;		// = viterbi[ s, t ]) + a[ s, s' ] + Bs'[ Ot ]
					double oldScore = viterbi[ nextStateNo ][ nextTimeSlice ];			// viterbi[ s', t+1 ] 
					if( ( oldScore == 0 ) || ( newScore > oldScore ) ) {
						viterbi[ nextStateNo ][ nextTimeSlice ] = newScore;				// viterbi[ s', t+1 ] <- new-score
																						// back-pointer[ s', t+1 ] <- s
						traceback[ nextStateNo ][ nextTimeSlice ] = new BackPointer( stateNo, timeslice );
					}
				}
			}
		}

		// Set the beginning of the traceback path
		BackPointer startBP = new BackPointer( numberOfStates-1, observationLength+1 );

		// Trace back the path, only interested in statenumbers -> BackPointer.i
	    List<State> res = new ArrayList<State>();
	    int i = startBP.i;
	    while( ( startBP = traceback[ startBP.i ][ startBP.j ]) != null ) {
	    	State bpState = stateList.get( i );
	    	if( !res.contains( bpState ) )
	    		res.add( bpState );
	    	i = startBP.i;
	    }

	    // Reconstruct the recognised transcription out of the path
	    List<String> transcription = new ArrayList<String>();
	    for( int index = ( res.size() - 1 ); index >= 1; index-- ) {
	    	State state = res.get( index );
	    	// dont add null, sil and duplicates of tri-state phonemes
	    	if( state.getPhoneme() != null
	    			&& !( state.getPhoneme().equals( "sil" ) )
	    			&& ( res.size() > index + 1 )
	    			&& !( res.get( index + 1 ).getPhoneme().equals( state.getPhoneme() ) ) )
	    		transcription.add( state.getPhoneme() );
	    }

	    // Get the word that matches the most probable transcription and write the word to the output
	    recognisedWord = theLexicon.getWordByTranscription( transcription );
	    if( recognisedWord != null )
	    	System.out.println( recognisedWord.getWord() );
	    else
	    	System.err.println( "No match!" );
	}
}