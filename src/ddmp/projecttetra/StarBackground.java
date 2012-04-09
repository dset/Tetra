package ddmp.projecttetra;

import java.util.HashSet;
import java.util.Iterator;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;

public class StarBackground extends Entity {
	/* The number of stars generated for the pool stars are chosen from. */
	private static final int NUM_GENERATED_STARS = 30;
	/* The number of max simultaneously visible stars. */
	private static final int NUM_ACTIVE_STARS = 10;
	
	/* In percent of camera height. */
	private static final float STAR_MIN_SIZE = 0.005f;
	/* In percent of camera height. */
	private static final float STAR_MAX_SIZE = 0.01f;
	
	private ITextureRegion starTextureRegion;
	private Comet comet;
	private Camera camera;
	private Sprite[] starPool;
	private HashSet<Sprite> activeStars;
	
	public StarBackground(ITextureRegion starTextureRegion, Comet comet, Camera camera) {
		this.starTextureRegion = starTextureRegion;
		this.comet = comet;
		this.camera = camera;
		setChildrenIgnoreUpdate(true);
		starPool = new Sprite[NUM_GENERATED_STARS];
		activeStars = new HashSet<Sprite>();
		generateStars();
		spawnInitialStars();
	}
	
	private void generateStars() {
		float size;
		for(int i = 0; i < starPool.length; i++) {
			size = (STAR_MIN_SIZE + (float) Math.random() * (STAR_MAX_SIZE - STAR_MIN_SIZE)) * camera.getHeight();
			starPool[i] = new Sprite(0, 0, size, size, starTextureRegion, comet.getShape().getVertexBufferObjectManager());
			starPool[i].setAlpha((float) Math.random());
			starPool[i].setVisible(false);
			attachChild(starPool[i]);
		}
	}
	
	private void spawnInitialStars() {
		float topLeftCornerX = camera.getXMin();
		float topLeftCornerY = camera.getYMin();
		float cameraWidth = camera.getWidth();
		float cameraHeight = camera.getHeight();
		int randomIndex;
		float x, y;
		for(int i = 0; i < NUM_ACTIVE_STARS; i++) {
			randomIndex = (int) (starPool.length * Math.random());
			if(activeStars.contains(starPool[randomIndex])) {
				continue;
			}
			x = topLeftCornerX + (float) Math.random() * cameraWidth;
			y = topLeftCornerY + (float) Math.random() * cameraHeight;
			starPool[randomIndex].setPosition(x, y);
			starPool[randomIndex].setVisible(true);
			activeStars.add(starPool[randomIndex]);
		}
	}
	
	@Override
	public void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
		
		float leftLimit = camera.getCenterX() - camera.getWidth();
		float rightLimit = camera.getCenterX() + camera.getWidth();
		float topLimit = camera.getCenterY() - camera.getHeight();
		float bottomLimit = camera.getCenterY() + camera.getHeight();
		
		Iterator<Sprite> starIterator = activeStars.iterator();
		while(starIterator.hasNext()) {
			Sprite star = starIterator.next();
			if( star.getX() < leftLimit || star.getX() > rightLimit 
				|| star.getY() < topLimit || star.getY() > bottomLimit ) {
				star.setVisible(false);
				starIterator.remove();
			}
		}
		
		if(activeStars.size() < NUM_ACTIVE_STARS) {
			spawnStar();
		}
	}
	
	private void spawnStar() {
		float spawnDistance = (camera.getWidth() / 2) * (camera.getWidth() / 2) +
								(camera.getHeight() / 2) * (camera.getHeight() / 2);
		spawnDistance = (float) Math.sqrt(spawnDistance);
		
		double angle = Math.atan2(comet.getBody().getLinearVelocity().y, comet.getBody().getLinearVelocity().x);
		angle += Math.PI/4 - Math.random() * Math.PI/2;
		
		float x = (float) (camera.getCenterX() + Math.cos(angle) * spawnDistance);
		float y = (float) (camera.getCenterY() + Math.sin(angle) * spawnDistance);
		
		int randomIndex = (int) (starPool.length * Math.random());
		if(activeStars.contains(starPool[randomIndex])) {
			return;
		}
		starPool[randomIndex].setPosition(x, y);
		starPool[randomIndex].setVisible(true);
		activeStars.add(starPool[randomIndex]);
	}
	
}
