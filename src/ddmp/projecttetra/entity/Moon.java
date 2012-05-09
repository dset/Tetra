package ddmp.projecttetra.entity;

import java.util.List;

import org.andengine.engine.Engine;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import ddmp.projecttetra.RegionManager;
import ddmp.projecttetra.TetraActivity;
import ddmp.projecttetra.Utilities;
import ddmp.projecttetra.entity.util.EntityVelocityModifier;
import ddmp.projecttetra.entity.util.MoonPieceCreator;

/**
 * A moon in the game.
 */
public class Moon extends Entity {
	
	private static final FixtureDef MOON_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, true);
	private static final float MOON_MIN_SIZE = 0.08f; //In percent of camera height
	private static final float MOON_MAX_SIZE = 0.12f;	//In percent of camera height
	
	private Entity planet;
	
	public static Moon createMoon(Engine engine, PhysicsWorld physicsWorld, float x, float y,
			Entity planet) {
		float scale = Utilities.getRandomFloatBetween(MOON_MIN_SIZE, MOON_MAX_SIZE);
		float size = scale * TetraActivity.CAMERA_HEIGHT;
		Sprite sprite = new Sprite(x, y, size, size, RegionManager.getInstance().get(
				RegionManager.Region.MOON), engine.getVertexBufferObjectManager());
		Body body = PhysicsFactory.createCircleBody(physicsWorld, sprite, BodyType.StaticBody,
				MOON_FIXTURE_DEF);
		return new Moon(engine, physicsWorld, sprite, body, planet);
	}
	
	private Moon(Engine engine, PhysicsWorld physicsWorld, Sprite sprite, Body body,
			Entity planet) {
		super(engine, physicsWorld, sprite, body);
		this.planet = planet;
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		if(planet.isDestroyed()) {
			destroySelf();
			return;
		}
		checkForCollisionWithCommet();
	}
	
	private void checkForCollisionWithCommet() {
		List<Contact> contacts = physicsWorld.getContactList();
		Contact contact = null;
		int size = contacts.size();
		for(int i = 0; i < size; i++) {
			contact = contacts.get(i);
			if(contact.isTouching()) {
				if(contact.getFixtureA() == null || contact.getFixtureB() == null) {
					continue;
				}
				Object aData = contact.getFixtureA().getBody().getUserData();
				Object bData = contact.getFixtureB().getBody().getUserData();
				if((aData == this || bData == this) && 
						(aData instanceof Comet || bData instanceof Comet)) {
					Comet comet;
					if(aData instanceof Comet) {
						comet = (Comet) aData;
					} else {
						comet = (Comet) bData;
					}
					onCommetCollision(comet);
				}
			}
		}
	}
	
	private void onCommetCollision(Entity comet) {
		new EntityVelocityModifier(engine, comet, 0.5f, 2f);
		engine.vibrate(80);
		MoonPieceCreator.createMoonPieces(engine, physicsWorld, this);
		destroySelf();
	}

	@Override
	public void reset() {
		
	}

}
