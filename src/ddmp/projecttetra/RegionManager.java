package ddmp.projecttetra;

import java.util.HashMap;

import org.andengine.opengl.texture.region.ITextureRegion;

/** 
 * Holds all the texture regions. Enables regions to be fetched
 * from anywhere.
 */
public class RegionManager {
	
	public enum Region {
		COMET, PLANET, MOON, STAR, ARROW_HOLE, ARROW_PLANET
	}
	
	private static final RegionManager INSTANCE = new RegionManager();
	
	private HashMap<Region, ITextureRegion> regions;
	
	private RegionManager() {
		regions = new HashMap<Region, ITextureRegion>();
	}
	
	public static RegionManager getInstance() {
		return RegionManager.INSTANCE;
	}
	
	public void put(Region region, ITextureRegion textureRegion) {
		regions.put(region, textureRegion);
	}
	
	public ITextureRegion get(Region region) {
		return regions.get(region);
	}
	
}
