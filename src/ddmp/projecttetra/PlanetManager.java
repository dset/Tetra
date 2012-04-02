package ddmp.projecttetra;

import java.util.ArrayList;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.PhysicsWorld;

/**
 * Manages all the planets currently spawned in the game. Makes
 * sure that the planets update on engine updates and that they
 * are removed when too far away from comet.
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
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		for(Planet planet : planets) {
			planet.update();
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
