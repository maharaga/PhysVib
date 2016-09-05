package PhysVib.Data;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;

public class ContactInformation {
	// In Java, all information is passed by reference.
	// Because of that, additional container to obtain velocities in the past is needed.
	Vector2 body1Velocity;
	Vector2 body2Velocity;
	float body1AngularVelocity;
	float body2AngularVelocity;
	
	public void copyInfo(Contact contact) {
		// Store velocities from the contacted bodies at the contact time
		body1Velocity = contact.getFixtureA().getBody().getLinearVelocity().cpy();
		body2Velocity = contact.getFixtureB().getBody().getLinearVelocity().cpy();
		body1AngularVelocity = contact.getFixtureA().getBody().getAngularVelocity();
		body2AngularVelocity = contact.getFixtureB().getBody().getAngularVelocity();			
	}
}