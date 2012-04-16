package ddmp.projecttetra;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSCounter;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
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
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TetraActivity extends SimpleBaseGameActivity {

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 720;

	private PhysicsWorld mPhysicsWorld;
	private Camera mCamera;
	private CameraRotator cameraRotator;
	private BuildableBitmapTextureAtlas mTextureAtlas;
	private ITextureRegion mCometTextureRegion;
	private ITextureRegion mPlanetTextureRegion;
	private ITextureRegion mStarTextureRegion;
	private Comet comet;
	private Font mFont;
	private HUD mHud;
	private FPSCounter fpsCounter;

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	protected void onCreateResources() {
		mTextureAtlas = new BuildableBitmapTextureAtlas(
				this.getTextureManager(), 1024, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		mCometTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "comet.png");
		mPlanetTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "planet_earthlike1.png");
		mStarTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, this, "star.png");

		try {
			this.mTextureAtlas
			.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
					0, 1, 0));
		} catch (final TextureAtlasBuilderException e) {
			Debug.e(e);
		}

		final ITexture fontTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mFont = new Font(this.getFontManager(), fontTexture,
				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20, true,
				Color.WHITE);
		this.mFont.load();

		this.mTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();

		scene.setBackground(new Background(0f, 0f, 0f));
		
		fpsCounter = new FPSCounter();
		mEngine.registerUpdateHandler(fpsCounter);
		
		final Text fpsText = new Text(50, 700, this.mFont, "FPS:",
				"FPS: XXXXXXXXXXX".length(),
				this.getVertexBufferObjectManager());
		final Text scoreText = new Text(300, 700, this.mFont, "Score:",
				"Score: XXXXXXXX".length(),
				this.getVertexBufferObjectManager());
		final Text blackHoleText = new Text(0, 0, this.mFont, "Black Hole",
				"Black Hole".length(),
				this.getVertexBufferObjectManager());
		mHud = new HUD();
		mHud.attachChild(fpsText);
		mHud.attachChild(scoreText);
		mHud.attachChild(blackHoleText);
		mCamera.setHUD(mHud);
		mHud.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				scoreText.setText("Score: "
						+ (int) (-comet.getShape().getY()));				
//				Vector2 tmpVector = new Vector2((float)Math.cos((mCamera.getRotation()+90)*Math.PI/180), (float)Math.sin((mCamera.getRotation()+90)*Math.PI/180)).mul(200f);
				Vector2 tmpVector = comet.getBody().getLinearVelocity().cpy().nor().mul(200f);
				blackHoleText.setPosition(CAMERA_WIDTH/2+tmpVector.x-40, CAMERA_HEIGHT/2-tmpVector.y);
			}

			@Override
			public void reset() {}
			
		});

		mHud.registerUpdateHandler(new TimerHandler(0.5f, true, new ITimerCallback() {

			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				fpsText.setText("FPS: " + (int)fpsCounter.getFPS());
				fpsCounter.reset();
			}

		}));
		
		mHud.setOnSceneTouchListener(new IOnSceneTouchListener() {

			@Override
			public boolean onSceneTouchEvent(Scene pScene,
					TouchEvent pSceneTouchEvent) {
				Log.d("Touch", "" + pSceneTouchEvent.getX() + ", " + pSceneTouchEvent.getY());
				if (pSceneTouchEvent.isActionDown()) {
					touchDown(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				}
				if (pSceneTouchEvent.isActionUp()) {
					touchUp(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				}

				return true;
			}
		});

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

		/* Create the comet sprite and add it to the scene. */
		final Sprite cometSprite = new Sprite(0, 0, 0.10f * CAMERA_HEIGHT,
				0.10f * CAMERA_HEIGHT, this.mCometTextureRegion,
				this.getVertexBufferObjectManager());
		/*
		 * Calculate the coordinates for the comet, so its centered on the
		 * camera.
		 */
		final float centerX = (CAMERA_WIDTH - cometSprite.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - cometSprite.getHeight()) / 2;
		cometSprite.setPosition(centerX, centerY);
		scene.attachChild(cometSprite);

		/* Create the comet body. */
		FixtureDef cometFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f,
				0.5f);
		Body cometBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
				cometSprite, BodyType.DynamicBody, cometFixtureDef);
		cometBody.setLinearVelocity(0, -7);
		this.comet = new Comet(cometSprite, cometBody, mCamera);
		this.mPhysicsWorld.registerPhysicsConnector(comet);
		scene.registerUpdateHandler(mPhysicsWorld);
		
		PlanetManager pManager = new PlanetManager(this.mEngine,
				this.mPhysicsWorld);
		scene.registerUpdateHandler(pManager);

		PlanetSpawner pSpawner = new PlanetSpawner(this.mEngine,
				this.mPhysicsWorld, pManager, comet, this.mPlanetTextureRegion);

		scene.registerUpdateHandler(pSpawner);


		this.cameraRotator = new CameraRotator(comet, mCamera);
		scene.registerUpdateHandler(cameraRotator);

		scene.attachChild(
				new StarBackground(mStarTextureRegion, comet, mCamera), 0);

		return scene;
	}

	private void touchDown(float x, float y) {
		cameraRotator.setCameraUpdates(false);
		if (x > CAMERA_WIDTH / 2) {
			comet.setTurnRight(true);
		} else {
			comet.setTurnLeft(true);
		}
	}

	private void touchUp(float x, float y) {
		cameraRotator.setCameraUpdates(true);
		if (x > CAMERA_WIDTH / 2) {
			comet.setTurnRight(false);
		} else {
			comet.setTurnLeft(false);
		}
	}

}