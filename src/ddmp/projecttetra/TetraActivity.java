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
	private static final float CAMERA_SLOWNESS_FAST = 20;
	private static final float CAMERA_SLOWNESS_SLOW = 55;

	private PhysicsWorld mPhysicsWorld;
	private Camera mCamera;
	private BuildableBitmapTextureAtlas mTextureAtlas;
	private ITextureRegion mCometTextureRegion;
	private ITextureRegion mPlanetTextureRegion;
	private ITextureRegion mStarTextureRegion;
	private Comet comet;
	private Font mFont;
	private HUD mHud;
	private boolean cameraUpdates = true;

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
		
		final Text scoreText = new Text(100, 160, this.mFont, "Score:",
				"Score: XXXXXXXX".length(),
				this.getVertexBufferObjectManager());
		mHud = new HUD();
		mHud.attachChild(scoreText);
		mCamera.setHUD(mHud);
		mHud.registerUpdateHandler(new TimerHandler(1 / 20.0f, true,
				new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				scoreText.setText("Score: "
						+ (int) (-comet.getShape().getY()));
			}

		}));
		
		mHud.setOnSceneTouchListener(new IOnSceneTouchListener() {

			@Override
			public boolean onSceneTouchEvent(Scene pScene,
					TouchEvent pSceneTouchEvent) {
				Log.d("Touch", "" + pSceneTouchEvent.getX() + ", " + pSceneTouchEvent.getY());
//				mCamera.convertSceneToSurfaceTouchEvent(pSceneTouchEvent,
//						mEngine.getSurfaceWidth(), mEngine.getSurfaceHeight());
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



		scene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if (cameraUpdates) {
					updateCamera(CAMERA_SLOWNESS_FAST);
				} else {
					updateCamera(CAMERA_SLOWNESS_SLOW);
				}
			}

			@Override
			public void reset() {
			}

		});

		scene.attachChild(
				new StarBackground(mStarTextureRegion, comet, mCamera), 0);

		return scene;
	}

	private void updateCamera(float slowness) {
		Vector2 tmpVel = comet.getBody().getLinearVelocity();
		float goalAngle = (float) -(Math.atan2(tmpVel.y, tmpVel.x) * 180
				/ Math.PI + 90);
		float camAngle = mCamera.getRotation();
		if (camAngle <= -180 && goalAngle >= 0) {
			float newAngle = (slowness * camAngle + (goalAngle - 360))
					/ (slowness + 1);
			if (newAngle < -270) { // keep angles between -270 and 90
				newAngle += 360;
			}
			mCamera.setRotation(newAngle);
		} else if (camAngle >= 0 && goalAngle <= -180) {
			float newAngle = (slowness * camAngle + (goalAngle + 360))
					/ (slowness + 1);
			if (newAngle > 90) {
				newAngle -= 360;
			}
			mCamera.setRotation(newAngle);
		} else {
			mCamera.setRotation((float) (slowness * camAngle + goalAngle)
					/ (slowness + 1));
		}
	}

	private void touchDown(float x, float y) {
		cameraUpdates = false;
		if (x > CAMERA_WIDTH / 2) {
			comet.setTurnRight(true);
		} else {
			comet.setTurnLeft(true);
		}
	}

	private void touchUp(float x, float y) {
		cameraUpdates = true;
		if (x > CAMERA_WIDTH / 2) {
			comet.setTurnRight(false);
		} else {
			comet.setTurnLeft(false);
		}
	}

}