package ddmp.projecttetra;

import java.util.Iterator;
import java.util.LinkedList;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.texture.region.ITextureRegion;

public class StarBackground extends Entity {
	/* The number of stars generated for the pool stars are chosen from. */
	private static final int NUM_GENERATED_STARS = 40;
	/* The number of max simultaneously visible stars. */
	private static final int NUM_ACTIVE_STARS = 20;
	/* The stars speed relative comet. */
	private static final float RELATIVE_SPEED = 0.9f;
	
	private Comet comet;
	private Camera camera;
	private LinkedList<Star> activeStars;
	private StarPool starPool;
	
	public StarBackground(ITextureRegion starTextureRegion, Comet comet, Camera camera) {
		this.comet = comet;
		this.camera = camera;
		this.activeStars = new LinkedList<Star>();
		this.starPool = new StarPool(starTextureRegion, camera, comet.getShape().getVertexBufferObjectManager());
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
		Iterator<Star> starIterator = activeStars.iterator();
		while(starIterator.hasNext()) {
			Star star = starIterator.next();
			if(star.isDead()) {
				star.setVisible(false);
				starPool.recycleStar(star);
				starIterator.remove();
			}
		}
	}
	
	private void updatePosition(float pSecondsElapsed) {
		float vX = comet.getBody().getLinearVelocity().x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT * RELATIVE_SPEED;
		float vY = comet.getBody().getLinearVelocity().y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT * RELATIVE_SPEED;
		this.setPosition(this.getX() + vX * pSecondsElapsed, this.getY() + vY * pSecondsElapsed);
	}
	
	private void spawnStarInsideView() {
		float spawnDistance = getSpawnDistance() * (float) Math.random();
		double angle = 2 * Math.PI * Math.random();
		spawnStar(spawnDistance, (float) angle);
	}
	
	private void spawnStarOutsideView() {
		float spawnDistance = getSpawnDistance();
		double angle = Math.atan2(comet.getBody().getLinearVelocity().y, comet.getBody().getLinearVelocity().x);
		angle += Math.PI/4 - Math.random() * Math.PI/2;
		spawnStar(spawnDistance, (float) angle);
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
