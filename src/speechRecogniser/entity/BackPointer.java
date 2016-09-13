package speechRecogniser.entity;

/**
 * BackPointers reconstruct the path through the Viterbi algorithm.
 * Keeps reference to the previous step.
 * @author Zeeger Lubsen
 */
public class BackPointer {
	// Coordinates in the viterbi-matrix
	public int i, j;
	
	public BackPointer( int i, int j ) {
		this.i = i;
		this.j = j;
	}
	
	public String toString() {
		return i + ":" + j;
	}
}
