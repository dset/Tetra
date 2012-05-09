package ddmp.projecttetra.entity.util;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.util.Vector2Pool;

import com.badlogic.gdx.math.Vector2;

import ddmp.projecttetra.entity.Entity;

/**
 * Attracts an entity towards another entity. If the attracted entity
 * gets close to the attracting entity it gets a speed boost.
 */
public class BoostGravityHandler implements IUpdateHandler {
	
	private Entity attractor;
	private Entity attracted;
	private float gravityConstant;
	private float fieldDistance;
	private float boostDistance;
	private float attractionAngle;
	
	public BoostGravityHandler(Entity attractor, Entity attracted, float gravityConstant,
			float fieldDistance, float boostDistance, float attractionAngle) {
		this.attractor = attractor;
		this.attracted = attracted;
		this.gravityConstant = gravityConstant;
		this.fieldDistance = fieldDistance;
		this.boostDistance = boostDistance;
		this.attractionAngle = attractionAngle;
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		Vector2 attractedCenterPos = attracted.getCenter();
		if(isGravitating(attractedCenterPos) && getAbsEntitiesAngle() < attractionAngle) {
			applyGravity();
			if (attractor.getDistanceMeters(attracted) < boostDistance) {
				applyBoost();
			}
		}
		Vector2Pool.recycle(attractedCenterPos);
	}
	
	private float getAbsEntitiesAngle() {
		Vector2 attractedVelocity = attracted.getLinearVelocity();
		Vector2 direction = getDirectionVector();
		double angle = Math.acos(direction.dot(attractedVelocity) / (direction.len() *
				attractedVelocity.len()));
		Vector2Pool.recycle(attractedVelocity);
		Vector2Pool.recycle(direction);
		return (float) Math.abs(angle);
	}
	
	private void applyGravity() {
		float distance = attractor.getDistanceMeters(attracted);
		float gravityScalar = gravityConstant * attractor.getMass() * attracted.getMass()
				/ distance;
		Vector2 direction = getDirectionVector().nor();
		Vector2 gravityForce = direction.mul(gravityScalar);
		attracted.applyForce(gravityForce.x, gravityForce.y);
		Vector2Pool.recycle(direction);
	}
	
	private void applyBoost() {
		float distance = attractor.getDistanceMeters(attracted);
		float boostScalar = gravityConstant * attractor.getMass() * attracted.getMass() / distance;
		Vector2 boostForce = attracted.getLinearVelocity().nor().mul(boostScalar);
		attracted.applyForce(boostForce.x, boostForce.y);
		Vector2Pool.recycle(boostForce);
	}
	
	private Vector2 getDirectionVector() {
		Vector2 attractedCenterPos = attracted.getCenter();
		Vector2 attractorCenterPos = attractor.getCenter();
		Vector2 direction = attractorCenterPos.sub(attractedCenterPos);
		Vector2Pool.recycle(attractedCenterPos);
		return direction;
	}

	@Override
	public void reset() {
		
	}
	
	public boolean isGravitating(Vector2 point) {
		Vector2 attractorCenterPos = attractor.getCenter();
		float distance = attractorCenterPos.dst(point);
		Vector2Pool.recycle(attractorCenterPos);
		
		return distance < fieldDistance * attractor.getWidth() / 2;
	}

}
