package ddmp.projecttetra;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * A planet in the game.
 */
public class Planet extends PhysicsConnector {
	
	private static final float GRAVITY_CONSTANT = 0.3f;
	private static final float KILL_DISTANCE_SQUARED = 2250000;
	
	private Comet comet;
	/* Since comets are static their body has mass 0. But mass is needed to calculate
	 * effect of gravity. Therefore mass is added. */
	private float mass;
	private boolean dead;

	public Planet(Sprite planetSprite, Body planetBody, Comet comet) {
		super(planetSprite, planetBody, true, true);
		this.comet = comet;
		this.mass = (float) (Math.PI * Math.pow(planetSprite.getWidthScaled()/2 * (1/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT), 2));
		this.dead = false;
	}
	
	public void update() {
		/* Attract comet by gravity */
		/* TODO: Very ineffective to create copies of vectors. GC will run often. 
		 * Should be rewritten so that it doesn't create any objects. */
		float scalar = -GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
						/ comet.getBody().getPosition().cpy().sub(this.getBody().getPosition()).len();
		Vector2 force = comet.getBody().getPosition().cpy().sub(this.getBody().getPosition()).nor().mul(scalar);
		comet.getBody().applyForce(force, comet.getBody().getPosition());
		
		/* Die if far away from comet */
		float distanceX = getShape().getX() - comet.getShape().getX();
		float distanceY = getShape().getY() - comet.getShape().getY();
		float distanceSq = distanceX * distanceX + distanceY * distanceY;
		if(distanceSq > KILL_DISTANCE_SQUARED) {
			dead = true;
		}
	}
	
	public boolean isDead() {
		return dead;
	}

}
