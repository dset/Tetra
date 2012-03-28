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
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

public class TetraActivity extends SimpleBaseGameActivity {
	
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	
	private ITexture mTexture;
	private ITextureRegion mMeteoroidTextureRegion;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		return new EngineOptions(true, 
								ScreenOrientation.LANDSCAPE_SENSOR, 
								new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), 
								new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT));
	}

	@Override
	protected void onCreateResources() {
		try {
			this.mTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/meteoroid.png");
				}
			});

			this.mTexture.load();
			this.mMeteoroidTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);
		} catch (IOException e) {
			Debug.e(e);
		}
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		final Scene scene = new Scene();
		scene.setBackground(new Background(0f, 0f, 0f));
		
		/* Calculate the coordinates for the meteoroid, so its centered on the camera. */
		final float centerX = (CAMERA_WIDTH - this.mMeteoroidTextureRegion.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.mMeteoroidTextureRegion.getHeight()) / 2;

		/* Create the meteoroid and add it to the scene. */
		final Sprite meteoroid = new Sprite(centerX, centerY, this.mMeteoroidTextureRegion, this.getVertexBufferObjectManager());
		scene.attachChild(meteoroid);
		
		return scene;
	}
	
}