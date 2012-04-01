package ddmp.projecttetra;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TetraActivity extends SimpleBaseGameActivity {
	
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	
	private PhysicsWorld mPhysicsWorld;
	private Camera mCamera;
	private ITexture mTexture;
	private ITextureRegion mCometTextureRegion;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, 
								ScreenOrientation.LANDSCAPE_SENSOR, 
								new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), 
								mCamera);
	}

	@Override
	protected void onCreateResources() {
		try {
			this.mTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/comet.png");
				}
			});

			this.mTexture.load();
			this.mCometTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);
		} catch (IOException e) {
			Debug.e(e);
		}
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		final Scene scene = new Scene();
		scene.setBackground(new Background(0f, 0f, 0f));
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		
		/* Calculate the coordinates for the comet, so its centered on the camera. */
		final float centerX = (CAMERA_WIDTH - this.mCometTextureRegion.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.mCometTextureRegion.getHeight()) / 2;

		/* Create the comet sprite and add it to the scene. */
		final Sprite cometSprite = new Sprite(centerX, centerY, this.mCometTextureRegion, this.getVertexBufferObjectManager());
		scene.attachChild(cometSprite);
		
		/* Create the comet body. */
		FixtureDef cometFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
		Body cometBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, cometSprite, BodyType.DynamicBody, cometFixtureDef);
		Comet comet = new Comet(cometSprite, cometBody);
		this.mPhysicsWorld.registerPhysicsConnector(comet);
		scene.registerUpdateHandler(mPhysicsWorld);
		
		this.mCamera.setChaseEntity(cometSprite);
		
		PlanetSpawner pSpawner = new PlanetSpawner(this.mEngine, this.mPhysicsWorld, comet, this.mCometTextureRegion);
		scene.registerUpdateHandler(pSpawner);
		
		return scene;
	}
	
}