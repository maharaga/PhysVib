package PhysVib.VibrationConverter;

import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class CustomAudioOutput {
	private Handler audioHandler;		
	public static final int SAMPLING_RATE = 44100;
	private int BUFFER_SIZE = (int)(SAMPLING_RATE * 0.01);
	private int UPDATE_RATE = 100;
	public static final int MAX_VALUE = Short.MAX_VALUE * 3 / 4;
	
	private short [] soundOutput;
	private short [] testOutput;
	public float [] woodsound;
	public float [] metalsound;
	public float [] beachsound;
	
	private boolean vibrationFlag = false;
	private boolean soundFlag = false;
	private float amplitude = 0;
	
	Lock listlock = new ReentrantLock();
	float referenceTime = 0;
	int index = 0;	
	public int vibMode = 0;
	
	float carrierIndex = 0;
	float carrierIncrement = 2.0f * (float)Math.PI * 10000 / 44100; 
	
	
	public int currentExperimentCondition = 0;
	
	
	
	float input;                    // input sample
	float output;                   // output sample
	float v;                        // This is the intermediate value that
	                                //    gets stored in the delay registers
	float old1;                     // delay register 1, initialized to 0
	float old2;                     // delay register 2, initialized to 0

	/* filter coefficients */
	float omega1	= (float) (2 * 3.14 * 205/44100);	// f is your center frequency
	float sn1 = (float)Math.sin(omega1);
	float cs1 = (float)Math.cos(omega1);
	float alpha1 = (float) (sn1/(2*1));        // Qvalue is none other than Q!
	float a0 = 1.0f + alpha1;		// a0
	float b0 = alpha1;			// b0
	float b1 = 0.0f;			// b1/b0
	float b2= -alpha1/b0;			// b2/b0
	float a1= -2.0f * cs1/a0;		// a1/a0
	float a2= (1.0f - alpha1)/a0;	        // a2/a0
	float k = b0/a0;

	
	
	public CustomAudioOutput() {
		soundOutput = new short[BUFFER_SIZE * 2];
		
		woodsound = new float[10736];
		beachsound = new float[15000];
		metalsound = new float[19736];
		
		for (int i = 0; i < BUFFER_SIZE * 2; i++) {
			soundOutput[i] = 0;
		}
		
		testOutput = new short[441];
		for (int i = 0; i < 441; i++) {
			testOutput[i] = 0;
		}
		
		
		
		
		audioHandler = new Handler() {
        	@Override
        	public void handleMessage(Message msg){
        		
        		listlock.lock(); 
        		try { // arg1 : update rate, arg2 : sampling rate
        			
        			vibrationFlag = true;
        			int samples = msg.arg2 / msg.arg1;
        			        			
	        		if (msg.what == 1){
	        			float data[][] = (float[][])msg.obj;
	        			if (BUFFER_SIZE != SAMPLING_RATE / msg.arg1) {
	        				BUFFER_SIZE = SAMPLING_RATE / msg.arg1;
	        				setUpdateRate(msg.arg1);
	        				
	        				soundOutput = new short[BUFFER_SIZE * 2];	        					        				
	        			}
	        			
	        			if (data.length == 2) {	        				
		        			for (int i = 0; i < BUFFER_SIZE; i++) {
		        				soundOutput[2 * i] = (short)(data[0][i * samples / BUFFER_SIZE] * MAX_VALUE);	        				
		        				soundOutput[2 * i + 1] = (short)(data[1][i * samples / BUFFER_SIZE] * MAX_VALUE);
		        			}		        			
	        			}
	        			else if (data.length == 4) {
	        				for (int i = 0; i < BUFFER_SIZE; i++) {
		        				soundOutput[2 * i] = (short)(data[0][i * samples / BUFFER_SIZE] * MAX_VALUE / 2);	        				
		        				soundOutput[2 * i] += (short)(data[1][i * samples / BUFFER_SIZE] * MAX_VALUE * Math.sin(carrierIndex)/ 2);
		        				soundOutput[2 * i + 1] = (short)(data[2][i * samples / BUFFER_SIZE] * MAX_VALUE / 2);
		        				soundOutput[2 * i + 1] += (short)(data[3][i * samples / BUFFER_SIZE] * MAX_VALUE * Math.sin(carrierIndex)/ 2);
		        				
		        				carrierIndex += carrierIncrement;
		        			}
	        			}
	        			else {
		        			for (int i = 0; i < BUFFER_SIZE; i++) {
		        				soundOutput[2 * i] = 0;	        				
		        				
		        				soundOutput[2 * i + 1] = (short)(data[0][i * samples / BUFFER_SIZE] * MAX_VALUE);		        				
		        				
		        				carrierIndex += carrierIncrement;		        				
		        			}	        					        			
	        			}
	        		}
	        		else if (msg.what == 2) {
	        			float data[] = (float[])msg.obj;
	        			if (BUFFER_SIZE != SAMPLING_RATE / msg.arg1) {
	        				BUFFER_SIZE = SAMPLING_RATE / msg.arg1;
	        				setUpdateRate(msg.arg1);
	        				soundOutput = new short[BUFFER_SIZE * 2];
	        			}
	        			
	        			for (int i = 0; i < BUFFER_SIZE; i++) {
	        				soundOutput[2 * i] = (short)(data[i * samples / BUFFER_SIZE] * MAX_VALUE);         					
	        				soundOutput[2 * i + 1] = (short)(data[i * samples / BUFFER_SIZE] * MAX_VALUE);
	        			}        			        			
	        		}
	        		else if (msg.what == 0) {
	        			if (BUFFER_SIZE != SAMPLING_RATE / msg.arg1) {
	        				BUFFER_SIZE = SAMPLING_RATE / msg.arg1;
	        				setUpdateRate(msg.arg1);
	        				soundOutput = new short[BUFFER_SIZE * 2];
	        			}
	        			
	        			for (int i = 0; i < BUFFER_SIZE; i++) {
	        				soundOutput[2 * i] = 0;         					
	        				soundOutput[2 * i + 1] = 0;
	        			}        			        				        			
	        		}
	        		else if (msg.what == 99) {
	        			amplitude = (Float)msg.obj;
        				index = 0;
        				soundFlag = true;
        				
        				
        				BUFFER_SIZE = (int)(SAMPLING_RATE * 0.01);
        				Log.e("maharaga", "transferred amplitude: " + amplitude);
        				if (amplitude >= 1) amplitude = 1;

	        			if (currentExperimentCondition == 1) { // Wood collision
	        				
	        			}
	        			else if (currentExperimentCondition == 2) { // Metal can
	        				
	        			}
	        			else if (currentExperimentCondition == 3) { // Beach ball
	        				
	        			}	        			
	        		}
        		}
        		finally {
        			listlock.unlock();
        		}
        	}        	
        };
        
       
        
        new Thread() {
			@Override
			public void run() {
				try	{
					setPriority(Thread.MAX_PRIORITY);
					int i = 0;
					int value = 0;
					float input = 0;
					old1 = 0;
					old2 = 0;
					
					int buffsize = AudioTrack.getMinBufferSize(SAMPLING_RATE,
			                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
			        // create an audiotrack object
			        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
			                                          SAMPLING_RATE, AudioFormat.CHANNEL_OUT_STEREO,
			                                  AudioFormat.ENCODING_PCM_16BIT, buffsize,
			                                  AudioTrack.MODE_STREAM);

			        // start audio
			       audioTrack.play();

			       // synthesis loop
			       while(true){
			    	   listlock.lock();
			    	   try {
			    		   /*
			    		   if (vibMode == 1) {
				    		   for (i = 0; i < BUFFER_SIZE; i++) {
			        				soundOutput[2 * i] = 0;         
			        				if (Math.random() > 0.5) value = 1;
			        				else value = 0;
			        				soundOutput[2 * i + 1] = (short)(value * MAX_VALUE);
				    		   }				    		
	
				    		   if (old1 == 0 && old2 == 0) {
				    			   old1 = soundOutput[1];
				    			   old2 = soundOutput[3];
				    		   }
				    		   
				    		   for (int j = 2; j < BUFFER_SIZE; j++) {
				    			   input = soundOutput[2 * j + 1];
				    			   v = k*input - a1*old1 - a2*old2;
					    			output = v + b1*old1 + b2*old2;
					    			old2 = old1;
					    			old1 = v;
					    		   soundOutput[2 * j + 1] = (short)output;
				    		   }
				    		   audioTrack.write(soundOutput, 0, soundOutput.length);
			    		   }
			    		   else if (vibMode == 2) {
			    			   for (i = 0; i < BUFFER_SIZE; i++) {
			        				soundOutput[2 * i] = 0;         
			        				if (Math.random() > 0.5) value = 1;
			        				else value = 0;
			        				soundOutput[2 * i + 1] = (short)(value * MAX_VALUE);
				    		   }				    		
	
				    		   audioTrack.write(soundOutput, 0, soundOutput.length);
			    		   }
			    		   */
				    	   if(vibrationFlag) {
							audioTrack.write(soundOutput, 0, soundOutput.length);
				    	   }
				    	   if (soundFlag) {				    		   
				    		   if (currentExperimentCondition == 1) {
				    			   
				    			   if (index + BUFFER_SIZE < 10736) {
						    		   for (i = 0; i < BUFFER_SIZE; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(woodsound[index + i] * MAX_VALUE * amplitude);
					        			}
						    		   index += BUFFER_SIZE;
				    			   }
				    			   else {
				    				   for (i = 0; i < 10736 - index; i++) {				    					   
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(woodsound[index + i] * MAX_VALUE * amplitude);
				    				   }
				    				   for (; i < BUFFER_SIZE; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(0);				    					   
				    				   }
				    				   soundFlag = false;
				    				   index = 0;
				    			   }
				    		   }
				    		   else if (currentExperimentCondition == 0) {				    			   
				    			   if (index + BUFFER_SIZE < 19736) {
						    		   for (i = 0; i < BUFFER_SIZE; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(metalsound[index + i] * MAX_VALUE * amplitude);
					        			}
						    		   index += BUFFER_SIZE;
				    			   }
				    			   else {
				    				   for (i = 0; i < 19736 - index; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(metalsound[index + i] * MAX_VALUE * amplitude);
				    				   }
				    				   for (; i < BUFFER_SIZE; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(0);				    					   
				    				   }
				    				   soundFlag = false;
				    				   index = 0;
				    			   }
					    		   
				    		   } 
				    		   else if (currentExperimentCondition == 2) {				    			   
				    			   if (index + BUFFER_SIZE < 15000) {
						    		   for (i = 0; i < BUFFER_SIZE; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(beachsound[index + i] * MAX_VALUE * amplitude);
					        			}
						    		   index += BUFFER_SIZE;
				    			   }
				    			   else {
				    				   for (i = 0; i < 15000 - index; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(beachsound[index + i] * MAX_VALUE * amplitude);
				    				   }
				    				   for (; i < BUFFER_SIZE; i++) {
					        				soundOutput[2 * i] = 0;         					
					        				soundOutput[2 * i + 1] = (short)(0);				    					   
				    				   }
				    				   soundFlag = false;
				    				   index = 0;
				    			   }
				    		   }
				    		   
				    		   audioTrack.write(soundOutput, 0, soundOutput.length);
				    		   
				    	   }
			    	   }
			    	   finally {
			    		   listlock.unlock();
			    	   }
			    	   
			    	   Thread.sleep(5);
			        }
			       			      
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
        }.start();
        	
	}
	
	public Handler getHandler() {
		return this.audioHandler;
	}

	public int getUpdateRate() {
		return UPDATE_RATE;
	}

	public void setUpdateRate(int mUpdateRate) {
		UPDATE_RATE = mUpdateRate;
	}
}