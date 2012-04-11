package ddmp.projecttetra;


import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * The comet that the player controls in the game.
 */
public class Comet extends PhysicsConnector {
	
	/* Rad / s */
	private static final float ROTATION_VELOCITY = (float) Math.PI / 2;
	private static final float FRICTION_COEFFICIENT = 0.00001f;
	
	private Body cometBody;
	private Vector2 velocity;
	private float[] rotationMatrix;
	private boolean turnLeft;
	private boolean turnRight;

	public Comet(Sprite cometSprite, Body cometBody) {
		super(cometSprite, cometBody, true, false);
		this.cometBody = cometBody;
		this.velocity = cometBody.getLinearVelocity();
		
		rotationMatrix = new float[4];
		
		turnLeft = false;
		turnRight = false;
	}
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		super.onUpdate(pSecondsElapsed);
				
		if(turnLeft) {
			setRotationMatrix(ROTATION_VELOCITY*pSecondsElapsed);
			velocity = cometBody.getLinearVelocity();
			cometBody.setLinearVelocity(rotationMatrix[0] * velocity.x + rotationMatrix[2] * velocity.y,
						rotationMatrix[1] * velocity.x + rotationMatrix[3] * velocity.y);
		}
		
		if(turnRight) {
			setRotationMatrix(-ROTATION_VELOCITY*pSecondsElapsed);
			velocity = cometBody.getLinearVelocity();
			cometBody.setLinearVelocity(rotationMatrix[0] * velocity.x + rotationMatrix[2] * velocity.y,
					rotationMatrix[1] * velocity.x + rotationMatrix[3] * velocity.y);
		}
		Vector2 tmpVel = getBody().getLinearVelocity();
		float angle = (float) -(Math.atan2(tmpVel.y, tmpVel.x) * 180/Math.PI + 90);
		getShape().setRotation(-angle);
		
		cometBody.applyForce(velocity.cpy().nor().mul(-FRICTION_COEFFICIENT * pSecondsElapsed * velocity.len2()), cometBody.getPosition());
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

}
