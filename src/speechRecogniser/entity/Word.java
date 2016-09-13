package speechRecogniser.entity;

import java.util.List;
import speechRecogniser.hmm.HMM;

/**
 * Represents a word in the lexicon.
 * Contains the word, it's transtriction and a HMM.
 * @author Zeeger Lubsen
 */
public class Word {
	private String theWord;
	private List<String> thePhonemicTranscription;
	private HMM theModel;
	
	public Word( String aWord, List<String> aTranscription ) {
		this.theWord = aWord;
		this.thePhonemicTranscription = aTranscription;
	}
	
	public String getWord() {
		return this.theWord;
	}

	public List<String> getTranscription() {
		return this.thePhonemicTranscription;
	}
	
	public HMM getModel() {
		return this.theModel;
	}
	
	public void setHMM( HMM aModel ) {
		this.theModel = aModel;
	}
	
	// For debugging
	public String toString() {
		String output = theWord + "\t\t";
		
		for( String s : thePhonemicTranscription ) {
			output += s  + " ";
		}
		
		return output;
	}
	
}
