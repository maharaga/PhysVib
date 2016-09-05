package PhysVib;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;

import PhysVib.Data.FixturePropagation;
import PhysVib.Data.contactNode;
import PhysVib.Data.userdata;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/******
 * Collision Catcher hooks the contact listener of AndEngine (or Box2D) and
 * calculates vibrotactile parameters using collision information 
 * if haptically focused fixtures are collided.
 * 
 * The main application must set its ContactListener as an instance of this CollisionCatcher,
 * and periodically call a method of 'setTrigger()'. 
 * Also, if an object is added to or removed from a scene, 'checkMass()' needs to be called
 * to update the mass normalization variable.
 ******/
public class CollisionCatcher extends Activity implements ContactListener {	
	/******
	 * List of instances of Contact that contain haptically focused fixtures.
	 ******/
	private List<contactNode> m_hapticContactList = new ArrayList<contactNode>();
	
	/******
	 * List of vibrotactile parameters with following impulses generated from the finished contacts. 
	 ******/
	private List<collisionData> impulseList = new ArrayList<collisionData>();

	
	/******
	 * Threshold of impulse filter.
	 * Only the impulses with higher than thresholdImpulse are dealt with. 
	 ******/
	public float thresholdImpulse = 0f;
	
	/******
	 * Instance of Vibration Manager. 
	 ******/
	public VibrationManager vibrationModule = new VibrationManager();
	
	
	/******
	 * Values for the normalization.
	 * maxVelDifference is suggested to set the longitudal pixels of a mobile application screen.
	 ******/
	public float gain = 0f;
	public float maxMass = 0f;
	public float mass = 0;
	float inertia = 0;
	float restitution = 0;
	float radius = 0;
	private float PIXEL_TO_METER_RATIO = 32f;
	public float maxVelDifference = 640f;
			
	int bodyCount = 0;
	
	/******
	 * Lock is used to prevent the preemption of callback functions during calculation.
	 ******/
	public Lock lock = new ReentrantLock();
	
	
	/******
	 * Variables used to search connected fixtures to the contact fixture using 
	 * Breadth-First Search algorithm. 
	 ******/
	List<Contact> cList = null;
	public int[][] cIndex = null;
	Vector2[][] cIndexVector = null;
	List<Integer> startIndexList = new ArrayList<Integer>();
	List<Body> bList = new ArrayList<Body>();
	World mWorld = null;
	int saturationCounter = 0;

	boolean setGainFlag = true;
	boolean emphasizeImpact = true;


	/******
	 * Set target fixture as removable if it is set to be removed in the user application.
	 ******/
	void setRemove(Fixture inFixture) {
		if (inFixture.getUserData() != null && inFixture != null) {
			if (((userdata)inFixture.getUserData()).toBeRemoved) {
				((userdata)inFixture.getUserData()).removable = true;			
			}
		}
		return;
	}
	
	// Constructor
	public CollisionCatcher() {
		
	}

	// Set this value during making environments is recommended 
	public void setThreshold(float realGain) {
		this.thresholdImpulse = realGain;
	}
	
	
	public void checkRemovable(contactNode thisContact) {
		Contact mContact = thisContact.m_contact;
		
		setRemove(mContact.getFixtureA());
		setRemove(mContact.getFixtureB());
	}

	/******
	 * setTrigger needs to be triggered regularly in a UI thread of a user application.
	 * This function checks contact list containing haptically focused fixtures, 
	 * and if the contact finishes or it seems to stick each other then generate vibrotactile feedback parameters.
	 ******/	
	public void setTrigger() {
		/******
		 * Operates iff there exists a list of contacts.
		 ******/
		if (m_hapticContactList.size() > 0) { 
			lock.lock();			
			
			try {	
				int i = 0;
				int size = m_hapticContactList.size();
				contactNode contactNode;
				Contact contact;			
				
													
				saturationCounter = 0;
				for (i = 0; i < size; i++) {								
					contactNode = m_hapticContactList.get(i);					
					
					if (contactNode == null) {
						m_hapticContactList.remove(i);												
						i--;
						size--;
						continue;
					}
					
					contact = contactNode.m_contact;
					
					/******
					 * If a contact passed the EndContact callback function, 
					 * then calculate its vibrotactile feedback parameter.
					 ******/
					if (contactNode.passedEndContact) { 																													
						
						
						/******
						 * If the contact object is removed right before generating the vibration,
						 * then Collision Catcher cannot generate vibrations because of no information.
						 ******/
						if (contact.getFixtureA() == null || contact.getFixtureB() == null || contact == null) {						
							m_hapticContactList.remove(i);												
							i--;
							size--;							
						}
						else {							
							if (mWorld == null)
								mWorld = contact.getFixtureA().getBody().getWorld();
							
							/******
							 * If two contact objects are not passed, then set them as removable
							 * and calculate its impulse.
							 ******/
							if (contactNode.hapticPlayed == false) {
								contactNode.hapticPlayed = true;
								calculateImpulse(contactNode);
								checkRemovable(contactNode);								
							}

							m_hapticContactList.remove(i);												
							i--;
							size--;																													
						}
						continue;
						
					}																		
					
					/******
					 * For the collisions that stop at the collision begin
					 * (System.currentTimeMillis() - contactNode.m_collisionBegin) >= 50
					 ******/

					if (System.currentTimeMillis() - contactNode.m_collisionBegin >= 50) { 																													
						contact = contactNode.m_contact;
						
						if (contact.getFixtureA() == null || contact.getFixtureB() == null || contact == null) {

							m_hapticContactList.remove(i);												
							i--;
							size--;							
						}
						else {							
							/******
							 * Sticked fixtures are forced to generate vibrations. 
							 ******/
							if (contactNode.hapticPlayed == false) {												
								contactNode.hapticPlayed = true;
								contactNode.m_collisionEnd = System.currentTimeMillis() - contactNode.m_collisionBegin;
								calculateImpulse(contactNode);
								checkRemovable(contactNode);
							}
															
							m_hapticContactList.remove(i);												
							i--;
							size--;
						}
						continue;
					}
				}								
				if (mWorld != null) {		
					/******
					 * Check all bodies in the environment and set removable variable to
					 * the fixtures that are not on the process of Collision Catcher.
					 ******/
					Iterator<Body> bodylist = mWorld.getBodies();
					ArrayList<Fixture> checkFixtures;
					int j = 0; int fixtureSize = 0;
					boolean checkDuplicate = false;
					while (bodylist.hasNext()) {
						Body nextBody = bodylist.next();
						
						if (nextBody != null) {
						
							checkFixtures = nextBody.getFixtureList();
							
							size = m_hapticContactList.size();										
							
							fixtureSize = checkFixtures.size();
							for (j = 0; j < fixtureSize; j++) {
								checkDuplicate = false;
								for (i = 0 ; i < size; i++) {											
									if (checkFixtures.get(j).equals(m_hapticContactList.get(i).m_contact.getFixtureA())
											|| checkFixtures.get(j).equals(m_hapticContactList.get(i).m_contact.getFixtureB())) {
										checkDuplicate = true;
									}												
								}
								if (!checkDuplicate) {
									setRemove(checkFixtures.get(j));
								}
							}
						}
					}
				}
			}						
    		finally{
    			lock.unlock();
    		}
		}			
	}
	
	
	/******
	 * Calculates impulses to the given contactnode information.
	 ******/
	public void impulseUsingCollisionInfo(contactNode contactnode) {
		Body tempbody1;
		Body tempbody2;
		Body tempbody;
		userdata tempData;
		collisionData temporalData = new collisionData();
		Vector2 impulseVector;
		
		Vector2 contactVector;
		Vector2 normalV;
		Vector2 tanV;
		
		float angularVelocity;
		
		
		
		if (contactnode.m_contact == null)
			return;
		
		if (contactnode.m_contact.getFixtureA() == null ||
				contactnode.m_contact.getFixtureB() == null) return;
			
		tempbody1 = contactnode.m_contact.getFixtureA().getBody();
		tempbody2 = contactnode.m_contact.getFixtureB().getBody();
		
		if (tempbody1 == null || tempbody2 == null || 
				contactnode.m_contact.getFixtureA().getUserData() == null || contactnode.m_contact.getFixtureB().getUserData() == null)
			return;
		
		/******
		 * contactnode contains a list of focused fixtures connected to its contact.
		 ******/
		ArrayList<FixturePropagation> tempPropagation = contactnode.mFocusedFixtures;
		FixturePropagation tPropagation = null;
		
		
		for (int i = 0; i < tempPropagation.size(); i++) {			
			tPropagation = tempPropagation.get(i);
			temporalData = new collisionData();
			tempData = (userdata)tPropagation.mFocusedFixture.getUserData();
			contactVector = new Vector2(0, 0);
			impulseVector = new Vector2(0, 0);
			
			
			
			/******
			 * If the focused fixture is in a static body,
			 * calculate the transferred impulse inversely using the touching fixtures.
			 ******/
			if (tPropagation.mFocusedFixture.getBody().getMass() == 0) { // static body				
				for (int j = 0; j < tPropagation.mConnectedFixtures.size(); j++) { // calculate impulse of connected fixtures' bodies
					
					contactVector.x = tPropagation.contactVectors.get(j).x;
					contactVector.y = tPropagation.contactVectors.get(j).y;
					normalV = contactVector.cpy().nor();
					tanV = new Vector2(-normalV.y, normalV.x); // 90 degree from the normal vector
					tempbody = tPropagation.mConnectedFixtures.get(j).getBody();
					
					
					impulseVector = new Vector2(tempbody.getLinearVelocity());																			
					impulseVector.sub(tPropagation.prevVelocities.get(j));
					impulseVector.mul(tempbody.getMass());
					normalV.mul(impulseVector.len());
					
					angularVelocity = tempbody.getAngularVelocity() - tPropagation.prevAngularVelocities.get(j);					
					tanV.mul(angularVelocity * tempbody.getInertia() / contactVector.len());
					
					tPropagation.sumOfImpulse.add(normalV.add(tanV));					
				}								
				temporalData.impulse = tPropagation.sumOfImpulse.len() / (tempData.mass + mass);	
			}
			else { 
				/******
				 * If the focused fixture is in a dynamic body,
				 * then calculate its impulse using its differential velocities and angular velocities.
				 ******/
				contactVector.x = tPropagation.contactVector.x;
				contactVector.y = tPropagation.contactVector.y;				
				normalV = contactVector.cpy().nor();
				tanV = new Vector2(-normalV.y, normalV.x);				
				tempbody = tPropagation.mFocusedFixture.getBody();								
				impulseVector = new Vector2(tempbody.getLinearVelocity());
				
				impulseVector.sub(tPropagation.prevVelocity);
				impulseVector.mul(tempbody.getMass());
				normalV.mul(impulseVector.len());
								
				angularVelocity = tempbody.getAngularVelocity() - tPropagation.prevAngularVelocity;				
				tanV.mul(angularVelocity * tempbody.getInertia() / contactVector.len());						
				
				tPropagation.sumOfImpulse.add(normalV.add(tanV));				
								
				
				temporalData.impulse = tPropagation.sumOfImpulse.len() / (tPropagation.mFocusedFixture.getBody().getMass() + mass);
				
			}				

			
			temporalData.naturalFrequency = tempData.naturalFrequency;
			temporalData.decayRate = tempData.decayRate;
			temporalData.collidedBody = tPropagation.mFocusedFixture;				
			 
			temporalData.collisionLocation = tPropagation.mFocusedFixture.getBody().getWorldPoint(tPropagation.mFocusedFixture.getBody().localPoint2.cpy()).cpy();			
			temporalData.hapticFocused = tempData.hapticCamera;
			
			if ((temporalData.impulse / gain) > this.thresholdImpulse) {				
				impulseList.add(temporalData);					
			}
		}
	}
	
	/******
	 * Manage the contact node
	 * Calculates impulse using impulseUsingCollisionInfo and sends it to Vibration Manager.
	 ******/
	public void calculateImpulse(contactNode thisNode) {
		collisionData temporalData;		
		
		if (thisNode.iscollided()) {
			if (thisNode.hapticEnabled) {
				impulseUsingCollisionInfo(thisNode);				
			}
		}

		// If there exists impulse information from collisions, then send the information to vibration.
    	if (impulseList.size() > 1)
    		Collections.sort(impulseList, new ImpulseComparator()); // Sorting by impulse		
		
		for (int j = 0; j < impulseList.size(); j++) {
			temporalData = impulseList.get(j);
		
			// Transfer a natural frequency, the calculated impulse with normalization, a decay rate value and the collided location.
			// V{A, F, D}
			vibrationModule.playVibration(temporalData.naturalFrequency, (float)(temporalData.impulse / gain), temporalData.decayRate, temporalData.collisionLocation);			
		}		
		
		if (impulseList != null)
			impulseList.clear();			
	}

	/******
	 * Set the normalization value using the maximum mass and velocity.
	 * If both of the values are high, then collisions become hard to generate strong vibrations.
	 ******/
	public void setGain(float inMass, float veldiff){		
		mass = inMass;
		gain = (float)(2 * veldiff);	
	}
	
	/******
	 * Set the normalization value using the maximum mass and velocity.
	 * 
	 ******/
	public void setGain(float inMass){		
		gain = inMass * maxVelDifference * maxVelDifference / 2;
	}
	
	private void calculateGains() {
		gain = (float)(2 * maxVelDifference);
	}

	/******
	 * Check all bodies in a scene,
	 * then find the maximum mass value. 
	 ******/
	public void checkMass() {					
		if (bList != null)
			bList.clear();
		
		if (mWorld == null) return;
		Iterator<Body> bodylist = mWorld.getBodies();
		
		if (bodylist != null) 
			mass = 0;				
	
		
		while (bodylist.hasNext()) {
			Body nextBody = bodylist.next();
			bList.add(nextBody);
		

			if (nextBody.getMass() != 0 && nextBody.getMass() > mass) {
				mass = nextBody.getMass();									
			}
			else if (nextBody.getMass() == 0 && ((userdata)nextBody.getFixtureList().get(0).getUserData()) != null) {
				if (((userdata)nextBody.getFixtureList().get(0).getUserData()).density != 0) {
					if (((userdata)nextBody.getFixtureList().get(0).getUserData()).mass > mass) {
						mass = ((userdata)nextBody.getFixtureList().get(0).getUserData()).mass;												
					}
				}
			}
			
		}
		bodyCount = mWorld.getBodyCount();
		if (setGainFlag) {					
			calculateGains();					
		}
	}
	
	/******
	 * beginContact is a callback function called when a contact occurs.
	 * 
	 * When a contact starts, this function finds haptically focused fixtures from all connected fixtures to the contact.
	 * Then add all related information to 'm_hapticContactList' for the vibrotactile parameter calculation.
	 ******/
	@Override
	public void beginContact(Contact contact) {
		// TODO Auto-generated method stub
		lock.lock();
		try {
			int i = 0;

			
			if (contact.getFixtureA() == null
					|| contact.getFixtureB() == null)
				return;
			
			if (((userdata)contact.getFixtureA().getUserData()) == null || ((userdata)contact.getFixtureB().getUserData()) == null)
				return;
			
			
			mWorld = contact.getFixtureA().getBody().getWorld();
			
			contactNode newNode = null;
			
			
			
			newNode = new contactNode();
									
			newNode.m_contact = contact;
			newNode.preContact.copyInfo(contact);
			
			newNode.m_collisionBegin = System.currentTimeMillis();
			
			if (((userdata)contact.getFixtureA().getUserData()).hapticCamera || ((userdata)contact.getFixtureB().getUserData()).hapticCamera)  {
				newNode.hapticEnabled = true;
			}
			
			
			/******
			 * Finds haptically focused fixtures from all connected fixtures to the contact.
			 * Then add all related information to 'm_hapticContactList' for the vibrotactile parameter calculation.
			 ******/
			findListFromContact(contact, newNode);
										
			
			// If so, then add this contactnode to the haptic contact list.
			if (newNode.mFocusedFixtures.size() == 0 && newNode.hapticEnabled == false) {
				newNode.hapticEnabled = false;
			}
			else {
				newNode.hapticEnabled = true;
			}				
						
			if (newNode.hapticEnabled) {
				m_hapticContactList.add(newNode);
			}
		}
		finally {
			lock.unlock();
		}
		
	}
	


	/******
	 * endContact is a callback function called when a contact finishes.
	 * 
	 * When a contact finishes, this function sets a boolean variable 'passEndContact' in the contactnode related to this contact.
	 * If the boolean variable is set, then setTrigger() calculates vibrotactile feedback parameters 
	 * using the contactnode and removes it from m_hapticContactList.	
	 ******/
	@Override
	public void endContact(Contact contact) {
		// TODO Auto-generated method stub
		lock.lock(); 
		try {

			if (contact == null) 
				return;				

			if (contact.getFixtureA() == null
					|| contact.getFixtureB() == null)
				return;
			
			if (((userdata)contact.getFixtureA().getUserData()) == null || ((userdata)contact.getFixtureB().getUserData()) == null)
				return;

			boolean checkSame = false;
			
			contactNode newNode = null;
			
			int i = 0;			
						
			
			if (contact.getFixtureA() == null || contact.getFixtureB() == null) {
				int size = m_hapticContactList.size();
				
				// Measure a finish time of collision, then store it. 
				for (i = 0; i < size; i++) {
					if (identifyTwoContacts(m_hapticContactList.get(i).m_contact, contact)) {				
						newNode = m_hapticContactList.get(i);
						newNode.m_collisionEnd = System.currentTimeMillis();
						newNode.passedEndContact = true;
						

						return;
					}
				}
			}
		
			int size = m_hapticContactList.size();
			
			// Measure a finish time of collision, then store it. 
			for (i = 0; i < size; i++) {
				if (identifyTwoContacts(m_hapticContactList.get(i).m_contact, contact)) {				
					newNode = m_hapticContactList.get(i);
					
					newNode.m_collisionEnd = System.currentTimeMillis();
					
					newNode.passedEndContact = true;
					
					checkSame = true;

					break;
				}
			}
			
			
		}
		finally {
			lock.unlock();
		}
	}
	
	
	

	/******
	 * Identify identity of contact1 and contact2.
	 * If they are the same, then return true, or return false.
	 ******/ 
	public boolean identifyTwoContacts(Contact contact1, Contact contact2) {		
		if (contact1.equals(contact2))
			return true;
		else return false;
	}

	
	// Collision information which are extracted from the contact and the userdata in fixture
	public class collisionData {
		public float impulse;
		public float naturalFrequency;
		public float decayRate;
		public Fixture collidedBody;	
		public int targetActuator;
		public Vector2 collisionLocation;
		public boolean hapticFocused;
		public float massRatio;
		
		collisionData() {
			impulse = 0;
			naturalFrequency = 0;
			decayRate = 0;
			massRatio = 1;
			collidedBody = null;
			hapticFocused = false;
			collisionLocation = new Vector2(0, 0);
		}	
		
		collisionData(collisionData inData1, collisionData inData2) {
			impulse = inData2.impulse * inData1.massRatio;
			naturalFrequency = inData2.naturalFrequency;
			decayRate = inData2.decayRate;
			massRatio = inData1.massRatio;
			collidedBody = inData1.collidedBody;
			hapticFocused = inData1.hapticFocused;
			collisionLocation = inData1.collisionLocation;
		}		
	}
	
	class ImpulseComparator implements Comparator<collisionData> {
		@Override
		public int compare(collisionData o1, collisionData o2) {
			if (o1.impulse > o2.impulse)
				return -1;
			else if (o1.impulse < o2.impulse)
				return 1;
			return 0;			
		}				
	}

	@Override
	public void postSolve(Contact arg0, ContactImpulse arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void preSolve(Contact arg0, Manifold arg1) {
		// TODO Auto-generated method stub
	}
	
	/******
	 * findListFromContact finds haptically focused connected fixtures to the contacted fixture (tContact)
	 * using Breadth-First Search algorithm.
	 * All found fixtures and related information is stored in tNode
	 ******/ 
	private void findListFromContact(Contact tContact, contactNode tNode) {		
		// Based on the graph cIndex, find all focused bodies connected to the tContact.
		findListFromGraph(tContact.getFixtureA(), tContact.getFixtureB(), tContact, tNode);
		
		return;
	}
	
	
	/******
	 * Normalization values are automatically updated when a new object is added in a scene
	 * if setGainFlag is set.
	 ******/ 
	public void setAutomaticGain(boolean setGains) {		
		setGainFlag = setGains;
	}

	private int findListFromGraph(Fixture startIndex, Fixture blockIndex, Contact tContact, contactNode tNode) {
		Body tBody;		
		int i, j;
		int queueIndex = 0;			
		ArrayList<Fixture> passedFixtures = new ArrayList<Fixture>();
		List<Contact> cList = mWorld.getContactList();
		Vector2 tempVector = null;
		
		FixturePropagation newFixture = null;
		
		passedFixtures.add(startIndex);
		passedFixtures.add(blockIndex);
				
		Fixture tempFixture;
		WorldManifold testFold = tContact.getWorldManifold();
		Contact tempContact, tempContact2;		
		
		// start from the contact fixture
		if (((userdata)startIndex.getUserData()).hapticCamera) {
			newFixture = new FixturePropagation(startIndex);
			
			if (startIndex.getBody().getMass() == 0) { // If static body is focused
				for (i = 0; i < cList.size(); i++) {					
					
					tempContact = cList.get(i);
					testFold = tempContact.getWorldManifold();					
					tempFixture = null;
					
					if (tempContact.getFixtureA().equals(startIndex)) 
						tempFixture = cList.get(i).getFixtureB();						 
					
					else if (tempContact.getFixtureB().equals(startIndex)) 
						tempFixture = cList.get(i).getFixtureA();
										
					if (tempFixture != null) {
						tBody = tempFixture.getBody();
						tempVector = new Vector2(tBody.localPoint2);
						newFixture.mConnectedFixtures.add(tempFixture);
						newFixture.contactVectors.add(tBody.getWorldPoint(tempVector).cpy().sub(testFold.getPoints()[0]));
						
						tempVector = new Vector2(tBody.getLinearVelocity().x, tBody.getLinearVelocity().y);						
						newFixture.prevVelocities.add(tempVector);
						newFixture.prevAngularVelocities.add(tBody.getAngularVelocity());
						newFixture.contactPoints.add(testFold.getPoints()[0].cpy());
					}
				}				
			}
			else { // If dynamic body is focused				
				tempVector = new Vector2(startIndex.getBody().localPoint2);

				tBody = startIndex.getBody();
				newFixture.contactVector = tBody.getWorldPoint(tempVector).cpy().sub(testFold.getPoints()[0].cpy());
								
				newFixture.prevVelocity = new Vector2(tBody.getLinearVelocity().x, tBody.getLinearVelocity().y);
				
				newFixture.contactPoint = testFold.getPoints()[0].cpy();
				newFixture.prevAngularVelocity = startIndex.getBody().getAngularVelocity();			
			}
		}
		
		if (newFixture != null)
			tNode.mFocusedFixtures.add(newFixture);
		
		// Then searches the connected fixtures
		Fixture tempFixture2 = null;
		while (queueIndex < passedFixtures.size()) {
			startIndex = passedFixtures.get(queueIndex);
			
			for (i = 0; i < cList.size(); i++) {
				tempContact = cList.get(i);
				tempFixture = null;
				newFixture = null;
				if (tempContact.getFixtureA().equals(startIndex))
					tempFixture = tempContact.getFixtureB();
				else if (tempContact.getFixtureB().equals(startIndex))
					tempFixture = tempContact.getFixtureA();
				
				if (tempFixture != null && tempFixture.getUserData() != null) {
					// Both objects are static, then pass this contact
					if (startIndex.getBody().getMass() == 0 && tempFixture.getBody().getMass() == 0)
						continue;
					
					if (((userdata)tempFixture.getUserData()).hapticCamera) {
						newFixture = new FixturePropagation(tempFixture);
						
						if (tempFixture.getBody().getMass() == 0) { // If static body is focused
							for (j = 0; j < cList.size(); j++) {					
								tempContact2 = cList.get(j);
								testFold = tempContact2.getWorldManifold();
								tempFixture2 = null;								
								
								if (tempContact2.getFixtureA().equals(tempFixture)) 
									tempFixture2 = tempContact2.getFixtureB();						 
								
								else if (tempContact2.getFixtureB().equals(tempFixture)) 
									tempFixture2 = tempContact2.getFixtureA();
													
								if (tempFixture2 != null && tempFixture2.getUserData() != null && tempFixture2.getBody().getMass() != 0) {
									tBody = tempFixture2.getBody();
									tempVector = new Vector2(tBody.localPoint2);
									newFixture.mConnectedFixtures.add(tempFixture2);
									newFixture.contactVectors.add(tBody.getWorldPoint(tempVector).cpy().sub(testFold.getPoints()[0]));
									
									tempVector = new Vector2(0, 0);
									tempVector.x = tBody.getLinearVelocity().x; tempVector.y = tBody.getLinearVelocity().y;
									newFixture.prevVelocities.add(tempVector);
									newFixture.contactPoints.add(testFold.getPoints()[0].cpy());
									
									newFixture.prevAngularVelocities.add(tBody.getAngularVelocity());			
								}
							}			
							if (newFixture.mConnectedFixtures.size() == 0) 
								newFixture = null;
						}
						else { // If dynamic body is focused				
							tempVector = new Vector2(tempFixture.getBody().localPoint2);
							
							newFixture.contactVector = (tempFixture.getBody().getWorldPoint(tempVector).cpy().sub(testFold.getPoints()[0].cpy()));
							
							tempVector = new Vector2(0, 0);
							tempVector.x = tempFixture.getBody().getLinearVelocity().x; tempVector.y = tempFixture.getBody().getLinearVelocity().y;
							newFixture.prevVelocity = tempVector;
							newFixture.prevAngularVelocity = tempFixture.getBody().getAngularVelocity();
							newFixture.contactPoint = testFold.getPoints()[0].cpy();
						}
					}
					
					if (newFixture != null) 
						tNode.mFocusedFixtures.add(newFixture);
					
					
					if (!passedFixtures.contains(tempFixture))
						passedFixtures.add(tempFixture);
				}
			}
			queueIndex++;
		}
		
		return queueIndex + 1;
	}
	
	/******
	 * Set the pixel_to_meter_ratio.
	 * This value is used in the vibration module to calculate the vibration location. 
	 ******/ 
	public void setPixelToMeterRatio(float inValue) {
		PIXEL_TO_METER_RATIO = inValue;
		vibrationModule.pixel_to_meter = inValue;
	}
	
	/******
	 * Set the camera pixels.
	 * 
	 * This value is used to set the normalization constant. 
	 ******/
	public void setCameraPixel(float inPixel) {
		maxVelDifference = inPixel / PIXEL_TO_METER_RATIO;
		vibrationModule.maxveldifference = inPixel;
	}	
	
}