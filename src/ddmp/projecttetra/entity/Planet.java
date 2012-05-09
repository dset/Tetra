package ddmp.projecttetra.entity;

import org.andengine.engine.Engine;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import ddmp.projecttetra.RegionManager;
import ddmp.projecttetra.TetraActivity;
import ddmp.projecttetra.Utilities;
import ddmp.projecttetra.entity.util.BoostGravityHandler;
import ddmp.projecttetra.entity.util.MoonCreator;

/**
 * A planet in the game.
 */
public class Planet extends Entity {
	
	private static final FixtureDef PLANET_FIXTURE_DEF = 
			PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final float PLANET_MIN_SIZE = 0.35f; //In percent of camera height
	private static final float PLANET_MAX_SIZE = 0.55f;	//In percent of camera height
	private static final float GRAVITY_CONSTANT = 7f;
	private static final float KILL_DISTANCE = 1500;
	/* Relative planet radius. */
	private static final float GRAVITY_FIELD_DISTANCE = 5f;
	/* Angle to determine when to apply gravity. */
	private static final float GRAVITY_ANGLE = (float) (Math.PI/1.5);
	private static final float BOOST_DISTANCE = 6f;
	
	private BoostGravityHandler gravityHandler;
	private Comet comet;
	/* Since planets are static their body has mass 0. But mass is needed to calculate
	 * effect of gravity. Therefore mass is added. */
	private float mass;
	
	public static Planet createPlanet(Engine engine, PhysicsWorld physicsWorld, float x, float y,
			Comet comet) {
		float scale = Utilities.getRandomFloatBetween(PLANET_MIN_SIZE, PLANET_MAX_SIZE);
		float size = scale * TetraActivity.CAMERA_HEIGHT;
		Sprite sprite = new Sprite(x, y, size, size, RegionManager.getInstance().get(
				RegionManager.Region.PLANET), engine.getVertexBufferObjectManager());
		Body body = PhysicsFactory.createCircleBody(physicsWorld, sprite, 
				BodyType.StaticBody, PLANET_FIXTURE_DEF);
		return new Planet(engine, physicsWorld, sprite, body, comet);
	}
	
	private Planet(Engine engine, PhysicsWorld physicsWorld, Sprite sprite, Body body, 
			Comet comet) {
		super(engine, physicsWorld, sprite, body);
		
		this.mass = (float) (Math.PI * Math.pow(sprite.getWidthScaled()/2 *
				(1/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT), 2));
		this.comet = comet;
		this.gravityHandler = new BoostGravityHandler(this, comet, GRAVITY_CONSTANT, 
				GRAVITY_FIELD_DISTANCE, BOOST_DISTANCE, GRAVITY_ANGLE);
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		if(getDistancePixels(comet) > KILL_DISTANCE) {
			destroySelf();
		}
	}

	@Override
	public void reset() {
		
	}

	public boolean isGravitating(Vector2 point) {
		return gravityHandler.isGravitating(point);
	}
	
	@Override
	public float getMass() {
		return mass;
	}
	
	@Override
	public void registerSelf() {
		super.registerSelf();
		MoonCreator.createMoons(engine, physicsWorld, this);
		engine.getScene().registerUpdateHandler(gravityHandler);
	}
	
	@Override
	public void unregisterSelf() {
		super.unregisterSelf();
		engine.getScene().unregisterUpdateHandler(gravityHandler);
	}
	
	@Override
	public void destroySelf() {
		super.destroySelf();
		engine.getScene().unregisterUpdateHandler(gravityHandler);
	}
}
