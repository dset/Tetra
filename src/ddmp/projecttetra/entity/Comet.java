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
import ddmp.projecttetra.Utilities;

/**
 * The comet that the player controls in the game.
 */
public class Comet extends Entity {
	
	private static final float COMET_SIZE = 0.10f; /* In percent of camera height. */
	private static final FixtureDef COMET_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final float INITIAL_SPEED_Y = -7f;
	private static final float ROTATION_VELOCITY = (float) Math.PI / 2;	/* Rad/s */
	private static final float FRICTION_COEFFICIENT = 5f;
	
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
		turnLeft = false;
		turnRight = false;
		bodySpriteConnector.getBody().setLinearVelocity(0, INITIAL_SPEED_Y);
		bodySpriteConnector.setUpdateRotation(false);
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		Sprite sprite = (Sprite) bodySpriteConnector.getShape();
		Body body = bodySpriteConnector.getBody();
		updateDirection(pSecondsElapsed);
		
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
	
	private void updateDirection(float pSecondsElapsed) {
		float directionDelta = 0;
		if(turnLeft && !turnRight) {
			directionDelta = -ROTATION_VELOCITY * pSecondsElapsed;
		}else if(turnRight && !turnLeft) {
			directionDelta = ROTATION_VELOCITY * pSecondsElapsed;
		}
		changeDirection(directionDelta);
	}
	
	private void changeDirection(float directionDelta) {
		Vector2 direction = getLinearVelocity();
		Utilities.rotateVector(direction, directionDelta);
		bodySpriteConnector.getBody().setLinearVelocity(direction);
		Vector2Pool.recycle(direction);
	}
	
	public void setTurnLeft(boolean val) {
		turnLeft = val;
	}
	
	public void setTurnRight(boolean val) {
		turnRight = val;
	}

	@Override
	public void reset() {
		
	}

}
