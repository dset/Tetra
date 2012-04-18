package ddmp.projecttetra;

import org.andengine.engine.Engine;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * A planet in the game.
 */
public class Planet extends PhysicsConnector {
	
	private static final float GRAVITY_CONSTANT = 3.5f;
	private static final float KILL_DISTANCE_SQUARED = 2250000;
	/* Relative planet radius. */
	private static final float GRAVITY_FIELD_DISTANCE = 2.5f;
	
	private Comet comet;
	/* Since planets are static their body has mass 0. But mass is needed to calculate
	 * effect of gravity. Therefore mass is added. */
	private float mass;
	private boolean dead;
	private float planetSize;

	public Planet(Sprite planetSprite, Body planetBody, Comet comet, Engine engine) {
		super(planetSprite, planetBody, true, true);
		this.comet = comet;
		this.mass = (float) (Math.PI * Math.pow(planetSprite.getWidthScaled()/2 * (1/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT), 2));
		this.dead = false;
		this.planetSize = getShape().getScaleCenterX()*2;
	}
	
	public void update() {
		/* Attract comet by gravity */
		/* TODO: Very ineffective to create copies of vectors. GC will run often. 
		 * Should be rewritten so that it doesn't create any objects. */
		Vector2 velTmp = comet.getBody().getLinearVelocity();
		float cometCenterX = comet.getShape().getX() + comet.getShape().getScaleCenterX();
		float cometCenterY = comet.getShape().getY() + comet.getShape().getScaleCenterY();
		if(isGravitating(new Vector2().set(cometCenterX, cometCenterY))) {
			float scalar = GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
							/ comet.getBody().getPosition().cpy().sub(this.getBody().getPosition()).len();
			Vector2 force = velTmp.cpy().nor().mul(scalar);
			comet.getBody().applyForce(force, comet.getBody().getPosition());
		}
		
		float distanceX = getShape().getX() + getShape().getScaleCenterX() - cometCenterX;
		float distanceY = getShape().getY() + getShape().getScaleCenterY() - cometCenterY;
		float distanceSq = distanceX * distanceX + distanceY * distanceY;
		/* Die if far away from comet */
		if(distanceSq > KILL_DISTANCE_SQUARED) {
			dead = true;
		}
	}
	
	public boolean isDead() {
		return dead;
	}

	public boolean isGravitating(Vector2 point) {
		float distanceX = getShape().getX() + getShape().getScaleCenterX() - point.x;
		float distanceY = getShape().getY() + getShape().getScaleCenterY() - point.y;
		float distanceSq = distanceX * distanceX + distanceY * distanceY;
		if(distanceSq < Math.pow(GRAVITY_FIELD_DISTANCE * planetSize, 2)){
			return true;
		} else {
			return false;
		}
	}

}
