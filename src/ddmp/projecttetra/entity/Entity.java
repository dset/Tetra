package ddmp.projecttetra.entity;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsWorld;

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
	
}
