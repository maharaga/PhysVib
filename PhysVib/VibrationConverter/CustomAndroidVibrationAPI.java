package PhysVib.VibrationConverter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;


public class CustomAndroidVibrationAPI {
	private Handler vibHandler;		
	
	private long [] vibOutput;
	private long [] tempOutput;
	private float [] stackInput;
		
	private boolean vibrationFlag = false;
	private boolean previouslyPlayed = false;
	
	Lock listlock = new ReentrantLock();	
	
	Vibrator mVibrator = null;
	
	private float ampThreshold = 0.1f;
	
	private int VM_SAMPLING_RATE = 5000;
	private int VM_UPDATE_RATE = 100;
	
	
	

	
	CustomAndroidVibrationAPI(Vibrator inVibrator) {
		mVibrator = inVibrator;
		stackInput = new float[VM_SAMPLING_RATE / VM_UPDATE_RATE];
		
		vibHandler = new Handler((Looper.getMainLooper())) {
        	@Override
        	public void handleMessage(Message msg){
        		// arg1 : update rate, arg2 : sampling rate
        		listlock.lock();      
        		try {        			
        			if (VM_SAMPLING_RATE != msg.arg2 || VM_UPDATE_RATE != msg.arg1) {
        				VM_SAMPLING_RATE = msg.arg2;
        				VM_UPDATE_RATE = msg.arg1;
        				stackInput = new float[VM_SAMPLING_RATE / VM_UPDATE_RATE];     
        			}

        			if (msg.what == 1) {
        				float data[] = (float[])msg.obj;
	        			for (int i = 0; i < data.length; i++) {
	        				stackInput[i] = data[i];	        				 
	        			}	        			
        			}
        			else if (msg.what == 2) {
	        			for (int i = 0; i < msg.arg2 / msg.arg1; i++) {
	        				stackInput[i] = 0;	        				 
	        			}        				
	        			vibrationFlag = false;
        			}
        			
        			int count = 0;
        			
        			
        			
        			int i = 0;        		
        			boolean vibrationHighFlag = false;
        			
        			for (i = 0; i < stackInput.length; i++) {
        				if (vibrationHighFlag) {
        					if (stackInput[i] < ampThreshold) { 
        						count++;
        						vibrationHighFlag = false;
        					}        					
        				}
        				else {
        					if (stackInput[i] >= ampThreshold) { 
        						count++;
        						vibrationHighFlag = true;
        					}
        				}        				
        			}
        			
        			if (count > 0) {
        				tempOutput = new long[count];
        				if (count % 2 == 0) // Even point, then add stop point.
        					vibOutput = new long[count + 1];
        				else vibOutput = new long[count];
        				vibOutput[0] = 0;
        				count = 0;
        				vibrationHighFlag = false;

            			for (i = 0; i < stackInput.length; i++) {
            				if (vibrationHighFlag) {
            					if (stackInput[i] < ampThreshold) {
            						tempOutput[count] = i * 1000 / msg.arg2;
            						count++;
            						vibrationHighFlag = false;	            			
            					}        					
            				}
            				else {
            					if (stackInput[i] >= ampThreshold) {
            						tempOutput[count] = i * 1000 / msg.arg2;
            						count++;
            						vibrationHighFlag = true;	            						
            					}
            				}        				
            			}
            			
            			for (i = 0; i < tempOutput.length - 1; i++) {
            				vibOutput[i + 1] = tempOutput[i + 1] - tempOutput[i];	            				
            			}
            			
            			if (count % 2 == 0) // Adding a stopping endpoint.
            				vibOutput[i] = 1;
            			
            			vibrationFlag = true;
        			}
        			else {
        				vibOutput = null;
        				vibrationFlag = false;
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
					
			       // synthesis loop
			       while(true){			    	   
			    	   listlock.lock();			    	   
			    	   try {			    		   
				    	   if(vibrationFlag && vibOutput != null) {
				    		   if (mVibrator != null)
				    			   mVibrator.vibrate(vibOutput,  -1);
				    		   vibrationFlag = false;
				    		   previouslyPlayed = true;
				    	   }
				    	   else if (!vibrationFlag) {
				    		   if (previouslyPlayed) {
				    			   mVibrator.cancel();
				    			   previouslyPlayed = false;
				    		   }
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
     	
	Handler getHandler() {
		return this.vibHandler;
	}
}