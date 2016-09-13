package speechRecogniser.entity;

import java.util.List;

/**
 * The Observation is the input of the recogniser, the spoken word to be recognised.
 * It contains the observed sequence of feature vectors.
 * @author Zeeger Lubsen
 *
 */
public class Observation {
	// The observed sequence of features
	private List<FeatureVector> theFeatureVectors;		
	
	public Observation( List<FeatureVector> aFeatureVectorList ) {
		this.theFeatureVectors = aFeatureVectorList;
	}
	
	public List<FeatureVector> getFeatureVectors() {
		return this.theFeatureVectors;
	}
	
	// For debugging
	public String toString() {
		String output = "";
		
		if( this.theFeatureVectors == null )
			return "empty";
		
		for( FeatureVector f : this.theFeatureVectors ) {
			output += f + "\n";
		}
		return output;
	}
}
