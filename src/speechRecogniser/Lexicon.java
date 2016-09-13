package speechRecogniser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import speechRecogniser.entity.Word;
import speechRecogniser.hmm.*;

/**
 * The Lexicon manages all words that the SpeechRecogniser can recognise.
 * It parses an input lexicon, stores the words and their
 * phonetic transcription, and creates/manages the associated HMMs
 * @author Zeeger Lubsen
 */
public class Lexicon {
	// List of Words that can be recognised
	private List<Word> theDictionairy;
	// Counter for numbering states when copying states from phoneme HMMs to word HMMs
	private static int statecnt = 0;

	public Lexicon( String aLexiconFile, PhonemeCollection phonemes ) {
		parseLexiconFile( aLexiconFile );
		constructHMMs( phonemes );
	}
	
	/**
	 * Reads a lexiconfile and parses all word and their phonemic transcription.
	 * @param aLexiconFile The file to parse the lexicon from.
	 */
	private void parseLexiconFile( String aLexiconFile ) {
		BufferedReader inputStream = null;
		
		this.theDictionairy = new ArrayList<Word>();
		
		try {
			inputStream = new BufferedReader( new FileReader( aLexiconFile ) );
			
			String theReadLine;
			while( ( theReadLine = inputStream.readLine() ) != null ) {
				StringTokenizer tokenizer = new StringTokenizer( theReadLine );
				String readWord = tokenizer.nextToken();
				ArrayList<String> readTranscription = new ArrayList<String>();
				
				while( tokenizer.hasMoreTokens() ) {
					readTranscription.add( tokenizer.nextToken() );
				}
				
				Word word = new Word( readWord, readTranscription );
				
				theDictionairy.add( word );
			}
		} catch( FileNotFoundException e ) {
			System.err.println( "File '" + aLexiconFile + "' not found!" );
			System.err.println( "Cannot built Lexicon. Exiting." );
			System.exit( 0 );
		} catch( IOException e ) {
			System.err.println( "IOException while reading " + aLexiconFile );
		}
	}
	
	/**
	 * Constructs a HMM for each word in the lexicon from a set of Phonemes.
	 * @param phonemes the set of phonemes to construct the HMM for a word.
	 */
	private void constructHMMs( PhonemeCollection phonemes ) {
		// construct HMMs for all words in the dictonairy
		// NOTE: the first and last nonemitting-states are not added!
		for( Word word : theDictionairy ) {
			// Set a 'sil' phoneme at the beginning of the model
			HMM silPhoneme = phonemes.getPhoneme( "sil" );
			HMM wordModel = new HMM( word.getWord() );
			
			appendAndCopyStatesPhonemeToModel( silPhoneme, wordModel );
			
			// and for each phoneme in the word, add the emitting states
			for( String wordPhoneme : word.getTranscription() ) {
				HMM phoneme = phonemes.getPhoneme( wordPhoneme );
				appendAndCopyStatesPhonemeToModel( phoneme, wordModel );
			}
			
			// Also set a 'sil' phoneme at the end of the model
			appendAndCopyStatesPhonemeToModel( silPhoneme, wordModel );

			// Set the constructed HMM to the Word
			word.setHMM( wordModel );
		}
	}
	
	/**
	 * Makes copies of the 3 emitting states and append these to the wordModel, and connect the transitions
	 * @param <b>phoneme</b> The phoneme HMM which emitting states need to be appended
	 * @param <b>wordModel</b> The HMM to append the states to
	 */
	private void appendAndCopyStatesPhonemeToModel( HMM phoneme, HMM wordModel ) {
		// Already maintain a reference to the final outgoing transition of the wordModel
		// so it can be fixed when the phoneme is appended
		Transition ta45 = wordModel.getTailConnectTransition();
		// append phoneme states
		State fromState, toState = null;
		fromState = phoneme.getFirstState().getNextState();
		while( !fromState.equals( phoneme.getLastState() ) ) {
			State s = new State( fromState );
			s.setStateNumber( ++statecnt );
			if( toState == null ) 
				toState = s;
			wordModel.appendState( s );
			fromState = fromState.getNextState();
		}
		// set transitions for all states
		fromState = phoneme.getFirstState().getNextState();
		while( !fromState.equals( phoneme.getLastState() ) ) {
			for( Transition t : fromState.getTransitions() ) {
				State transState;
				switch( t.getStepCount() ) {
				case 1:
					transState = toState.getNextState();
					break;
				case 2:
					transState = toState.getNextState().getNextState();
					break;
				case -2:
					transState = toState.getPreviousState().getPreviousState();
					break;
				default: // case 0
					transState = toState;
					break;
				}
				toState.addTransition( t.getProbability() , transState );
			}
			fromState = fromState.getNextState();
			toState = toState.getNextState();
		}
		// Fix 'a45'-transition between the new last and old last phonemes
		if( ta45 != null ) {
			State firstOfLastPhoneme = wordModel.getLastState().getPreviousState().getPreviousState();
			ta45.setNextState( firstOfLastPhoneme );
		}
	}
	
	public Word getWord( String wordname ) {
		for( Word word : theDictionairy ) {
			if( word.getWord().equals( wordname ) )
				return word;
		}
		return null;
	}
	
	public List<Word> getWords() {
		return this.theDictionairy;
	}
	
	/**
	 * Return the Word that matches the specified transcription 
	 * @param <b>aTranscription</b> The transcription of the Word
	 * @return The Word that matches the transcription
	 */
	public Word getWordByTranscription( List<String> aTranscription ) {
		Word matchedWord = null;
		boolean match = false;

		// Check each word, stop when a match is found
		for( int wordIndex = 0; wordIndex < theDictionairy.size() && !match; wordIndex++ ) {
			Word word = theDictionairy.get( wordIndex );
			// If the size of the transcription is different, don't bother
			if( word.getTranscription().size() == aTranscription.size() )
				// All phonemes must be in the transcription of the Word
				if( aTranscription.containsAll( word.getTranscription() ) ) {
					matchedWord = word;
					match = true;
				}
		}
		
		return matchedWord;
	}
}