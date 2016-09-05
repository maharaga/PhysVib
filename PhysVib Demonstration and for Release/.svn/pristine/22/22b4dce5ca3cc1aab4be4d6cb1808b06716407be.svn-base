package PhysVib.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

/******
 * PatternList is a class of a list of the currently playing vibration patterns. 
 ******/
public class PatternList {
	public List<DecayingPatternPoint> patterns;
	
	public PatternList() {	
		patterns = new ArrayList<DecayingPatternPoint>();
	}

	
	/******
	 * Find if the testPattern data is in the current vibration pattern. 
	 * If it does not exist, then return null.
	 ******/
	public DecayingPatternPoint findDecayingPatternPoint(DecayingPatternPoint testPattern) {
		DecayingPatternPoint checkPattern;
		
		for (int i = 0; i < patterns.size(); i++) {
			checkPattern = patterns.get(i);
			if (checkPattern.equals(testPattern)) {
				return checkPattern;
			}
		}	
		return null;
	}
	
	/******
	 * Adds a decayingpatternpoint to the current playing list with the parameters.  
	 ******/
	public void addDecayingPatternPoint(float decayingFactor, float duration, float frequency, float amplitude, Vector2 collidedLocation) {
		DecayingPatternPoint checkPattern = new DecayingPatternPoint();
		checkPattern.decayingFactor = decayingFactor;
		checkPattern.duration = duration;
		checkPattern.frequency = frequency;
		checkPattern.elapsedTime = 0;
		checkPattern.previousPhase = 0;
		checkPattern.amplitude = amplitude;
		checkPattern.collidedLocation = collidedLocation;
			
		patterns.add(checkPattern);
	}

	
	/******
	 * Reset all playing patterns in the list.  
	 ******/
	public void freeAll() {
		while (patterns.size() > 0)
			patterns.remove(0);			
	}
	
	
	/******
	 * Sort the current playing patterns by the maximum amplitude of next sampling duration.  
	 ******/
	public void sortByNextAmp()
	{		
		Collections.sort(patterns, new AmpComparator());	
	}
	
	class AmpComparator implements Comparator<DecayingPatternPoint> {
		@Override
		public int compare(DecayingPatternPoint o1, DecayingPatternPoint o2) {
			if (o1.nextMax > o2.nextMax)
				return 1;
			else if (o1.nextMax < o2.nextMax)
				return -1;
			return 0;			
		}				
	}
}