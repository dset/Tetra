package ddmp.projecttetra.entity;


import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import ddmp.projecttetra.RegionManager;
import ddmp.projecttetra.TetraActivity;

/**
 * The comet that the player controls in the game.
 */
public class Comet extends Entity {
	
	private static final float COMET_SIZE = 0.10f; /* In percent of camera height. */
	private static final FixtureDef COMET_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final float INITIAL_SPEED_Y = -7f;
	private static final float ROTATION_VELOCITY = (float) Math.PI / 2;	/* Rad/s */
	private static final float FRICTION_COEFFICIENT = 5f;
	
	private float[] rotationMatrix;
	private boolean turnLeft;
	private boolean turnRight;
	private Camera camera;
	
	public static Comet createComet(Engine engine, PhysicsWorld physicsWorld) {
		float size = COMET_SIZE * TetraActivity.CAMERA_HEIGHT;
		float x = (TetraActivity.CAMERA_WIDTH - size) / 2;
		float y = (TetraActivity.CAMERA_HEIGHT - size) / 2;
		Sprite sprite = new Sprite(x, y, size, size, RegionManager.getInstance().get(
				RegionManager.Region.COMET), engine.getVertexBufferObjectManager());
		Body body = PhysicsFactory.createCircleBody(physicsWorld, sprite, BodyType.DynamicBody,
				COMET_FIXTURE_DEF);
		return new Comet(engine, physicsWorld, sprite, body, engine.getCamera());
	}
	
	private Comet(Engine engine, PhysicsWorld physicsWorld, Sprite sprite, Body body, 
			Camera camera) {
		super(engine, physicsWorld, sprite, body);
		
		this.camera = camera;
		rotationMatrix = new float[4];
		turnLeft = false;
		turnRight = false;
		bodySpriteConnector.getBody().setLinearVelocity(0, INITIAL_SPEED_Y);
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		Sprite sprite = (Sprite) bodySpriteConnector.getShape();
		Body body = bodySpriteConnector.getBody();
		Vector2 velocity;
		if(turnLeft) {
			setRotationMatrix(ROTATION_VELOCITY * pSecondsElapsed);
			velocity = body.getLinearVelocity();
			body.setLinearVelocity(rotationMatrix[0] * velocity.x + rotationMatrix[2] * velocity.y,
						rotationMatrix[1] * velocity.x + rotationMatrix[3] * velocity.y);
		}
		
		if(turnRight) {
			setRotationMatrix(-ROTATION_VELOCITY*pSecondsElapsed);
			velocity = body.getLinearVelocity();
			body.setLinearVelocity(rotationMatrix[0] * velocity.x + rotationMatrix[2] * velocity.y,
					rotationMatrix[1] * velocity.x + rotationMatrix[3] * velocity.y);
		}
		
		Vector2 tmpVel = Vector2Pool.obtain().set(body.getLinearVelocity());
		float angle = (float) -(Math.atan2(tmpVel.y, tmpVel.x) * 180/Math.PI + 90);
		sprite.setRotation(-angle);
		
		float lenSq = tmpVel.len2();
		body.applyForce(tmpVel.nor().mul(
				-FRICTION_COEFFICIENT * pSecondsElapsed * lenSq), body.getPosition());
		Vector2Pool.recycle(tmpVel);
		camera.setCenter(sprite.getX() + sprite.getScaleCenterX(), 
				sprite.getY() + sprite.getScaleCenterY());
		
	}
	
	public void setTurnLeft(boolean val) {
		turnLeft = val;
	}
	
	public void setTurnRight(boolean val) {
		turnRight = val;
	}
	
	private void setRotationMatrix(float angle) {
		rotationMatrix[0] = (float) Math.cos(angle);
		rotationMatrix[1] = (float) -Math.sin(angle);
		rotationMatrix[2] = (float) Math.sin(angle);
		rotationMatrix[3] = (float) Math.cos(angle);
	}

	@Override
	public void reset() {
		
	}

}
