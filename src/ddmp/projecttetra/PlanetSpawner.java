package ddmp.projecttetra;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import com.badlogic.gdx.math.Vector2;

/**
 * Spawns planets in the vicinity of the comet.
 */
public class PlanetSpawner implements IUpdateHandler {
	
	private static final float MIN_SPAWN_TIME = 0.5f;
	private static final float MAX_SPAWN_TIME = 0.8f;
	
	private Engine engine;
	private PhysicsWorld physicsWorld;
	private PlanetManager planetManager;
	private Comet comet;
	private float timeSinceSpawn;
	
	/* Vector that is reused over and over instead of creating a new
	 * vector every time a spawn point is calculated. */
	private Vector2 spawnPoint;
	
	public PlanetSpawner(Engine engine, PhysicsWorld physicsWorld, PlanetManager pManager, 
										Comet comet) {
		this.engine = engine;
		this.physicsWorld = physicsWorld;
		this.planetManager = pManager;
		this.comet = comet;
		spawnPoint = new Vector2();
		timeSinceSpawn = getSpawnTime();
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		if (timeSinceSpawn <= 0.0f && planetManager.canSpawn()) {
			/* Spawn planet */
			Vector2 spt = getSpawnPoint(0); // TODO Should not be 0 here.
			
			/* Check so it is not too close to another planet. */
			if(!planetManager.isGravitated(spt)) {
				Planet planet = new Planet(spt.x, spt.y, comet, engine, physicsWorld);
				engine.getScene().attachChild(planet.getShape());
				physicsWorld.registerPhysicsConnector(planet.getPhysicsConnector());
				this.planetManager.addPlanet(planet);
				timeSinceSpawn = getSpawnTime();
			}
		} else {
			timeSinceSpawn -= pSecondsElapsed;
		}
	}

	private float getSpawnTime() {
		return MIN_SPAWN_TIME + (float) Math.random() * (MAX_SPAWN_TIME - MIN_SPAWN_TIME);
	}

	@Override
	public void reset() {
		// Nothing to do here.
	}
	
	private Vector2 getSpawnPoint(float size) {
		double angle = Math.atan2(comet.getBody().getLinearVelocity().y, comet.getBody().getLinearVelocity().x);
		angle += Math.PI/4 - Math.random() * Math.PI/2;
		double spawnDistance = (engine.getCamera().getWidth() / 2) * (engine.getCamera().getWidth() / 2) +
								(engine.getCamera().getHeight() / 2) * (engine.getCamera().getHeight() / 2);
		spawnDistance = Math.sqrt(spawnDistance) + (float) 1/Math.sqrt(2) * size;
		spawnDistance = spawnDistance + Math.random() * 2 * spawnDistance;
		
		float tmpX = (float) (engine.getCamera().getCenterX() + Math.cos(angle) * spawnDistance) - size/2;
		float tmpY = (float) (engine.getCamera().getCenterY() + Math.sin(angle) * spawnDistance) - size/2;
		spawnPoint.set(tmpX, tmpY);
		return spawnPoint;
	}

}
