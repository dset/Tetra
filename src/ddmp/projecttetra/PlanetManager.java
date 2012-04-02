package ddmp.projecttetra;

import java.util.ArrayList;

import org.andengine.engine.Engine;
import org.andengine.engine.Engine.EngineLock;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.util.debug.Debug;

/**
 * Manages all the planets currently spawned in the game. Makes
 * sure that the planets update on engine updates and that they
 * are removed when dead.
 */
public class PlanetManager implements IUpdateHandler {
	
	private Engine engine;
	private PhysicsWorld physicsWorld;
	private ArrayList<Planet> planets;
	
	public PlanetManager(Engine engine, PhysicsWorld physicsWorld) {
		this.engine = engine;
		this.physicsWorld = physicsWorld;
		planets = new ArrayList<Planet>();
	}
	
	public void addPlanet(Planet planet) {
		planets.add(planet);
		Debug.d("Number of planets: " + planets.size());
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		for(Planet planet : planets) {
			planet.update();
		}
		
		for(int i = planets.size() - 1; i >= 0; i--) {
			if(planets.get(i).isDead()) {
				remove(planets.get(i));
			}
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	private void remove(Planet planet) {
		EngineLock engineLock = engine.getEngineLock();
		engineLock.lock();
		
		engine.getScene().detachChild(planet.getShape());
		planet.getShape().dispose();
		physicsWorld.unregisterPhysicsConnector(planet);
		physicsWorld.destroyBody(planet.getBody());
		
		engineLock.unlock();
		planets.remove(planet);
	}

}
