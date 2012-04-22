package ddmp.projecttetra;

import java.util.List;

import org.andengine.engine.Engine;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * A planet in the game.
 */
public class Planet {
	
	private static final FixtureDef PLANET_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, true);
	private static final float PLANET_MIN_SIZE = 0.15f; //In percent of camera height
	private static final float PLANET_MAX_SIZE = 0.25f;	//In percent of camera height
	private static final float GRAVITY_CONSTANT = 7f;
	private static final float KILL_DISTANCE_SQUARED = 2250000;
	/* Relative planet radius. */
	private static final float GRAVITY_FIELD_DISTANCE = 5f;
	/* Angle to determine when to apply gravity. */
	private static final float GRAVITY_ANGLE = (float) (Math.PI/1.5);
	private static final float BOOST_DISTANCE = 6f;
	
	private Engine engine;
	private PhysicsWorld physicsWorld;
	private ITextureRegion planetTextureRegion;
	private PhysicsConnector con;
	private Body body;
	private Sprite shape;
	private Comet comet;
	/* Since planets are static their body has mass 0. But mass is needed to calculate
	 * effect of gravity. Therefore mass is added. */
	private float mass;
	private boolean dead;
	private float planetSize;

	public Planet(float x, float y, ITextureRegion planetTextureRegion, Comet comet, Engine engine,
					PhysicsWorld physicsWorld) {
		this.comet = comet;
		this.engine = engine;
		this.physicsWorld = physicsWorld;
		this.planetTextureRegion = planetTextureRegion;
		this.dead = false;
		
		float scale = PLANET_MIN_SIZE + (PLANET_MAX_SIZE - PLANET_MIN_SIZE) * (float) Math.random();
		float size = scale * TetraActivity.CAMERA_HEIGHT;
		shape = new Sprite(x, y, size, size, planetTextureRegion, 
				engine.getVertexBufferObjectManager());
		body = PhysicsFactory.createCircleBody(physicsWorld, shape, 
				BodyType.StaticBody, PLANET_FIXTURE_DEF);
		body.getFixtureList().get(0).setUserData(this); /* A bit hacky. */
		this.con = new PhysicsConnector(shape, body, true, true);
		
		this.mass = (float) (Math.PI * Math.pow(shape.getWidthScaled()/2 * (1/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT), 2));
		this.planetSize = con.getShape().getScaleCenterX()*2;
	}
	
	public void update() {
		/* Attract comet by gravity */
		float cometCenterX = comet.getShape().getX() + comet.getShape().getScaleCenterX();
		float cometCenterY = comet.getShape().getY() + comet.getShape().getScaleCenterY();
		Vector2 cometCenterPos = Vector2Pool.obtain().set(cometCenterX, cometCenterY);
		if(isGravitating(cometCenterPos)) {
			 /* Apply gravity if comet is moving towards or almost towards planet. */
			Vector2 velTmp = comet.getBody().getLinearVelocity();
			Vector2 planetDir = Vector2Pool.obtain().set(body.getPosition()).sub(comet.getBody().getPosition());
			double angle = Math.acos(planetDir.dot(velTmp) / (planetDir.len() * velTmp.len()));
			Vector2Pool.recycle(planetDir);
			if(Math.abs(angle) < GRAVITY_ANGLE) {
				Vector2 distanceVector = Vector2Pool.obtain().set(comet.getBody().getPosition()).sub(this.body.getPosition());
				float distance = distanceVector.len();
				float gravityScalar = (float) (-GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
						/ distance);
				Vector2 gravityForce = distanceVector.nor().mul(gravityScalar);
				comet.getBody().applyForce(gravityForce, comet.getBody().getPosition());
				Vector2Pool.recycle(distanceVector);
				
				/* Give comet boost if close to planet. */
				if (distance < BOOST_DISTANCE) {
					float boostScalar = GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
							/ distance;
					Vector2 boostForce = Vector2Pool.obtain().set(velTmp).nor().mul(boostScalar);
					comet.getBody().applyForce(boostForce, comet.getBody().getPosition());
					Vector2Pool.recycle(boostForce);
				}
			} else {
				/* Give comet some extra speed away from planet. */
//				float escapeScalar = GRAVITY_CONSTANT * this.mass * comet.getBody().getMass()
//						/ comet.getBody().getPosition().cpy().sub(this.getBody().getPosition()).len();
//				Vector2 escapeForce = velTmp.cpy().nor().mul(escapeScalar);
//				comet.getBody().applyForce(escapeForce, comet.getBody().getPosition());
			}
		}
		Vector2Pool.recycle(cometCenterPos);
		
		float distanceX = shape.getX() + shape.getScaleCenterX() - cometCenterX;
		float distanceY = shape.getY() + shape.getScaleCenterY() - cometCenterY;
		float distanceSq = distanceX * distanceX + distanceY * distanceY;
		/* Die if far away from comet */
		if(distanceSq > KILL_DISTANCE_SQUARED) {
			dead = true;
		}
		
		checkForCollisionWithCommet();
	}
	
	private void checkForCollisionWithCommet() {
		if(dead) {
			return;
		}
		
		List<Contact> contacts = physicsWorld.getContactList();
		for(Contact contact : contacts) {
			if(contact.isTouching()) {
				Object aData = contact.getFixtureA().getUserData();
				Object bData = contact.getFixtureB().getUserData();
				if((aData == this || bData == this) && 
						(aData instanceof Comet || bData instanceof Comet)) {
					/* This has collided with comet, break apart. */
					breakApart();
				}
			}
		}
	}
	
	private void breakApart() {
		dead = true;
		ITexture texture = planetTextureRegion.getTexture();
		int tX = (int) planetTextureRegion.getTextureX();
		int tY = (int) planetTextureRegion.getTextureY();
		int tW = (int) planetTextureRegion.getWidth();
		int tH = (int) planetTextureRegion.getHeight();
		ITextureRegion reg1 = TextureRegionFactory.extractFromTexture(texture, tX, tY, tW/2, tH/2);
		ITextureRegion reg2 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY, tW/2, tH/2);
		ITextureRegion reg3 = TextureRegionFactory.extractFromTexture(texture, tX, tY+tH/2, tW/2, tH/2);
		ITextureRegion reg4 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY+tH/2, tW/2, tH/2);
		
		float sX = shape.getX();
		float sY = shape.getY();
		float size = shape.getWidth() / 2;
		new PlanetPiece(sX, sY, size, reg1, engine, physicsWorld);
		new PlanetPiece(sX+size, sY, size, reg2, engine, physicsWorld);
		new PlanetPiece(sX, sY+size, size, reg3, engine, physicsWorld);
		new PlanetPiece(sX+size, sY+size, size, reg4, engine, physicsWorld);
	}
	
	public Body getBody() {
		return con.getBody();
	}
	
	public IShape getShape() {
		return con.getShape();
	}
	
	public PhysicsConnector getPhysicsConnector() {
		return con;
	}
	
	public boolean isDead() {
		return dead;
	}

	public boolean isGravitating(Vector2 point) {
		float distanceX = shape.getX() + shape.getScaleCenterX() - point.x;
		float distanceY = shape.getY() + shape.getScaleCenterY() - point.y;
		float distanceSq = distanceX * distanceX + distanceY * distanceY;
		if(distanceSq < Math.pow(GRAVITY_FIELD_DISTANCE * planetSize, 2)){
			return true;
		} else {
			return false;
		}
	}

}
