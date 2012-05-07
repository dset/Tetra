package ddmp.projecttetra.entity;

import java.util.ArrayList;

import org.andengine.engine.Engine;
import org.andengine.engine.Engine.EngineLock;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
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

/**
 * A planet in the game.
 */
public class Planet extends Entity {
	
	private static final FixtureDef PLANET_FIXTURE_DEF = 
			PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final float PLANET_MIN_SIZE = 0.35f; //In percent of camera height
	private static final float PLANET_MAX_SIZE = 0.55f;	//In percent of camera height
	private static final float GRAVITY_CONSTANT = 7f;
	private static final float KILL_DISTANCE_SQUARED = 2250000;
	/* Relative planet radius. */
	private static final float GRAVITY_FIELD_DISTANCE = 5f;
	/* Angle to determine when to apply gravity. */
	private static final float GRAVITY_ANGLE = (float) (Math.PI/1.5);
	private static final float BOOST_DISTANCE = 6f;
	
	private Comet comet;
	private MoonManager moonManager;
	/* Since planets are static their body has mass 0. But mass is needed to calculate
	 * effect of gravity. Therefore mass is added. */
	private float mass;
	
	public static Planet createPlanet(Engine engine, PhysicsWorld physicsWorld, float x, float y,
			Comet comet) {
		float scale = PLANET_MIN_SIZE + (PLANET_MAX_SIZE - PLANET_MIN_SIZE) * (float) Math.random();
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
		this.moonManager = new MoonManager(engine, physicsWorld);
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		/* Attract comet by gravity */
		float cometCenterX = comet.getShape().getX() + comet.getShape().getScaleCenterX();
		float cometCenterY = comet.getShape().getY() + comet.getShape().getScaleCenterY();
		Vector2 cometCenterPos = Vector2Pool.obtain().set(cometCenterX, cometCenterY);
		if(isGravitating(cometCenterPos)) {
			 /* Apply gravity if comet is moving towards or almost towards planet. */
			Vector2 velTmp = comet.getBody().getLinearVelocity();
			Vector2 planetDir = Vector2Pool.obtain().set(body.getPosition()).sub(comet.getBody().getPosition());
			double angle = Math.acos(planetDir.dot(velTmp) / (planetDir.len() * velTmp.len()));
			Vector2Pool.recycle(planetDir);
			if(Math.abs(angle) < GRAVITY_ANGLE) {
				Vector2 distanceVector = Vector2Pool.obtain().set(comet.getBody().getPosition()).sub(this.body.getPosition());
				float distance = distanceVector.len();
				float gravityScalar = (float) (-GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
						/ distance);
				Vector2 gravityForce = distanceVector.nor().mul(gravityScalar);
				comet.getBody().applyForce(gravityForce, comet.getBody().getPosition());
				Vector2Pool.recycle(distanceVector);
				
				/* Give comet boost if close to planet. */
				if (distance < BOOST_DISTANCE) {
					float boostScalar = GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
							/ distance;
					Vector2 boostForce = Vector2Pool.obtain().set(velTmp).nor().mul(boostScalar);
					comet.getBody().applyForce(boostForce, comet.getBody().getPosition());
					Vector2Pool.recycle(boostForce);
				}
			} else {
				/* Give comet some extra speed away from planet. */
//				float escapeScalar = GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
//						/ comet.getBody().getPosition().cpy().sub(this.getBody().getPosition()).len();
//				Vector2 escapeForce = velTmp.cpy().nor().mul(escapeScalar);
//				comet.getBody().applyForce(escapeForce, comet.getBody().getPosition());
			}
		}
		Vector2Pool.recycle(cometCenterPos);
		
		moonManager.updateAll();
		
		float distanceX = shape.getX() + shape.getScaleCenterX() - cometCenterX;
		float distanceY = shape.getY() + shape.getScaleCenterY() - cometCenterY;
		float distanceSq = distanceX * distanceX + distanceY * distanceY;
		/* Die if far away from comet */
		if(distanceSq > KILL_DISTANCE_SQUARED) {
			dead = true;
			moonManager.killAll();
		}
		
	}

	@Override
	public void reset() {
		
	}

	public boolean isGravitating(Vector2 point) {
		float distanceX = shape.getX() + shape.getScaleCenterX() - point.x;
		float distanceY = shape.getY() + shape.getScaleCenterY() - point.y;
		float distanceSq = distanceX * distanceX + distanceY * distanceY;
		if(distanceSq < Math.pow(GRAVITY_FIELD_DISTANCE * planetSize, 2)){
			return true;
		} else {
			return false;
		}
	}
	
	private class MoonManager {
		
		private static final float MIN_DISTANCE = 1.75f; /* In planet radii. */
		private static final float MAX_DISTANCE = 3.0f; /* In planet radii. */
		private static final int MIN_NUMBER = 3;
		private static final int MAX_NUMBER = 5;
		
		private Engine engine;
		private PhysicsWorld physicsWorld;
		private ArrayList<Moon> moons;
		
		public MoonManager(Engine engine, PhysicsWorld physicsWorld) {
			this.engine = engine;
			this.physicsWorld = physicsWorld;
			moons = new ArrayList<Moon>();
			int number = (int) (MIN_NUMBER + (MAX_NUMBER - MIN_NUMBER) * Math.random());
			for(int i = 0; i < number; i++) {
				createMoon();
			}
		}
		
		private void createMoon() {
			float distance = (float) (MIN_DISTANCE + (MAX_DISTANCE - MIN_DISTANCE) * Math.random());
			distance *= shape.getWidth()/2;
			float angle = (float) (2 * Math.PI * Math.random());
			float x = (float) (shape.getX() + shape.getWidth()/2 + distance * Math.cos(angle));
			float y = (float) (shape.getY() + shape.getHeight()/2 + distance * Math.sin(angle));
			Moon moon = new Moon(x, y, engine, physicsWorld);
			moons.add(moon);
			engine.getScene().attachChild(moon.getShape());
			physicsWorld.registerPhysicsConnector(moon.getPhysicsConnector());
		}
		
		public void updateAll() {
			int size = moons.size();
			for(int i = 0; i < size; i++) {
				moons.get(i).update();
			}
			
			EngineLock engineLock = engine.getEngineLock();
			engineLock.lock();
			for(int i = size - 1; i >= 0; i--) {
				if(moons.get(i).isDead()) {
					remove(moons.get(i));
				}
			}
			engineLock.unlock();
		}
		
		private void remove(Moon moon) {
			engine.getScene().detachChild(moon.getShape());
			moon.getShape().dispose();
			physicsWorld.unregisterPhysicsConnector(moon.getPhysicsConnector());
			physicsWorld.destroyBody(moon.getBody());
			moons.remove(moon);
		}
		
		public void killAll() {
			EngineLock engineLock = engine.getEngineLock();
			engineLock.lock();
			
			int size = moons.size();
			Moon moon = null;
			for(int i = size - 1; i >= 0; i--) {
				moon = moons.get(i);
				remove(moon);
			}
			
			engineLock.unlock();
		}
		
	}
}