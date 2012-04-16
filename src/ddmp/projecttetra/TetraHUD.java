package ddmp.projecttetra;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSCounter;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;

/**
 * The HUD visible to the player in game.
 */
public class TetraHUD extends HUD {
	
	private Text fpsText;
	private Text scoreText;
	private Text blackHoleText;
	private FPSCounter fpsCounter;
	private Comet comet;
	
	public TetraHUD(Font font, VertexBufferObjectManager VBOM, Comet comet) {
		super();
		this.comet = comet;
		this.fpsText = new Text(50, 700, font, "FPS:", "FPS: XXXXXXXXXXX".length(), VBOM);
		this.scoreText = new Text(300, 700, font, "Score:", "Score: XXXXXXXX".length(), VBOM);
		this.blackHoleText = new Text(0, 0, font, "Black Hole", "Black Hole".length(), VBOM);
		this.attachChild(fpsText);
		this.attachChild(scoreText);
		this.attachChild(blackHoleText);
		
		this.fpsCounter = new FPSCounter();
		this.registerUpdateHandler(fpsCounter);
		this.registerUpdateHandler(new TimerHandler(0.5f, true, new ITimerCallback() {

			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				fpsText.setText("FPS: " + (int)fpsCounter.getFPS());
				fpsCounter.reset();
			}

		}));
	}
	
	@Override
	public void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
		
		scoreText.setText("Score: " + (int) (-comet.getShape().getY()));				
		Vector2 tmpVector = comet.getBody().getLinearVelocity().cpy().nor().mul(200f);
		blackHoleText.setPosition(this.getCamera().getWidth()/2+tmpVector.x-40,
									this.getCamera().getHeight()/2-tmpVector.y);
	}
	
}
