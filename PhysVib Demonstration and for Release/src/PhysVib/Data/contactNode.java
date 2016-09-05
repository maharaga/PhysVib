package PhysVib.Data;
import java.util.ArrayList;

import com.badlogic.gdx.physics.box2d.Contact;

public class contactNode {
	/******
	 * velocity, contact point, angular velocity, and touched fixtures to the contacted fixture
	 * if this fixture is the dynamic object..
	 * The impulse is calculated using the differential physical properties of the mFocusedFixture.
	 ******/
	
	public Contact m_contact;		
	public long m_collisionBegin;
	public long m_collisionEnd;
	int connectedFocusedFixtures = 0;		
	
	public ContactInformation preContact;
	public ArrayList<FixturePropagation> mFocusedFixtures;
	
	float mass;
	public boolean hapticEnabled = false;
	public boolean hapticPlayed = false;
	public boolean passedEndContact = false;
	
	public contactNode() {
		m_collisionBegin = 0;
		m_collisionEnd = 0;
		mass = 0;
		m_contact = null;
		preContact = new ContactInformation();
		connectedFocusedFixtures = 0;
		mFocusedFixtures = new ArrayList<FixturePropagation>();
	}

	
	public boolean iscollided() {
		if (m_collisionEnd != 0 && m_contact != null)
			return true;
		else return false;		
	}
	
	public float collisionTime() {
		return (m_collisionEnd - m_collisionBegin);
	}
	
	public Contact getContact() {
		return m_contact;
	}		
}