package ddmp.projecttetra;


import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * The comet that the player controls in the game.
 */
public class Comet implements IUpdateHandler {
	
	private static final float COMET_SIZE = 0.10f; /* In percent of camera height. */
	private static final FixtureDef COMET_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final float INITIAL_SPEED_Y = -7f;
	private static final float ROTATION_VELOCITY = (float) Math.PI / 2;	/* Rad/s */
	private static final float FRICTION_COEFFICIENT = 5f;
	
	private PhysicsConnector con;
	private Vector2 velocity;
	private float[] rotationMatrix;
	private boolean turnLeft;
	private boolean turnRight;
	private Camera camera;

	public Comet(Engine engine, PhysicsWorld physicsWorld, Scene scene, 
			ITextureRegion cometTextureRegion, Camera camera) {
		this.camera = camera;
		rotationMatrix = new float[4];
		turnLeft = false;
		turnRight = false;
		
		float size = COMET_SIZE * camera.getHeight();
		Sprite cometSprite = new Sprite((camera.getWidth() - size) / 2, (camera.getHeight() - size) / 2,
				size, size, cometTextureRegion, engine.getVertexBufferObjectManager());
		Body cometBody = PhysicsFactory.createCircleBody(physicsWorld, cometSprite, 
				BodyType.DynamicBody, COMET_FIXTURE_DEF);
		con = new PhysicsConnector(cometSprite, cometBody, true, false);
		scene.attachChild(cometSprite);
		//physicsWorld.registerPhysicsConnector(con); SEE onUpdate
		cometBody.setLinearVelocity(0, INITIAL_SPEED_Y);
		this.velocity = con.getBody().getLinearVelocity();
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		con.onUpdate(pSecondsElapsed); // TODO XXX Ugly hack to get camera "shaking" to stop.
		
		if(turnLeft) {
			setRotationMatrix(ROTATION_VELOCITY*pSecondsElapsed);
			velocity = con.getBody().getLinearVelocity();
			con.getBody().setLinearVelocity(rotationMatrix[0] * velocity.x + rotationMatrix[2] * velocity.y,
						rotationMatrix[1] * velocity.x + rotationMatrix[3] * velocity.y);
		}
		
		if(turnRight) {
			setRotationMatrix(-ROTATION_VELOCITY*pSecondsElapsed);
			velocity = con.getBody().getLinearVelocity();
			con.getBody().setLinearVelocity(rotationMatrix[0] * velocity.x + rotationMatrix[2] * velocity.y,
					rotationMatrix[1] * velocity.x + rotationMatrix[3] * velocity.y);
		}
		
		Vector2 tmpVel = con.getBody().getLinearVelocity();
		float angle = (float) -(Math.atan2(tmpVel.y, tmpVel.x) * 180/Math.PI + 90);
		con.getShape().setRotation(-angle);
		
		con.getBody().applyForce(velocity.cpy().nor().mul(
				-FRICTION_COEFFICIENT * pSecondsElapsed * velocity.len2()), con.getBody().getPosition());
		camera.setCenter(con.getShape().getX() + con.getShape().getScaleCenterX(), 
				con.getShape().getY() + con.getShape().getScaleCenterY());
		
	}
	
	public Body getBody() {
		return con.getBody();
	}
	
	public IShape getShape() {
		return con.getShape();
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
