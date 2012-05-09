package ddmp.projecttetra.entity.util;

import org.andengine.engine.Engine;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;

import com.badlogic.gdx.math.Vector2;

import ddmp.projecttetra.Utilities;
import ddmp.projecttetra.entity.Entity;
import ddmp.projecttetra.entity.Moon;

/**
 * Creates moons for planets.
 */
public class MoonCreator {
	
	private static final float MIN_DISTANCE = 1.2f; /* In planet radii. */
	private static final float MAX_DISTANCE = 2.0f; /* In planet radii. */
	private static final int MIN_NUMBER = 3;
	private static final int MAX_NUMBER = 5;
	
	/**
	 * Creates moons around the given planet.
	 */
	public static void createMoons(Engine engine, PhysicsWorld physicsWorld, Entity planet) {
		int numPlanets = Utilities.getRandomIntBetween(MIN_NUMBER, MAX_NUMBER);
		for(int i = 0; i < numPlanets; i++) {
			createMoon(engine, physicsWorld, planet);
		}
	}
	
	private static void createMoon(Engine engine, PhysicsWorld physicsWorld, Entity planet) {
		Vector2 spawnPoint = getRandomSpawnPoint(planet);
		Moon.createMoon(engine, physicsWorld, spawnPoint.x, spawnPoint.y, planet).registerSelf();
		Vector2Pool.recycle(spawnPoint);
	}
	
	private static Vector2 getRandomSpawnPoint(Entity planet) {
		Vector2 planetPosition = planet.getCenter();
		Vector2 planetOffset = getPlanetOffset(planet);
		Vector2 spawnPoint = planetOffset.add(planetPosition);
		Vector2Pool.recycle(planetPosition);
		return spawnPoint;
	}
	
	private static Vector2 getPlanetOffset(Entity planet) {
		float distance = Utilities.getRandomFloatBetween(MIN_DISTANCE, MAX_DISTANCE) *
				planet.getWidth() / 2;
		float randomAngle = Utilities.getRandomFloatBetween(0, 2 * (float) Math.PI);
		Vector2 planetOffset = Vector2Pool.obtain(0, 1).mul(distance);
		Utilities.rotateVector(planetOffset, randomAngle);
		return planetOffset;
	}
	
}
