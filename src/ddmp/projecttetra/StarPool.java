package ddmp.projecttetra;

import java.util.LinkedList;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

/**
 * A pool of stars. Used to recycle the same star objects instead
 * of creating new ones.
 */
public class StarPool {
	
	private ITextureRegion starTextureRegion;
	private Camera camera;
	private VertexBufferObjectManager vertexBufferObjectManager;
	private LinkedList<Star> pool;
	
	public StarPool(ITextureRegion starTextureRegion, Camera camera, VertexBufferObjectManager vertexBufferObjectManager) {
		this.starTextureRegion = starTextureRegion;
		this.camera = camera;
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		pool = new LinkedList<Star>();
	}
	
	public void generateStars(int num, Entity entity) {
		for(int i = 0; i < num; i++) {
			Star star = new Star(starTextureRegion, camera, vertexBufferObjectManager);
			pool.add(star);
			entity.attachChild(star);
		}
	}
	
	/**
	 * Returns a random star from the pool.
	 */
	public Star getStar() {
		Debug.d("GET STAR!");
		int randomIndex = (int) (Math.random() * pool.size());
		return pool.remove(randomIndex);
	}
	
	public void recycleStar(Star star) {
		Debug.d("recycle star!");
		pool.add(star);
	}
	
}
