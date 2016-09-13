package speechRecogniser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import speechRecogniser.entity.FeatureVector;
import speechRecogniser.hmm.*;

/**
 * The PhonemeCollection contains all phonemes the Recogniser can distinguish
 * Each phoneme is a HMM as parsed from the (hmms.mmf) configuration file.
 * All (transition-)probabilities are log-probabilities
 * The SignalProcessor sets all emission probabilities.
 * @author Zeeger Lubsen
 *
 */
public class PhonemeCollection {
	// List containing all phonemes as a HMM
	private List<HMM> theCollection;

	public PhonemeCollection( String aHMMInputFile ) {
		parsePhonemeFile( aHMMInputFile );
	}

	/**
	 * Parses a configuration file (description of trained recogniser)
	 * @param <b>aHMMInputFile</b> Filename of the input file
	 */ 
	private void parsePhonemeFile( String aHMMInputFile ) {
		BufferedReader inputStream = null;
		StringTokenizer tokenizer = null;
		String theReadLine = null;
		HMM currentPhoneme = null;
		State currentState = null;
		
		
		this.theCollection = new ArrayList<HMM>();
		
		try {
			inputStream = new BufferedReader( new FileReader( aHMMInputFile ) );

			// read each line in the file
			// match each section and parse/evaluate the contents of the section 
			while( ( theReadLine = inputStream.readLine() ) != null ) {
				tokenizer = new StringTokenizer( theReadLine );
				String sectionWord = tokenizer.nextToken();
				
				if( sectionWord.equals( "~h" ) ) {
					// Construct a new Phoneme with the parsed name
					String phonemeName = tokenizer.nextToken();
					phonemeName = phonemeName.substring( 1, phonemeName.length()-1 );
					
					currentPhoneme = new HMM( phonemeName );
				} else if( sectionWord.equals( "<BEGINHMM>" ) ) {
					// Add first, nonemitting, state
					currentState = new State();
					currentPhoneme.appendState( currentState );
					currentState.setStateNumber( 1 );
					currentState.setPhoneme( currentPhoneme.getName() );
				} else if( sectionWord.equals( "<NUMSTATES>" ) ) {
					// Parse the number of states, store in the phoneme, just for debugging or something.
					currentPhoneme.setReadNumberOfStates(
							Integer.parseInt( tokenizer.nextToken() )
							);
				} else if( sectionWord.equals( "<STATE>" ) ) {
					// Begin constructing a new State
					currentState = new State();
					currentState.setStateNumber( Integer.parseInt( tokenizer.nextToken() ) );
					currentState.setPhoneme( currentPhoneme.getName() );
					currentPhoneme.appendState( currentState );
				} else if( sectionWord.equals( "<MEAN>" ) ) {
					// Parse State mean vector
					FeatureVector meanVector = new FeatureVector();
					
					theReadLine = inputStream.readLine();
					tokenizer = new StringTokenizer( theReadLine );
										
					while( tokenizer.hasMoreTokens() ) {
						meanVector.add( Float.parseFloat( tokenizer.nextToken() ) );
					}
					
					currentState.setMean( meanVector );
				} else if( sectionWord.equals( "<VARIANCE>" ) ) {
					// Parse State variance vector
					FeatureVector varianceVector = new FeatureVector();
					
					theReadLine = inputStream.readLine();
					tokenizer = new StringTokenizer( theReadLine );
										
					while( tokenizer.hasMoreTokens() ) {
						varianceVector.add( Float.parseFloat( tokenizer.nextToken() ) );
					}
					
					currentState.setVariance( varianceVector );
				} else if( sectionWord.equals( "<GCONST>" ) ) {
					// Parse GCONST value
					currentState.setGCONSTValue( Double.parseDouble( tokenizer.nextToken() ) );
				} else if( sectionWord.equals( "<TRANSP>" ) ) {
					// Finished parsing states, need to insert final nonemitting State
					currentState = new State();
					currentPhoneme.appendState( currentState );
					currentState.setStateNumber( 5 );
					currentState.setPhoneme( currentPhoneme.getName() );
					
					// Parse the Transitionmatrix, set log-probabilities
					
					// noStates is always 5, but parse it anyway
					int noStates = Integer.parseInt( tokenizer.nextToken() );
					currentPhoneme.setReadNumberOfStates( noStates );
					
					State rowState = currentPhoneme.getFirstState();
					
					while( rowState != null ) {
						State colState = currentPhoneme.getFirstState();
						theReadLine = inputStream.readLine();
						tokenizer = new StringTokenizer( theReadLine );
						
						while( colState != null ) {
							double probability = Double.parseDouble( tokenizer.nextToken() );
							probability = Math.log( probability );
							// log( 0 ) => -Infinity
							// Only set nonzero transitions
							if( probability != Double.NEGATIVE_INFINITY ) {
								rowState.addTransition(
										probability,
										colState
										);
							}
							colState = colState.getNextState();
						}
						rowState = rowState.getNextState();
					}
				} else if( sectionWord.equals( "<ENDHMM>" ) ) {
					// Add the completed Phoneme to the Collection
					theCollection.add( currentPhoneme );
					
					currentPhoneme = null;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println( "File '" + aHMMInputFile + "' not found!" );
			System.err.println( "Cannot built Lexicon. Exiting." );
			System.exit( 0 );
		} catch (IOException e) {
			System.err.println( "IOException while reading " + aHMMInputFile );
		}
	}
	
	public List<HMM> getPhonemes() {
		return this.theCollection;
	}
	
	/**
	 * @return Returns the phoneme labeled name in theCollection. Returns null when it does not exist
	 * @param <b>name</b> The name of the phoneme to be returned.
	 */
	public HMM getPhoneme( String name ) {
		for( HMM p : this.theCollection ) {
			if( p.getName().equals( name ) )
				return p;
		}
		return null;
	}
}