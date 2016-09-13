package speechRecogniser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import speechRecogniser.entity.FeatureVector;
import speechRecogniser.entity.Observation;
import speechRecogniser.hmm.*;

/**
 * The SignalProcessor reads the observation,
 * and calculates the emission log-probabilities for the states in the phonemes.
 * @author Zeeger Lubsen
 */
public class SignalProcessor {
	private String theFeatureVectorFile;	// The input file
	private Observation theObservation;		// The Observation
	private int nSamples;					// the number of samples in the file
	private int sampSize;					// The size of each sample in bytes

	/**
	 * Parses a HTK format file and contructs an Observation
	 * @param aFeatureVectorFile A file in HTK format
	 */
	public SignalProcessor( String aFeatureVectorFile ) {
		this.theFeatureVectorFile = aFeatureVectorFile;
		this.parseHTKfile();
	}
	
	/**
	 * Parses a HTK format file and constructs an Observation,
	 * then calculates the emission log-probabilities for the given phonemes
	 * @param <b>aFeatureVectorFile</b> A HTK format file with an observation
	 * @param <b>phonemes</b> A collection of phonemes of a trained recogniser
	 */
	public SignalProcessor( String aFeatureVectorFile, PhonemeCollection phonemes ) {
		this( aFeatureVectorFile );
		this.calculateEmissionProbabilities( phonemes );
	}
	
	/**
	 * Parses a HTK format file and creates an Observation object from the data in the file
	 * Assumes the "file" to be located in mfc/"file".mfc 
	 */
	private void parseHTKfile() {
		DataInputStream inputStream = null;
		
		try {
			inputStream = new DataInputStream(
							new BufferedInputStream(
								new FileInputStream( "mfc/" + this.theFeatureVectorFile + ".mfc" ) ) );
			
			// Read 12byte header
			nSamples = inputStream.readInt();			// 4-bytes
			/*sampPeriod = */inputStream.readInt();		// 4-bytes
			sampSize = inputStream.readShort();			// 2-bytes
			/*parmKind = */inputStream.readShort();		// 2-bytes
													//	= 12-bytes

			// Each vector contains 39 4-byte floats ( sampSize / sizeof( float ) )
			int nVectorSize = ( sampSize / 4 );
			
			// Read the number of specified feature-vectors ( nSamples ) 
			List<FeatureVector> featureVectors = new ArrayList<FeatureVector>();
			for( int i = 0; i < nSamples; i++ ) {
				FeatureVector featureVector = new FeatureVector();
				for( int j = 0; j < nVectorSize ; j++ ) {
					float readFloat = inputStream.readFloat();
					// Read float, stored as double, presents no problems
					featureVector.add( readFloat );
				}
				featureVectors.add( featureVector );
			}
			// Set the resulting Observation
			this.theObservation = new Observation( featureVectors );
			
		} catch ( EOFException e ) {
			System.err.println( "EOFException while reading " + this.theFeatureVectorFile );
		} catch ( FileNotFoundException e ) {
			System.err.println( "File '" + this.theFeatureVectorFile + ".mfc' not found!" );
			System.err.println( "Cannot parse audio input file. Exiting." );
			System.exit( 0 );
		} catch ( IOException e ) {
			System.err.println( "IOException while reading " + this.theFeatureVectorFile );
		}
	}
	
	/**
	 * Calculate the emission log-probabilities for Phonemes
	 * @param <b>phonemes</b>
	 */
	private void calculateEmissionProbabilities( PhonemeCollection phonemes ) {
		// for each phoneme in the collection
		for( HMM phoneme : phonemes.getPhonemes() ) {
			// Consider each 2nd, 3rd and 4th state of a phoneme; 1st and 2nd are non-emitting
			State state = phoneme.getFirstState().getNextState();
			int vectorIndex, featureIndex;
			
			while( state != phoneme.getLastState() ) {
				// Get the mean and variance of the state
				List<Double> mean = state.getMean().getFeatures();
				List<Double> variance = state.getVariance().getFeatures();
				vectorIndex = 0;
				
				// calculate the emission log-probabiltity for every timeslice
				for( FeatureVector slice : theObservation.getFeatureVectors() ) {
					double gconst, probability;
					double totala = 0, totalb = Double.NEGATIVE_INFINITY;
					featureIndex = 0;
					
					// for every element in a featurevector calculate the probability density
					for( double feature : slice.getFeatures() ) {
						double a, b;
						
						// Calculate the 'e^'-part of the distribution function
						a = Math.pow( feature - mean.get( featureIndex ), 2 ) / ( 2 * variance.get( featureIndex ) );
						totala -= a;
						
						// Calculate the 'gconst'-part of the distribution function
						b = 2 * Math.PI * variance.get( featureIndex );
						if( totalb == Double.NEGATIVE_INFINITY )
							totalb = b;
						else
							totalb *= b;
						
						featureIndex++;
					}
					// Add the totals for the log-probability; <validate GCONST value>
					gconst = Math.log( totalb );
					totalb = 0.5 * gconst;
					// quick: probability = totala + 0.5 * state.getGCONSTValue();
					
					probability = totala - totalb;
					
					// Set emission[state][slice] = probability
					state.addEmission( probability );
					
					vectorIndex++;
				}
				state = state.getNextState();
			}
		}
	}
	
	public Observation getObservation() {
		return this.theObservation;
	}
	
	// For debugging
	/*private void printEmissions( HMM phoneme ) {
		System.out.println( phoneme.getName() );
		State state = phoneme.getFirstState().getNextState();
		int i = 0;
		for( double d : state.getEmissions() ) {
			System.out.println( i + " : " + d + " : " + Math.log( d ) + " : " + Math.exp( d ) );
			i++;
		}		
	}*/
}
