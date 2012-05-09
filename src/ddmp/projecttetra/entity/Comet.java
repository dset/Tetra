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
		updateDirection(pSecondsElapsed);
		updateRotation();
		applyFriction(pSecondsElapsed);
		updateCamera();
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
		setLinearVelocity(direction.x, direction.y);
		Vector2Pool.recycle(direction);
	}
	
	private void updateRotation() {
		Vector2 velocity = getLinearVelocity();
		float angle = (float) (Math.atan2(velocity.y, velocity.x) * 180/Math.PI + 90);
		bodySpriteConnector.getShape().setRotation(angle);
	}
	
	private void applyFriction(float pSecondsElapsed) {
		Vector2 velocity = getLinearVelocity();
		float speedSquared = velocity.len2();
		float frictionScalar = -FRICTION_COEFFICIENT * pSecondsElapsed * speedSquared;
		Vector2 frictionForce = velocity.nor().mul(frictionScalar);
		applyForce(frictionForce.x, frictionForce.y);
		Vector2Pool.recycle(velocity);
	}
	
	private void updateCamera() {
		camera.setCenter(getCenterX(), getCenterY());
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
