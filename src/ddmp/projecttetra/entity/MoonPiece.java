package ddmp.projecttetra.entity;

import org.andengine.engine.Engine;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * A piece of a moon.
 */
public class MoonPiece extends Entity {
	
	private static final FixtureDef PIECE_FIXTURE_DEF = 
			PhysicsFactory.createFixtureDef(0.1f, 0.5f, 0.5f);
	private static final float ALPHA_FADE_SPEED = 0.75f; // Alpha/s
	
	public static MoonPiece createMoonPiece(Engine engine, PhysicsWorld physicsWorld, float x,
			float y, float size, ITextureRegion textureRegion) {
		Sprite sprite = new Sprite(x, y, size, size, textureRegion, 
				engine.getVertexBufferObjectManager());
		Body body = PhysicsFactory.createBoxBody(physicsWorld, sprite, BodyType.DynamicBody,
				PIECE_FIXTURE_DEF);
		return new MoonPiece(engine, physicsWorld, sprite, body);
	}
	
	public MoonPiece(Engine engine, PhysicsWorld physicsWorld, Sprite sprite, Body body) {
		super(engine, physicsWorld, sprite, body);
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		Sprite sprite = (Sprite) bodySpriteConnector.getShape();
		float spriteAlpha = sprite.getAlpha();
		sprite.setAlpha(spriteAlpha - ALPHA_FADE_SPEED * pSecondsElapsed);
		if(sprite.getAlpha() <= 0) {
			destroySelf();
		}
	}

	@Override
	public void reset() {
		
	}
	
}
