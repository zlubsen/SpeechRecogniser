package speechRecogniser;

public class SpeechRecogniser {
	private final static int HMM_INPUT_FILE_INDEX = 0;
	private final static int LEXICON_INPUT_FILE_INDEX = 1;
	private final static int AUDIO_INPUT_FILE_INDEX = 2;
	private final static int TEST_KEYWORD_INDEX = 2;
	private final static int TESTSET_FILE_INDEX = 3;
	private final static int DEBUG_FLAG_NORMAL_INDEX = 3;
	private final static int DEBUG_FLAG_TESTSET_INDEX = 4;
	
	private static String _hmm_input_filename;
	private static String _lexicon_filename;
	private static String _audio_filename;
	private static String _testset_filename;
	private static boolean _doDebugOutput = false;
	
	/**
	 * @author Zeeger Lubsen
	 * @param args
	 */
	public static void main( String[] args ) {
		// SpeechRecogniser <HMM_input_file> <lexicon_input_file> runtest <testset_file> [debug]?
		if( args.length >= 4 && args[ TEST_KEYWORD_INDEX ].equals( "runtest" ) ) {
			_hmm_input_filename = args[ HMM_INPUT_FILE_INDEX ];
			_lexicon_filename = args[ LEXICON_INPUT_FILE_INDEX ];
			_testset_filename = args[ TESTSET_FILE_INDEX ];
			
			if( args.length == 5 && args[ DEBUG_FLAG_TESTSET_INDEX ].equals( "debug" ) )
				_doDebugOutput = true;
			
			runTestSet();
		// SpeechRecogniser <HMM_input_file> <lexicon_input_file> <audio_input_file> [debug]?
		} else if( args.length >= 3 ) {
			_hmm_input_filename = args[ HMM_INPUT_FILE_INDEX ];
			_lexicon_filename = args[ LEXICON_INPUT_FILE_INDEX ];
			_audio_filename = args[ AUDIO_INPUT_FILE_INDEX ];
			
			if( args.length == 4 && args[ DEBUG_FLAG_NORMAL_INDEX ].equals( "debug" ) )
				_doDebugOutput = true;
			
			runRecogniser();
		} else {
			System.err.println( "Incorrect arguments." );
			System.err.println( "Usage: java SpeechRecogniser <HMM_input_file> <lexicon_input_file> <audio_input_file>" );
			System.exit( 1 );
		}
	}
	
	private static void runTestSet() {
		Lexicon theLexicon;
		PhonemeCollection thePhonemeCollection;
		SignalProcessor theSignalProcessor;
		Recogniser theRecogniser;
		OutputVerifier theOutputVerifier;
		TestSet theTestSet;
		
		// The testset contains a list of input files to run
		theTestSet = new TestSet( _testset_filename );
		
		int countTrue = 0;
		int countFalse = 0;
		
		for( String testset_input_file : theTestSet.getTestSet() ) {
			// The phonemeCollection contains the recognisers trained HMM data
			thePhonemeCollection = new PhonemeCollection( _hmm_input_filename );
			// The signalProcessor parses the feature file that must be recognised, and adds the emission-probabilities to the phonemes
			theSignalProcessor = new SignalProcessor( testset_input_file, thePhonemeCollection );
			// The lexicon contains all word that can be recognised, including a HMM for each word with transition and emission probabilities
			theLexicon = new Lexicon( _lexicon_filename, thePhonemeCollection );
			// The recogniser tries to reconstruct the word being said in the observation, and produces the output
			theRecogniser = new Recogniser( theLexicon, theSignalProcessor.getObservation() );
			
			if( _doDebugOutput ) {
				// The outputVerifier knows what word is actually being said in the audio file
				theOutputVerifier = new OutputVerifier( testset_input_file );
				
				if( theOutputVerifier.matchWord( theRecogniser.recognisedWord ) )
					countTrue++;
				else
					countFalse++;
			}
		}
		
		if( _doDebugOutput ) {
			System.err.println( "Summary of " + (countTrue + countFalse) + " words:" );
			System.err.println( "\t" + countTrue + " word correctly recognised." );
			System.err.println( "\t" + countFalse + " word incorrectly recognised." );
		}
	}
	
	private static void runRecogniser() {
		Lexicon theLexicon;
		PhonemeCollection thePhonemeCollection;
		SignalProcessor theSignalProcessor;
		Recogniser theRecogniser;
		OutputVerifier theOutputVerifier;
		
		// The phonemeCollection contains the recognisers trained HMM data
		thePhonemeCollection = new PhonemeCollection( _hmm_input_filename );
		// The signalProcessor parses the feature file that must be recognised, and adds the emission-probabilities to the phonemes
		theSignalProcessor = new SignalProcessor( _audio_filename, thePhonemeCollection );
		// The lexicon contains all word that can be recognised, including a HMM for each word with transition and emission probabilities
		theLexicon = new Lexicon( _lexicon_filename, thePhonemeCollection );
		// The recogniser tries to reconstruct the word being said in the observation, and produces the output
		try {
			theRecogniser = new Recogniser( theLexicon, theSignalProcessor.getObservation() );
		} catch( OutOfMemoryError outofmem ) {
			System.err.println( "Out of memory" );
		}
		
		if( _doDebugOutput ) {
			// The outputVerifier knows what word is actually being said in the audio file
			theOutputVerifier = new OutputVerifier( _audio_filename );
		}
	}
}
