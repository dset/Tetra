package ddmp.projecttetra.entity;

import org.andengine.engine.Engine;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import ddmp.projecttetra.RegionManager;
import ddmp.projecttetra.TetraActivity;
import ddmp.projecttetra.Utilities;

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
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		/* Attract comet by gravity */
		Vector2 cometCenterPos = comet.getCenter();
		if(isGravitating(cometCenterPos)) {
			 /* Apply gravity if comet is moving towards or almost towards planet. */
			Vector2 velTmp = comet.getLinearVelocity();
			Vector2 centerPosition = getCenter();
			Vector2 cometCenterPosition = comet.getCenter();
			Vector2 direction = centerPosition.sub(cometCenterPosition);
			double angle = Math.acos(direction.dot(velTmp) / (direction.len() * velTmp.len()));
			if(Math.abs(angle) < GRAVITY_ANGLE) {
				float distance = getDistanceMeters(comet);
				direction = direction.nor();
				float gravityScalar = (float) (GRAVITY_CONSTANT * mass * comet.getMass()
						/ distance);
				Vector2 gravityForce = direction.mul(gravityScalar);
				comet.applyForce(gravityForce.x, gravityForce.y);
				
				/* Give comet boost if close to planet. */
				if (distance < BOOST_DISTANCE) {
					float boostScalar = GRAVITY_CONSTANT * mass * comet.getMass() / distance;
					Vector2 boostForce = Vector2Pool.obtain().set(velTmp).nor().mul(boostScalar);
					comet.applyForce(boostForce.x, boostForce.y);
					Vector2Pool.recycle(boostForce);
				}
			} else {
				/* Give comet some extra speed away from planet. */
//				float escapeScalar = GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
//						/ comet.getBody().getPosition().cpy().sub(this.getBody().getPosition()).len();
//				Vector2 escapeForce = velTmp.cpy().nor().mul(escapeScalar);
//				comet.getBody().applyForce(escapeForce, comet.getBody().getPosition());
			}
			Vector2Pool.recycle(velTmp);
			Vector2Pool.recycle(centerPosition);
			Vector2Pool.recycle(cometCenterPosition);
		}
		Vector2Pool.recycle(cometCenterPos);
		
		/* Die if far away from comet */
		if(getDistancePixels(comet) > KILL_DISTANCE) {
			destroySelf();
		}
		
	}

	@Override
	public void reset() {
		
	}

	public boolean isGravitating(Vector2 point) {
		Vector2 centerPosition = getCenter();
		float distance = centerPosition.dst(point);
		Vector2Pool.recycle(centerPosition);
		
		return distance < GRAVITY_FIELD_DISTANCE * getWidth() / 2;
	}
	
	@Override
	public float getMass() {
		return mass;
	}
}
