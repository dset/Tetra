package ddmp.projecttetra;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;

import com.badlogic.gdx.math.Vector2;

/**
 * Handles the camera rotation. The camera rotation follows the
 * rotation of the comet.
 */
public class CameraRotator implements IUpdateHandler {
	
	private static final float CAMERA_SLOWNESS_FAST = 20;
	private static final float CAMERA_SLOWNESS_SLOW = 55;
	
	private Comet comet;
	private ZoomCamera camera;
	private boolean cameraUpdates;
	
	public CameraRotator(Comet comet, ZoomCamera camera) {
		this.comet = comet;
		this.camera = camera;
		cameraUpdates = true;
	}
	
	public void setCameraUpdates(boolean value) {
		cameraUpdates = value;
	}
	
	public boolean getCameraUpdates() {
		return cameraUpdates;
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		if (cameraUpdates) {
			updateCamera(CAMERA_SLOWNESS_FAST);
		} else {
			updateCamera(CAMERA_SLOWNESS_SLOW);
		}
	}

	@Override
	public void reset() {
		
	}
	
	private void updateCamera(float slowness) {
		Vector2 tmpVel = comet.getBody().getLinearVelocity();
		float goalAngle = (float) -(Math.atan2(tmpVel.y, tmpVel.x) * 180
				/ Math.PI + 90);
		float camAngle = camera.getRotation();
		if (camAngle <= -180 && goalAngle >= 0) {
			float newAngle = (slowness * camAngle + (goalAngle - 360))
					/ (slowness + 1);
			if (newAngle < -270) { // keep angles between -270 and 90
				newAngle += 360;
			}
			camera.setRotation(newAngle);
		} else if (camAngle >= 0 && goalAngle <= -180) {
			float newAngle = (slowness * camAngle + (goalAngle + 360))
					/ (slowness + 1);
			if (newAngle > 90) {
				newAngle -= 360;
			}
			camera.setRotation(newAngle);
		} else {
			camera.setRotation((float) (slowness * camAngle + goalAngle)
					/ (slowness + 1));
		}
		float zoomFactor = 2.5f/(float)Math.pow(comet.getBody().getLinearVelocity().len(),0.5f);
		camera.setZoomFactor((zoomFactor<1.0f)? zoomFactor : 1.0f);
	}
}
