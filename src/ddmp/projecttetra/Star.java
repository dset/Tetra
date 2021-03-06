package ddmp.projecttetra;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Star extends Sprite {
	
	/* In percent of camera height. */
	private static final float STAR_MIN_SIZE = 0.005f;
	/* In percent of camera height. */
	private static final float STAR_MAX_SIZE = 0.01f;
	
	private Camera camera;
	
	public Star(Camera camera, VertexBufferObjectManager vertexBufferObjectManager) {
		super(0, 0, RegionManager.getInstance().get(RegionManager.Region.STAR), vertexBufferObjectManager);
		
		float size = Utilities.getRandomFloatBetween(STAR_MIN_SIZE, STAR_MAX_SIZE) * camera.getHeight();
		setSize(size, size);
		setAlpha((float) Math.random());
		this.camera = camera;
	}
	
	public boolean isDead() {
		float leftLimit = camera.getCenterX() - camera.getWidth();
		float rightLimit = camera.getCenterX() + camera.getWidth();
		float topLimit = camera.getCenterY() - camera.getHeight();
		float bottomLimit = camera.getCenterY() + camera.getHeight();
		
		float[] coords = this.getParent().convertLocalToSceneCoordinates(getX(), getY());
		float tmpX = coords[Sprite.VERTEX_INDEX_X];
		float tmpY = coords[Sprite.VERTEX_INDEX_Y];
		if( tmpX < leftLimit || tmpX > rightLimit || tmpY < topLimit || tmpY > bottomLimit ) {
			return true;
		} else {
			return false;
		}
	}
	
}
