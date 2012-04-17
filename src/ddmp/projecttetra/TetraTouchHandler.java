package ddmp.projecttetra;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;

/**
 * Handles the touch events in the game.
 */
public class TetraTouchHandler implements IOnSceneTouchListener {
	
	private Camera camera;
	private CameraRotator cameraRotator;
	private Comet comet;
	
	public TetraTouchHandler(Camera camera, CameraRotator cameraRotator, Comet comet) {
		this.camera = camera;
		this.cameraRotator = cameraRotator;
		this.comet = comet;
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown()) {
			touchDown(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
		}
		if (pSceneTouchEvent.isActionUp()) {
			touchUp(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
		}

		return true;
	}
	
	private void touchDown(float x, float y) {
		cameraRotator.setCameraUpdates(false);
		if (x > camera.getWidth() / 2) {
			comet.setTurnRight(true);
		} else {
			comet.setTurnLeft(true);
		}
	}

	private void touchUp(float x, float y) {
		cameraRotator.setCameraUpdates(true);
		if (x > camera.getWidth() / 2) {
			comet.setTurnRight(false);
		} else {
			comet.setTurnLeft(false);
		}
	}

}
