package PhysVib.Data;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

public class FixturePropagation {
	/******
	 * When a contact starts, and if this fixture is focused (mFocusedFixture),
	 * then preserves all the related information required for the vibration generation
	 * to the instance of this class.
	 ******/
	public Fixture mFocusedFixture;
	
	/******
	 * velocities, contact points, angular velocities, and touched fixtures to the contacted fixture
	 * if this fixture is the static object.
	 * Because the mFocusedFixture is immovable, the resultant impulse is calculated using
	 * the information of fixtures touching this mFocusedFixture.
	 ******/
	public ArrayList<Fixture> mConnectedFixtures;
	public ArrayList<Vector2> prevVelocities;
	public ArrayList<Vector2> contactVectors;
	public ArrayList<Vector2> contactPoints;
	public ArrayList<Float> prevAngularVelocities;
	
	
	/******
	 * velocity, contact point, angular velocity, and touched fixtures to the contacted fixture
	 * if this fixture is the dynamic object..
	 * The impulse is calculated using the differential physical properties of the mFocusedFixture.
	 ******/	
	public Vector2 contactVector;
	public Vector2 prevVelocity;
	public Vector2 contactPoint;
	public float prevAngularVelocity;
	
	
	/******
	 * The resultant impulse.
	 ******/
	public Vector2 sumOfImpulse;
	
	FixturePropagation() {
		mFocusedFixture = null;
		mConnectedFixtures = new ArrayList<Fixture>();
		prevVelocities = new ArrayList<Vector2>();
		prevAngularVelocities = new ArrayList<Float>();
		contactVectors = new ArrayList<Vector2>();
		contactPoints = new ArrayList<Vector2>();
		sumOfImpulse = new Vector2(0, 0);
		
		contactVector = new Vector2(0, 0);
		prevVelocity = new Vector2(0, 0);
		prevAngularVelocity = 0;
		contactPoint = new Vector2(0, 0);
		
	}
	
	public FixturePropagation(Fixture inFixture) {
		mFocusedFixture = inFixture;			
		mConnectedFixtures = new ArrayList<Fixture>();
		prevVelocities = new ArrayList<Vector2>();
		prevAngularVelocities = new ArrayList<Float>();
		contactVectors = new ArrayList<Vector2>();
		contactPoints = new ArrayList<Vector2>();
		sumOfImpulse = new Vector2(0, 0);
		
		contactVector = new Vector2(0, 0);
		prevVelocity = new Vector2(0, 0);
		contactPoint = new Vector2(0, 0);
		prevAngularVelocity = 0;
	}
}