package PhysVib.Data;

import com.badlogic.gdx.math.Vector2;

/******
 * DecayingPatternPoint is a data class for rendering the exponentially decaying sinusoids. 
 ******/
public class DecayingPatternPoint {	
	public float duration;
	public float decayingFactor;
	public float frequency;
	public float elapsedTime;
	public float amplitude;
	public float previousPhase;
	public float nextMax;
	public Vector2 collidedLocation;
		
	DecayingPatternPoint () {
		duration = 0;
		decayingFactor = 0;
		frequency = 0;
		elapsedTime = 0;
		amplitude = 0;
		previousPhase = 0;
		nextMax = 0;
		collidedLocation = new Vector2(0, 0);		
	}
}
