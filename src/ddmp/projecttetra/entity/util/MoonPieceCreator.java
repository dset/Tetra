package ddmp.projecttetra.entity.util;

import org.andengine.engine.Engine;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;

import ddmp.projecttetra.RegionManager;
import ddmp.projecttetra.entity.Entity;
import ddmp.projecttetra.entity.MoonPiece;

public class MoonPieceCreator {
	
	/**
	 * Creates pieces of the given moon
	 */
	public static void createMoonPieces(Engine engine, PhysicsWorld physicsWorld, Entity moon) {
		ITextureRegion moonTextureRegion = RegionManager.getInstance().get(RegionManager.Region.MOON);
		ITexture texture = moonTextureRegion.getTexture();
		int tX = (int) moonTextureRegion.getTextureX();
		int tY = (int) moonTextureRegion.getTextureY();
		int tW = (int) moonTextureRegion.getWidth();
		int tH = (int) moonTextureRegion.getHeight();
		ITextureRegion reg1 = TextureRegionFactory.extractFromTexture(texture, tX, tY, tW/2, tH/2);
		ITextureRegion reg2 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY, tW/2, tH/2);
		ITextureRegion reg3 = TextureRegionFactory.extractFromTexture(texture, tX, tY+tH/2, tW/2, tH/2);
		ITextureRegion reg4 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY+tH/2, tW/2, tH/2);
		
		float mX = moon.getCenterX();
		float mY = moon.getCenterY();
		float size = moon.getWidth() / 2;
		MoonPiece.createMoonPiece(engine, physicsWorld, mX-size, mY-size, size, reg1).registerSelf();
		MoonPiece.createMoonPiece(engine, physicsWorld, mX, mY-size, size, reg2).registerSelf();
		MoonPiece.createMoonPiece(engine, physicsWorld, mX-size, mY, size, reg3).registerSelf();
		MoonPiece.createMoonPiece(engine, physicsWorld, mX, mY, size, reg4).registerSelf();
	}
}
