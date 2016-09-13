package speechRecogniser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Parses a testset file, stores as a List
 * @author Zeeger Lubsen
 */
public class TestSet {
	// The list of input files in the testset
	private List<String> theInputFiles;

	public TestSet( String aTestSetFile ) {
		this.theInputFiles = new ArrayList<String>();
		parseTestSetFile( aTestSetFile );
	}
	
	private void parseTestSetFile( String aTestSetFile ) {
		BufferedReader inputStream = null;
		String theReadLine = null;
		
		try {
			inputStream = new BufferedReader( new FileReader( aTestSetFile ) );
			
			while( ( theReadLine = inputStream.readLine() ) != null ) {
				this.theInputFiles.add( theReadLine );
			}
		} catch (FileNotFoundException e) {
			System.err.println( "File '" + aTestSetFile + "' not found!" );
		} catch (IOException e) {
			System.err.println( "IOException while reading file '" + aTestSetFile + ".lab'" );
		}		
	}
	
	public List<String> getTestSet() {
		return theInputFiles;
	}
}
