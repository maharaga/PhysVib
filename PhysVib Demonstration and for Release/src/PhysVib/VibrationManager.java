package PhysVib;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.badlogic.gdx.math.Vector2;

import PhysVib.Data.DecayingPatternPoint;
import PhysVib.Data.PatternList;
import PhysVib.Data.VibListInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/******
 * VibrationManager manages a vibration playlist.
 * This manager receives vibrotactile patterns from the collision catcher or an application directly.
 * 
 * An application needs to set setPixelToMeterRatio and setCameraPixel in the collision catcher
 * for the initialization.
 * Additionally, SAMPLING_RATE and UPDATE_RATE can be modified by the hardware performance. 
 ******/
public class VibrationManager {
	public static final int SAMPLING_RATE = 5000;
	public static final int BUFFER_SIZE = (int)(SAMPLING_RATE * 0.01);
	private int UPDATE_RATE = 100;
	private boolean vibrationOn = true;
	PatternList patternList = new PatternList();	
	
	
	VibListInfo listInfo =  null;
	protected float[][] mVibBufferOrigin;
	protected float[][] mVibBufferSend;
	protected float[] testBuffer;
	protected float[] forcedHapticSignal;
	protected Vector2 forcedHapticLocation = new Vector2(0, 0);
	protected float[] sampledSignal = new float[(int) (SAMPLING_RATE * 0.01)];
	   
	Lock listlock = new ReentrantLock();
	
	private List<VibrationAPI> vibrationAPIList = new ArrayList<VibrationAPI>();
	private List<Vector2> actuatorLocation = new ArrayList<Vector2>();	
	private VibrationAPI currentVibrationAPI = null;
	
	private boolean forced = false;
	private int forcedIndex = 0;
	private boolean soundFlag = false;
	
	float maxveldifference = 10;
	float pixel_to_meter = 10;
	float counter = 0;
	
	long elapsedTime = 0;
	long startTime = 0;
	

	/******
	 * Make a buffer to prevent the intervention between the VibrationManager writing and the VibrationManager Playing.
	 ******/
	float[][] cloningBuffer(float[][] indata, int actuatornumber, int buffersize) {
		float[][] newFloat = new float[actuatornumber][buffersize];		
		
		for (int i = 0; i < actuatornumber; i++) {
			for (int j = 0; j < buffersize; j++) {
				newFloat[i][j] = indata[i][j];
			}
		}
		return newFloat;
	}
	

	
	/******
	 * VibrationManager supports the DecayingPatternPoint to play exponentially decaying sinusoids (EDS)
	 * coupled with the Collision Catcher.
	 * However, an application can directly play the EDS by calling playVibration() in the application.
	 *  
	 * In addition, it also supports the free-waveform vibration by using forcedVibrationRendering(). 
	 * This function accepts float-type waveform with the maximum amplitude of 1,
	 * and the actuating location vector.
	 * But current version supports no location on the free waveform vibration. 	 
	 ******/

	public VibrationManager () {					
		listInfo = new VibListInfo();
		
		mVibBufferOrigin = new float[1][BUFFER_SIZE];
		mVibBufferSend = new float[1][BUFFER_SIZE];
		testBuffer = new float[BUFFER_SIZE];

		
		new Thread() {
			@Override
			public void run() {
				try {
					setPriority(Thread.NORM_PRIORITY);
					startTime = System.currentTimeMillis(); 
					
					while(vibrationOn) {
						elapsedTime = System.currentTimeMillis() - startTime;
						
						if (elapsedTime < 1000 / UPDATE_RATE) { 
							Thread.sleep(1);					
							continue;
						}
						
						startTime = startTime + 1000 / UPDATE_RATE;
						
						if (currentVibrationAPI == null) // There is no vibration system 
							continue;											
						else {
							listlock.lock();
							try {
								if (forced) {								
									int i = 0;
									for (i = 0; i < sampledSignal.length; i++) {
										if (i + sampledSignal.length * forcedIndex >= forcedHapticSignal.length) 
											break;
										sampledSignal[i] = forcedHapticSignal[i + sampledSignal.length * forcedIndex];										
									}
									if (i < sampledSignal.length) {
										// forcedHapticSignal is finished
										for (; i < sampledSignal.length; i++)
											sampledSignal[i] = 0;
										forced = false;										
									}
									forcedIndex++;

									Message msg = currentVibrationAPI.vibrationAPI.obtainMessage(2,  UPDATE_RATE,  SAMPLING_RATE,  sampledSignal.clone());
									currentVibrationAPI.vibrationAPI.sendMessage(msg);
								}
								else if (listInfo.patternList.patterns.size() > 0) {
									
									listInfo.nextMaxAmp((int)(BUFFER_SIZE));		
									
									if (listInfo.patternList.patterns.size() > 1) 
										listInfo.patternList.sortByNextAmp();
									
					
									if (actuatorLocation.size() <= 1) {
										generateVibSignal(BUFFER_SIZE, 0, mVibBufferOrigin[0]);
									}
									else {
										for (int i = 0; i < actuatorLocation.size(); i++) 
											generateVibSignal(BUFFER_SIZE, i, mVibBufferOrigin[i]);
									}
									
									listInfo.releaseLists(BUFFER_SIZE, SAMPLING_RATE);
				        			
				        			//mVibBufferOrigin[0][0] = ++counter; 
				        			
									Message msg = currentVibrationAPI.vibrationAPI.obtainMessage(1,  UPDATE_RATE,  SAMPLING_RATE,  cloningBuffer(mVibBufferOrigin, mVibBufferOrigin.length, mVibBufferOrigin[0].length));
									currentVibrationAPI.vibrationAPI.sendMessage(msg);
								}
								else {
									Message msg = currentVibrationAPI.vibrationAPI.obtainMessage(0,  UPDATE_RATE,  SAMPLING_RATE,  null);
									currentVibrationAPI.vibrationAPI.sendMessage(msg);									
								}
							}
				    		finally{
				    			listlock.unlock();
				    		}
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}.start();		
	}
	
  
	/******
	 * clearList() reset the list of vibration patterns. 	 
	 ******/
	public void clearList() {
		listInfo.patternList.freeAll();		
	}
	
	
	/******
	 * playVibration adds vibrotactile feedback parameters of frequency, amplitude, decaying rate, and location.
	 * This function can be called from an application directly. 	 
	 ******/
    public void playVibration(float frequency, float amplitude, float decayingFactor, Vector2 collidedLocation) {
    	float duration = 0f;
    	
		listlock.lock(); 
		try {		
	   		// Calculate the duration of vibration
	   		// natural log of 0.1 is -2.3026, thus t = 2.3026 / a for e^(at) = 0.1, t = log_e(0.1) / -a		
			
			if (soundFlag) {
				// This module is for the user experiment.
				// No need to take care of this for general purpose.
				Message msg = currentVibrationAPI.vibrationAPI.obtainMessage(99,  UPDATE_RATE,  SAMPLING_RATE,  amplitude);
				currentVibrationAPI.vibrationAPI.sendMessage(msg);													
			}
			else {
				if (decayingFactor == 0){ 
					duration = 0.020f;
					
					if (amplitude >= 1) 
						amplitude = 1;
					
					listInfo.patternList.addDecayingPatternPoint(decayingFactor, duration, frequency, amplitude, collidedLocation);
				}
				else if (decayingFactor == -1) {
					duration = amplitude * 0.020f;
					
					listInfo.patternList.addDecayingPatternPoint(0, duration, frequency, 1, collidedLocation);
				}
				else if (decayingFactor == -2) {
					duration = amplitude * 0.020f;
					
					listInfo.patternList.addDecayingPatternPoint(0, duration, frequency, amplitude, collidedLocation);
				}
				else {
					duration = 2.3026f / decayingFactor;
					if (amplitude >= 1) 
						amplitude = 1;
					listInfo.patternList.addDecayingPatternPoint(decayingFactor, duration, frequency, amplitude, collidedLocation);
				}
			}
		}
		finally {
			listlock.unlock();
		}
    }

    /******
	 * VibrationManager plans to support multiple actuators.
	 * For that, VibrationManager manages locations of actuators that are available
	 * on the screen coordinate.
	 * Put the location using the pixel information.
	 ******/
    public void addActuatorLocation(Vector2 actuator) {
    	actuatorLocation.add(new Vector2(actuator.x, actuator.y));
    	    	
    	if (actuatorLocation.size() > 1) {
    		// Each actuator has its own waveform buffer to play,
    		// So change the number of actuatorLocation.  
    		mVibBufferOrigin = new float[actuatorLocation.size()][BUFFER_SIZE];
    		mVibBufferSend = new float[actuatorLocation.size()][BUFFER_SIZE];
    	}
    }
    
    // This function is for the experimental use.
    public void setSoundFlag(boolean inFlag) {
    	soundFlag = inFlag;
    }
    
    
    /******
	 * generateVibSignal generates vibration signals using the current vibrotactile parameters
	 * to each actuator. 	 
	 ******/
	int generateVibSignal(int sampleSize, int actuator, float[] mVibBuffer) {
		float freq;
		double tempAmp = 0;

		int size = 0;
		
		if (actuatorLocation.size() <= 1)
			size = 1;
		else size = actuatorLocation.size();
		
				
		// If there are no patterns available,
		// play no vibrations.
		if (listInfo.patternList.patterns.size() <= 0) {
			for (int i = 0; i < sampleSize; i++) {
				mVibBuffer[i] = 0;
			}
			return sampleSize;
		}
					
		DecayingPatternPoint tempPoint = null;
	
		float localMax = 0;
		double increment = 1f / SAMPLING_RATE;
		double index = 0;

		for (int j = 0; j < listInfo.patternList.patterns.size(); j++) {
			tempPoint = listInfo.patternList.patterns.get(j);			
			tempAmp = tempPoint.amplitude;
			index = tempPoint.elapsedTime;
			freq = tempPoint.frequency;
			Vector2 collisionLocation = tempPoint.collidedLocation;
			Vector2 tempLocation;
						
			int i = 0;
			
			localMax = 0;
			float ratio = 1;
			float totalDistance = 0;

			// Calculate the ratio of amplitude by the location ratio between multiple actuators.
			// If there exists only one actuator, then just play the original signal.
			if (actuatorLocation.size() <= 1) {
				ratio = 1;
			}
			else if (actuatorLocation.size() == 2) {
				totalDistance = collisionLocation.dst(actuatorLocation.get(0)) + collisionLocation.dst(actuatorLocation.get(1));
								
				ratio = (float) (1 - collisionLocation.dst(actuatorLocation.get(actuator)) / totalDistance);
			}
			else {				
				for (int k = 0; k < actuatorLocation.size(); k++) {
					tempLocation = actuatorLocation.get(k);
					
					totalDistance += collisionLocation.dst(tempLocation.x, tempLocation.y);					 
				}
				ratio = (totalDistance - collisionLocation.dst(actuatorLocation.get(actuator))) / totalDistance;
			}
			
			if (j == 0) { 
				for (i = 0; i < sampleSize; i++) 
					mVibBuffer[i] = 0;					
			}
			
			for (i = 0; i < sampleSize; i++) {
				mVibBuffer[i] = (float)(mVibBuffer[i] + (tempAmp * ratio 
						* Math.sin((double)(2.0 * 3.14 * freq * index)) 
						* Math.exp((double)(-1.0 * tempPoint.decayingFactor * index)) 
								));
				index += increment;
			}									
		}	

		// Normalization after superposition of all patterns.
		localMax = 0;
		for (int i = 0; i < sampleSize; i++) {
			if (Math.abs(mVibBuffer[i]) > localMax)
				localMax = Math.abs(mVibBuffer[i]);
		}
		
		if (localMax > 1f) {			
			for (int i = 0; i < sampleSize; i++) {
				mVibBuffer[i] = mVibBuffer[i] / localMax;
			}					
		}
		
		return sampleSize;
	}
	
	/******
	 * setUpdateRate...  	 
	 ******/
	public void setUpdateRate(int inRate) {
		this.UPDATE_RATE = inRate;
	}
	
	/******
	 * addVibrationAPI adds handlers to this VibrationManager.
	 ******/
	public void addVibrationAPI(Handler inHandle) {
		int i = 0;
		for (i = 0; i < this.vibrationAPIList.size(); i++) {
			if (this.vibrationAPIList.get(i).vibrationAPI == inHandle) break;
		}
		if (i == this.vibrationAPIList.size())
			this.vibrationAPIList.add(new VibrationAPI(inHandle));
	}
	
	/******
	 * vibrationOnOff sets the vibration be available or not.
	 ******/
	public void vibrationOnOff(boolean invalue) {
		vibrationOn = invalue;
	}
	
	/******
	 * setCurrentVibrationAPI connects a inHandle handler to this VibrationManager,
	 * and the handler receives the waveform of next SAMPLING_RATE/UPDATE_RATE
	 ******/
	public void setCurrentVibrationAPI(Handler inHandle) {
		int i = 0;
		for (i = 0; i < this.vibrationAPIList.size(); i++) {
			if (this.vibrationAPIList.get(i).vibrationAPI == inHandle) break;
		}
		
		if (i == this.vibrationAPIList.size()) {
			this.vibrationAPIList.add(new VibrationAPI(inHandle));
		}
		this.currentVibrationAPI = this.vibrationAPIList.get(i);
	}

	private class VibrationAPI {
		private Handler vibrationAPI;
		// For a future use of multiple rendering.
		// private Vector2 location;
		
		VibrationAPI(Handler inH) {
			vibrationAPI = inH;
		}
	}		
	
	/******
	 * forcedVibrationRendering plays the vibration  using inSignal waveform to the hapticLocation.
	 * Currently, the location is not implemented yet.
	 ******/
	public void forcedVibrationRendering(float [] inSignal, Vector2 hapticLocation) {
		listlock.lock();
		try {
			forcedIndex = 0;
			forcedHapticSignal = inSignal;
			forcedHapticLocation = hapticLocation;			
			forced = true;		
		}
		finally {
			listlock.unlock();
		}
	}
}