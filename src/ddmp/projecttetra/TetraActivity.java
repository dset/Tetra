package ddmp.projecttetra;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.graphics.Color;
import android.graphics.Typeface;

import com.badlogic.gdx.math.Vector2;

import ddmp.projecttetra.entity.Comet;

public class TetraActivity extends SimpleBaseGameActivity {

	public static final int CAMERA_WIDTH = 480;
	public static final int CAMERA_HEIGHT = 720;

	private BuildableBitmapTextureAtlas mTextureAtlas;
	private PlanetManager pManager;
	private PlanetSpawner pSpawner;
	private Font mFont;
	private Scene scene;
	private ZoomCamera mCamera;
	private PhysicsWorld mPhysicsWorld;
	private Comet comet;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	protected void onCreateResources() {
		mTextureAtlas = new BuildableBitmapTextureAtlas(
				this.getTextureManager(), 1024, 1024,
				TextureOptions.BILINEAR);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		ITextureRegion cometTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "comet.png");
		ITextureRegion planetTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "planet_earthlike1.png");
		ITextureRegion moonTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "moon.png");
		ITextureRegion starTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "star.png");
		
		ITextureRegion holeArrowTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "holearrow.png");
		ITextureRegion planetArrowTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "planetarrow.png");
		
		try {
			this.mTextureAtlas
			.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
					0, 1, 0));
		} catch (final TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		this.mTextureAtlas.load();
		
		RegionManager rM = RegionManager.getInstance();
		rM.put(RegionManager.Region.COMET, cometTextureRegion);
		rM.put(RegionManager.Region.PLANET, planetTextureRegion);
		rM.put(RegionManager.Region.MOON, moonTextureRegion);
		rM.put(RegionManager.Region.STAR, starTextureRegion);
		rM.put(RegionManager.Region.ARROW_HOLE, holeArrowTextureRegion);
		rM.put(RegionManager.Region.ARROW_PLANET, planetArrowTextureRegion);

		final ITexture fontTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mFont = new Font(this.getFontManager(), fontTexture,
				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20, true,
				Color.WHITE);
		this.mFont.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.scene = new Scene();
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		scene.registerUpdateHandler(mPhysicsWorld);
		
		createComet();
		createPlanets();
		createBackground();

		CameraRotator cameraRotator = new CameraRotator(comet, mCamera);
		scene.registerUpdateHandler(cameraRotator);
		
		createHUD(cameraRotator);

		return scene;
	}
	
	private void createComet() {
		this.comet = Comet.createComet(mEngine, mPhysicsWorld);
		this.comet.registerSelf();
	}
	
	private void createPlanets() {
		pManager = new PlanetManager(this.mEngine, this.mPhysicsWorld);
		pSpawner = new PlanetSpawner(this.mEngine, this.mPhysicsWorld, pManager, comet);
		scene.registerUpdateHandler(pManager);
		scene.registerUpdateHandler(pSpawner);
	}
	
	private void createBackground() {
		scene.setBackground(new Background(0f, 0f, 0f));
		scene.attachChild( new StarBackground(comet, mCamera), 0);
	}
	
	private void createHUD(CameraRotator cameraRotator) {
		TetraHUD hud = new TetraHUD(this.mFont, this.getVertexBufferObjectManager(), this.comet, pManager);
		hud.setOnSceneTouchListener(new TetraTouchHandler(mCamera, cameraRotator, comet));
		mCamera.setHUD(hud);
	}

}