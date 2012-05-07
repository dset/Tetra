package ddmp.projecttetra.entity;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * An entity is something in the game that has a sprite and a
 * physical body and that updates on every tick.
 */
public abstract class Entity implements IUpdateHandler {
	
	protected Engine engine;
	protected PhysicsWorld physicsWorld;
	protected PhysicsConnector bodySpriteConnector;
	
	public Entity(Engine engine, PhysicsWorld physicsWorld, Sprite sprite, Body body) {
		this.engine = engine;
		this.physicsWorld = physicsWorld;
		body.setUserData(this);
		bodySpriteConnector = new PhysicsConnector(sprite, body);
	}
	
	/**
	 * Attaches this entity to the scene and registers it to the
	 * physics simulation.
	 */
	public void registerSelf() {
		engine.getScene().attachChild(bodySpriteConnector.getShape());
		engine.getScene().registerUpdateHandler(this);
		physicsWorld.registerPhysicsConnector(bodySpriteConnector);
	}
	
	/**
	 * Detaches this entity from the scene and unregisters it from
	 * the physics simulation.
	 */
	public void unregisterSelf() {
		acquireEngineLock();
		unregister();
		releaseEngineLock();
	}
	
	private void unregister() {
		engine.getScene().detachChild(bodySpriteConnector.getShape());
		engine.getScene().unregisterUpdateHandler(this);
		physicsWorld.unregisterPhysicsConnector(bodySpriteConnector);
	}
	
	/**
	 * Detaches this entity from the scene and disposes its sprite.
	 * Unregisters it from the physics simulation and destroys its
	 * body.
	 */
	public void destroySelf() {
		acquireEngineLock();
		unregister();
		bodySpriteConnector.getShape().dispose();
		physicsWorld.destroyBody(bodySpriteConnector.getBody());
		bodySpriteConnector = null;
		releaseEngineLock();
	}
	
	private void acquireEngineLock() {
		engine.getEngineLock().lock();
	}
	
	private void releaseEngineLock() {
		engine.getEngineLock().unlock();
	}
	
	/**
	 * Returns x-coordinate of center of entity in scene coordinates.
	 */
	public float getCenterX() {
		return bodySpriteConnector.getShape().getSceneCenterCoordinates()[Sprite.VERTEX_INDEX_X];
	}
	
	/**
	 * Returns y-coordinate of center of entity in scene coordinates.
	 */
	public float getCenterY() {
		return bodySpriteConnector.getShape().getSceneCenterCoordinates()[Sprite.VERTEX_INDEX_Y];
	}
	
	/**
	 * Returns a vector holding the coordinates to the center of the
	 * entity in scene coordinates. The vector should be returned
	 * to the vector pool by calling Vector2Pool.recycle(v);
	 */
	public Vector2 getCenter() {
		return Vector2Pool.obtain(getCenterX(), getCenterY());
	}
	
	/**
	 * Returns a vector holding the linear velocity of the entity.
	 * The vector should be returned to the vector pool by calling
	 * Vector2Pool.recycle(v);
	 */
	public Vector2 getLinearVelocity() {
		return Vector2Pool.obtain(bodySpriteConnector.getBody().getLinearVelocity());
	}
	
	/**
	 * Returns the mass of the entity.
	 */
	public float getMass() {
		return bodySpriteConnector.getBody().getMass();
	}
	
	/**
	 * Applies the given force at the mass center of the entity.
	 */
	public void applyForce(float forceX, float forceY) {
		Vector2 position = bodySpriteConnector.getBody().getPosition();
		bodySpriteConnector.getBody().applyForce(forceX, forceY, position.x, position.y);
	}
	
	/**
	 * Returns the distance between this entity and the given entity
	 * in meters.
	 */
	public float getDistanceMeters(Entity other) {
		Vector2 thisPos = bodySpriteConnector.getBody().getPosition();
		Vector2 otherPos = other.bodySpriteConnector.getBody().getPosition();
		return thisPos.dst(otherPos);
	}
	
	/**
	 * Returns the distance between this entity and the given entity
	 * in pixels.
	 */
	public float getDistancePixels(Entity other) {
		Vector2 thisPos = getCenter();
		Vector2 otherPos = other.getCenter();
		return thisPos.dst(otherPos);
	}
	
	/**
	 * Returns true if this entity has been destroyed.
	 */
	public boolean isDestroyed() {
		return bodySpriteConnector == null;
	}
}
