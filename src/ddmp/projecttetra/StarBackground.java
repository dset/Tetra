package ddmp.projecttetra;

import java.util.ArrayList;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;

import com.badlogic.gdx.math.Vector2;

import ddmp.projecttetra.entity.Comet;

public class StarBackground extends Entity {
	/* The number of stars generated for the pool stars are chosen from. */
	private static final int NUM_GENERATED_STARS = 40;
	/* The number of max simultaneously visible stars. */
	private static final int NUM_ACTIVE_STARS = 20;
	/* The stars speed relative comet. */
	private static final float RELATIVE_SPEED = 0.9f;
	
	private Comet comet;
	private Camera camera;
	private ArrayList<Star> activeStars;
	private StarPool starPool;
	
	public StarBackground(Comet comet, Camera camera, Engine engine) {
		this.comet = comet;
		this.camera = camera;
		this.activeStars = new ArrayList<Star>(NUM_GENERATED_STARS);
		this.starPool = new StarPool(camera, engine.getVertexBufferObjectManager());
		this.starPool.generateStars(NUM_GENERATED_STARS, this);
		setChildrenIgnoreUpdate(true);
		spawnInitialStars();
	}
	
	private void spawnInitialStars() {
		for(int i = 0; i < NUM_ACTIVE_STARS; i++) {
			spawnStarInsideView();
		}
	}
	
	@Override
	public void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
		
		recycleDeadStars();
		if(activeStars.size() < NUM_ACTIVE_STARS) {
			spawnStarOutsideView();
		}
		updatePosition(pSecondsElapsed);
	}
	
	private void recycleDeadStars() {
		int size = activeStars.size();
		Star star = null;
		for(int i = size - 1; i >= 0; i--) {
			star = activeStars.get(i);
			if(star.isDead()) {
				star.setVisible(false);
				starPool.recycleStar(star);
				activeStars.remove(star);
			}
		}
	}
	
	private void updatePosition(float pSecondsElapsed) {
		Vector2 cometVelocity = comet.getLinearVelocity();
		float vX = cometVelocity.x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT * RELATIVE_SPEED;
		float vY = cometVelocity.y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT * RELATIVE_SPEED;
		Vector2Pool.recycle(cometVelocity);
		this.setPosition(this.getX() + vX * pSecondsElapsed, this.getY() + vY * pSecondsElapsed);
	}
	
	private void spawnStarInsideView() {
		float spawnDistance = getSpawnDistance() * (float) Math.random();
		float angle = Utilities.getRandomFloatBetween(0, 2 * (float) Math.PI);
		spawnStar(spawnDistance, angle);
	}
	
	private void spawnStarOutsideView() {
		float spawnDistance = getSpawnDistance();
		Vector2 cometVelocity = comet.getLinearVelocity();
		float angle = (float) Math.atan2(cometVelocity.y, cometVelocity.x);
		Vector2Pool.recycle(cometVelocity);
		angle += Utilities.getRandomFloatBetween((float) -Math.PI / 4, (float) Math.PI / 4);
		spawnStar(spawnDistance, angle);
	}
	
	private void spawnStar(float distance, float angle) {
		float sceneX = camera.getCenterX() + distance * (float) Math.cos(angle);
		float sceneY = camera.getCenterY() + distance * (float) Math.sin(angle);
		float localCoords[] = this.convertSceneToLocalCoordinates(sceneX, sceneY);
		
		Star star = starPool.getStar();
		star.setPosition(localCoords[Sprite.VERTEX_INDEX_X], localCoords[Sprite.VERTEX_INDEX_Y]);
		star.setVisible(true);
		activeStars.add(star);
	}
	
	private float getSpawnDistance() {
		return (float) Math.sqrt((camera.getWidth() / 2) * (camera.getWidth() / 2) + 
				(camera.getHeight() / 2) * (camera.getHeight() / 2));
	}
	
}
