package speechRecogniser.entity;

import java.util.*;

/**
 * A FeatureVector represents a timeslice of an Observation, or vectors for trained data of a phoneme
 * @author Zeeger Lubsen
 */
public class FeatureVector {
	private List<Double> featureVector;
	
	public FeatureVector() {
		featureVector = new ArrayList<Double>();
	}
	
	public List<Double> getFeatures() {
		return this.featureVector;
	}
	
	public void add( double value ) {
		featureVector.add( value );
	}
	public double get( int index ) {
		if( index < featureVector.size() )
			return featureVector.get( index );
		throw new IndexOutOfBoundsException();
	}
	
	// For debugging
	public String toString() {
		String output = "";
		
		if( featureVector == null )
			return "empty";
		
		for( Double f : this.featureVector ) {
			output += f + " ";
		}
		return output;
	}
}
