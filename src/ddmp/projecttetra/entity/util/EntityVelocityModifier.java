package ddmp.projecttetra.entity.util;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.util.Vector2Pool;

import com.badlogic.gdx.math.Vector2;

import ddmp.projecttetra.entity.Entity;

/**
 * Boosts an entity's velocity for a period of time.
 */
public class EntityVelocityModifier implements IUpdateHandler {
	
	private Engine engine;
	private Entity boosted;
	private float durationLeft;
	private float acceleration;
	
	/**
	 * 
	 * @param engine
	 * @param boosted
	 * @param duration		In seconds.
	 * @param multiplier	How much the velocity will increase
	 * 						during the duration. For example if
	 * 						multiplier = 2 and nothing else affects
	 * 						the entities velocity, the velocity will
	 * 						be doubled after duration seconds.
	 */
	public EntityVelocityModifier(Engine engine, Entity boosted, float duration, float multiplier) {
		this.engine = engine;
		this.boosted = boosted;
		this.durationLeft = duration;
		this.acceleration = getAcceleration(boosted, duration, multiplier);
		engine.getScene().registerUpdateHandler(this);
	}
	
	private float getAcceleration(Entity boosted, float duration, float multiplier) {
		Vector2 initialVelocity = boosted.getLinearVelocity();
		float initialSpeed = initialVelocity.len();
		Vector2Pool.recycle(initialVelocity);
		float newSpeed = initialSpeed * multiplier;
		return (newSpeed - initialSpeed) / duration;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		applyBoost();
		durationLeft -= pSecondsElapsed;
		if(durationLeft <= 0) {
			engine.getScene().unregisterUpdateHandler(this);
		}
	}
	
	private void applyBoost() {
		Vector2 oldVelocity = boosted.getLinearVelocity();
		Vector2 boostForce = oldVelocity.nor().mul(acceleration * boosted.getMass());
		boosted.applyForce(boostForce.x, boostForce.y);
		Vector2Pool.recycle(oldVelocity);
	}

	@Override
	public void reset() {
		
	}
	
}
