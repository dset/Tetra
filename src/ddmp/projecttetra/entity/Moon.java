package ddmp.projecttetra.entity;

import org.andengine.engine.Engine;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
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
	private static final float COMET_BOOST_TIME = 0.5f;
	private static final float COMET_BOOST_MULTIPLIER = 2f;
	
	private Entity planet;
	private Entity comet;
	
	public static Moon createMoon(Engine engine, PhysicsWorld physicsWorld, float x, float y,
			Entity planet, Entity comet) {
		float scale = Utilities.getRandomFloatBetween(MOON_MIN_SIZE, MOON_MAX_SIZE);
		float size = scale * TetraActivity.CAMERA_HEIGHT;
		Sprite sprite = new Sprite(x, y, size, size, RegionManager.getInstance().get(
				RegionManager.Region.MOON), engine.getVertexBufferObjectManager());
		Body body = PhysicsFactory.createCircleBody(physicsWorld, sprite, BodyType.StaticBody,
				MOON_FIXTURE_DEF);
		return new Moon(engine, physicsWorld, sprite, body, planet, comet);
	}
	
	private Moon(Engine engine, PhysicsWorld physicsWorld, Sprite sprite, Body body,
			Entity planet, Entity comet) {
		super(engine, physicsWorld, sprite, body);
		this.planet = planet;
		this.comet = comet;
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
		if(getDistancePixels(comet) < getWidth() / 2 + comet.getWidth() / 2) {
			onCommetCollision();
		}
	}
	
	private void onCommetCollision() {
		new EntityVelocityModifier(engine, comet, COMET_BOOST_TIME, COMET_BOOST_MULTIPLIER);
		engine.vibrate(80);
		MoonPieceCreator.createMoonPieces(engine, physicsWorld, this);
		destroySelf();
	}

	@Override
	public void reset() {
		
	}

}
