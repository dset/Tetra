package ddmp.projecttetra;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;

import com.badlogic.gdx.math.Vector2;

import ddmp.projecttetra.entity.Comet;
import ddmp.projecttetra.entity.Planet;

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
				Planet planet = Planet.createPlanet(engine, physicsWorld, spt.x, spt.y, comet);
				planet.registerSelf();
				this.planetManager.addPlanet(planet);
				timeSinceSpawn = getSpawnTime();
			}
		} else {
			timeSinceSpawn -= pSecondsElapsed;
		}
	}

	private float getSpawnTime() {
		return Utilities.getRandomFloatBetween(MIN_SPAWN_TIME, MAX_SPAWN_TIME);
	}

	@Override
	public void reset() {
		// Nothing to do here.
	}
	
	private Vector2 getSpawnPoint(float size) {
		Vector2 direction = comet.getLinearVelocity().nor();
		float randomAngle = Utilities.getRandomFloatBetween((float) -Math.PI/4, (float) Math.PI/4);
		Utilities.rotateVector(direction, randomAngle);
		float spawnDistance = (engine.getCamera().getWidth() / 2) * (engine.getCamera().getWidth() / 2) +
								(engine.getCamera().getHeight() / 2) * (engine.getCamera().getHeight() / 2);
		spawnDistance = (float) Math.sqrt(spawnDistance);
		spawnDistance = Utilities.getRandomFloatBetween(spawnDistance, 3 * spawnDistance);
		Vector2 centerOffset = direction.mul(spawnDistance);
		spawnPoint.set(comet.getCenterX(), comet.getCenterY());
		spawnPoint.add(centerOffset);
		Vector2Pool.recycle(direction);
		return spawnPoint;
	}

}
