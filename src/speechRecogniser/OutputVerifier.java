package speechRecogniser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import speechRecogniser.entity.Word;

/**
 * Parsed a .lab file for a given observation input file
 * @author Zeeger Lubsen
 *
 */
public class OutputVerifier {
	private String theVerificationFile;
	private String theSpokenWord;

	public OutputVerifier( String aVerificationFile ) {
		parseVerificationFile( aVerificationFile );
	}
	
	/**
	 * Parses the label-file and outputs the verification
	 * Assumes the "file" to be in label/"file".lab
	 * @param aVerificationFile
	 */
	private void parseVerificationFile( String aVerificationFile ) {
		this.theVerificationFile = "label/" + aVerificationFile + ".lab";
		BufferedReader inputStream = null;
		String theReadLine = null;
		
		try {
			inputStream = new BufferedReader( new FileReader( this.theVerificationFile ) );
			
			if( ( theReadLine = inputStream.readLine() ) != null ) {
				this.theSpokenWord = theReadLine;
				System.err.println( "Verification of spoken word: " + this.theSpokenWord );
			} else {
				System.err.println( "Error parsing audio verification word...\nSpeechRecogniser output verification is not available." );
			}
		} catch (FileNotFoundException e) {
			System.err.println( "File '" + this.theVerificationFile + "' not found!" );
			System.err.println( "Cannot parse audio verification file.\nSpeechRecogniser output verification is not available." );
		} catch (IOException e) {
			System.err.println( "IOException while reading file '" + this.theVerificationFile + ".lab'" );
		}		
	}
	
	public boolean matchWord( Word recognisedWord ) {
		return recognisedWord.getWord().equals( theSpokenWord );
	}
}
