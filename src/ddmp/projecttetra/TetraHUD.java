package ddmp.projecttetra;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSCounter;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * The HUD visible to the player in game.
 */
public class TetraHUD extends HUD {
	private Text fpsText;
	private Text scoreText;
	private ITextureRegion arrowTextures[];
	private Sprite holeArrowSprite;
//	private Sprite planetSprites[];
	private FPSCounter fpsCounter;
//	private PlanetManager pManager;
	private Comet comet;

	public TetraHUD(Font font, VertexBufferObjectManager VBOM, Comet comet,
			ITextureRegion arrowTextures[], PlanetManager pManager) {
		super();
		this.comet = comet;
		this.arrowTextures = arrowTextures;
//		this.pManager = pManager;
		this.fpsText = new Text(50, 700, font, "FPS:",
				"FPS: XXXXXXXXXXX".length(), VBOM);
		this.scoreText = new Text(300, 700, font, "Score:",
				"Score: XXXXXXXX".length(), VBOM);
		holeArrowSprite = new Sprite(0, 0, 0.08f * TetraActivity.CAMERA_HEIGHT,
				0.08f * TetraActivity.CAMERA_HEIGHT, this.arrowTextures[0], VBOM);
		/*planetSprites = new Sprite[3];
		for (int i = 0; i < planetSprites.length; i++) {
			planetSprites[i] = new Sprite(0, 0, 0.07f * CAMERA_HEIGHT,
					0.07f * CAMERA_HEIGHT, this.arrowTextures[1], VBOM);

			this.attachChild(planetSprites[i]);
		}*/
		this.attachChild(fpsText);
		this.attachChild(scoreText);
		this.attachChild(holeArrowSprite);

		this.fpsCounter = new FPSCounter();
		this.registerUpdateHandler(fpsCounter);
		this.registerUpdateHandler(new TimerHandler(0.5f, true,
				new ITimerCallback() {

					@Override
					public void onTimePassed(final TimerHandler pTimerHandler) {
						fpsText.setText("FPS: " + (int) fpsCounter.getFPS());
						fpsCounter.reset();
					}

				}));
	}

	@Override
	public void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);

		scoreText.setText("Score: " + (int) (-comet.getShape().getY()));
		setSidePosition(holeArrowSprite, (float) Math.atan2(comet.getBody()
				.getLinearVelocity().x, comet.getBody().getLinearVelocity().y));

		/*ArrayList<Planet> planets = pManager.getPlanets();
		Planet topPlanets[] = new Planet[3];
		float cometX = comet.getShape().getX() + comet.getShape().getScaleCenterX();
		float cometY = comet.getShape().getY() + comet.getShape().getScaleCenterY();
		
		if (!planets.isEmpty()) {
			topPlanets[0] = planets.get(0);
			for (Planet planet : planets) {
				if (new Vector2(cometX - planet.getShape().getX()
						- planet.getShape().getScaleCenterX(), cometY
						- planet.getShape().getY()
						- planet.getShape().getScaleCenterY()).len2() < new Vector2(
						topPlanets[0].getShape().getX()
								- topPlanets[0].getShape().getScaleCenterX()
								- cometX,
						topPlanets[0].getShape().getY()
								- topPlanets[0].getShape().getScaleCenterY()
								- cometY).len2()) {
					topPlanets[0] = planet;
				}
			}

			float topPlanetX = topPlanets[0].getShape().getX() + topPlanets[0].getShape().getScaleCenterX();
			float topPlanetY = topPlanets[0].getShape().getY() + topPlanets[0].getShape().getScaleCenterY();
			
			float rotation = (float)Math.atan2(topPlanetX - cometX, cometY - topPlanetY);
			Log.d("ROT", "" + rotation);
			
			setSidePosition(planetSprites[0], rotation);
		}*/

	}

	private void setSidePosition(Entity arrow, float rotation) {
		final float MARGIN_HEIGHT = arrow.getScaleCenterY() * 2;
		final float MARGIN_RIGHT = arrow.getScaleCenterX() * 2;
		if (rotation > Math.PI / 4 && rotation <= 3 * Math.PI / 4) {
			arrow.setPosition(
					(float) TetraActivity.CAMERA_WIDTH - MARGIN_RIGHT,
					(float) (MARGIN_HEIGHT + (TetraActivity.CAMERA_HEIGHT - MARGIN_HEIGHT - MARGIN_HEIGHT)
							* (rotation - Math.PI / 4) / (Math.PI / 2)));
			arrow.setRotation(-90f);
		} else if (rotation > 3 * Math.PI / 4 && rotation <= Math.PI) {
			arrow.setPosition(
					(float) (TetraActivity.CAMERA_WIDTH - MARGIN_RIGHT - (TetraActivity.CAMERA_WIDTH - MARGIN_RIGHT)
							* (rotation - 3 * Math.PI / 4) / (Math.PI / 2)),
							TetraActivity.CAMERA_HEIGHT - MARGIN_HEIGHT);
			arrow.setRotation(0f);
		} else if (rotation > -Math.PI && rotation <= -3 * Math.PI / 4) {
			arrow.setPosition((float) ((TetraActivity.CAMERA_WIDTH - MARGIN_RIGHT)
					* (-rotation - 3 * Math.PI / 4) / (Math.PI / 2)),
					TetraActivity.CAMERA_HEIGHT - MARGIN_HEIGHT);
			arrow.setRotation(00f);
		} else if (rotation > -3 * Math.PI / 4 && rotation <= -Math.PI / 4) {
			arrow.setPosition(0.0f, (float) (MARGIN_HEIGHT + (TetraActivity.CAMERA_HEIGHT
					- MARGIN_HEIGHT - MARGIN_HEIGHT)
					* (-rotation - Math.PI / 4) / (Math.PI / 2)));
			arrow.setRotation(90f);
		} else if (rotation > -Math.PI / 4 && rotation <= 0) {
			arrow.setPosition((float) ((TetraActivity.CAMERA_WIDTH - MARGIN_RIGHT)
					* (rotation + Math.PI / 4) / (Math.PI / 2)), 0);
			arrow.setRotation(180f);
		} else {
			arrow.setPosition(
					(float) (TetraActivity.CAMERA_WIDTH - MARGIN_RIGHT - (TetraActivity.CAMERA_WIDTH - MARGIN_RIGHT)
							* (-rotation + Math.PI / 4) / (Math.PI / 2)), 0);
			arrow.setRotation(180f);
		}
	}

}
