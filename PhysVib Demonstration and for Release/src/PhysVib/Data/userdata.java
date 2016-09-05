package PhysVib.Data;

import android.util.Log;

/******
 * A userdata containing information for vibrotactile rendering.
 * Additional data can be added in this data class.  
 ******/
public class userdata {
	/******
	 * Essential variables 
	 ******/
	public boolean hapticCamera;
	public float naturalFrequency;
	public float decayRate;
	public float density;
	public float mass;
	public boolean toBeRemoved = false;
	public boolean removable = false;

	
	public userdata() {
		objectType = 0;
		objectID = 0;
		hapticCamera = false;
		naturalFrequency = 0;
		decayRate = 0;
		density = 0;
		mass = 0;
	}
	
	public userdata(boolean flag) {
		objectType = 0;
		objectID = 0;
		hapticCamera = flag;			
		naturalFrequency = 0;
		decayRate = 0;
		density = 0;
		mass = 0;
	}

	public void remove() {
		toBeRemoved = true;
	}
	
	public boolean isRemovable() {
		if (removable)
			return true;				
		return false;
	}
	
	/******
	 * if the body is static, then calculate mass manually 
	 ******/	
	public void calculateMass(float width, float height, float radius, float ptm) {
		// Calculates the mass of a fixture attached by this userdata,
		// by its shape of a rectangular or a sphere.
		if (radius == 0) {
			mass = width * height * density / ptm / ptm;
		}
		else mass = (float)Math.PI * radius * radius * density / ptm / ptm / 4;
	}
	
	/******
	 * User-added variables and methods for an application.  
	 ******/
	public int objectType;
	public int objectID;
	protected Object mPhysVibUserData;
	
	public int getType() {
		return this.objectType;
	}
	
	public void setType(int inType) {
		this.objectType = inType;
	}
	
	public void setPhysVibUserData (Object inData) {
		mPhysVibUserData = inData;
	}

	public Object getPhysVibUserData () {
		return mPhysVibUserData;
	}
}
