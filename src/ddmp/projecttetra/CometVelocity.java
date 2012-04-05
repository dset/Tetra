package ddmp.projecttetra;

import org.andengine.engine.handler.IUpdateHandler;

import com.badlogic.gdx.physics.box2d.Body;

public class CometVelocity  implements IUpdateHandler {
	private final float Y_BASE_VELOCITY = -5f;
	private Body comet;
	private final TetraActivity tActivity;
	
	public CometVelocity(TetraActivity tActivity, Body comet) {
		this.comet = comet;
		this.tActivity = tActivity;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		comet.setLinearVelocity(comet.getLinearVelocity().x + tActivity.getXVelocityModifier(), Y_BASE_VELOCITY);
		tActivity.setXVelocityModifier(tActivity.getXVelocityModifier()/10);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
