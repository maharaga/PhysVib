package PhysVib.Data;

import android.util.Log;
import PhysVib.VibrationManager;


/******
 * VibListInfo manages the current vibration play list using the PatternList instance. 
 ******/

public class VibListInfo {
	public PatternList patternList;
	
	public VibListInfo() {
		patternList = new PatternList();
		return;
	}
	
	/******
	 * relaseLists is a method that calculates elapsed time for each vibration pattern
	 * and removes a pattern if the pattern exceeds its lifetime. 
	 ******/
	public void releaseLists(int SAMPLE_SIZE, int SAMPLE_RATE) {
		DecayingPatternPoint checkPattern;
		
		int size = patternList.patterns.size();
		
		float timetick = (float)SAMPLE_SIZE/(float)SAMPLE_RATE;
				
		for (int i = 0; i < size; i++) {
			checkPattern = patternList.patterns.get(i);
			
			if ((checkPattern.elapsedTime + timetick) >= checkPattern.duration) {
				patternList.patterns.remove(i);
				i--;
				size--;
			}
			else {
				checkPattern.elapsedTime += timetick;	
				patternList.patterns.set(i,  checkPattern);
			}
		}
		
	}
	
	/******
	 * isFilled returns the number of patterns in the current playlist.
	 ******/
	public boolean isFilled() {
		if (this.patternList.patterns.size() > 0) {			
			return true;
		}			
		else return false;
	}	
	
	
	/******
	 * nextMaxAmp calculates the maximum amplitude of each pattern in the next sampleSize
	 * and stores it in each pattern for sorting process. 
	 ******/
	
	public void nextMaxAmp(int sampleSize) {
		DecayingPatternPoint tempPoint;
		float tempAmp;		
		float freq;
		float maxAmp;
		
		for (int j = 0; j < patternList.patterns.size(); j++) {
			tempPoint = patternList.patterns.get(j);
			tempAmp = tempPoint.amplitude;
			
	
			// Find Strongest
			float referencePhase = tempPoint.elapsedTime;
			freq = tempPoint.frequency;
			long tempCalculatedValue;
			double index = referencePhase;
			double increment = 1f / (double)VibrationManager.SAMPLING_RATE;
			int i;
			maxAmp = 0;
			for (i = 0; i < sampleSize; i++) {				
				tempCalculatedValue = (long) Math.abs(
						(double) tempAmp 
						* Math.sin((double)(2.0 * 3.14 * freq * index) )
						* Math.exp((double)(-1.0 * tempPoint.decayingFactor * index)) 
						);					
				if (maxAmp < tempCalculatedValue) {					
					maxAmp = tempCalculatedValue;
				}
				index += increment;
			}
			
			tempPoint.nextMax = maxAmp;
		}
	}
}
